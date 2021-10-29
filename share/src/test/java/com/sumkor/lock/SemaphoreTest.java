package com.sumkor.lock;

import org.junit.Test;

import java.util.concurrent.Semaphore;

/**
 * @author Sumkor
 * @since 2021/3/31
 */
public class SemaphoreTest {

    @Test
    public void acquireShared() throws InterruptedException {
        Semaphore semaphore = new Semaphore(10, true);
        semaphore.acquire(5);
        System.out.println("semaphore = " + semaphore); // 5

        semaphore.acquire(6); // 阻塞
        System.out.println("semaphore = " + semaphore);
    }

    /**
     * 释放许可的时候并不会检查当前线程有没有获取过许可
     * 因此可能会动态增加许可
     */
    @Test
    public void releaseShared() {
        Semaphore semaphore = new Semaphore(10, false);
        semaphore.release(5);
        System.out.println("semaphore = " + semaphore); // 15
    }
}
