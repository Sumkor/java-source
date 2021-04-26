package com.sumkor.pool;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * @author Sumkor
 * @since 2021/4/26
 */
public class FutureTest {

    /**
     * 在当前线程获取 Callable 任务的执行结果，会进入阻塞直到完成
     */
    @Test
    public void call() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                return "This is a result";
            }
        };
        try {
            String result = callable.call();
            System.out.println("Callable 执行的结果是: " + result);
        } catch (Exception e) {
            System.out.println("There is a exception.");
        }
    }

    /**
     * 异步执行 Callable 任务，使用 Future 来获取任务的执行结果
     */
    @Test
    public void future() throws InterruptedException {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                if (true) {
                    throw new RuntimeException("oh!");
                }
                return "This is a result";
            }
        };
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<String> future = executorService.submit(callable);

        Thread.sleep(1000); // 模拟处理其他耗时操作

        try {
            String result = future.get();
            System.out.println("Callable 执行的结果是: " + result);
        } catch (ExecutionException e) {
            System.out.println("There is a exception.");
        }

        System.out.println("future.isDone() = " + future.isDone());
        System.out.println("future.isCancelled() = " + future.isCancelled());
    }
}
