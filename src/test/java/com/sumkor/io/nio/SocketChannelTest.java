package com.sumkor.io.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Sumkor
 * @since 2021/5/25
 */
public class SocketChannelTest {

    /**
     * Java NIO 的前生今世 之二 NIO Channel 小结
     * https://segmentfault.com/a/1190000006824107
     *
     * Stream 和 NIO Channel 对比
     *
     *     可以在同一个 Channel 中执行读和写操作，然而同一个 Stream 仅仅支持读或写.
     *     Channel 可以非阻塞地读写，而 Stream 是阻塞地读写.
     *     Channel 总是从 Buffer 中读取数据，或将数据写入到 Buffer 中.
     *
     * Channel 类型有:
     *
     *     FileChannel，文件操作
     *     DatagramChannel，UDP 操作
     *     SocketChannel，TCP 操作
     *     ServerSocketChannel，TCP 操作，使用在服务器端.
     */

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
            // waiting to connect...
        }

        // 从 Channel 读取数据到 Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        while (socketChannel.read(byteBuffer) == 0) {
            // waiting to read...
        }
        // Buffer 转为 String
        System.out.println(bufferToString(byteBuffer));

        // String 写入 Buffer
        byteBuffer.clear();
        byteBuffer.put("Hello, I am Client. ".getBytes());
        // Buffer 写入 Channel
        byteBuffer.flip();
        while (socketChannel.write(byteBuffer) == 0) {
            // waiting to write...
        }
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
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        byteBuffer.put("Hello, I am Server. ".getBytes());
        // Buffer 写入 Channel
        byteBuffer.flip();
        socketChannel.write(byteBuffer);

        // Channel 写入 Buffer
        byteBuffer.clear();
        socketChannel.read(byteBuffer);
        // Buffer 转为 String
        System.out.println(bufferToString(byteBuffer));
    }

    /**
     * Buffer 转换 String
     */
    private String bufferToString(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.flip();
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        CharBuffer charBuffer = decoder.decode(byteBuffer);
        return charBuffer.toString();
    }
}
