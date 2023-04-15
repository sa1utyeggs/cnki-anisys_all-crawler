package com.hh.task.cnki;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hh.config.OpenAIConfig;
import com.hh.entity.cnki.DietMetaboliteAlias;
import com.hh.entity.cnki.DiseaseAlias;
import com.hh.entity.cnki.PaperAbstract;
import com.hh.entity.cnki.PaperInfo;
import com.hh.entity.openai.Message;
import com.hh.mapper.cnki.DietMetaboliteAliasMapper;
import com.hh.mapper.cnki.DiseaseAliasMapper;
import com.hh.task.Task;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.http.HttpConnectionPool;
import com.hh.mapper.cnki.PaperAbstractMapper;
import com.hh.mapper.cnki.PaperInfoMapper;
import com.hh.utils.AssertUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author ab875
 * 使用 ChatGPT 分析 paper_abstart 并找到 key_word 的相关词作为 alias
 */
public class AliasTask extends Task<Boolean> {
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private static final HttpConnectionPool HTTP_CONNECTION_POOL = CONTEXT.getBean("httpConnectionPool", HttpConnectionPool.class);
    private static final OpenAIConfig OPENAI_CONFIG = CONTEXT.getBean("openAIConfig", OpenAIConfig.class);
    private static final String CHAT_GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final Map<String, String> Headers;
    private static final String ChatTemplate = "从\"%s\" 中寻找与“%s”相关的词语（包括但不限于同义词、近义词、英文翻译、缩写），结果使用数组形式返回";
    public static final String TYPE_DIET = "diet_metabolite";
    public static final String TYPE_DISEASE = "disease";

    static {
        Headers = new HashMap<>(2);
        Headers.put("OpenAI-Organization", OPENAI_CONFIG.getOpenAIOrganization());
        Headers.put("Authorization", OPENAI_CONFIG.getBearToken());
        Headers.put("Host", "api.openai.com");
        System.out.println(Headers);
    }


    private String type;
    private String keyword;
    private boolean test;
    private final PaperInfoMapper paperInfoMapper;
    private final PaperAbstractMapper paperAbstractMapper;
    private final DietMetaboliteAliasMapper dietMetaboliteAliasMapper;
    private final DiseaseAliasMapper diseaseAliasMapper;


    public AliasTask(String type,String keyWord) {
        this.type = type;
        this.keyword = keyWord;
        paperInfoMapper = CONTEXT.getBean("paperInfoMapper", PaperInfoMapper.class);
        paperAbstractMapper = CONTEXT.getBean("paperAbstractMapper", PaperAbstractMapper.class);
        dietMetaboliteAliasMapper = CONTEXT.getBean("dietMetaboliteAliasMapper", DietMetaboliteAliasMapper.class);
        diseaseAliasMapper = CONTEXT.getBean("diseaseAliasMapper", DiseaseAliasMapper.class);
    }

    private void process(String keyWord) {
        try {
            // get paper_info
            List<PaperInfo> papers = paperInfoMapper.selectList(new LambdaQueryWrapper<PaperInfo>()
                    .eq("disease".equals(type), PaperInfo::getDisease, keyWord)
                    .eq("metabolite".equals(type), PaperInfo::getMetabolite, keyWord)
                    .eq(PaperInfo::getRelation, 0)
                    .last("limit 10"));

            // get abstract
            ArrayList<PaperAbstract> abs = new ArrayList<>(papers.size());
            for (PaperInfo paper : papers) {
                abs.add(paperAbstractMapper.selectOne(new LambdaQueryWrapper<PaperAbstract>()
                        .eq(PaperAbstract::getPaperId, paper.getId())));
            }

            // chatGPT analyze keyword-abstract
            for (PaperAbstract ab : abs) {
                // load post data
                HashMap<String, Object> data = new HashMap<>(4);
                data.put("model", "gpt-3.5-turbo");
                data.put("temperature", 0.2);
                ArrayList<Message> messages = new ArrayList<>();
                messages.add(Message.builder().role("user").content(String.format(ChatTemplate, ab.getText(), keyWord)).build());
                data.put("messages", messages);

                // post
                String res = "";
                try (CloseableHttpResponse response = HTTP_CONNECTION_POOL.postWithResponse(CHAT_GPT_URL, data, Headers, "application/json");) {
                    res = EntityUtils.toString(response.getEntity(), "UTF-8");
                }

                // process response
                JSONObject json = JSONObject.parseObject(res);
                JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
                String finishReason = choice.getString("finish_reason");
                String content = choice.getJSONObject("message").getString("content");

                // gpt answer succeed
                if ("stop".equals(finishReason)) {
                    ArrayList<String> list = contentToArrayList(content);

                    // persist alias
                    for (String s : list) {
                        if (!s.equals(keyWord)) {
                            switch (type) {
                                case TYPE_DIET:
                                    boolean exists = dietMetaboliteAliasMapper.exists(new LambdaQueryWrapper<DietMetaboliteAlias>()
                                            .eq(DietMetaboliteAlias::getName, keyWord)
                                            .eq(DietMetaboliteAlias::getAlias, s));
                                    if (!exists) {
                                        dietMetaboliteAliasMapper.insert(DietMetaboliteAlias.builder()
                                                .name(keyWord).alias(s).priority(10).build());
                                    }
                                    break;
                                case TYPE_DISEASE:
                                    boolean exists2 = diseaseAliasMapper.exists(new LambdaQueryWrapper<DiseaseAlias>()
                                            .eq(DiseaseAlias::getName, keyWord)
                                            .eq(DiseaseAlias::getAlias, s));
                                    if (!exists2) {
                                        diseaseAliasMapper.insert(DiseaseAlias.builder()
                                                .name(keyWord).alias(s).priority(10).build());
                                    }
                                    break;
                                default:
                                    System.out.println("no such type: " + type);
                            }
                        }
                    }
                }

            }
            System.out.println("======> keyword: \"" + keyWord +"\" done");

            // generate main_sentence

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public Boolean call() throws Exception {
        if (Strings.isNotBlank(keyword)) {
            process(keyword);
            return true;
        }
        return false;
    }

    private ArrayList<String> contentToArrayList(String str) {
        str = str.substring(1, str.length() - 1);
        String[] arr = str.split(", ");
        ArrayList<String> list = new ArrayList<String>();
        for (String s : arr) {
            list.add(s.substring(1, s.length() - 1));
        }
        return list;
    }
}
