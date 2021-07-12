package com.sumkor.io.bio;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Sumkor
 * @since 2021/5/29
 */
public class SocketTest {

    @Test
    public void server() throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        Socket socket = serverSocket.accept();
        // write
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello, I am server. ".getBytes());
        outputStream.flush();
        // read
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[20];
        inputStream.read(bytes);
        System.out.println(new String(bytes));

        /**
         * @see java.net.SocketOutputStream#socketWrite(byte[], int, int)
         * @see java.net.SocketInputStream#socketRead(java.io.FileDescriptor, byte[], int, int, int)
         */
    }

    /**
     * 为了避免资源耗尽，我们采用线程池的方式来处理读写服务。但是这么做依然有很明显的弊端：
     * 1. 同步阻塞IO，读写阻塞，线程等待时间过长
     * 2. 在制定线程策略的时候，只能根据CPU的数目来限定可用线程资源，不能根据连接并发数目来制定，也就是连接有限制。否则很难保证对客户端请求的高效和公平。
     * 3. 多线程之间的上下文切换，造成线程使用效率并不高，并且不易扩展
     * 4. 状态数据以及其他需要保持一致的数据，需要采用并发同步控制
     *
     * 事实上NIO已经解决了上述BIO暴露的1&2问题了，服务器的并发客户端有了量的提升，不再受限于一个客户端一个线程来处理，
     * 而是一个线程可以维护多个客户端（selector 支持对多个socketChannel 监听）。
     * https://blog.csdn.net/bingxuesiyang/article/details/89888664
     */
    @Test
    public void serverWithThread() throws Exception {
        ServerSocket serverSocket = new ServerSocket(9999);
        while (true) {
            Socket socket = serverSocket.accept();
            // 提交给线程处理（可以池化）
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // write
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write("hello, I am server. ".getBytes());
                        outputStream.flush();
                        // read
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[20];
                        inputStream.read(bytes);
                        System.out.println(new String(bytes));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Test
    public void client() throws IOException {
        Socket socket = new Socket("localhost", 9999);
        // read
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[20];
        inputStream.read(bytes);
        System.out.println(new String(bytes));
        // write
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello, I am client. ".getBytes());
        outputStream.flush();
    }
}
