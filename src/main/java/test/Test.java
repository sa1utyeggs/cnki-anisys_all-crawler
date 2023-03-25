package test;

import com.hh.task.Task;
import com.hh.task.cnki.AliasTask;
import com.hh.task.qidian.RankTask;
import com.hh.task.test.TestTask;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.Const;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.ThreadPoolFactory;
import org.springframework.context.ApplicationContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author ab875
 */
public class Test {
    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);
    public static ThreadPoolFactory threadPoolFactory = context.getBean("threadPoolFactory", ThreadPoolFactory.class);


    // public static DataSource dataSource = context.getBean("dataSource", DataSource.class);

    public static void main(String[] args1) throws Exception {
        getAlias();
    }

    public static void getAlias() throws SQLException {
        List<String> allDisease = dataBaseUtils.getAllDisease();
        ArrayList<Task<?>> tasks = new ArrayList<>(allDisease.size());
        for (String s : allDisease) {
            AliasTask task = new AliasTask(AliasTask.TYPE_DISEASE, s);
            tasks.add(task);
        }
        List<Future<?>> futures = multiThreadStart(tasks);
        System.out.println(futures.size());
    }

    public static void getRank() {
        String url = "https://www.qidian.com/rank/";
        Map<String, String> excessHeaders = new HashMap<>(4);
        Map<String, Object> params = new HashMap<>(4);
        RankTask rankTask = new RankTask(url);
        ArrayList<Task<?>> tasks = new ArrayList<>();
        tasks.add(rankTask);
        List<Future<?>> futures = multiThreadStart(tasks);

    }

    public static List<Future<?>> multiThreadStart(List<Task<?>> tasks) {
        ExecutorService threadPool = threadPoolFactory.getThreadPool(ThreadPoolFactory.WORK_POOL_PREFIX);
        ArrayList<Future<?>> res = new ArrayList<>(tasks.size());
        try {
            for (Task<?> task : tasks) {
                res.add(threadPool.submit(task));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }


    public static void test() {
        String url = "https://github.com/sa1utyeggs/cnki-anisys_all-crawler";
        Map<String, String> excessHeaders = new HashMap<>(4);
        Map<String, Object> params = new HashMap<>(4);
        // excessHeaders.put("Host", "baidu.com");
        TestTask testTask = new TestTask(0L, url, url, params, excessHeaders, Const.HTTP_GET);
        ArrayList<Task<?>> tasks = new ArrayList<>();
        tasks.add(testTask);
        List<Future<?>> futures = multiThreadStart(tasks);

        // 处理结果
        for (Future<?> future : futures) {
            if (future.isDone()) {
                try {
                    System.out.println(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

