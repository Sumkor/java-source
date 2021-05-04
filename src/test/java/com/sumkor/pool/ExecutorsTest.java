package com.sumkor.pool;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
}
