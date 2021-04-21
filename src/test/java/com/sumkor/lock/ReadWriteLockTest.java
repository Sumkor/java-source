package com.sumkor.lock;

import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/4/21
 */
public class ReadWriteLockTest {

    static final int SHARED_SHIFT   = 16;
    static final int SHARED_UNIT    = (1 << SHARED_SHIFT);
    static final int MAX_COUNT      = (1 << SHARED_SHIFT) - 1;
    static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

    /** Returns the number of shared holds represented in count  */
    static int sharedCount(int c)    { return c >>> SHARED_SHIFT; }
    /** Returns the number of exclusive holds represented in count  */
    static int exclusiveCount(int c) { return c & EXCLUSIVE_MASK; }

    @Test
    public void count() {
        System.out.println("SHARED_UNIT = " + SHARED_UNIT);// 65536
        System.out.println("MAX_COUNT = " + MAX_COUNT);    // 65535
        System.out.println("Integer.toBinaryString(EXCLUSIVE_MASK) = " + Integer.toBinaryString(EXCLUSIVE_MASK)); // 1111 1111 1111 1111

        System.out.println("Integer.MAX_VALUE = " + Integer.MAX_VALUE); // 2147483647
        System.out.println("(1 << 31 - 1) = " + ((1 << 31) - 1));       // 2147483647
    }

    @Test
    public void count02() {
        int state = 10;
        System.out.println("sharedCount(state) = " + sharedCount(state));
        System.out.println("exclusiveCount(state) = " + exclusiveCount(state));
    }
}
