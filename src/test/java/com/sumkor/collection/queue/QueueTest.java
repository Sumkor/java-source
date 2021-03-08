package com.sumkor.collection.queue;

import java.util.Queue;
import java.util.concurrent.TransferQueue;

/**
 * 队列接口笔记，来源 JDK 官方文档
 *
 * @author Sumkor
 * @since 2021/3/5
 */
public class QueueTest {

    /**
     * 大多数并发 Collection 实现（包括大多数 Queue）与常规的 java.util 约定也不同，因为它们的迭代器提供了弱一致的，而不是快速失败的遍历。
     * 弱一致的迭代器是线程安全的，但是在迭代时没有必要冻结 collection，所以它不一定反映自迭代器创建以来的所有更新。
     */

    /**
     * happen-before 先行发生原则
     * 《深入理解 Java 虚拟机》
     *
     * 先行发生是 Java 内存模型中定义的两项操作之间的偏序关系。
     * 比如说操作 A 先行发生于操作 B，其实就是说在发生操作 B 之前，操作 A 产生的影响能被操作 B 观察到，
     * “影响”包括修改了内存中共享变量的值、发送了消息、调用了方法等。
     *
     * 下面是 Java 内存模型下一些“天然的”先行发生关系，这些先行发生关系无须任何同步器协助就已经存在，可以在编码中直接使用。
     * 如果两个操作之间的关系不在此列，并且无法从下列规则推导出来，则它们就没有顺序性保障，虚拟机可以对它们随意地进行重排序。
     *
     * ·程序次序规则（Program Order Rule）：在一个线程内，按照控制流顺序，书写在前面的操作先行发生于书写在后面的操作。注意，这里说的是控制流顺序而不是程序代码顺序，因为要考虑分支、循 环等结构。
     * ·管程锁定规则（Monitor Lock Rule）：一个 unlock 操作先行发生于后面对同一个锁的 lock 操作。这里必须强调的是“同一个锁”，而“后面”是指时间上的先后。
     * ·volatile变量规则（Volatile Variable Rule）：对一个 volatile 变量的写操作先行发生于后面对这个变量的读操作，这里的“后面”同样是指时间上的先后。
     * ·线程启动规则（Thread Start Rule）：Thread对象的 start() 方法先行发生于此线程的每一个动作。
     * ·线程终止规则（Thread Termination Rule）：线程中的所有操作都先行发生于对此线程的终止检测，我们可以通过 Thread::join()方法是否结束、Thread::isAlive() 的返回值等手段检测线程是否已经终止执行。
     * ·线程中断规则（Thread Interruption Rule）：对线程 interrupt() 方法的调用先行发生于被中断线程的代码检测到中断事件的发生，可以通过 Thread::interrupted() 方法检测到是否有中断发生。
     * ·对象终结规则（Finalizer Rule）：一个对象的初始化完成（构造函数执行结束）先行发生于它的 finalize() 方法的开始。
     * ·传递性（Transitivity）：如果操作 A 先行发生于操作 B，操作 B 先行发生于操作 C，那就可以得出操作 A 先行发生于操作 C 的结论。
     */

    /**
     * @see java.util.Queue
     *
     * public interface Queue<E> extends Collection<E>
     * 继承 Collection 接口。
     * 除了基本的 Collection 操作外，队列提供插入、提取和检查操作。
     * 每个方法都存在两种形式：一种抛出异常（操作失败时），另一种返回一个特殊值（null 或 false，具体取决于操作）。
     * 插入操作的后一种形式是用于专门为有容量限制的 Queue 实现设计的；在大多数实现中，插入操作不会失败。
     *
     * 方法说明：
     *
     *      抛出异常    返回特殊值
     * 插入  add(e)     offer(e)
     * 移除  remove()   poll()
     * 检查  element()  peek()
     *
     * 插入：
     * boolean  add(E e)   //将指定的元素插入此队列（如果立即可行且不会违反容量限制），在成功时返回 true，如果当前没有可用的空间，则抛出 IllegalStateException。
     * boolean  offer(E e) //将指定的元素插入此队列（如果立即可行且不会违反容量限制），当使用有容量限制的队列时，此方法通常要优于 add(E)，后者可能无法插入元素，而只是抛出一个异常。
     * @see Queue#add(java.lang.Object)
     * @see Queue#offer(java.lang.Object)
     *
     * 获取，不移除：
     * E        element() //获取，但是不移除此队列的头。此方法与 peek 唯一的不同在于：此队列为空时将抛出一个异常。
     * E        peek()    //获取但不移除此队列的头；如果此队列为空，则返回 null。
     * @see Queue#element()
     * @see Queue#peek()
     *
     * 获取并移除：
     * E        remove() //获取并移除此队列的头。此方法与 poll 唯一的不同在于：此队列为空时将抛出一个异常。
     * E        poll()   //获取并移除此队列的头，如果此队列为空，则返回 null。
     * @see Queue#remove()
     * @see Queue#poll()
     *
     *
     * 队列排序方式：
     * FIFO（先进先出）: 插入元素是从尾部插，取出元素是从头部取。
     * 优先级：根据提供的比较器或元素的自然顺序对元素进行排序。
     * LIFO（后进先出）（栈 stack）: 插入元素是从头部插，取出元素是从头部取。
     *
     * Queue 实现通常不允许插入 null 元素，尽管某些实现（如 LinkedList）并不禁止插入 null。
     * 即使在允许 null 的实现中，也不应该将 null 插入到 Queue 中，因为 null 也用作 poll 方法的一个特殊返回值，表明队列不包含元素。
     *
     *
     * 子接口
     * @see java.util.Deque
     * @see java.util.concurrent.BlockingDeque
     * @see java.util.concurrent.BlockingQueue
     * @see java.util.concurrent.TransferQueue
     */

