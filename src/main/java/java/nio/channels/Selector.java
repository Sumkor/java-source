/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;


/**
 * A multiplexor of {@link SelectableChannel} objects.                         // SelectableChannel 对象的多路复用器
 *
 * <p> A selector may be created by invoking the {@link #open open} method of
 * this class, which will use the system's default {@link
 * java.nio.channels.spi.SelectorProvider selector provider} to
 * create a new selector.  A selector may also be created by invoking the
 * {@link java.nio.channels.spi.SelectorProvider#openSelector openSelector}
 * method of a custom selector provider.  A selector remains open until it is
 * closed via its {@link #close close} method.
 *
 * <a name="ks"></a>
 *
 * <p> A selectable channel's registration with a selector is represented by a // 当 channel 注册到 selector 时会生成一个 selector 对象，表示该 channel 已经注册到 selector 上。
 * {@link SelectionKey} object.  A selector maintains three sets of selection  // selector 中具有三种 key 集合
 * keys:
 *
 * <ul>
 *
 *   <li><p> The <i>key set</i> contains the keys representing the current     // key set 存放所有已注册的 SelectionKey 对象，相当于一个注册信息缓存。
 *   channel registrations of this selector.  This set is returned by the      // 存在缓存的情况下，同一个 channel 反复注册到 selector 都会得到相同的 SelectionKey 对象。
 *   {@link #keys() keys} method. </p></li>                                    // 这个集合由 keys() 方法返回
 *
 *   <li><p> The <i>selected-key set</i> is the set of keys such that each     // selected-key set 存放所有已就绪的 SelectionKey 对象。
 *   key's channel was detected to be ready for at least one of the operations // SelectionKey 对象可以设置对一件或多件事件感兴趣，
 *   identified in the key's interest set during a prior selection operation.  // 只要有一件事件是就绪的，被 selector 检测到后就会把该 SelectionKey 存入 selected-key 集合。
 *   This set is returned by the {@link #selectedKeys() selectedKeys} method.  // 这个集合由 selectedKeys() 方法返回
 *   The selected-key set is always a subset of the key set. </p></li>
 *
 *   <li><p> The <i>cancelled-key</i> set is the set of keys that have been    // selected-key set 存放所有已取消的 SelectionKey 对象。
 *   cancelled but whose channels have not yet been deregistered.  This set is // channel 未解除注册但是 SelectionKey 已经被取消，则会把 SelectionKey 存入该集合。
 *   not directly accessible.  The cancelled-key set is always a subset of the
 *   key set. </p></li>
 *
 * </ul>
 *
 * <p> All three sets are empty in a newly-created selector.                   // 在新创建的 selector 中，这三个集合都是空集合。
 *
 * <p> A key is added to a selector's key set as a side effect of registering a
 * channel via the channel's {@link SelectableChannel#register(Selector,int)
 * register} method.  Cancelled keys are removed from the key set during
 * selection operations.  The key set itself is not directly modifiable.
 *
 * <p> A key is added to its selector's cancelled-key set when it is cancelled,
 * whether by closing its channel or by invoking its {@link SelectionKey#cancel
 * cancel} method.  Cancelling a key will cause its channel to be deregistered  // 取消一个 SelectionKey 会导致下一次选择过程时，解除对应的 channel 的注册
 * during the next selection operation, at which time the key will removed from // 此时该 channel 的 key 会从各个 selector 中移除
 * all of the selector's key sets.
 *
 * <a name="sks"></a><p> Keys are added to the selected-key set by selection    // selector 的选择操作会把就绪的 key 添加到 selected-key 集合
 * operations.  A key may be removed directly from the selected-key set by
 * invoking the set's {@link java.util.Set#remove(java.lang.Object) remove}
 * method or by invoking the {@link java.util.Iterator#remove() remove} method
 * of an {@link java.util.Iterator iterator} obtained from the
 * set.  Keys are never removed from the selected-key set in any other way;
 * they are not, in particular, removed as a side effect of selection
 * operations.  Keys may not be added directly to the selected-key set. </p>
 *
 *
 * <a name="selop"></a>
 * <h2>Selection</h2>
 *
 * <p> During each selection operation, keys may be added to and removed from a
 * selector's selected-key set and may be removed from its key and
 * cancelled-key sets.  Selection is performed by the {@link #select()}, {@link
 * #select(long)}, and {@link #selectNow()} methods, and involves three steps:   // 选择过程的三个步骤
 * </p>
 *
 * <ol>
 *                                                                               // 步骤 (1)：
 *   <li><p> Each key in the cancelled-key set is removed from each key set of   // 将 cancelled-key 集合中的每个键从所有键集中移除（如果该键是键集的成员），并注销其通道。
 *   which it is a member, and its channel is deregistered.  This step leaves    // 此步骤使已取消键集成为空集。
 *   the cancelled-key set empty. </p></li>
 *                                                                               // 步骤 (2)：
 *   <li><p> The underlying operating system is queried for an update as to the  // 在选择操作开始时，selector 将向底层操作系统查询每个剩余通道（remaining channel）是否准备就绪，
 *   readiness of each remaining channel to perform any of the operations        // 以执行由其键的兴趣集（key's interest set）确定的任何操作。
 *   identified by its key's interest set as of the moment that the selection
 *   operation began.  For a channel that is ready for at least one such         // 对于已准备好进行至少一个此类操作的通道，将执行以下两个操作之一：
 *   operation, one of the following two actions is performed: </p>
 *
 *   <ol>
 *
 *     <li><p> If the channel's key is not already in the selected-key set then  // 如果 channel's key 还没有在 selected-key 集合中
 *     it is added to that set and its ready-operation set is modified to        // 则将该 key 存入集合，并修改该 key 的已就绪的事件集合（SelectionKeyImpl#readyOps）
 *     identify exactly those operations for which the channel is now reported
 *     to be ready.  Any readiness information previously recorded in the ready  // 已就绪的事件集合中的历史数据会被清除
 *     set is discarded.  </p></li>
 *
 *     <li><p> Otherwise the channel's key is already in the selected-key set,   // 如果 channel's key 已经存在 selected-key 集合中
 *     so its ready-operation set is modified to identify any new operations     // 则该 key 的已就绪的事件集合会加入新的就绪事件
 *     for which the channel is reported to be ready.  Any readiness
 *     information previously recorded in the ready set is preserved; in other   // 已就绪的事件集合中的历史数据会被保留
 *     words, the ready set returned by the underlying system is
 *     bitwise-disjoined into the key's current ready set. </p></li>
 *
 *   </ol>
 *
 *   If all of the keys in the key set at the start of this step have empty
 *   interest sets then neither the selected-key set nor any of the keys'        // 如果 key 的 interest sets 都是空的，则不会执行任何操作
 *   ready-operation sets will be updated.
 *
 *   <li><p> If any keys were added to the cancelled-key set while step (2) was  // 如果在步骤 (2) 的执行过程中要将任意键添加到 cancelled-key 集合中，则处理过程如步骤 (1)。
 *   in progress then they are processed as in step (1). </p></li>
 *
 * </ol>
 *
 * <p> Whether or not a selection operation blocks to wait for one or more
 * channels to become ready, and if so for how long, is the only essential
 * difference between the three selection methods. </p>
 *
 *
 * <h2>Concurrency</h2>
 *
 * <p> Selectors are themselves safe for use by multiple concurrent threads;     // Selector 本身是并发安全的，SelectionKey 也是并发安全的，但是 key set 不是
 * their key sets, however, are not.
 *
 * <p> The selection operations synchronize on the selector itself, on the key   // 在选择操作中，依次获取 Selector 对象锁、key set 对象锁、selected-key set 对象锁，见 SelectorImpl#lockAndDoSelect
 * set, and on the selected-key set, in that order.  They also synchronize on    // 在取消操作时，需要获取 cancelled-key set 对象锁，见 AbstractSelector#cancel
 * the cancelled-key set during steps (1) and (3) above.
 *
 * <p> Changes made to the interest sets of a selector's keys while a            // 在执行选择操作的过程中，更改 key 的 interest sets 对当前操作没有影响；
 * selection operation is in progress have no effect upon that operation; they   // 在下一次选择操作时，才会看到更改。
 * will be seen by the next selection operation.
 *
 * <p> Keys may be cancelled and channels may be closed at any time.  Hence the  // key 随时都可能被取消，channel 随时都可能被关闭。
 * presence of a key in one or more of a selector's key sets does not imply      // 因此从 selector 的 key set 中的 key 不一定都是有效的，该 key 的 channel 也不一定是打开状态。
 * that the key is valid or that its channel is open.  Application code should   // 应用代码因对此做好检查
 * be careful to synchronize and check these conditions as necessary if there
 * is any possibility that another thread will cancel a key or close a channel.
 *
 * <p> A thread blocked in one of the {@link #select()} or {@link                // 线程在调用 Selector#select 方法时进入阻塞，可能会被其他线程以下列三种方式之一唤醒：
 * #select(long)} methods may be interrupted by some other thread in one of
 * three ways:
 *
 * <ul>
 *
 *   <li><p> By invoking the selector's {@link #wakeup wakeup} method,           // 调用 Selector#wakeup
 *   </p></li>
 *
 *   <li><p> By invoking the selector's {@link #close close} method, or          // 调用 Selector#close
 *   </p></li>
 *
 *   <li><p> By invoking the blocked thread's {@link
 *   java.lang.Thread#interrupt() interrupt} method, in which case its          // 调用 Thread#interrupt 唤醒阻塞线程
 *   interrupt status will be set and the selector's {@link #wakeup wakeup}     // 这种情况下会设置该线程的中断状态，并调用 Selector#wakeup 方法。见 AbstractSelector#begin 和 Thread#interrupt
 *   method will be invoked. </p></li>
 *
 * </ul>
 *
 * <p> The {@link #close close} method synchronizes on the selector and all     // Selector 的 close 方法也是同步的，见 SelectorImpl#implCloseSelector
 * three key sets in the same order as in a selection operation.
 *
 * <a name="ksc"></a>
 *
 * <p> A selector's key and selected-key sets are not, in general, safe for use // 并发情况下使用 key set 和 selected-key set 都不是并发安全的
 * by multiple concurrent threads.  If such a thread might modify one of these
 * sets directly then access should be controlled by synchronizing on the set
 * itself.  The iterators returned by these sets' {@link
 * java.util.Set#iterator() iterator} methods are <i>fail-fast:</i> If the set  // key 的集合是快速失败的
 * is modified after the iterator is created, in any way except by invoking the
 * iterator's own {@link java.util.Iterator#remove() remove} method, then a
 * {@link java.util.ConcurrentModificationException} will be thrown. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see SelectableChannel
 * @see SelectionKey
 */

