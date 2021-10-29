package com.sumkor.proxy.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGlib 动态代理（工具类）
 *
 * @author Sumkor
 * @since 2021/6/22
 */
public class CglibDynamicProxy implements MethodInterceptor {

    private Object target;

    /**
     * 传入原始对象（原始类），获取代理对象（代理类）
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Object target) {
        this.target = target;
        Enhancer enhancer = new Enhancer();
        // 设置父类，因为Cglib是针对指定的类生成一个子类，所以需要指定父类
        enhancer.setSuperclass(this.target.getClass());
        // 设置回调方法，这里相当于是对于代理类上所有方法的调用都会调用CallBack，而Callback则需要实行intercept()方法进行拦截
        enhancer.setCallback(this);
        // 创建代理对象
        return (T) enhancer.create();
    }

    /**
     * 对方法进行代理
     */
    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println(System.currentTimeMillis() + " 方法代理开始");
        Object result = proxy.invokeSuper(object, args);
        System.out.println(System.currentTimeMillis() + " 方法代理结束");
        return result;
    }

}
