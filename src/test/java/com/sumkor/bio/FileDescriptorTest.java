package com.sumkor.bio;

import org.junit.Test;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

/**
 * @author Sumkor
 * @since 2021/5/30
 */
public class FileDescriptorTest {

    /**
     * FileDescriptor 文件描述符类的实例用作与基础机器有关的某种结构的不透明句柄，该结构表示开放文件、开放套接字或者字节的另一个源或接收者。
     * 文件描述符的主要实际用途是创建一个包含该结构的 FileInputStream 或 FileOutputStream。
     *
     * FileDescriptor.out 是标准输出流的句柄。通常，此文件描述符不是直接使用的，而是通过称为 System.out 的输出流使用的。
     */
    @Test
    public void out() throws IOException {
        FileOutputStream out = new FileOutputStream(FileDescriptor.out);
        out.write('A');
        out.flush();

        System.out.print("A");
    }

    /**
     * 使用 FileDescriptor 构建 FileOutputStream 写入文件，覆盖模式
     */
    @Test
    public void write() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("D://b.txt", "rw");
        FileDescriptor fd = randomAccessFile.getFD();
        FileOutputStream fileOutputStream = new FileOutputStream(fd);
        fileOutputStream.write("bbbbbb".getBytes());
        fileOutputStream.flush();
    }

    /**
     * SDP需要网卡支持InfiniBand高速网络通信技术，windows不支持该协议。
     */
    @Test
    public void socket() throws IOException {
        Socket socket = new Socket("localhost", 9999);
        /**
         * java.net.ConnectException: Connection refused: connect
         *
         * 	at java.net.DualStackPlainSocketImpl.connect0(Native Method)
         * 	at java.net.DualStackPlainSocketImpl.socketConnect(DualStackPlainSocketImpl.java:79)
         * 	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
         * 	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
         * 	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
         * 	at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:172)
         * 	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
         * 	at java.net.Socket.connect(Socket.java:589)
         * 	at java.net.Socket.connect(Socket.java:538)
         * 	at java.net.Socket.<init>(Socket.java:434)
         * 	at java.net.Socket.<init>(Socket.java:211)
         * 	at com.sumkor.bio.FileDescriptorTest.socket(FileDescriptorTest.java:48)
         * 	at ...
         *
         * Disconnected from the target VM, address: '127.0.0.1:62264', transport: 'socket'
         *
         * Process finished with exit code -1
         */

        /**
         * Window 下创建 Socket
         *
         * @see Socket#Socket(java.net.SocketAddress, java.net.SocketAddress, boolean)
         * @see Socket#createImpl(boolean)
         *
         * 创建文件描述符
         * @see java.net.AbstractPlainSocketImpl#create(boolean)
         *
         * 创建 Socket，与文件描述符绑定
         * @see java.net.DualStackPlainSocketImpl#socketCreate(boolean)
         * @see java.net.DualStackPlainSocketImpl#socket0(boolean, boolean)
         */
    }
}
