package com.sumkor.pool;

import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/5/7
 */
public class ThreadLocalTest {

    /**
     * 多个线程使用同一个 ThreadLocal。观察到多个线程使用不同的 ThreadLocalMap
     */
    @Test
    public void test() throws InterruptedException {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("haha");
        String result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result);

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

    /**
     * 同一个线程使用多个 ThreadLocal。观察到多个 ThreadLocal 使用同一个 ThreadLocalMap，即线程使用的 ThreadLocalMap 不会变！
     *
     * 本例中两个 threadLocal 实例会定位到同一个 ThreadLocalMap 的不同桶上。
     */
    @Test
    public void test02() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("haha");
        String result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result); // haha
        System.out.println("Thread.currentThread() = " + Thread.currentThread());

        threadLocal = new ThreadLocal<>();
        threadLocal.set("xixi");
        result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result); // xixi
        System.out.println("Thread.currentThread() = " + Thread.currentThread());
    }

    /**
     * 同一个线程多次使用同一个 ThreadLocal，会被定位到同一个桶上
     */
    @Test
    public void test03() {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("haha");
        threadLocal.set("xixi");
        String result = threadLocal.get();
        System.out.println(Thread.currentThread().getName() + " " + result); // xixi
    }

    /**
     * cleanSomeSlots 对 n 的控制
     */
    @Test
    public void test04() {
        int n = 16;
        System.out.println(n >>>= 1);
        System.out.println("n = " + n);
    }

}
