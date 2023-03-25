package com.hh.entity.cnki;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ab875
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("paper_abstract")
public class PaperAbstract {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private String text;
}