public abstract class Selector implements Closeable {

    /**
     * Initializes a new instance of this class.
     */
    protected Selector() { }

    /**
     * Opens a selector.
     *
     * <p> The new selector is created by invoking the {@link
     * java.nio.channels.spi.SelectorProvider#openSelector openSelector} method
     * of the system-wide default {@link
     * java.nio.channels.spi.SelectorProvider} object.  </p>
     *
     * @return  A new selector
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    /**
     * Tells whether or not this selector is open.
     *
     * @return <tt>true</tt> if, and only if, this selector is open
     */
    public abstract boolean isOpen();

    /**
     * Returns the provider that created this channel.
     *
     * @return  The provider that created this channel
     */
    public abstract SelectorProvider provider();

    /**
     * Returns this selector's key set.
     *
     * <p> The key set is not directly modifiable.  A key is removed only after
     * it has been cancelled and its channel has been deregistered.  Any
     * attempt to modify the key set will cause an {@link
     * UnsupportedOperationException} to be thrown.
     *
     * <p> The key set is <a href="#ksc">not thread-safe</a>. </p>
     *
     * @return  This selector's key set
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     */
    public abstract Set<SelectionKey> keys();

    /**
     * Returns this selector's selected-key set.
     *
     * <p> Keys may be removed from, but not directly added to, the
     * selected-key set.  Any attempt to add an object to the key set will
     * cause an {@link UnsupportedOperationException} to be thrown.
     *
     * <p> The selected-key set is <a href="#ksc">not thread-safe</a>. </p>
     *
     * @return  This selector's selected-key set
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     */
    public abstract Set<SelectionKey> selectedKeys();

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a non-blocking <a href="#selop">selection
     * operation</a>.  If no channels have become selectable since the previous
     * selection operation then this method immediately returns zero.
     *
     * <p> Invoking this method clears the effect of any previous invocations
     * of the {@link #wakeup wakeup} method.  </p>
     *
     * @return  The number of keys, possibly zero, whose ready-operation sets
     *          were updated by the selection operation
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     */
    public abstract int selectNow() throws IOException;

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a blocking <a href="#selop">selection
     * operation</a>.  It returns only after at least one channel is selected,
     * this selector's {@link #wakeup wakeup} method is invoked, the current
     * thread is interrupted, or the given timeout period expires, whichever
     * comes first.
     *
     * <p> This method does not offer real-time guarantees: It schedules the
     * timeout as if by invoking the {@link Object#wait(long)} method. </p>
     *
     * @param  timeout  If positive, block for up to <tt>timeout</tt>
     *                  milliseconds, more or less, while waiting for a
     *                  channel to become ready; if zero, block indefinitely;
     *                  must not be negative
     *
     * @return  The number of keys, possibly zero,
     *          whose ready-operation sets were updated
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     *
     * @throws  IllegalArgumentException
     *          If the value of the timeout argument is negative
     */
    public abstract int select(long timeout)
        throws IOException;

    /**
     * Selects a set of keys whose corresponding channels are ready for I/O
     * operations.
     *
     * <p> This method performs a blocking <a href="#selop">selection
     * operation</a>.  It returns only after at least one channel is selected,
     * this selector's {@link #wakeup wakeup} method is invoked, or the current
     * thread is interrupted, whichever comes first.  </p>
     *
     * @return  The number of keys, possibly zero,
     *          whose ready-operation sets were updated
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this selector is closed
     */
    public abstract int select() throws IOException;

    /**
     * Causes the first selection operation that has not yet returned to return
     * immediately.
     *
     * <p> If another thread is currently blocked in an invocation of the
     * {@link #select()} or {@link #select(long)} methods then that invocation
     * will return immediately.  If no selection operation is currently in
     * progress then the next invocation of one of these methods will return
     * immediately unless the {@link #selectNow()} method is invoked in the
     * meantime.  In any case the value returned by that invocation may be
     * non-zero.  Subsequent invocations of the {@link #select()} or {@link
     * #select(long)} methods will block as usual unless this method is invoked
     * again in the meantime.
     *
     * <p> Invoking this method more than once between two successive selection
     * operations has the same effect as invoking it just once.  </p>
     *
     * @return  This selector
     */
    public abstract Selector wakeup();

    /**
     * Closes this selector.
     *
     * <p> If a thread is currently blocked in one of this selector's selection
     * methods then it is interrupted as if by invoking the selector's {@link
     * #wakeup wakeup} method.
     *
     * <p> Any uncancelled keys still associated with this selector are
     * invalidated, their channels are deregistered, and any other resources
     * associated with this selector are released.
     *
     * <p> If this selector is already closed then invoking this method has no
     * effect.
     *
     * <p> After a selector is closed, any further attempt to use it, except by
     * invoking this method or the {@link #wakeup wakeup} method, will cause a
     * {@link ClosedSelectorException} to be thrown. </p>
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public abstract void close() throws IOException;

}
