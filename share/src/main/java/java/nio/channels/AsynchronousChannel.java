/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.nio.channels;

import java.io.IOException;
import java.util.concurrent.Future;  // javadoc

/**
 * A channel that supports asynchronous I/O operations. Asynchronous I/O         // 支持异步 IO 操作的通道
 * operations will usually take one of two forms:                                // 通常采用以下两种形式中的一种
 *
 * <ol>
 * <li><pre>{@link Future}&lt;V&gt; <em>operation</em>(<em>...</em>)</pre></li>  // Future<V> operation(...)
 * <li><pre>void <em>operation</em>(<em>...</em> A attachment, {@link
 *   CompletionHandler}&lt;V,? super A&gt; handler)</pre></li>                   // void operation(... A attachment, CompletionHandler<V,? super A> handler)
 * </ol>
 *
 * where <i>operation</i> is the name of the I/O operation (read or write for    // 上面两种形式中，operation 表示 IO 操作，如读或写；
 * example), <i>V</i> is the result type of the I/O operation, and <i>A</i> is   // V 是 IO 操作的结果类型；A 是附加到 IO 操作的对象，提供了可供处理的上下文；
 * the type of an object attached to the I/O operation to provide context when
 * consuming the result. The attachment is important for cases where a
 * <em>state-less</em> {@code CompletionHandler} is used to consume the result   // CompletionHandler 用于消费 IO 结果。
 * of many I/O operations.
 *
 * <p> In the first form, the methods defined by the {@link Future Future}       // 在第一种形式中，Future 用于检查操作是否完成、等待操作结果、获取操作结果。
 * interface may be used to check if the operation has completed, wait for its
 * completion, and to retrieve the result. In the second form, a {@link
 * CompletionHandler} is invoked to consume the result of the I/O operation when // 在第二种形式中，CompletionHandler 用于在 IO 操作完成时，消费操作结果。
 * it completes or fails.
 *
 * <p> A channel that implements this interface is <em>asynchronously
 * closeable</em>: If an I/O operation is outstanding on the channel and the
 * channel's {@link #close close} method is invoked, then the I/O operation
 * fails with the exception {@link AsynchronousCloseException}.
 *
 * <p> Asynchronous channels are safe for use by multiple concurrent threads.    // 并发安全
 * Some channel implementations may support concurrent reading and writing, but  // 一些通道实现可能支持并发读和写，但可能不允许在任何给定时间有多个读和一个写操作。
 * may not allow more than one read and one write operation to be outstanding at
 * any given time.
 *
 * <h2>Cancellation</h2>
 *
 * <p> The {@code Future} interface defines the {@link Future#cancel cancel}
 * method to cancel execution. This causes all threads waiting on the result of  // Future#cancel 会导致所有等待 IO 操作结果的线程抛出 CancellationException
 * the I/O operation to throw {@link java.util.concurrent.CancellationException}.
 * Whether the underlying I/O operation can be cancelled is highly implementation
 * specific and therefore not specified. Where cancellation leaves the channel,  // 底层 IO 操作是否会取消取决于具体实现，并没有作具体规定。
 * or the entity to which it is connected, in an inconsistent state, then the    // 如果取消操作将两个互联的 Channel 置于不一致状态，
 * channel is put into an implementation specific <em>error state</em> that      // 那么 Channel 将进入错误状态，避免后续再发起相识的 IO 操作。
 * prevents further attempts to initiate I/O operations that are <i>similar</i>
 * to the operation that was cancelled. For example, if a read operation is      // 比如，取消了 read 操作，但是在取消之前 channel 可能已经读到了数据。
 * cancelled but the implementation cannot guarantee that bytes have not been
 * read from the channel then it puts the channel into an error state; further   // 此时会将 channel 置为错误状态，后续再发起 read 操作则 channel 会抛出异常。
 * attempts to initiate a {@code read} operation cause an unspecified runtime
 * exception to be thrown. Similarly, if a write operation is cancelled but the
 * implementation cannot guarantee that bytes have not been written to the
 * channel then subsequent attempts to initiate a {@code write} will fail with
 * an unspecified runtime exception.
 *
 * <p> Where the {@link Future#cancel cancel} method is invoked with the {@code
 * mayInterruptIfRunning} parameter set to {@code true} then the I/O operation   // Future#cancel 的入参 mayInterruptIfRunning 置为 true，表示会中断正在执行中的任务。
 * may be interrupted by closing the channel. In that case all threads waiting   // 这种情况下，所有等待 IO 操作结果的线程会抛出 CancellationException。
 * on the result of the I/O operation throw {@code CancellationException} and
 * any other I/O operations outstanding on the channel complete with the         // Channel 中未完成的 IO 操作会以抛出 AsynchronousCloseException 而结束。
 * exception {@link AsynchronousCloseException}.
 *
 * <p> Where the {@code cancel} method is invoked to cancel read or write        // cancel 操作发生时，建议丢弃 buffer 中的数据。
 * operations then it is recommended that all buffers used in the I/O operations
 * be discarded or care taken to ensure that the buffers are not accessed while
 * the channel remains open.
 *
 *  @since 1.7
 */

public interface AsynchronousChannel
    extends Channel
{
    /**
     * Closes this channel.
     *
     * <p> Any outstanding asynchronous operations upon this channel will
     * complete with the exception {@link AsynchronousCloseException}. After a
     * channel is closed, further attempts to initiate asynchronous I/O
     * operations complete immediately with cause {@link ClosedChannelException}.
     *
     * <p>  This method otherwise behaves exactly as specified by the {@link
     * Channel} interface.
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    @Override
    void close() throws IOException;
}
