package com.sumkor.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SPI 全称 Service Provider Interface，是 Java 提供的一套用来被第三方实现或者扩展的 API，它可以用来启用框架扩展和替换组件。
 * https://www.jianshu.com/p/46b42f7f593c
 *
 * 按照 ServiceLoader 使用说明文档，应该分下面几个步骤来使用：
 * 1.创建一个接口文件
 * 2.在 resources 资源目录下创建 META-INF/services文件夹
 * 3.在 services 文件夹中创建文件，以接口全名命名
 * 4.创建接口实现类
 * https://www.jianshu.com/p/7601ba434ff4
 *
 * @author Sumkor
 * @since 2020/8/30
 */
public class MyServiceLoaderTest {

    public static void main(String[] argus) {
        ServiceLoader<IMyServiceLoader> serviceLoader = ServiceLoader.load(IMyServiceLoader.class);
        Iterator<IMyServiceLoader> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            IMyServiceLoader next = iterator.next();
            System.out.println(next.getName() + next.sayHello());
        }
        System.out.println();
        /**
         * 1. 指定类加载器
         * @see ServiceLoader#load(Class)
         * ClassLoader cl = Thread.currentThread().getContextClassLoader();
         * ClassLoader loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
         *
         * 这里为什么要使用线程上下文类加载器呢？
         * SPI 的接口（eg. java.sql.Driver）是 Java 核心库的一部分，是由启动类加载器（Bootstrap Classloader）来加载的；
         * SPI 的实现类（eg. com.mysql.cj.jdbc.Driver）是由系统类加载器（System ClassLoader）来加载的。
         * 依照双亲委派模型，BootstrapClassloader 无法委派 AppClassLoader 来加载类。
         * 而线程上下文类加载器破坏了“双亲委派模型”，可以在执行线程中抛弃双亲委派加载链模式，使程序可以逆向使用类加载器。
         *
         * 2. 获取 ServiceLoader 迭代器
         * 通过 {@link ServiceLoader#iterator()} 中定义的匿名内部类，将迭代操作转发给内部类 {@link ServiceLoader.LazyIterator}
         *
         * 3. 判断是否有下一个元素
         * @see ServiceLoader.LazyIterator#hasNext()
         * @see ServiceLoader.LazyIterator#hasNextService()
         *
         * 其中，读取文件所有内容至 list 集合，提前设置下一个元素至 nextName 属性。
         * 核心逻辑：
         * Class<S> service = IMyServiceLoader.class;
         * String fullName = "META-INF/services/com.sumkor.spi.IMyServiceLoader";
         * Enumeration<URL> configs = ClassLoader.getSystemResources(fullName);
         * Iterator<String> pending = parse(service, configs.nextElement());
         * String nextName = pending.next() = "com.sumkor.spi.MyServiceLoaderImpl1";
         *
         * 4. 取下一个元素
         * @see ServiceLoader.LazyIterator#next()
         * @see ServiceLoader.LazyIterator#nextService()
         *
         * 核心逻辑：
         * String cn = nextName;
         * Class<?> c = Class.forName(cn, false, loader);
         * service.isAssignableFrom(c)
         * c.newInstance()
         */

        // 核心逻辑，等价于
        try {
            Class<?> aClass = Class.forName("com.sumkor.spi.MyServiceLoaderImpl1",
                    false, Thread.currentThread().getContextClassLoader());
            IMyServiceLoader object = (IMyServiceLoader) aClass.newInstance();
            System.out.println(object.getName() + object.sayHello());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
