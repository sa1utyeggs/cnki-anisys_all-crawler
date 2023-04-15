package com.hh.task.cnki;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hh.entity.cnki.*;
import com.hh.function.base.Const;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.http.useragent.DefaultUserAgentManager;
import com.hh.mapper.cnki.*;
import com.hh.task.Task;
import com.hh.utils.StringUtils;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ab875
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@Builder
public class UpdateMainSentenceTask extends Task<Boolean> {
    private final Logger logger = LogManager.getLogger(UpdateMainSentenceTask.class);
    private static final ApplicationContext CONTEXT = ContextSingletonFactory.getInstance();
    private final PaperInfoMapper paperInfoMapper;
    private final PaperAbstractMapper paperAbstractMapper;
    private final DietMetaboliteAliasMapper dietMetaboliteAliasMapper;
    private final DiseaseAliasMapper diseaseAliasMapper;
    private final PaperMainSentenceMapper paperMainSentenceMapper;

    public static final ConcurrentHashMap<String, List<String>> dietAliasMap;
    public static final ConcurrentHashMap<String, List<String>> diseaseAliasMap;
    private static final ReentrantLock diseaseMutex;
    private static final ReentrantLock dietMutex;

    static {
        dietAliasMap = new ConcurrentHashMap<>(1000);
        diseaseAliasMap = new ConcurrentHashMap<>(1000);
        diseaseMutex = new ReentrantLock();
        dietMutex = new ReentrantLock();
    }


    private Long paperId;

    private byte[] diseaseB = new byte[1000];


    public UpdateMainSentenceTask() {
        paperInfoMapper = CONTEXT.getBean("paperInfoMapper", PaperInfoMapper.class);
        paperAbstractMapper = CONTEXT.getBean("paperAbstractMapper", PaperAbstractMapper.class);
        dietMetaboliteAliasMapper = CONTEXT.getBean("dietMetaboliteAliasMapper", DietMetaboliteAliasMapper.class);
        diseaseAliasMapper = CONTEXT.getBean("diseaseAliasMapper", DiseaseAliasMapper.class);
        paperMainSentenceMapper = CONTEXT.getBean("paperMainSentenceMapper", PaperMainSentenceMapper.class);
    }

    @Override
    public Boolean call() throws Exception {
        PaperInfo paperInfo = paperInfoMapper.selectById(paperId);
        int success = 0;
        PaperAbstract paperAbstract = paperAbstractMapper.selectOne(new LambdaQueryWrapper<PaperAbstract>().eq(PaperAbstract::getPaperId, paperId));
        if (paperInfo != null && paperAbstract != null) {
            List<MainSentence> mainSentence = getMainSentence(paperAbstract.getText(), paperInfo.getMetabolite(), paperInfo.getDisease(), Const.EXCLUSION_WORDS);
            for (MainSentence sentence : mainSentence) {
                success += paperMainSentenceMapper.insert(sentence);
            }
            String log = paperId + "===>" + paperInfo.getDisease() + "~" + paperInfo.getMetabolite() + "===> success: " + success;
            logger.info(log);
            return true;
        } else {
            logger.warn(paperId + "does not exist");
            return false;
        }

    }

    private List<MainSentence> getMainSentence(String text, String metabolite, String disease, Set<String> exclusions) {
        String[] sentences = text.split("。");
        // 获得疾病的 alias，并寻找可能的别名
        List<String> diseaseAliases = getDiseaseAlias(disease);
        // 获得饮食的 alias，并寻找可能的别名
        List<String> dietAliases = getDietAlias(metabolite);

        // result
        ArrayList<MainSentence> mainSentences = new ArrayList<>(sentences.length);


        // 用于跳出循环到 sentence循环
        boolean flag = true;
        // 遍历
        for (String sentence : sentences) {
            // 如果句子含有排除词，就不需要考虑
            if (exclusions != null && StringUtils.startWith(sentence, exclusions)) {
                continue;
            }
            for (String dietA : dietAliases) {
                if (!flag) {
                    flag = true;
                    break;
                }
                for (String diseaseA : diseaseAliases) {
                    int headOffset = sentence.indexOf(dietA);
                    int tailOffset = sentence.indexOf(diseaseA);
                    if (headOffset > -1 && tailOffset > -1 && headOffset != tailOffset) {
                        // 查找代谢物、疾病（别称）都在的句子
                        mainSentences.add(MainSentence.builder()
                                .paperId(paperId)
                                .text(sentence)
                                .head(dietA).headOffset(headOffset)
                                .tail(diseaseA).tailOffset(tailOffset)
                                .build()
                        );
                    }
                }
            }
        }
        // 结果可能有多个句子
        return mainSentences;
    }


    private List<String> getDiseaseAlias(String disease) {
        if (!diseaseAliasMap.containsKey(disease)) {
            List<DiseaseAlias> diseaseAliases = diseaseAliasMapper.selectList(new LambdaQueryWrapper<DiseaseAlias>().eq(DiseaseAlias::getName, disease));
            ArrayList<String> res = new ArrayList<>(diseaseAliases.size());
            for (DiseaseAlias alias : diseaseAliases) {
                if (!alias.getAlias().equals(disease)) {
                    res.add(alias.getAlias());
                }
            }
            diseaseMutex.lock();
            try {
                if (!diseaseAliasMap.containsKey(disease)) {
                    diseaseAliasMap.put(disease, res);
                }
            } finally {
                diseaseMutex.unlock();
            }
        }
        return diseaseAliasMap.get(disease);
    }

    private List<String> getDietAlias(String diet) {
        if (!dietAliasMap.containsKey(diet)) {
            List<DietMetaboliteAlias> dietAlias = dietMetaboliteAliasMapper.selectList(new LambdaQueryWrapper<DietMetaboliteAlias>().eq(DietMetaboliteAlias::getName, diet));
            ArrayList<String> res = new ArrayList<>(dietAlias.size());
            for (DietMetaboliteAlias alias : dietAlias) {
                if (!alias.getAlias().equals(diet)) {
                    res.add(alias.getAlias());
                }
            }
            dietMutex.lock();
            try {
                if (!dietAliasMap.containsKey(diet)) {
                    dietAliasMap.put(diet, res);
                }
            } finally {
                dietMutex.unlock();
            }
        }
        return dietAliasMap.get(diet);
    }
}
