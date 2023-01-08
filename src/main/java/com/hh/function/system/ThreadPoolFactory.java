package com.hh.function.system;

import lombok.Data;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 86183
 * 注意：Thread的命名规则必须用 "-" 分割，且最后一部分必须是 int 类型
 */
@Data
public class ThreadPoolFactory {
    public static final String WORK_POOL_PREFIX = "cnki-worker-pool-thread-";
    public static final String HTTP_CONNECTION_POOL_PREFIX = "cnki-http-pool-thread-";

    private int threadNum;

    public ExecutorService getThreadPool(String threadNamePrefix) {

        return buildThreadPool(threadNum, threadNamePrefix);
    }

    private ExecutorService buildThreadPool(int threadNum, String threadNamePrefix) {
        // 将 corePoolSize 设置为 0，在所有任务结束后自动关闭线程池
        return new ThreadPoolExecutor(0,
                threadNum,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new CnkiThreadFactory(threadNamePrefix),
                new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * 自定义 ThreadFactory，自定义线程信息
     * 仿照 DefaultThreadFactory 的写法
     */
    static class CnkiThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CnkiThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        }
    }

    public static int getThreadId() {
        String[] split = Thread.currentThread().getName().split("-");
        return Integer.parseInt(split[split.length - 1]);
    }
}
