package com.sumkor.io.bio;

import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Sumkor
 * @since 2022/8/12
 */
public class ConnectionResetTest {

    @Test
    public void server() throws Exception {
        ServerSocket serverSocket = new ServerSocket(9999);
        Socket socket = serverSocket.accept();

        System.out.println("server connect success: " + socket);

        // write
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello, I am server. ".getBytes());
        outputStream.flush();

        System.out.println("server write finish.");

        Thread.sleep(1000);
        socket.close();
    }

    /**
     * 服务器返回了 RST 时，如果此时客户端正在从 Socket 套接字的输出流中读数据则会提示 "Connection reset"；
     */
    @Test
    public void client01() throws Exception {
        Socket socket = new Socket("localhost", 9999);

        Thread.sleep(3000);

        // read
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[20];
        inputStream.read(bytes);
        System.out.println(new String(bytes));
    }

    /**
     * 服务器返回了 RST 时，如果此时客户端正在往 Socket 套接字的输入流中写数据则会提示 "Connection reset by peer"。
     */
    @Test
    public void client02() throws Exception {
        Socket socket = new Socket("localhost", 9999);

        Thread.sleep(3000);

        // write
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello, I am client. ".getBytes());
        outputStream.flush();
    }

    @Test
    public void client03() throws Exception {
        for (int i = 0; i < 5; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("localhost", 9999);

                        System.out.println(Thread.currentThread().getName() + " connect success: " + socket);

                        Thread.sleep(3000);

                        System.out.println(Thread.currentThread().getName() + " start to read.");

                        // read
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[20];
                        inputStream.read(bytes);

                        System.out.println(Thread.currentThread().getName() + " read success: " + new String(bytes));
                    } catch (Exception e) {
                        System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
                    }
                }
            }, "name_" + i).start();
        }
        Thread.sleep(5000);
    }

}
