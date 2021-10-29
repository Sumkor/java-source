package com.sumkor.pool;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author Sumkor
 * @since 2021/5/2
 */
public class ExecutorsTest {

    /**
     * newSingleThreadExecutor 对比 newFixedThreadPool(1)
     *
     * newSingleThreadExecutor 创建的线程池保证内部只有一个线程执行任务，并且线程数不可扩展；
     * newFixedThreadPool(1) 创建的线程池可以通过 setCorePoolSize 方法来修改核心线程数。
     */
    @Test
    public void test() {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        ThreadPoolExecutor fixedThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        fixedThreadPool.setCorePoolSize(2);
    }

    /**
     * FutureTask 交给线程池执行
     */
    @Test
    public void future() throws ExecutionException, InterruptedException {
        // 定义任务
        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(10000);
                return "哦豁";
            }
        });
        // 提交任务
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.submit(futureTask);
        // 获取结果
        String result = futureTask.get();
        System.out.println("result = " + result);
    }
}
