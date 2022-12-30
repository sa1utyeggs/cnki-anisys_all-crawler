package com.hh.entity.system;

import com.hh.function.PaperDetail;
import com.hh.function.PaperNum;
import com.hh.function.system.Const;
import com.hh.function.system.ContextSingltonFactory;
import com.hh.utils.DataBaseUtils;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.SQLException;
import java.util.List;

/**
 * @author 86183
 */
@Data
public class InsertTask implements Runnable {
    private String disease;
    private DataBaseUtils dataBaseUtils;


    public InsertTask(String disease) {
        dataBaseUtils = ContextSingltonFactory.getInstance().getBean("dataBaseUtils", DataBaseUtils.class);
        this.disease = disease;
    }

    @Override
    public void run() {
        try {
            searchAndInsert(disease, true, false);
            // 完成后修改疾病数据状态
            dataBaseUtils.setDiseaseStatus(disease, Const.FINISHED);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    PaperDetail.insertPaperInfo(metabolite, disease, Const.SEARCH_KY, Const.EXCLUSION_WORDS, maxPaperNumPerTime, false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}