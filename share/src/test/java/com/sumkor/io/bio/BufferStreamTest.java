package com.sumkor.io.bio;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * @author Sumkor
 * @since 2021/6/3
 */
public class BufferStreamTest {

    @Test
    public void input_bio() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream("D://a.txt"));
        byte[] bytes = new byte[5];
        bufferedInputStream.read(bytes);
        System.out.println(new String(bytes));

        bytes = new byte[5];
        bufferedInputStream.read(bytes);
        System.out.println(new String(bytes));
    }

    @Test
    public void input_nio() throws IOException {
        FileChannel fileChannel = FileChannel.open(new File("D://a.txt").toPath(), StandardOpenOption.READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        fileChannel.read(byteBuffer);
        System.out.println(new String(byteBuffer.array()));

        byteBuffer.clear();
        fileChannel.read(byteBuffer);
        System.out.println(new String(byteBuffer.array()));
    }
}
