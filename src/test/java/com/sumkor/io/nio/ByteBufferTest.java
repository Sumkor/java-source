package com.sumkor.io.nio;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
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

    @Test
    public void byteOrder() {
        // 在TCP/IP协议规定了在网络上必须采用网络字节顺序，也就是大端模式
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        ByteOrder order = byteBuffer.order();
        System.out.println("order = " + order); // BIG_ENDIAN

        // 在操作系统中，x86和一般的OS(如windows，FreeBSD，Linux)使用的是小端模式。
        ByteOrder nativeOrder = ByteOrder.nativeOrder();
        System.out.println("nativeOrder = " + nativeOrder); // LITTLE_ENDIAN
    }

    /**
     * java.nio.Bits#static
     */
    @Test
    public void getByteOrder() throws Exception {
        // Unsafe
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (Unsafe) field.get(null);

        // ByteOrder
        ByteOrder byteOrder;
        long a = unsafe.allocateMemory(8); // long 类型为 8 个字节
        try {
            unsafe.putLong(a, 0x0102030405060708L);
            byte b = unsafe.getByte(a); // 获取低位地址的字节
            switch (b) {
                case 0x01: byteOrder = ByteOrder.BIG_ENDIAN;     break;
                case 0x08: byteOrder = ByteOrder.LITTLE_ENDIAN;  break;
                default:
                    assert false;
                    byteOrder = null;
            }
        } finally {
            unsafe.freeMemory(a);
        }
        System.out.println("byteOrder = " + byteOrder);
    }
}
