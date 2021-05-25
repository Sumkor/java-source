package com.sumkor.nio;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Sumkor
 * @since 2021/5/25
 */
public class ChannelTest {

    /**
     * Java NIO 的前生今世 之二 NIO Channel 小结
     * https://segmentfault.com/a/1190000006824107
     *
     * Stream 和 NIO Channel 对比
     *
     *     可以在同一个 Channel 中执行读和写操作，然而同一个 Stream 仅仅支持读或写.
     *     Channel 可以异步地读写，而 Stream 是阻塞的同步读写.
     *     Channel 总是从 Buffer 中读取数据，或将数据写入到 Buffer 中.
     *
     * Channel 类型有:
     *
     *     FileChannel，文件操作
     *     DatagramChannel，UDP 操作
     *     SocketChannel，TCP 操作
     *     ServerSocketChannel，TCP 操作，使用在服务器端.
     */


    @Test
    public void fileChannel() throws Exception {
        RandomAccessFile aFile = new RandomAccessFile("D:\\a.txt", "rw");
        FileChannel inChannel = aFile.getChannel();

        System.out.println(inChannel.size()); // 文件大小，单位 bytes

        ByteBuffer buf = ByteBuffer.allocate(48); // 分配一个新的字节缓冲区。

        int bytesRead = inChannel.read(buf); // Reads a sequence of bytes from this channel into the given buffer
        /**
         * Channel#read 从 Channel 中读取数据
         * Map#get      从 Map     中获取数据
         */
        while (bytesRead != -1) {
            buf.flip(); // 准备从 Buffer 的 0-limit 读取数据

            while (buf.hasRemaining()) { // Buffer 中还有数据可读
                System.out.print((char) buf.get()); // 读取此缓冲区当前位置的字节，然后该位置递增。
            }

            buf.clear(); // 准备从 Buffer 的 0-capacity 写入数据
            bytesRead = inChannel.read(buf);
        }
        aFile.close();
    }

    /**
     * SocketChannel 是一个客户端用来进行 TCP 连接的 Channel.
     * 创建一个 SocketChannel 的方法有两种：
     * 1. 打开一个 SocketChannel，然后将其连接到某个服务器中
     * 2. 当一个 ServerSocketChannel 接受到连接请求时，会返回一个 SocketChannel 对象.
     */
    @Test
    public void socketChannel() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9999));

        // 非阻塞模式
        socketChannel.configureBlocking(false);
        // 在非阻塞模式中，或许连接还没有建立，connect 方法就返回了，因此需要检查当前是否是连接到了主机
        while (!socketChannel.finishConnect()) {
            System.out.println("waiting to connect...");
        }

        // 从 Channel 读取数据到 Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        int read = socketChannel.read(byteBuffer);
        System.out.println("read = " + read);

        while (socketChannel.read(byteBuffer) == 0) {
            System.out.println("waiting to read...");
        }

        byteBuffer.flip();

        // Buffer 转换 String
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        CharBuffer charBuffer = decoder.decode(byteBuffer);
        System.out.println(charBuffer.toString());
    }

    /**
     * ServerSocketChannel 是用在服务器为端的，可以监听客户端的 TCP 连接
     */
    @Test
    public void serverSocketChannel() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9999));

        SocketChannel socketChannel = serverSocketChannel.accept();

        // String 写入 Buffer
        String newData = "New String to write to file... " + System.currentTimeMillis();
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        byteBuffer.put(newData.getBytes());

        // Buffer 写入 Channel
        byteBuffer.flip();
        while(byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }
    }
}
