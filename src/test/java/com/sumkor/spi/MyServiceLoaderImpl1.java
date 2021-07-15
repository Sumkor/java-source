package com.sumkor.spi;

/**
 * @author Sumkor
 * @since 2020/8/30
 */
public class MyServiceLoaderImpl1 implements IMyServiceLoader {

    @Override
    public String sayHello() {
        return "hello1";
    }

    @Override
    public String getName() {
        return "name1";
    }
}
