package test;

import com.hh.entity.application.test.TestTask;
import com.hh.function.application.CnkiDatabaseService;
import com.hh.function.base.ContextSingletonFactory;
import com.hh.function.base.ThreadPoolFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author ab875
 */
public class Test {
    public static ApplicationContext context = ContextSingletonFactory.getInstance();
    public static CnkiDatabaseService dataBaseUtils = context.getBean("dataBaseUtils", CnkiDatabaseService.class);
    public static ThreadPoolFactory threadPoolFactory = context.getBean("threadPoolFactory", ThreadPoolFactory.class);

    // public static DataSource dataSource = context.getBean("dataSource", DataSource.class);

    public static void main(String[] args1) throws Exception {
        multiThreadStart();
    }

    public static void multiThreadStart() {
        ExecutorService threadPool = threadPoolFactory.getThreadPool(ThreadPoolFactory.WORK_POOL_PREFIX);
        String url = "https://github.com/sa1utyeggs/cnki-anisys_all-crawler";
        Map<String, String> excessHeaders = new HashMap<>(4);
        Map<String, Object> params = new HashMap<>(4);
        // excessHeaders.put("Host", "baidu.com");
        try {
            TestTask testTask = new TestTask(0L, url, url, params, excessHeaders, TestTask.GET);
            threadPool.submit(testTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