    /**
     * @see java.util.Deque
     *
     * public interface Deque<E> extends Queue<E>
     * 一个线性 collection，支持在两端插入和移除元素。名称 deque 是“double ended queue（双端队列）”的缩写，通常读为“deck”。
     *
     *          第一个元素（头部）                 最后一个元素（尾部）
     *       抛出异常        特殊值            抛出异常        特殊值
     * 插入  addFirst(e)     offerFirst(e)    addLast(e)     offerLast(e)
     * 移除  removeFirst()   pollFirst()      removeLast()   pollLast()
     * 检查  getFirst()      peekFirst()      getLast()      peekLast()
     *
     * 注意，在将双端队列用作队列或堆栈时，peek 方法同样正常工作；无论哪种情况下，都从双端队列的开头抽取元素。
     * 虽然 Deque 实现没有严格要求禁止插入 null 元素，但建议最好这样做。建议任何事实上允许 null 元素的 Deque 实现用户最好不 要利用插入 null 的功能。
     * 这是因为各种方法会将 null 用作特殊的返回值来指示双端队列为空。
     *
     *
     * 对比 Queue 队列（先进先出）：
     *
     * Queue 方法   等效 Deque 方法
     * add(e)       addLast(e)
     * offer(e)     offerLast(e)
     * remove()     removeFirst()
     * poll()       pollFirst()
     * element()    getFirst()
     * peek()       peekFirst()
     *
     *
     * 对比 Stack 栈（先进后出）：
     *
     * 堆栈方法   等效 Deque 方法
     * push(e)   addFirst(e)
     * pop()     removeFirst()
     * peek()    peekFirst()
     */

    /**
     * @see java.util.concurrent.BlockingQueue
     *
     * public interface BlockingQueue<E> extends Queue<E>
     * 支持两个附加操作的 Queue，这两个操作是：获取元素时等待队列变为非空，以及存储元素时等待空间变得可用。
     *
     * BlockingQueue 方法以四种形式出现，对于不能立即满足但可能在将来某一时刻可以满足的操作，这四种形式的处理方式不同：
     * 第一种是抛出一个异常，
     * 第二种是返回一个特殊值（null 或 false，具体取决于操作），
     * 第三种是在操作可以成功前，无限期地阻塞当前线程，
     * 第四种是在放弃前只在给定的最大时间限制内阻塞。
     *
     * 下表中总结了这些方法：
     *
     *      抛出异常    特殊值     阻塞      超时
     * 插入  add(e)     offer(e)  put(e)   offer(e, time, unit)
     * 移除  remove()   poll()    take()   poll(time, unit)
     * 检查  element()  peek()    不可用   不可用
     *
     * BlockingQueue 不接受 null 元素。
     * 试图 add、put 或 offer 一个 null 元素时，某些实现会抛出 NullPointerException。null 被用作指示 poll 操作失败的警戒值。
     *
     * BlockingQueue 可以是限定容量的。它在任意给定时间都可以有一个 remainingCapacity，超出此容量，便无法无阻塞地 put 附加元素。
     * 没有任何内部容量约束的 BlockingQueue 总是报告 Integer.MAX_VALUE 的剩余容量。
     *
     * BlockingQueue 实现主要用于生产者-使用者队列。
     * 注意，BlockingQueue 可以安全地与多个生产者和多个使用者一起使用。
     *
     * BlockingQueue 实现是【线程安全】的。所有排队方法都可以使用内部锁或其他形式的并发控制来自动达到它们的目的。
     *
     * BlockingQueue 实质上不支持使用任何一种“close”或“shutdown”操作来指示不再添加任何项。这种功能的需求和使用有依赖于实现的倾向。
     * 例如，一种常用的策略是：对于生产者，插入特殊的 end-of-stream 或 poison 对象，并根据使用者获取这些对象的时间来对它们进行解释。
     *
     * 内存一致性效果：
     * 当存在其他并发 collection 时，将对象放入 BlockingQueue 之前的线程中的操作 happen-before 随后通过另一线程从 BlockingQueue 中访问或移除该元素的操作。
     */

