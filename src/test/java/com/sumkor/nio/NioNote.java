package com.sumkor.nio;

/**
 * @author Sumkor
 * @since 2021/5/24
 */
public class NioNote {

    /**
     * Java NIO 的前生今世 之一 简介
     * https://segmentfault.com/a/1190000006824091?utm_source=sf-similar-article
     *
     *
     * IO 和 NIO 的区别主要体现在三个方面：
     *
     * 1. IO 基于流(Stream oriented), 而 NIO 基于 Buffer (Buffer oriented)
     * 2. IO 操作是阻塞的, 而 NIO 操作是非阻塞的
     * 3. IO 没有 selector 概念, 而 NIO 有 selector 概念
     *
     *
     * 基于 Stream 与基于 Buffer
     *
     * 传统的 IO 是面向字节流或字符流的, 而在 NIO 中, 我们抛弃了传统的 IO 流, 而是引入了 Channel 和 Buffer 的概念.
     * 在 NIO 中, 我只能从 Channel 中读取数据到 Buffer 中或将数据从 Buffer 中写入到 Channel.
     * 那么什么是 基于流 呢? 在一般的 Java IO 操作中, 我们以流式的方式顺序地从一个 Stream 中读取一个或多个字节, 因此我们也就不能随意改变读取指针的位置.
     * 而 基于 Buffer 就显得有点不同了. 我们首先需要从 Channel 中读取数据到 Buffer 中, 当 Buffer 中有数据后, 我们就可以对这些数据进行操作了.
     * 不像 IO 那样是顺序操作, NIO 中我们可以随意地读取任意位置的数据.
     *
     *
     * 阻塞和非阻塞
     *
     * Java 提供的各种 Stream 操作都是阻塞的,
     * 例如我们调用一个 read 方法读取一个文件的内容, 那么调用 read 的线程会被阻塞住, 直到 read 操作完成.
     *
     * 而 NIO 的非阻塞模式允许我们非阻塞地进行 IO 操作.
     * 例如我们需要从网络中读取数据, 在 NIO 的非阻塞模式中, 当我们调用 read 方法时, 如果此时有数据, 则 read 读取并返回; 如果此时没有数据, 则 read 直接返回, 而不会阻塞当前线程.
     *
     *
     * selector
     *
     * selector 是 NIO 中才有的概念, 它是 Java NIO 之所以可以非阻塞地进行 IO 操作的关键.
     * 通过 Selector, 一个线程可以监听多个 Channel 的 IO 事件, 当我们向一个 Selector 中注册了 Channel 后,
     * Selector 内部的机制就可以自动地为我们不断地查询(select) 这些注册的 Channel 是否有已就绪的 IO 事件(例如可读, 可写, 网络连接完成等).
     * 通过这样的 Selector 机制, 我们就可以很简单地使用一个线程高效地管理多个 Channel 了.
     *
     */

    /**
     * Java NIO 系列教程
     * https://segmentfault.com/a/1190000000378800?utm_source=sf-similar-article
     * https://blog.csdn.net/qq_21125183/category_8964358.html
     * https://www.cnblogs.com/Jack-Blog/p/11991240.html
     */
}
