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
        System.out.println("semaphore = " + semaphore);
    }
}
