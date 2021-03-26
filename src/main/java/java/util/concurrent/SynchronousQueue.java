/*
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

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea, Bill Scherer, and Michael Scott with
 * assistance from members of JCP JSR-166 Expert Group and released to
 * the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A {@linkplain BlockingQueue blocking queue} in which each insert // 每个插入操作必须等待其他线程发起移除操作，反之亦然。
 * operation must wait for a corresponding remove operation by another
 * thread, and vice versa.  A synchronous queue does not have any // 并没有内部容量，所以无法遍历该队列
 * internal capacity, not even a capacity of one.  You cannot
 * {@code peek} at a synchronous queue because an element is only
 * present when you try to remove it; you cannot insert an element
 * (using any method) unless another thread is trying to remove it;
 * you cannot iterate as there is nothing to iterate.  The
 * <em>head</em> of the queue is the element that the first queued
 * inserting thread is trying to add to the queue; if there is no such
 * queued thread then no element is available for removal and
 * {@code poll()} will return {@code null}.  For purposes of other
 * {@code Collection} methods (for example {@code contains}), a
 * {@code SynchronousQueue} acts as an empty collection.  This queue
 * does not permit {@code null} elements.
 *
 * <p>Synchronous queues are similar to rendezvous channels used in
 * CSP and Ada. They are well suited for handoff designs, in which an
 * object running in one thread must sync up with an object running
 * in another thread in order to hand it some information, event, or
 * task.
 *
 * <p>This class supports an optional fairness policy for ordering
 * waiting producer and consumer threads.  By default, this ordering
 * is not guaranteed. However, a queue constructed with fairness set
 * to {@code true} grants threads access in FIFO order.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 * @param <E> the type of elements held in this collection
 */
