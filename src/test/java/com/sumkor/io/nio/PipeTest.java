package com.sumkor.io.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * @author Sumkor
 * @since 2021/5/29
 */
public class PipeTest {

    /**
     * NIO支持tcp的半连接，由于TCP是全双工的，即有输入流和输出流。在某些时候我们可以中断其中一个流，而另一个流仍然可以继续工作。
     * 比如作为客户端我们可以关闭输出流，但是仍然能继续接收到服务端发送的数据。
     * 当客户端关闭了输出流，实际上会送FIN包到服务端，服务端接收到后响应ACK，若服务端不发送FIN包，关闭服务端的输出流(客户端的输入流)时，
     * 则服务端仍然能继续发送(响应)数据给客户端，客户端也仍然可以继续接收到数据。
     * NIO的Pipe就是通过两个socket的半连接实现的单项数据传输。
     *
     * https://www.cnblogs.com/Jack-Blog/p/12061595.html
     *
     * netstat -aon|findstr 17236
     *
     * Pipe 的 sinkChannel 和 sourceChannel 是服务器内部的两个不同线程之间的数据交互
     */
    @Test
    public void test() throws IOException, InterruptedException {
        Pipe pipe = Pipe.open();

        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                Pipe.SinkChannel sinkChannel = pipe.sink(); // 发送消息
                ByteBuffer byteBuffer = ByteBuffer.allocate(50);
                try {
                    byteBuffer.put("i am a message. ".getBytes());
                    byteBuffer.flip();
                    sinkChannel.write(byteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                Pipe.SourceChannel sourceChannel = pipe.source(); // 接收消息
                ByteBuffer byteBuffer = ByteBuffer.allocate(50);
                try {
                    Thread.sleep(100000);
                    sourceChannel.read(byteBuffer);
                    System.out.println(new String(byteBuffer.array()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        threadA.start();
        threadA.join();

        threadB.start();
        threadB.join();
    }
}
