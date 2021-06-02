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
        byte[] bytes = new byte[36];
        inputStream.read(bytes);
        System.out.println(new String(bytes));

        /**
         * @see java.net.SocketOutputStream#socketWrite(byte[], int, int)
         * @see java.net.SocketInputStream#socketRead(java.io.FileDescriptor, byte[], int, int, int)
         */
    }

    @Test
    public void client() throws IOException {
        Socket socket = new Socket("localhost", 9999);
        // read
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[36];
        inputStream.read(bytes);
        System.out.println(new String(bytes));
        // write
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello, I am client. ".getBytes());
        outputStream.flush();
    }
}
