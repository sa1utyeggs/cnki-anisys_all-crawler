package com.hh.mapper.cnki;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hh.entity.cnki.MainSentence;
import com.hh.entity.cnki.PaperAbstract;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ab875
 */
@Mapper
public interface PaperMainSentenceMapper extends BaseMapper<MainSentence> {
}
