package com.sumkor.io.nio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Sumkor
 * @since 2021/5/28
 */
public class FileChannelTest {

    /**
     * 文件 Channel，只有阻塞模式
     * <p>
     * Channel#read 从 Channel 中读取数据
     * Map#get      从 Map     中获取数据
     */
    @Test
    public void fileChannel() throws Exception {
        RandomAccessFile raf = new RandomAccessFile("D:\\a.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        System.out.println(fileChannel.size()); // 文件大小，单位 bytes

        ByteBuffer buf = ByteBuffer.allocate(48); // 分配一个新的字节缓冲区。

        int bytesRead = fileChannel.read(buf); // Reads a sequence of bytes from this channel into the given buffer
        while (bytesRead != -1) {
            buf.flip(); // 准备从 Buffer 的 0~limit 读取数据

            while (buf.hasRemaining()) { // Buffer 中还有数据可读
                System.out.print((char) buf.get()); // 读取此缓冲区当前位置的字节，然后该位置递增。
            }

            buf.clear(); // 准备从 Buffer 的 0~capacity 写入数据
            bytesRead = fileChannel.read(buf);
        }
        raf.close();
    }

    /**
     * 使用内存映射 mmap 进行零拷贝。
     * @see FileChannel#map(java.nio.channels.FileChannel.MapMode, long, long)
     *
     * NIO详解（十）：FileChannel零拷贝技术（注意这篇文章的说法不是很严谨）
     * https://blog.csdn.net/qq_21125183/article/details/88701448
     *
     * 在 Java NIO 中 Channel（通道）就相当于操作系统中的内核缓冲区，而 Buffer（缓冲区）分为 HeapBuffer 和 DirectBuffer。
     * 其中 HeapBuffer 就相当于操作系统中的用户空间缓冲区； mmap 零拷贝使用的是直接内存 DirectBuffer，无需将数据复制到堆内存中！
     *
     * NIO 中的 FileChannel.map() 方法其实就是采用了操作系统中的内存映射方式，将内核缓冲区的内存和用户缓冲区的内存做了一个地址映射。
     * 使用 mmap 将数据从磁盘读取到内核缓冲区后，不需要再将内核缓冲区的数据复制移动到用户空间缓冲区，即可让应用程序访问数据。
     * 适合读取大文件，同时也能对文件内容进行更改，但是如果其后要通过 SocketChannel 发送，还是需要 CPU 在内核态中对数据进行拷贝。
     */
    @Test
    public void mmap() throws Exception {
        RandomAccessFile raf = new RandomAccessFile("D:\\a.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        // 返回值 MappedByteBuffer 是一个直接缓冲区，使用的是堆外内存，是 java.nio.DirectByteBufferR 的实例。
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

        // 读取缓冲区数据
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        // 由于读过一次了，需要重置
        buffer.rewind();

        // 在内核态中直接把页缓冲区的数据拷贝到网络缓冲区
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 1234));
        socketChannel.write(buffer);

    }

    /**
     * 使用 sendfile 进行零拷贝（window 不支持 sendfile）
     * @see FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)
     */
    @Test
    public void transfer() throws Exception {
        RandomAccessFile raf = new RandomAccessFile("D:\\a.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 1234));

        // file -> socket，是否需要将页缓冲区的数据拷贝到网络缓冲区取决于操作系统
        fileChannel.transferTo(0, fileChannel.size(), socketChannel);

        // file -> file
        FileChannel bFileChannel = new RandomAccessFile("D:\\b.txt", "rw").getChannel();
        fileChannel.transferTo(0, fileChannel.size(), bFileChannel);
    }

    /**
     * 读取客户端发送的数据
     */
    @Test
    public void serverSocketChannel() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(1234));

        SocketChannel socketChannel = serverSocketChannel.accept();

        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        System.out.println(new String(byteBuffer.array()));
    }

    @Test
    public void stream_vs_channel() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("D://a.txt");

        fileInputStream.read(new byte[10]);

        FileChannel channel = fileInputStream.getChannel();
        channel.read(ByteBuffer.allocate(10));
    }

    /**
     * 分散读
     * 从 Channel 中读取的数据分散（scatter）到多个 Buffer 中
     */
    @Test
    public void scatter() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("D:\\a.txt", "rw"); // hello world
        FileChannel fileChannel = raf.getChannel();

        ByteBuffer header = ByteBuffer.allocate(6);
        ByteBuffer body = ByteBuffer.allocate(5);
        ByteBuffer[] bufferArray = {header, body};
        fileChannel.read(bufferArray);

        header.flip();
        body.flip();
        System.out.println(new String(header.array())); // hello
        System.out.println(new String(body.array()));   // world
    }

    /**
     * 聚集写
     */
    @Test
    public void gather() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("D:\\a.txt", "rw");
        FileChannel fileChannel = raf.getChannel();

        ByteBuffer header = ByteBuffer.allocate(7);
        header.put("header~".getBytes());
        ByteBuffer body = ByteBuffer.allocate(5);
        body.put("body~".getBytes());
        ByteBuffer[] bufferArray = {header, body};

        header.flip();
        body.flip();

        fileChannel.write(bufferArray); // header~body~
    }
}
