package com.hh.entity.cnki;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ab875
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("paper_info")
public class PaperInfo extends Model<PaperInfo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String metabolite;

    private String disease;

    private String title;

    private String url;

    private Integer relation;

    private Double confidence;

    private Long mainSentenceId;

    private String uniqueKey;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}