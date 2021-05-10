package com.sumkor.pool;

import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/5/10
 */
public class InheritableThreadLocalTest {

    /**
     * 主线程传递给子线程
     */
    @Test
    public void baseUse() throws InterruptedException {
        ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("haha");
        String result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result); // haha

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = threadLocal.get();
                System.out.println(Thread.currentThread().getName() + " " + result);
            }
        }, "sub");
        thread.start();
        thread.join();
    }

}
