package com.hh.task.cnki;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * @author 86183
 * 主要为 nlp 服务，所以包含了部分冗余信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class MainSentence {
    public static final HashMap<Integer, String> RELATION_EXPLAIN;

    static {
        RELATION_EXPLAIN = new HashMap<>();
        RELATION_EXPLAIN.put(0, "未知");
        RELATION_EXPLAIN.put(1, "不相关");
        RELATION_EXPLAIN.put(2, "正相关");
        RELATION_EXPLAIN.put(3, "负相关");
    }

    /**
     * 句子内容
     */
    private String text;

    /**
     * 相关性
     */
    private String relation;

    /**
     * nlp 的第一个关键词
     */
    private String head;

    /**
     * nlp 的第一个关键词在句子中的位置
     */
    private Integer headOffset;

    /**
     * nlp 的第二个关键词
     */
    private String tail;

    /**
     * nlp 的第二个关键词在句子中的位置
     */
    private Integer tailOffset;

}
