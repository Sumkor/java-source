package com.sumkor.io.aio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Sumkor
 * @since 2021/6/2
 */
public class AIOSocketTest {

    @Test
    public void server() throws IOException, ExecutionException, InterruptedException {
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 9999));
        Future<AsynchronousSocketChannel> acceptFuture = serverSocketChannel.accept();
        AsynchronousSocketChannel asynchronousSocketChannel = acceptFuture.get();

        // 写入
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        byteBuffer.put("Hello, I am Server. ".getBytes());
        byteBuffer.flip();
        Future<Integer> writeFuture = asynchronousSocketChannel.write(byteBuffer);
        writeFuture.get();
    }

    @Test
    public void client() throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        Future<Void> connectFuture = socketChannel.connect(new InetSocketAddress("localhost", 9999));
        connectFuture.get();

        // 读取
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        Future<Integer> readFuture = socketChannel.read(byteBuffer);
        readFuture.get();
        byteBuffer.flip();
        System.out.println(new String(byteBuffer.array()));
    }
}
