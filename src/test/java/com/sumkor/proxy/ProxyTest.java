package com.sumkor.proxy;

import com.sumkor.proxy.entity.Landlord;
import com.sumkor.proxy.entity.LandlordImpl;
import com.sumkor.proxy.proxy.CglibDynamicProxy;
import com.sumkor.proxy.proxy.JdkDynamicProxy;
import com.sumkor.proxy.proxy.LandlordProxy;
import org.junit.Test;

/**
 * 代理：房东把房子委托给了中介， 中介帮他带人看房子，然后收取中介费。
 *
 * @author Sumkor
 * @since 2021/6/22
 */
public class ProxyTest {

    /**
     * 静态代理
     * https://blog.csdn.net/weixin_37139197/article/details/82355735
     *
     * 优点
     *     原始对象可以专注于自己的业务逻辑控制；
     *     非业务逻辑相关的部分，可以通过代理类来处理；
     *     隐藏了真实的对象，对外只暴露代理对象。
     *     扩展性：由于实现了相同的接口，因此被代理对象的逻辑不管如何变化，代理对象都不需要更改。
     */
    @Test
    public void statical() {
        LandlordImpl landlord = new LandlordImpl();
        LandlordProxy landlordProxy = new LandlordProxy(landlord);
        boolean result = landlordProxy.rent();
        System.out.println("result = " + result);
    }

    /**
     * 动态代理：在程序运行时运用反射机制动态创建代理类
     *
     * Spring 的两种动态代理：Jdk 和 Cglib 的区别和实现
     * https://www.cnblogs.com/leifei/p/8263448.html
     *
     * java动态代理是利用反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用 InvokeHandler 来处理。
     * cglib动态代理是利用asm开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。
     *
     * 1、如果目标对象实现了接口，默认情况下会采用 JDK 的动态代理实现 AOP
     * 2、如果目标对象实现了接口，可以强制使用 CGLIB 实现 AOP
     * 3、如果目标对象没有实现了接口，必须采用 CGLIB 库，spring 会自动在 JDK 动态代理和 CGLIB 之间转换
     *
     *
     * JDK 动态代理和 CGLIB 字节码生成的区别？
     * （1）JDK 动态代理只能对实现了接口的类生成代理，而不能针对类
     * （2）CGLIB 是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法
     *  因为是继承，所以该类或方法最好不要声明成 final
     */

    /**
     * JDK动态代理创建机制 --- 通过接口
     * JDK提供了 sun.misc.ProxyGenerator.generateProxyClass(String proxyName,class[] interfaces) 底层方法来产生动态代理类的字节码.
     *
     * 生成的动态代理类有以下特点:
     * 继承自 java.lang.reflect.Proxy，实现原始类上的所有接口。
     * 类中的所有方法都是 final 的；
     * 所有的方法功能的实现都统一调用了 InvocationHandler#invoke() 方法。
     */
    @Test
    public void jdk() {
        Landlord landlord = new LandlordImpl();
        JdkDynamicProxy dynamicProxy = new JdkDynamicProxy();
        Landlord landlordProxy = dynamicProxy.getProxy(landlord);
        landlordProxy.rent();
    }

    /**
     * cglib (Code Generation Library) 生成动态代理类的机制 --- 通过类继承
     *
     * cglib 动态代理类的模式是：
     * 查找原始类上的所有非 final 的 public 类型的方法定义；
     * 将这些方法的定义转换成字节码；
     * 将组成的字节码转换成相应的代理的 class 对象；
     * 实现 MethodInterceptor 接口，用来处理对代理类上所有方法的请求（这个接口和 JDK 动态代理 InvocationHandler 的功能和角色是一样的）
     *
     * （需要导入两个jar包，asm-5.2.jar,cglib-3.2.5.jar。版本自行选择）
     */
    @Test
    public void cglib() {
        Landlord landlord = new LandlordImpl();
        CglibDynamicProxy dynamicProxy = new CglibDynamicProxy();
        Landlord landlordProxy = dynamicProxy.getInstance(landlord);
        landlordProxy.rent();
    }
}
