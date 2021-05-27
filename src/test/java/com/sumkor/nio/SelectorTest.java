package com.sumkor.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Sumkor
 * @since 2021/5/26
 */
public class SelectorTest {

    /**
     * Java NIO 的前生今世 之四 NIO Selector 详解
     * https://segmentfault.com/a/1190000006824196
     *
     * Selector 允许一个单一的线程来操作多个 Channel
     * 但是因为在一个线程中使用了多个 Channel，因此也会造成了每个 Channel 传输效率的降低。
     *
     * 为了使用 Selector，我们首先需要将 Channel 注册到 Selector 中，
     * 随后调用 Selector 的 select() 方法，这个方法会阻塞，直到注册在 Selector 中的 Channel 发送可读写事件.
     * 当这个方法返回后，当前的这个线程就可以处理 Channel 的事件了。
     *
     *
     * SelectionKey#isAcceptable: a connection was accepted by a ServerSocketChannel.
     * SelectionKey#isConnectable: a connection was established with a remote server.
     * SelectionKey#isReadable: a channel is ready for reading.
     * SelectionKey#isWritable: a channel is ready for writing.
     */

    private final int port = 9999;

    /**
     * 非阻塞 NIO 服务端，先写后读
     */
    @Test
    public void server() throws IOException {
        // 打开服务端 Socket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 打开 Selector
        Selector selector = Selector.open();

        // 服务端 Socket 监听8080端口，并配置为非阻塞模式（必须是非阻塞才可以注册 Selector）
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        // 将 Channel 注册到 Selector 中，监听 OP_ACCEPT 事件，等待连接建立之后再监听其他事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 通过调用 select 方法，阻塞等待直到 Channel 可进行 IO 操作
            if (selector.select(3000) == 0) {
                System.out.print(".");
                continue;
            }

            // 获取 I/O 操作就绪的 SelectionKey，通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                // 当获取一个 SelectionKey 后，就要将它删除，表示我们已经对这个 IO 事件进行了处理.
                keyIterator.remove();

                // 注意，在 OP_ACCEPT 事件中，从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                // 而在 OP_WRITE 和 OP_READ 事件中，从 key.channel() 返回的是 SocketChannel.

                if (key.isAcceptable()) {
                    System.out.println("Channel is acceptable...");
                    // 当 OP_ACCEPT 事件到来时，从 ServerSocketChannel 中获取一个 SocketChannel，代表客户端的连接
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    // 监听 Channel 的读写事件，设置附加对象 ByteBuffer
                    clientChannel.register(key.selector(), SelectionKey.OP_WRITE, ByteBuffer.allocate(256));
                }

                if (key.isReadable()) {
                    System.out.println("Channel is readable...");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment(); // 获取附加对象 ByteBuffer
                    byteBuffer.clear();
                    // 从 Channel 中读取数据到 ByteBuffer
                    long bytesRead = clientChannel.read(byteBuffer);
                    if (bytesRead == -1) { // IOStatus.EOF
                        clientChannel.close();
                    } else if (bytesRead > 0) { // 说明从 Channel 读取到了数据
                        key.interestOps(SelectionKey.OP_WRITE);
                        System.out.println(new String(byteBuffer.array()));
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    System.out.println("Channel is writeable...");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                    byteBuffer.clear();
                    byteBuffer.put("hello, I am Server. ".getBytes());
                    byteBuffer.flip();
                    // 将 ByteBuffer 的数据写入 Channel
                    clientChannel.write(byteBuffer);
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    /**
     * 非阻塞 NIO 客户端，先读后写
     */
    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        Selector selector = Selector.open();

        // 连接服务端
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", port));

        // 将 Channel 注册到 Selector
        socketChannel.register(selector, SelectionKey.OP_CONNECT, ByteBuffer.allocate(256));

        // 等待 Channel 的 IO 操作就绪
        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isConnectable()) {
                    System.out.println("Channel is connectable...");
                    // 完成连接的建立
                    if (socketChannel.isConnectionPending()) {
                        socketChannel.finishConnect();
                    }
                    key.interestOps(SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    System.out.println("Channel is readable...");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment(); // 获取附加对象 ByteBuffer
                    byteBuffer.clear();
                    // 从 Channel 中读取数据到 ByteBuffer
                    long bytesRead = clientChannel.read(byteBuffer);
                    if (bytesRead == -1) { // IOStatus.EOF
                        clientChannel.close();
                    } else if (bytesRead > 0) { // 说明从 Channel 读取到了数据
                        key.interestOps(SelectionKey.OP_WRITE);
                        System.out.println(new String(byteBuffer.array()));
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    System.out.println("Channel is writeable...");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                    byteBuffer.clear();
                    byteBuffer.put("hello, I am Client. ".getBytes());
                    byteBuffer.flip();
                    // 将 ByteBuffer 的数据写入 Channel
                    clientChannel.write(byteBuffer);
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }

    /**
     * Java NIO类库Selector机制解析（上）
     * https://blog.csdn.net/haoel/article/details/2224055
     */
    @Test
    public void selectorOpen() {
        int MAXSIZE = 65535;
        Selector[] selectors = new Selector[MAXSIZE];
        try {
            for (int i = 0; i < MAXSIZE; ++i) {
                selectors[i] = Selector.open();
                //selectors[i].close();
            }
            Thread.sleep(3000000);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Java NIO类库Selector机制解析（下）
     * https://blog.csdn.net/haoel/article/details/2224069
     *
     * 对于熟悉于系统调用的C/C++程序员来说，一个阻塞在select上的线程有以下三种方式可以被唤醒：
     *
     * 1）  有数据可读/写，或出现异常。
     * 2）  阻塞时间到，即time out。
     * 3）  收到一个non-block的信号。可由kill或pthread_kill发出。
     *
     * 所以，Selector.wakeup()要唤醒阻塞的select，那么也只能通过这三种方法，其中：
     *
     * 1）第二种方法可以排除，因为select一旦阻塞，应无法修改其time out时间。
     * 2）而第三种看来只能在Linux上实现，Windows上没有这种信号通知的机制。
     *
     * 所以，看来只有第一种方法了。再回想到为什么每个Selector.open()，在Windows会建立一对自己和自己的loopback的TCP连接；在Linux上会开一对pipe（pipe在Linux下一般都是成对打开），
     * 估计我们能够猜得出来——那就是如果想要唤醒select，只需要朝着自己的这个loopback连接发点数据过去，于是，就可以唤醒阻塞在select上的线程了。
     *
     * 可见，JDK的Selector自己和自己建的那些TCP连接或是pipe，正是用来实现Selector的notify和wakeup的功能的。
     */
}
