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
import java.util.Set;

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
     * Selector 的基本使用流程
     *
     * 1. 通过 Selector.open() 打开一个 Selector.
     * 2. 将 Channel 注册到 Selector 中, 并设置需要监听的事件(interest set)
     * 3. 不断重复:
     *    调用 select() 方法
     *    调用 selector.selectedKeys() 获取 selected keys
     *    迭代每个 selected key:
     *       *从 selected key 中获取 对应的 Channel 和附加信息(如果有的话)
     *       *判断是哪些 IO 事件已经就绪了, 然后处理它们. 如果是 OP_ACCEPT 事件, 则通过 accept() 获取 SocketChannel, 并将它设置为非阻塞的, 并注册到 Selector 中.
     *       *根据需要更改 selected key 的监听事件.
     *       *将已经处理过的 key 从 selected keys 集合中删除.
     *
     *
     * SelectionKey#isAcceptable: a connection was accepted by a ServerSocketChannel.
     * SelectionKey#isConnectable: a connection was established with a remote server.
     * SelectionKey#isReadable: a channel is ready for reading.
     * SelectionKey#isWritable: a channel is ready for writing.
     */

    @Test
    public void server() throws IOException {
        final int BUF_SIZE = 256;
        final int TIMEOUT = 3000;

        // 打开服务端 Socket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 打开 Selector
        Selector selector = Selector.open();

        // 服务端 Socket 监听8080端口，并配置为非阻塞模式（必须是非阻塞才可以注册 Selector）
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);

        // 将 channel 注册到 selector 中，返回一个 SelectionKey 对象。
        // 通常都是先监听 OP_ACCEPT 事件，然后在 OP_ACCEPT 事件到来时，再监听该 Channel 的 OP_READ 事件。
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 通过调用 select 方法，阻塞等待直到 channel 可进行 IO 操作
            if (selector.select(TIMEOUT) == 0) {
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
                    // 当 OP_ACCEPT 事件到来时，从 ServerSocketChannel 中获取一个 SocketChannel，代表客户端的连接
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    // 再将这个 Channel 的 OP_READ 注册到 Selector 中，表示监听 OP_READ 事件
                    clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUF_SIZE));
                    // 注意，如果没有监听 OP_READ，即 interest set 仍然是 OP_ACCEPT 的话，那么 select 方法会直接返回.
                }

                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = (ByteBuffer) key.attachment(); // 获取监听 OP_READ 事件时所设置的附加对象 ByteBuffer
                    long bytesRead = clientChannel.read(buf);
                    if (bytesRead == -1) {
                        clientChannel.close();
                    } else if (bytesRead > 0) {
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length: " + bytesRead);
                    }
                }

                if (key.isValid() && key.isWritable()) {
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    buf.flip();
                    SocketChannel clientChannel = (SocketChannel) key.channel();

                    clientChannel.write(buf);

                    if (!buf.hasRemaining()) {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    buf.compact();
                }
            }
        }
    }
}