public class SynchronousQueue<E> extends AbstractQueue<E>
    implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * This class implements extensions of the dual stack and dual
     * queue algorithms described in "Nonblocking Concurrent Objects
     * with Condition Synchronization", by W. N. Scherer III and
     * M. L. Scott.  18th Annual Conf. on Distributed Computing,
     * Oct. 2004 (see also
     * http://www.cs.rochester.edu/u/scott/synchronization/pseudocode/duals.html).
     * The (Lifo) stack is used for non-fair mode, and the (Fifo)
     * queue for fair mode. The performance of the two is generally
     * similar. Fifo usually supports higher throughput under
     * contention but Lifo maintains higher thread locality in common
     * applications.
     *
     * A dual queue (and similarly stack) is one that at any given
     * time either holds "data" -- items provided by put operations,
     * or "requests" -- slots representing take operations, or is
     * empty. A call to "fulfill" (i.e., a call requesting an item
     * from a queue holding data or vice versa) dequeues a
     * complementary node.  The most interesting feature of these
     * queues is that any operation can figure out which mode the
     * queue is in, and act accordingly without needing locks.
     *
     * Both the queue and stack extend abstract class Transferer
     * defining the single method transfer that does a put or a
     * take. These are unified into a single method because in dual
     * data structures, the put and take operations are symmetrical,
     * so nearly all code can be combined. The resulting transfer
     * methods are on the long side, but are easier to follow than
     * they would be if broken up into nearly-duplicated parts.
     *
     * The queue and stack data structures share many conceptual
     * similarities but very few concrete details. For simplicity,
     * they are kept distinct so that they can later evolve
     * separately.
     *
     * The algorithms here differ from the versions in the above paper
     * in extending them for use in synchronous queues, as well as
     * dealing with cancellation. The main differences include:
     *
     *  1. The original algorithms used bit-marked pointers, but
     *     the ones here use mode bits in nodes, leading to a number
     *     of further adaptations.
     *  2. SynchronousQueues must block threads waiting to become
     *     fulfilled.
     *  3. Support for cancellation via timeout and interrupts,
     *     including cleaning out cancelled nodes/threads
     *     from lists to avoid garbage retention and memory depletion.
     *
     * Blocking is mainly accomplished using LockSupport park/unpark,
     * except that nodes that appear to be the next ones to become
     * fulfilled first spin a bit (on multiprocessors only). On very
     * busy synchronous queues, spinning can dramatically improve
     * throughput. And on less busy ones, the amount of spinning is
     * small enough not to be noticeable.
     *
     * Cleaning is done in different ways in queues vs stacks.  For
     * queues, we can almost always remove a node immediately in O(1)
     * time (modulo retries for consistency checks) when it is
     * cancelled. But if it may be pinned as the current tail, it must
     * wait until some subsequent cancellation. For stacks, we need a
     * potentially O(n) traversal to be sure that we can remove the
     * node, but this can run concurrently with other threads
     * accessing the stack.
     *
     * While garbage collection takes care of most node reclamation
     * issues that otherwise complicate nonblocking algorithms, care
     * is taken to "forget" references to data, other nodes, and
     * threads that might be held on to long-term by blocked
     * threads. In cases where setting to null would otherwise
     * conflict with main algorithms, this is done by changing a
     * node's link to now point to the node itself. This doesn't arise
     * much for Stack nodes (because blocked threads do not hang on to
     * old head pointers), but references in Queue nodes must be
     * aggressively forgotten to avoid reachability of everything any
     * node has ever referred to since arrival.
     */

    /**
     * Shared internal API for dual stacks and queues.
     */
    abstract static class Transferer<E> {
        /**
         * Performs a put or take.
         *
         * @param e if non-null, the item to be handed to a consumer;   // 传输的数据元素。非空表示需要向消费者传递数据，为空表示需要向生产者请求数据
         *          if null, requests that transfer return an item
         *          offered by producer.
         * @param timed if this operation should timeout                // 该操作是否会超时
         * @param nanos the timeout, in nanoseconds
         * @return if non-null, the item provided or received; if null, // 非空表示数据元素传递或接收成功，为空表示失败
         *         the operation failed due to timeout or interrupt --  // 失败的原因有两种：1.超时；2.中断，通过 Thread.interrupted 来检测中断
         *         the caller can distinguish which of these occurred
         *         by checking Thread.interrupted.
         */
        abstract E transfer(E e, boolean timed, long nanos);
    }

    /** The number of CPUs, for spin control */
    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /**
     * The number of times to spin before blocking in timed waits.
     * The value is empirically derived -- it works well across a
     * variety of processors and OSes. Empirically, the best value
     * seems not to vary with number of CPUs (beyond 2) so is just
     * a constant.
     */
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32; // 指定超时时间情况下，当前线程最大自旋次数。 // CPU小于2，不需要自旋，否则自旋32次。

    /**
     * The number of times to spin before blocking in untimed waits.
     * This is greater than timed value because untimed waits spin
     * faster since they don't need to check times on each spin.
     */
    static final int maxUntimedSpins = maxTimedSpins * 16; // 未指定超时时间情况下，当前线程最大自旋次数。// 16倍

    /**
     * The number of nanoseconds for which it is faster to spin
     * rather than to use timed park. A rough estimate suffices.
     */
    static final long spinForTimeoutThreshold = 1000L;     // 指定超时时间情况下，若自旋结束后如果剩余时间大于该值，则阻塞相应时间

    /** Dual stack */
    static final class TransferStack<E> extends Transferer<E> { // 栈实现
        /*
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */

        /* Modes for SNodes, ORed together in node fields */
        /** Node represents an unfulfilled consumer */
        static final int REQUEST    = 0; // 请求模式，消费者请求数据
        /** Node represents an unfulfilled producer */
        static final int DATA       = 1; // 数据模式，生产者提供数据
        /** Node is fulfilling another unfulfilled DATA or REQUEST */
        static final int FULFILLING = 2; // 匹配模式，表示数据从正一个节点传递给另外的节点

        /** Returns true if m has fulfilling bit set. */
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

        /** Node class for TransferStacks. */
        static final class SNode {
            volatile SNode next;        // next node in stack         // 栈中下一个节点
            volatile SNode match;       // the node matched to this   // 当前节点所匹配的节点
            volatile Thread waiter;     // to control park/unpark     // 等待着的线程
            Object item;                // data; or null for REQUESTs // 数据元素
            int mode;                   // 节点的模式：REQUEST、DATA、FULFILLING
            // Note: item and mode fields don't need to be volatile
            // since they are always written before, and read after,
            // other volatile/atomic operations.

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) { // 若下一个节点为cmp，将其替换为节点val
                return cmp == next &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            /**
             * Tries to match node s to this node, if so, waking up thread.
             * Fulfillers call tryMatch to identify their waiters.
             * Waiters block until they have been matched.
             *
             * @param s the node to match
             * @return true if successfully matched to s
             */
            boolean tryMatch(SNode s) { // m.tryMatch(s)，表示节点m尝试与节点s进行匹配，注意入参节点s由当前线程所持有，而节点m的线程是阻塞状态的（等待匹配）
                if (match == null &&
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) { // 如果m还没有匹配者，就把s作为它的匹配者，设置m.match为s
                    Thread w = waiter;  // 节点m的线程
                    if (w != null) {    // waiters need at most one unpark
                        waiter = null;
                        LockSupport.unpark(w); // 唤醒m中的线程，两者匹配完毕（节点m的线程从方法TransferStack#awaitFulfill中被唤醒，检查得知被匹配成功了，返回匹配的节点）
                    }
                    return true;
                }
                return match == s; // 可能其它线程先一步匹配了m，判断是否是s
            }

            /**
             * Tries to cancel a wait by matching node to itself.
             */
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this); // 将节点的match属性设为自身，表示取消
            }

            boolean isCancelled() {
                return match == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = SNode.class;
                    matchOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("match"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** The head (top) of the stack */
        volatile SNode head;

        boolean casHead(SNode h, SNode nh) {
            return h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        /**
         * Creates or resets fields of a node. Called only from transfer
         * where the node to push on stack is lazily created and
         * reused when possible to help reduce intervals between reads
         * and CASes of head and to avoid surges of garbage when CASes
         * to push nodes fail due to contention.
         */
        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }

        /**
         * Puts or takes an item.
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /*
             * Basic algorithm is to loop trying one of three actions:
             *
             * 1. If apparently empty or already containing nodes of same // 当前栈内为空，或者栈顶节点模式与当前节点模式一致，尝试着把当前节点入栈并且等待一个匹配，最后会返回对应的数据
             *    mode, try to push node on stack and wait for a match,
             *    returning it, or null if cancelled.                     // 若匹配成功，返回该节点的数据。若取消，则返回空。
             *
             * 2. If apparently containing node of complementary mode,    // 如果栈顶节点模式与当前节点模式互补，尝试把当前节点入栈进行匹配，再把匹配的两个节点弹出栈。
             *    try to push a fulfilling node on to stack, match
             *    with corresponding waiting node, pop both from
             *    stack, and return matched item. The matching or
             *    unlinking might not actually be necessary because of
             *    other threads performing action 3:
             *
             * 3. If top of stack already holds another fulfilling node,  // 如果栈顶节点的模式为匹配中，则协助匹配和弹出。
             *    help it out by doing its match and/or pop
             *    operations, and then continue. The code for helping
             *    is essentially the same as for fulfilling, except
             *    that it doesn't return the item.
             */

            SNode s = null; // constructed/reused as needed
            int mode = (e == null) ? REQUEST : DATA; // 元素为空，为请求模式，否则为数据模式（注意，当前节点入栈前不会是匹配模式！）

            for (;;) { // 自旋CAS
                SNode h = head;
                if (h == null || h.mode == mode) {  // empty or same-mode // 栈顶没有元素，或者栈顶元素跟当前元素是一个模式的（请求模式或数据模式）
                    if (timed && nanos <= 0) {      // can't wait // 已超时
                        if (h != null && h.isCancelled()) // 头节点h不为空且已取消
                            casHead(h, h.next);     // pop cancelled node // 把h.next作为新的头节点 // 把已经取消的头节点弹出，并进入下一次循环
                        else
                            return null;
                    } else if (casHead(h, s = snode(s, e, h, mode))) { // 把节点s入栈，作为新的头节点，s.next指向h（因为是模式相同的，所以只能入栈）
                        SNode m = awaitFulfill(s, timed, nanos); // 等待匹配，自旋阻塞当前线程
                        if (m == s) {               // wait was cancelled // 等待被取消了，需要清除节点s
                            clean(s);               // 出栈
                            return null;
                        }
                        if ((h = head) != null && h.next == s) // 执行到这里，说明匹配成功。若栈顶有数据，头节点为h，下一个节点为s （因为是等待匹配，这里是等到了其他线程入栈新的节点，跟当前节点s匹配了）
                            casHead(h, s.next);     // help s's fulfiller // 将节点h和s出栈，把s.next作为新的头节点
                        return (E) ((mode == REQUEST) ? m.item : s.item); // 若当前为请求模式，返回匹配到的节点m的数据；否则返回节点s的数据
                    }
                } else if (!isFulfilling(h.mode)) { // try to fulfill // 执行到这里，说明栈顶元素跟当前元素的模式不同。这里判断栈顶模式不是FULFILLING（匹配中），说明是互补的
                    if (h.isCancelled())            // already cancelled
                        casHead(h, h.next);         // pop and retry // 弹出已取消的栈顶元素，并重试
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) { // 头节点不是匹配中，且非取消，则把节点s入栈，并设为匹配模式（FULFILLING|mode的结果符合TransferStack#isFulfilling）
                        for (;;) { // loop until matched or waiters disappear
                            SNode m = s.next;       // m is s's match
                            if (m == null) {        // all waiters are gone // 如果m为null，说明除了s节点外的节点都被其它线程先一步匹配掉了，就清空栈并跳出内部循环，到外部循环再重新入栈判断
                                casHead(s, null);   // pop fulfill node
                                s = null;           // use new node next time
                                break;              // restart main loop
                            }
                            SNode mn = m.next;
                            if (m.tryMatch(s)) {    // 如果m和s尝试匹配成功，就弹出栈顶的两个元素s和m
                                casHead(s, mn);     // pop both s and m
                                return (E) ((mode == REQUEST) ? m.item : s.item);
                            } else                  // lost match  // m和s匹配失败，说明m已经先一步被其它线程匹配了，需要清除
                                s.casNext(m, mn);   // help unlink // 若s的下一个节点为m，则将m换成mn
                        }
                    }
                } else {                            // help a fulfiller // 执行到这里，说明栈顶元素跟当前元素的模式不同，且栈顶是匹配模式（表示栈顶和栈顶下面的节点正在发生匹配，当前请求需要做协助工作）
                    SNode m = h.next;               // m is h's match
                    if (m == null)                  // waiter is gone // 节点m是h匹配的节点，但是m为空，说明m已经被其它线程先一步匹配了
                        casHead(h, null);       // pop fulfilling node
                    else {
                        SNode mn = m.next;
                        if (m.tryMatch(h))          // help match // 协助匹配，如果节点m和h匹配，则弹出h和m
                            casHead(h, mn);         // pop both h and m
                        else                        // lost match
                            h.casNext(m, mn);       // help unlink // 如果节点m和h不匹配，则将h的下一个节点换为mn（即m已经被其他线程匹配了，需要清除它）
                    }
                }
            }
        }

        /**
         * Spins/blocks until node s is matched by a fulfill operation.
         *
         * @param s the waiting node
         * @param timed true if timed wait
         * @param nanos timeout value
         * @return matched node, or s if cancelled
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) { // 自旋或阻塞，直到节点s被其他节点匹配
            /*
             * When a node/thread is about to block, it sets its waiter
             * field and then rechecks state at least one more time
             * before actually parking, thus covering race vs
             * fulfiller noticing that waiter is non-null so should be
             * woken.
             *
             * When invoked by nodes that appear at the point of call
             * to be at the head of the stack, calls to park are
             * preceded by spins to avoid blocking when producers and
             * consumers are arriving very close in time.  This can
             * happen enough to bother only on multiprocessors.
             *
             * The order of checks for returning out of main loop
             * reflects fact that interrupts have precedence over
             * normal returns, which have precedence over
             * timeouts. (So, on timeout, one last check for match is
             * done before giving up.) Except that calls from untimed
             * SynchronousQueue.{poll/offer} don't check interrupts
             * and don't wait at all, so are trapped in transfer
             * method rather than calling awaitFulfill.
             */
            final long deadline = timed ? System.nanoTime() + nanos : 0L; // 到期时间
            Thread w = Thread.currentThread(); // 当前线程
            int spins = (shouldSpin(s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0); // 自旋次数（自旋结束后再判断是否需要阻塞！）
            for (;;) {
                if (w.isInterrupted()) // 当前线程中断了，尝试取消节点s
                    s.tryCancel();
                SNode m = s.match; // 检查节点s是否匹配到了节点m（由其它线程的m匹配到当前线程的s，或者s已取消）
                if (m != null)
                    return m; // 如果匹配到了，直接返回m
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) { // 已超时，尝试取消节点s
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    spins = shouldSpin(s) ? (spins-1) : 0; // 如果spins大于0，则一直自减直到为0，才会执行去elseif的逻辑
                else if (s.waiter == null)
                    s.waiter = w; // establish waiter so can park next iter // 如果s的waiter为null，把当前线程注入进去，并进入下一次自旋
                else if (!timed)
                    LockSupport.park(this); // 如果不允许超时，直接阻塞，并等待被其它线程唤醒，唤醒后继续自旋并查看是否匹配到了元素
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos); // 如果允许超时且还有剩余时间，就阻塞相应时间
            }
        }

        /**
         * Returns true if node s is at head or there is an active
         * fulfiller.
         */
        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        /**
         * Unlinks s from the stack.
         */
        void clean(SNode s) {
            s.item = null;   // forget item
            s.waiter = null; // forget thread

            /*
             * At worst we may need to traverse entire stack to unlink
             * s. If there are multiple concurrent calls to clean, we
             * might not see s if another thread has already removed
             * it. But we can stop when we see any node known to
             * follow s. We use s.next unless it too is cancelled, in
             * which case we try the node one past. We don't check any
             * further because we don't want to doubly traverse just to
             * find sentinel.
             */

            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;

            // Absorb cancelled nodes at head // 找到有效head
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // Unsplice embedded nodes // 移除head到past之间已取消的节点
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /** Dual Queue */
    static final class TransferQueue<E> extends Transferer<E> { // 队列实现
        /*
         * This extends Scherer-Scott dual queue algorithm, differing,
         * among other ways, by using modes within nodes rather than
         * marked pointers. The algorithm is a little simpler than
         * that for stacks because fulfillers do not need explicit
         * nodes, and matching is done by CAS'ing QNode.item field
         * from non-null to null (for put) or vice versa (for take).
         */

        /** Node class for TransferQueue. */
        static final class QNode {
            volatile QNode next;          // next node in queue // 队列的下一个节点
            volatile Object item;         // CAS'ed to or from null // 数据元素
            volatile Thread waiter;       // to control park/unpark // 等待着的线程
            final boolean isData;         // true表示为DATA类型，false表示为REQUEST类型

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                return next == cmp &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                return item == cmp &&
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            /**
             * Tries to cancel by CAS'ing ref to this as item.
             */
            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() {
                return item == this;
            }

            /**
             * Returns true if this node is known to be off the queue
             * because its next pointer has been forgotten due to
             * an advanceHead operation.
             */
            boolean isOffList() {
                return next == this; // 如果节点已经出队了，则返回 true。由 TransferQueue#advanceHead 操作将 next 指向自身。
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }

        /** Head of queue */
        transient volatile QNode head; // 头节点
        /** Tail of queue */
        transient volatile QNode tail; // 尾节点
        /**
         * Reference to a cancelled node that might not yet have been
         * unlinked from queue because it was the last inserted node
         * when it was cancelled.
         */
        transient volatile QNode cleanMe;

        TransferQueue() {
            QNode h = new QNode(null, false); // initialize to dummy node. // 队列的头节点，是一个空节点
            head = h;
            tail = h;
        }

        /**
         * Tries to cas nh as new head; if successful, unlink
         * old head's next node to avoid garbage retention.
         */
        void advanceHead(QNode h, QNode nh) { // CAS将头节点（TransferQueue#head属性的值）从节点h改为节点nh，再将h出队
            if (h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next // 将节点h的next指针设为自身，表示h出队，方便垃圾回收
        }

        /**
         * Tries to cas nt as new tail.
         */
        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        /**
         * Tries to CAS cleanMe slot.
         */
        boolean casCleanMe(QNode cmp, QNode val) {
            return cleanMe == cmp &&
                UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * Puts or takes an item.
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            /* Basic algorithm is to loop trying to take either of
             * two actions:
             *
             * 1. If queue apparently empty or holding same-mode nodes, // 如果队列为空，或者是相同模式的节点，则入队（尾部）并等待匹配
             *    try to add node to queue of waiters, wait to be
             *    fulfilled (or cancelled) and return matching item.
             *
             * 2. If queue apparently contains waiting items, and this  // 如果队列中有等待中的节点，且当前请求为互补模式，则匹配并出队（头部）
             *    call is of complementary mode, try to fulfill by CAS'ing
             *    item field of waiting node and dequeuing it, and then
             *    returning matching item.
             *
             * In each case, along the way, check for and try to help
             * advance head and tail on behalf of other stalled/slow
             * threads.
             *
             * The loop starts off with a null check guarding against
             * seeing uninitialized head or tail values. This never
             * happens in current SynchronousQueue, but could if
             * callers held non-volatile/final ref to the
             * transferer. The check is here anyway because it places
             * null checks at top of loop, which is usually faster
             * than having them implicitly interspersed.
             */

            QNode s = null; // constructed/reused as needed
            boolean isData = (e != null);

            for (;;) {
                QNode t = tail;
                QNode h = head;
                if (t == null || h == null)         // saw uninitialized value
                    continue;                       // spin

                if (h == t || t.isData == isData) { // empty or same-mode // 队列为空，或者队尾节点模式与当前节点模式相同，只能入队
                    QNode tn = t.next;
                    if (t != tail)                  // inconsistent read // 尾节点已过期，需重新获取尾节点
                        continue;
                    if (tn != null) {               // lagging tail // 尾节点已过期，需重新获取尾节点
                        advanceTail(t, tn);
                        continue;
                    }
                    if (timed && nanos <= 0)        // can't wait // 已超时
                        return null;
                    if (s == null)
                        s = new QNode(e, isData);   // 当前节点设为s
                    if (!t.casNext(null, s))   // failed to link in // 把当前节点s插入节点t之后，若失败则重试
                        continue;

                    advanceTail(t, s);              // swing tail and wait // 将节点s设为新的尾节点
                    Object x = awaitFulfill(s, e, timed, nanos); // 节点s等待匹配
                    if (x == s) {                   // wait was cancelled // 说明当前节点s已取消，需要出队
                        clean(t, s);
                        return null;
                    }

                    if (!s.isOffList()) {           // not already unlinked // 执行到这里，说明节点s匹配成功。如果s尚未出队，则进入以下逻辑
                        advanceHead(t, s);          // unlink if head // 已知t是s的上一个节点，这里判断如果t是头节点，则把t出队，把节点s作为新的头节点（后续操作进一步把s设为空节点，即dummy node）
                        if (x != null)              // and forget fields
                            s.item = s;             // 把节点s的数据域设为自身，表示已取消，不再匹配
                        s.waiter = null;
                    }
                    return (x != null) ? (E)x : e;  // REQUEST类型，返回匹配到的数据x，否则返回e

                } else {                            // complementary-mode // 互补
                    QNode m = h.next;               // node to fulfill // 首个非空节点，将它与请求节点匹配
                    if (t != tail || m == null || h != head)
                        continue;                   // inconsistent read // 每次自旋都严格保证获取最新的head/tail节点

                    Object x = m.item;
                    if (isData == (x != null) ||    // m already fulfilled // 并不是互补的
                        x == m ||                   // m cancelled
                        !m.casItem(x, e)) {         // lost CAS // 节点m的数据元素为x，则将其替换为e。若CAS失败说明其数据元素不为x，即节点m已经被匹配了。若CAS成功则说明匹配成功。
                        advanceHead(h, m);          // dequeue and retry // 把头节点出队，把节点m作为新的头节点（dummy node），继续重试
                        continue;
                    }

                    advanceHead(h, m);              // successfully fulfilled // 匹配成功，把节点m作为新的头节点（dummy node）
                    LockSupport.unpark(m.waiter);   // 唤醒m上的线程
                    return (x != null) ? (E)x : e;
                }
            }
        }

        /**
         * Spins/blocks until node s is fulfilled.
         *
         * @param s the waiting node
         * @param e the comparison value for checking match
         * @param timed true if timed wait
         * @param nanos timeout value
         * @return matched item, or s if cancelled
         */
        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {    // 节点s等待匹配，其数据元素为e
            /* Same idea as TransferStack.awaitFulfill */
            final long deadline = timed ? System.nanoTime() + nanos : 0L; // 到期时间
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0); // 自旋次数（自旋结束后再判断是否需要阻塞）
            for (;;) {
                if (w.isInterrupted()) // 当前线程中断了，尝试取消节点s
                    s.tryCancel(e); // 取消操作，设置 s.item = s
                Object x = s.item; // 检查节点s是否匹配到了数据x，注意这里是跟栈结构不一样的地方！
                if (x != e)
                    return x;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) { // 已超时，尝试取消节点s
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        /**
         * Gets rid of cancelled node s with original predecessor pred.
         */
        void clean(QNode pred, QNode s) {
            s.waiter = null; // forget thread
            /*
             * At any given time, exactly one node on list cannot be
             * deleted -- the last inserted node. To accommodate this,   // 无论什么情况下，只有一个节点无法删除 -- 最后入队的节点
             * if we cannot delete s, we save its predecessor as         // 为了应对这种情况，如果无法删除节点s，则把它的上一个节点设为cleanMe
             * "cleanMe", deleting the previously saved version
             * first. At least one of node s or the node previously
             * saved can always be deleted, so this always terminates.
             */
            while (pred.next == s) { // Return early if already unlinked // 节点pred的下一个节点是s
                QNode h = head;
                QNode hn = h.next;   // Absorb cancelled first node as head // 如果head.next不是有效节点，则进行清理（无需清理head节点，因为head是dummy node，无需清理）
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // Ensure consistent read for tail // 确保获取到最新的tail
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        // If not tail, try to unsplice // 如果节点s不是尾节点，把s出队
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;  // 进入这里，说明节点s是尾节点，出队失败。需要获取cleanMe节点
                if (dp != null) {    // Try unlinking previous cancelled node // cleanMe节点不为空，需要把cleanMe节点的下一个节点清理掉
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d is gone or
                        d == dp ||                 // d is off list or
                        !d.isCancelled() ||        // d not cancelled or
                        (d != t &&                 // d not tail and
                         (dn = d.next) != null &&  //   has successor
                         dn != d &&                //   that is on list
                         dp.casNext(d, dn)))       // d unspliced
                        casCleanMe(dp, null);  // 节点cleanMe为dp，尝试把它设为null
                    if (dp == pred)
                        return;      // s is already saved node
                } else if (casCleanMe(null, pred)) // 节点cleanMe为空，尝试把节点pred设为cleanMe
                    return;          // Postpone cleaning s
            }
        }

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * The transferer. Set only in constructor, but cannot be declared
     * as final without further complicating serialization.  Since
     * this is accessed only at most once per public method, there
     * isn't a noticeable performance penalty for using volatile
     * instead of final here.
     */
    private transient volatile Transferer<E> transferer; // 传输器，即两个线程交换元素使用的东西。提供两种实现方式：队列、栈

    /**
     * Creates a {@code SynchronousQueue} with nonfair access policy.
     */
    public SynchronousQueue() {
        this(false);
    }

    /**
     * Creates a {@code SynchronousQueue} with the specified fairness policy.
     *
     * @param fair if true, waiting threads contend in FIFO order for
     *        access; otherwise the order is unspecified.
     */
    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>(); // 如果是公平模式就使用队列，如果是非公平模式就使用栈
    }

    /**
     * Adds the specified element to this queue, waiting if necessary for
     * another thread to receive it.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, false, 0) == null) { // 将元素传递给消费者，不超时。若传递失败返回空。
            Thread.interrupted(); // 检测当前线程是否已经中断，如果当前线程已经中断，则返回 true；否则返回 false。线程的中断状态由该方法清除。
            throw new InterruptedException();
        }
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * up to the specified wait time for another thread to receive it.
     *
     * @return {@code true} if successful, or {@code false} if the
     *         specified waiting time elapses before a consumer appears
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    /**
     * Inserts the specified element into this queue, if another thread is
     * waiting to receive it.
     *
     * @param e the element to add
     * @return {@code true} if the element was added to this queue, else
     *         {@code false}
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * for another thread to insert it.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        E e = transferer.transfer(null, false, 0); // 向生产者请求数据元素，不超时
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, waiting
     * if necessary up to the specified wait time, for another thread
     * to insert it.
     *
     * @return the head of this queue, or {@code null} if the
     *         specified waiting time elapses before an element is present
     * @throws InterruptedException {@inheritDoc}
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    /**
     * Retrieves and removes the head of this queue, if another thread
     * is currently making an element available.
     *
     * @return the head of this queue, or {@code null} if no
     *         element is available
     */
    public E poll() {
        return transferer.transfer(null, true, 0);
    }

    /**
     * Always returns {@code true}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return {@code true}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int size() {
        return 0; // 注意！！
    }

    /**
     * Always returns zero.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @return zero
     */
    public int remainingCapacity() {
        return 0;
    }

    /**
     * Does nothing.
     * A {@code SynchronousQueue} has no internal capacity.
     */
    public void clear() {
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element
     * @return {@code false}
     */
    public boolean contains(Object o) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param o the element to remove
     * @return {@code false}
     */
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Returns {@code false} unless the given collection is empty.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false} unless given collection is empty
     */
    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code false}.
     * A {@code SynchronousQueue} has no internal capacity.
     *
     * @param c the collection
     * @return {@code false}
     */
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Always returns {@code null}.
     * A {@code SynchronousQueue} does not return elements
     * unless actively waited on.
     *
     * @return {@code null}
     */
    public E peek() {
        return null;
    }

    /**
     * Returns an empty iterator in which {@code hasNext} always returns
     * {@code false}.
     *
     * @return an empty iterator
     */
    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Returns an empty spliterator in which calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return an empty spliterator
     * @since 1.8
     */
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    /**
     * Returns a zero-length array.
     * @return a zero-length array
     */
    public Object[] toArray() {
        return new Object[0];
    }

    /**
     * Sets the zeroeth element of the specified array to {@code null}
     * (if the array has non-zero length) and returns it.
     *
     * @param a the array
     * @return the specified array
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /*
     * To cope with serialization strategy in the 1.5 version of
     * SynchronousQueue, we declare some unused classes and fields
     * that exist solely to enable serializability across versions.
     * These fields are never used, so are initialized only if this
     * object is ever serialized or deserialized.
     */

    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    /**
     * Saves this queue to a stream (that is, serializes it).
     * @param s the stream
     * @throws java.io.IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    /**
     * Reconstitutes this queue from a stream (that is, deserializes it).
     * @param s the stream
     * @throws ClassNotFoundException if the class of a serialized object
     *         could not be found
     * @throws java.io.IOException if an I/O error occurs
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue<E>();
        else
            transferer = new TransferStack<E>();
    }

    // Unsafe mechanics
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

}
