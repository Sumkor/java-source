package com.sumkor.io.aio;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Sumkor
 * @since 2021/6/2
 */
public class AIOFileChannelTest {

    @Test
    public void read() throws IOException, ExecutionException, InterruptedException {
        Path path = new File("D://a.txt").toPath();
        AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        /**
         * @see java.nio.channels.AsynchronousFileChannel#open(java.nio.file.Path, java.util.Set, java.util.concurrent.ExecutorService, java.nio.file.attribute.FileAttribute[])
         * @see sun.nio.fs.WindowsFileSystemProvider#newAsynchronousFileChannel(java.nio.file.Path, java.util.Set, java.util.concurrent.ExecutorService, java.nio.file.attribute.FileAttribute[])
         * @see sun.nio.fs.WindowsChannelFactory#newAsynchronousFileChannel(java.lang.String, java.lang.String, java.util.Set, long, sun.nio.ch.ThreadPool)
         * @see sun.nio.ch.WindowsAsynchronousFileChannelImpl#open(java.io.FileDescriptor, boolean, boolean, sun.nio.ch.ThreadPool)
         * @see sun.nio.ch.WindowsAsynchronousFileChannelImpl#WindowsAsynchronousFileChannelImpl(java.io.FileDescriptor, boolean, boolean, sun.nio.ch.Iocp, boolean)
         */

        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        Future<Integer> future = asynchronousFileChannel.read(byteBuffer, 0);

        Integer integer = future.get();
        System.out.println(integer);
        System.out.println(new String(byteBuffer.array()));
    }

    @Test
    public void read02() throws IOException {
        Path path = new File("D://a.txt").toPath();
        AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        asynchronousFileChannel.read(byteBuffer, 0, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                System.out.println(result);
                System.out.println(new String(byteBuffer.array()));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println(exc.getMessage());
            }
        });
    }

    @Test
    public void write() throws IOException {
        Path path = new File("D://a.txt").toPath();
        AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        byteBuffer.put("hello world".getBytes());
        byteBuffer.flip();
        asynchronousFileChannel.write(byteBuffer, 0, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                System.out.println(result);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println(exc.getMessage());
            }
        });
    }
}
