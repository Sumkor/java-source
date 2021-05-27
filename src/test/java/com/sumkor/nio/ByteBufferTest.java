package com.sumkor.nio;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 0 <= mark <= position <= limit <= capacity
 * @see java.nio.Buffer
 *
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

        ByteOrder order = byteBuffer.order(); // Java和所有的网络通讯协议都是使用Big-Endian的编码。
        System.out.println("order = " + order);
    }

    /**
     * 手动修改 limit、position
     */
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

    /**
     * 先把 position~limit 之间的数据拷贝到 0~n 处
     * 再把 position 设置为 n+1，把 limit 设为 capacity
     */
    @Test
    public void compact() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put(new byte[]{1, 2, 3});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        byteBuffer.flip();
        byte b = byteBuffer.get();
        System.out.println("b = " + b);

        byteBuffer.compact();
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        b = byteBuffer.get();
        System.out.println("b = " + b);

        b = byteBuffer.get();
        System.out.println("b = " + b);

        /**
         * byteBuffer = [1, 2, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 1
         * byteBuffer = [2, 3, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 3
         * b = 0
         */
    }

    /**
     * 通过调用 Buffer.mark() 将当前的 position 的值保存起来,
     * 随后可以通过调用 Buffer.reset() 方法将 position 的值恢复回来.
     */
    @Test
    public void mark() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put(new byte[]{1, 2, 3});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        byteBuffer.flip();
        byte b = byteBuffer.get();
        System.out.println("b = " + b);

        byteBuffer.mark();

        b = byteBuffer.get();
        System.out.println("b = " + b);

        byteBuffer.reset();

        b = byteBuffer.get();
        System.out.println("b = " + b);

        /**
         * byteBuffer = [1, 2, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 1
         * b = 2
         * b = 2
         */
    }
}
