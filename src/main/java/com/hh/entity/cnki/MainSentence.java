package com.hh.entity.cnki;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
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
@TableName("paper_main_sentence")
@Builder
public class MainSentence {
    public static final HashMap<Integer, String> RELATION_EXPLAIN;

    static {
        RELATION_EXPLAIN = new HashMap<>();
        RELATION_EXPLAIN.put(0, "未知");
        RELATION_EXPLAIN.put(1, "不相关");
        RELATION_EXPLAIN.put(2, "正相关");
        RELATION_EXPLAIN.put(3, "负相关");
    }

    public MainSentence(String text, String relation, String head, Integer headOffset, String tail, Integer tailOffset) {
        this.text = text;
        this.relation = relation;
        this.head = head;
        this.headOffset = headOffset;
        this.tail = tail;
        this.tailOffset = tailOffset;
    }

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Double confidence;

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
