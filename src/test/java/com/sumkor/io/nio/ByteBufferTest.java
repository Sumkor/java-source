package com.sumkor.io.nio;

import org.junit.Test;
import sun.misc.Unsafe;
import sun.misc.VM;

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

        byteBuffer.limit(10);   // 表示第一个不应该读取或写入的元素的索引
        byteBuffer.position(3); // 表示下一个要读取或写入的元素的索引

        byteBuffer.put(new byte[]{4, 5});
        System.out.println("byteBuffer = " + Arrays.toString(byteBuffer.array()));

        /**
         * byteBuffer = [1, 2, 3, 0, 0, 0, 0, 0, 0, 0]
         * b = 1
         * byteBuffer = [1, 2, 3, 4, 5, 0, 0, 0, 0, 0]
         */
    }

    /**
     * 先把 position~limit 之间的数据拷贝到 0~n 处（说明 0~position 是已经读取的数据，可以废弃）
     * 再把 position 设置为 n+1，把 limit 设为 capacity（说明新的 position~limit 之间可以写入数据）
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

    /**
     * -verbose:gc -XX:+PrintGCDetails -Xmx10M -Xmn3M -XX:MaxDirectMemorySize=10M
     */
    @Test
    public void bufferGC() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024 * 5); // 5M
        byteBuffer = null;
        System.gc();

        /**
         * 堆内存分配 5M 空间，直接分配到老年代，发生 GC 时从老年代回收
         *
         * [GC (Allocation Failure) [PSYoungGen: 2048K->488K(2560K)] 2048K->720K(9728K), 0.0007896 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
         * [GC (Allocation Failure) [PSYoungGen: 2534K->488K(2560K)] 2766K->1044K(9728K), 0.0082250 secs] [Times: user=0.05 sys=0.00, real=0.01 secs]
         * [GC (Allocation Failure) [PSYoungGen: 2536K->488K(2560K)] 3092K->1347K(9728K), 0.0049060 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
         * [GC (System.gc()) [PSYoungGen: 943K->504K(2560K)] 6922K->6507K(9728K), 0.0043910 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
         * [Full GC (System.gc()) [PSYoungGen: 504K->0K(2560K)] [ParOldGen: 6003K->1089K(7168K)] 6507K->1089K(9728K), [Metaspace: 5044K->5044K(1056768K)], 0.0099551 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
         * Heap
         *  PSYoungGen      total 2560K, used 120K [0x00000000ffd00000, 0x0000000100000000, 0x0000000100000000)
         *   eden space 2048K, 5% used [0x00000000ffd00000,0x00000000ffd1e0f0,0x00000000fff00000)
         *   from space 512K, 0% used [0x00000000fff80000,0x00000000fff80000,0x0000000100000000)
         *   to   space 512K, 0% used [0x00000000fff00000,0x00000000fff00000,0x00000000fff80000)
         *  ParOldGen       total 7168K, used 1089K [0x00000000ff600000, 0x00000000ffd00000, 0x00000000ffd00000)
         *   object space 7168K, 15% used [0x00000000ff600000,0x00000000ff7107d0,0x00000000ffd00000)
         *  Metaspace       used 5060K, capacity 5264K, committed 5504K, reserved 1056768K
         *   class space    used 593K, capacity 627K, committed 640K, reserved 1048576K
         */
    }

    /**
     * -verbose:gc -XX:+PrintGCDetails -Xmx10M -Xmn3M -XX:MaxDirectMemorySize=10M
     *
     * 直接内存只有在使用时，才真正地分配内存。因此这里无论声明开辟多少个 10M 的直接内存，都不会造成内存溢出。
     * 但是，在声明开辟直接内存的时候，会检查所要开辟的容量是否超过 VM 的最大直接内存容量，若超过会报内存溢出。
     */
    @Test
    public void directBufferGC() {
        System.out.println(VM.maxDirectMemory() / 1024 / 1024); // 10M

        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 10);
        ByteBuffer.allocateDirect(1024 * 1024 * 11); // 到了这一行才报内存溢出

        /**
         * java.lang.OutOfMemoryError: Direct buffer memory
         *
         * 	at java.nio.Bits.reserveMemory(Bits.java:693)
         * 	at java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:123)
         * 	at java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:311)
         * 	at com.sumkor.io.nio.ByteBufferTest.directBufferGC(ByteBufferTest.java:222)
         */
    }

    /**
     * -verbose:gc -XX:+PrintGCDetails -Xmx10M -Xmn3M -XX:MaxDirectMemorySize=10M
     *
     * 直接内存只有在使用时，才真正地分配内存。
     * 当 VM 中已经分配了 10M 直接内存之后，无法继续声明开辟 10M 直接内存
     */
    @Test
    public void directBufferGC02() {
        System.out.println(VM.maxDirectMemory() / 1024 / 1024); // 10M

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024 * 10);
        byte[] _1M = new byte[1024 * 1024];
        byteBuffer.put(_1M);
        ByteBuffer.allocateDirect(1024 * 1024 * 10); // 这里报内存溢出

        /**
         * java.lang.OutOfMemoryError: Direct buffer memory
         *
         * 	at java.nio.Bits.reserveMemory(Bits.java:693)
         * 	at java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:123)
         * 	at java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:311)
         * 	at com.sumkor.io.nio.ByteBufferTest.directBufferGC02(ByteBufferTest.java:250)
         */
    }
}
