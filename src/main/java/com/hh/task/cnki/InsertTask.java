package com.hh.task.cnki;

import com.hh.task.Task;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.application.PaperDetail;
import com.hh.function.application.PaperNum;
import com.hh.function.base.Const;
import com.hh.function.base.ContextSingletonFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.SQLException;
import java.util.List;

/**
 * @author 86183
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InsertTask extends Task<Object> {
    private String disease;
    private CnkiDatabaseService dataBaseUtils;
    private boolean getAndInsertPaperNum;
    private boolean test;

    public InsertTask(String disease) {
        dataBaseUtils = ContextSingletonFactory.getInstance().getBean("dataBaseUtils", CnkiDatabaseService.class);
        this.disease = disease;
        this.getAndInsertPaperNum = true;
        this.test = false;
    }

    @Override
    public Object call() throws Exception {
        try {
            // 执行爬虫逻辑
            searchAndInsert(disease, getAndInsertPaperNum, test);
            // 完成后修改疾病数据状态
            dataBaseUtils.setDiseaseStatus(disease, Const.FINISHED);
            // 完成一次任务后就关闭数据库连接
            dataBaseUtils.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在知网中查询信息，并插入数据库
     */
    private void searchAndInsert(String disease, boolean getAndInsertPaperNum, boolean test) {
        // 参数：代谢物，疾病
        // type：SU按照主题搜索，KY按照关键词搜索
        // test：true不记录到数据库并输出detail信息，false记录到数据库不输出detail信息
        if (getAndInsertPaperNum) {
            PaperNum.getAndInsertMetabolitesDiseasePaperNum(disease, Const.SEARCH_KY, test, Integer.MAX_VALUE, false);
        }
        int maxPaperNumPerTime = 500;
        try {
            List<String> metabolites = dataBaseUtils.getMetaboliteByMaxPaperNumber(disease, Integer.MAX_VALUE);
            for (String metabolite : metabolites) {
                // 若 没完成 则 继续完成
                if (!dataBaseUtils.isMetaboliteDiseaseChecked(metabolite, disease)) {
                    PaperDetail.insertPaperInfo(metabolite, disease, Const.SEARCH_KY, Const.EXCLUSION_WORDS, maxPaperNumPerTime, true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
