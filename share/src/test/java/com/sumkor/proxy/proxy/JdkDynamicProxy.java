package com.sumkor.proxy.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK 动态代理（工具类）
 *
 * @author Sumkor
 * @since 2021/6/22
 */
public class JdkDynamicProxy implements InvocationHandler {

    private Object target;

    /**
     * 传入原始对象（原始类），获取代理对象（代理类）
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Object target) {
        this.target = target;
        // JDK动态代理只能针对实现了接口的类进行代理，newProxyInstance 函数所需参数就可看出
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                this // 将当前类实例传递给 Proxy 的 InvocationHandler 属性。后续调用代理类的方法时，实际调用当前类的invoke方法。
        );
    }

    /**
     * 对方法进行代理
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(System.currentTimeMillis() + " 方法代理开始");
        Object result = method.invoke(target, args);
        System.out.println(System.currentTimeMillis() + " 方法代理结束");
        return result;
    }

}
