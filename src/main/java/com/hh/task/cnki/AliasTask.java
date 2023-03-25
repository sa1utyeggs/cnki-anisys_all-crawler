package com.hh.task.cnki;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hh.config.OpenAIConfig;
import com.hh.entity.cnki.PaperAbstract;
import com.hh.entity.cnki.PaperInfo;
import com.hh.entity.openai.Message;
import com.hh.task.Task;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.http.HttpConnectionPool;
import com.hh.mapper.cnki.PaperAbstractMapper;
import com.hh.mapper.cnki.PaperInfoMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author ab875
 */
public class AliasTask extends Task<String> {
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private static final HttpConnectionPool HTTP_CONNECTION_POOL = CONTEXT.getBean("httpConnectionPool", HttpConnectionPool.class);
    private static final OpenAIConfig OPENAI_CONFIG = CONTEXT.getBean("openAIConfig", OpenAIConfig.class);
    private static final String CHAT_GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final Map<String, String> Headers;
    private static final String ChatTemplate = "从\"%s\" 中寻找与“%s”相关的词语（包括但不限于同义词、近义词、英文翻译、缩写），结果使用数组形式返回";

    static {
        Headers = new HashMap<>(2);
        Headers.put("OpenAI-Organization", OPENAI_CONFIG.getOpenAIOrganization());
        Headers.put("Authorization", OPENAI_CONFIG.getBearToken());
        Headers.put("Host", "api.openai.com");
        System.out.println(Headers);
    }


    private String type;
    private List<String> keyWords;
    private boolean test;
    private final PaperInfoMapper paperInfoMapper;
    private final PaperAbstractMapper paperAbstractMapper;
    private final CnkiDatabaseService dataBaseUtils;


    public AliasTask(String type) {
        this.type = type;
        dataBaseUtils = CONTEXT.getBean("dataBaseUtils", CnkiDatabaseService.class);
        paperInfoMapper = CONTEXT.getBean("paperInfoMapper", PaperInfoMapper.class);
        paperAbstractMapper = CONTEXT.getBean("paperAbstractMapper", PaperAbstractMapper.class);
    }

    private void getKeyWord() {
        try {
            switch (type) {
                case "disease":
                    keyWords = dataBaseUtils.getAllDisease();
                    break;
                case "metabolite":
                    keyWords = dataBaseUtils.getAllMetabolite();
                    break;
                default:
                    System.out.println("illegal type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(String keyWord) {
        System.out.println("process");
        // get abstract
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
                HashMap<String, Object> data = new HashMap<>();
                data.put("model", "gpt-3.5-turbo");
                data.put("temperature", 0.7);
                ArrayList<Message> messages = new ArrayList<>();
                messages.add(Message.builder().role("user").content(String.format(ChatTemplate, ab.getText(), keyWord)).build());
                data.put("messages", messages);
                CloseableHttpResponse response = HTTP_CONNECTION_POOL.postWithResponse(CHAT_GPT_URL, data, Headers, "application/json");
                String s = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(s);
                response.close();
                break;
            }


            // persist alias


            // generate main_sentence

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public String call() throws Exception {
        process("乳酸");
        return "";
    }
}
