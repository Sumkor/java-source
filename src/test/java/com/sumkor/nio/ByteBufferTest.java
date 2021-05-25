package com.sumkor.nio;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Sumkor
 * @since 2021/5/25
 */
public class ByteBufferTest {

    /**
     * flip() 将 limit 大小设为 position，再将 position 设为 0
     * 因此可以从 buffer 的 0 到 limit 范围读取到数据
     */
    @Test
    public void flip() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put(new byte[]{1, 2, 3});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        byteBuffer.flip();
        byte b = byteBuffer.get();
        System.out.println("b = " + b);

        byteBuffer.put(new byte[]{4, 5});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        /**
         * byteBuffer = [1, 2, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 1
         * byteBuffer = [1, 4, 5, 0, 0, 0, 0, 0, 0, 0]
         */

        // byteBuffer.get();         // java.nio.BufferUnderflowException
        // byteBuffer.put((byte) 6); // java.nio.BufferOverflowException

    }

    @Test
    public void position() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put(new byte[]{1, 2, 3});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        byteBuffer.flip();
        byte b = byteBuffer.get();
        System.out.println("b = " + b);

        byteBuffer.limit(10);
        byteBuffer.position(3);

        byteBuffer.put(new byte[]{4, 5});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        /**
         * byteBuffer = [1, 2, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 1
         * byteBuffer = [1, 2, 3, 4, 5, 0, 0, 0, 0, 0]
         */
    }
}