    /**
     * @see java.util.concurrent.BlockingDeque
     *
     * public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E>
     * 支持两个附加操作的 Queue，这两个操作是：获取元素时等待双端队列变为非空；存储元素时等待双端队列中的空间变得可用。
     *
     * BlockingDeque 方法有四种形式，使用不同的方式处理无法立即满足但在将来某一时刻可能满足的操作：
     * 第一种方式抛出异常；
     * 第二种返回一个特殊值（null 或 false，具体取决于操作）；
     * 第三种无限期阻塞当前线程，直至操作成功；
     * 第四种只阻塞给定的最大时间，然后放弃。
     *
     *
     * 像所有 BlockingQueue 一样，BlockingDeque 是线程安全的，但不允许 null 元素，并且可能有（也可能没有）容量限制。
     *
     * BlockingDeque 实现可以直接用作 FIFO BlockingQueue。
     * 继承自 BlockingQueue 接口的方法精确地等效于下表中描述的 BlockingDeque 方法：
     *
     *      BlockingQueue 方法   等效的 BlockingDeque 方法
     * 插入
     *      add(e)               addLast(e)
     *      offer(e)             offerLast(e)
     *      put(e)               putLast(e)
     *      offer(e, time, unit) offerLast(e, time, unit)
     * 移除
     *      remove()             removeFirst()
     *      poll()               pollFirst()
     *      take()               takeFirst()
     *      poll(time, unit)     pollFirst(time, unit)
     * 检查
     *      element()            getFirst()
     *      peek()               peekFirst()
     *
     *
     * 内存一致性效果：
     * 当存在其他并发 collection 时，将对象放入 BlockingDeque 之前的线程中的操作 happen-before 随后通过另一线程从 BlockingDeque 中访问或移除该元素的操作。
     */

    /**
     * @see java.util.concurrent.TransferQueue
     *
     * public interface TransferQueue<E> extends BlockingQueue<E>
     *
     * 在 BlockingQueue 中，生产者会等待消费者接收元素。例如：队列满了，生产者 put 操作放入元素，会阻塞直到消费者 take/poll 取出数据。
     *
     * 与之相比，TransferQueue 更适用于生产者-消费者场景。
     * 生产者可使用 transfer 操作放入元素，会阻塞直到消费者 take/poll 取出数据。
     * 生产者可使用 put 操作放入元素，不用等待消费者接收。
     *
     * tryTransfer 还提供了以下两种方法供使用
     * {@linkplain TransferQueue#tryTransfer(java.lang.Object) Non-blocking} 立即把元素传递给等待中的消费者，如果没有消费者在等待，则返回 false 且不把元素入队
     * {@linkplain TransferQueue#tryTransfer(java.lang.Object, long, java.util.concurrent.TimeUnit) time-out} 把元素传递给消费者，等待超时
     *
     * 像其他阻塞队列一样， TransferQueue可能是容量有限的。
     */

    /**
     * JDK 提供了 7 个阻塞队列。分别是：
     *
     * @see java.util.concurrent.ArrayBlockingQueue 一个由数组结构组成的有界（定长，不会扩容）阻塞队列。
     * @see java.util.concurrent.LinkedBlockingQueue 一个由链表结构组成的有界（定长，不会扩容，默认 Integer.MAX_VALUE）阻塞队列。
     * @see java.util.concurrent.LinkedBlockingDeque 一个由链表结构组成的有界（定长，不会扩容，默认 Integer.MAX_VALUE）双向阻塞队列。
     * @see java.util.concurrent.LinkedTransferQueue 一个由链表结构组成的无界阻塞 TransferQueue 队列。
     * @see java.util.concurrent.PriorityBlockingQueue 一个支持优先级排序的无界阻塞队列。
     * @see java.util.concurrent.DelayQueue 一个使用优先级队列 PriorityQueue 实现的无界阻塞队列，只有在延迟期满时才能从中提取元素。
     * @see java.util.concurrent.SynchronousQueue 一个不存储元素、没有内部容量的阻塞队列。
     *
     * 非阻塞队列：
     *
     * @see java.util.ArrayDeque
     * @see java.util.LinkedList
     * @see java.util.PriorityQueue
     * @see java.util.concurrent.ConcurrentLinkedQueue
     * @see java.util.concurrent.ConcurrentLinkedDeque
     */
}
