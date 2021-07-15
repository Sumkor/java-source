package com.sumkor.proxy;

import com.sumkor.proxy.entity.Landlord;
import com.sumkor.proxy.entity.LandlordImpl;
import com.sumkor.proxy.proxy.CglibDynamicProxy;
import com.sumkor.proxy.proxy.JdkDynamicProxy;
import com.sumkor.proxy.proxy.LandlordProxy;
import net.sf.cglib.proxy.Enhancer;
import org.junit.Test;

import java.lang.reflect.Proxy;

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
     *
     *
     * JDK动态代理创建机制 --- 通过接口
     *
     * JDK提供了 sun.misc.ProxyGenerator.generateProxyClass(String proxyName,class[] interfaces) 底层方法来产生动态代理类的字节码.
     * 生成的动态代理类有以下特点:
     * 继承自 java.lang.reflect.Proxy，实现原始类上的所有接口。
     * 类中的所有方法都是 final 的；
     * 所有的方法功能的实现都统一调用了 InvocationHandler#invoke() 方法。
     *
     *
     * cglib (Code Generation Library) 生成动态代理类的机制 --- 通过类继承
     *
     * cglib 动态代理类的模式是：
     * 查找原始类上的所有非 final 的 public 类型的方法定义；
     * 将这些方法的定义转换成字节码；
     * 将组成的字节码转换成相应的代理的 class 对象；
     * 实现 MethodInterceptor 接口，用来处理对代理类上所有方法的请求（这个接口和 JDK 动态代理 InvocationHandler 的功能和角色是一样的）
     * （需要导入两个jar包，asm-5.2.jar,cglib-3.2.5.jar。版本自行选择）
     */

    /**
     * JDK动态代理创建机制 --- 通过接口
     */
    @Test
    public void jdk() {
        Landlord landlord = new LandlordImpl();
        JdkDynamicProxy dynamicProxy = new JdkDynamicProxy();
        Landlord landlordProxy = dynamicProxy.getProxy(landlord);
        landlordProxy.rent();
        /**
         * 代理类：
         * $Proxy4 extends Proxy implements Landlord
         *
         * 由 {@link Proxy#newProxyInstance(java.lang.ClassLoader, java.lang.Class[], java.lang.reflect.InvocationHandler)} 生成代理类实例
         * 其中 {@link Proxy.ProxyClassFactory#apply(java.lang.ClassLoader, java.lang.Class[])} 生成代理类的代码
         */
    }

    /**
     * cglib (Code Generation Library) 生成动态代理类的机制 --- 通过类继承
     */
    @Test
    public void cglib() {
        Landlord landlord = new LandlordImpl();
        CglibDynamicProxy dynamicProxy = new CglibDynamicProxy();
        Landlord landlordProxy = dynamicProxy.getInstance(landlord);
        landlordProxy.rent();
        /**
         * 代理类：
         * LandlordImpl$$EnhancerByCGLIB$$721fad33 extends LandlordImpl implements Factory
         *
         * 由 {@link Enhancer#create()} 生成代理类实例
         */
    }

    /**
     * 使用 JDK 动态代理，得到 Landlord 接口的代理类为 com.sun.proxy.$Proxy4 类的实例
     *
     * [arthas@21236]$ jad com.sun.proxy.$Proxy4
     *
     * ClassLoader:
     * +-sun.misc.Launcher$AppClassLoader@18b4aac2
     *   +-sun.misc.Launcher$ExtClassLoader@23223dd8
     *
     * Location:
     *
     *
     * package com.sun.proxy;
     *
     * import com.sumkor.proxy.entity.Landlord;
     * import java.lang.reflect.InvocationHandler;
     * import java.lang.reflect.Method;
     * import java.lang.reflect.Proxy;
     * import java.lang.reflect.UndeclaredThrowableException;
     *
     * public final class $Proxy4
     * extends Proxy
     * implements Landlord {
     *     private static Method m1;
     *     private static Method m3;
     *     private static Method m2;
     *     private static Method m0;
     *
     *     public $Proxy4(InvocationHandler invocationHandler) {
     *         super(invocationHandler);
     *     }
     *
     *     public final boolean equals(Object object) {
     *         try {
     *             return (Boolean)this.h.invoke(this, m1, new Object[]{object});
     *         }
     *         catch (Error | RuntimeException throwable) {
     *             throw throwable;
     *         }
     *         catch (Throwable throwable) {
     *             throw new UndeclaredThrowableException(throwable);
     *         }
     *     }
     *
     *     public final boolean rent() {
     *         try {
     *             return (Boolean)this.h.invoke(this, m3, null);
     *         }
     *         catch (Error | RuntimeException throwable) {
     *             throw throwable;
     *         }
     *         catch (Throwable throwable) {
     *             throw new UndeclaredThrowableException(throwable);
     *         }
     *     }
     *
     *     public final String toString() {
     *         try {
     *             return (String)this.h.invoke(this, m2, null);
     *         }
     *         catch (Error | RuntimeException throwable) {
     *             throw throwable;
     *         }
     *         catch (Throwable throwable) {
     *             throw new UndeclaredThrowableException(throwable);
     *         }
     *     }
     *
     *     public final int hashCode() {
     *         try {
     *             return (Integer)this.h.invoke(this, m0, null);
     *         }
     *         catch (Error | RuntimeException throwable) {
     *             throw throwable;
     *         }
     *         catch (Throwable throwable) {
     *             throw new UndeclaredThrowableException(throwable);
     *         }
     *     }
     *
     *     static {
     *         try {
     *             m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
     *             m3 = Class.forName("com.sumkor.proxy.entity.Landlord").getMethod("rent", new Class[0]);
     *             m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
     *             m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
     *             return;
     *         }
     *         catch (NoSuchMethodException noSuchMethodException) {
     *             throw new NoSuchMethodError(noSuchMethodException.getMessage());
     *         }
     *         catch (ClassNotFoundException classNotFoundException) {
     *             throw new NoClassDefFoundError(classNotFoundException.getMessage());
     *         }
     *     }
     * }
     */

    /**
     * 使用 CGLIB 动态代理，得到 LandlordImpl 类的代理类为 LandlordImpl$$EnhancerByCGLIB$$721fad33 类的实例
     *
     * [arthas@21236]$ jad LandlordImpl$$EnhancerByCGLIB$$721fad33 > 1.txt
     *
     * ClassLoader:
     * +-sun.misc.Launcher$AppClassLoader@18b4aac2
     *   +-sun.misc.Launcher$ExtClassLoader@5674cd4d
     *
     * Location:
     * /D:/work/github/java-source/target/test-classes/
     *
     * package com.sumkor.proxy.entity;
     *
     * import com.sumkor.proxy.entity.LandlordImpl;
     * import java.lang.reflect.Method;
     * import net.sf.cglib.core.ReflectUtils;
     * import net.sf.cglib.core.Signature;
     * import net.sf.cglib.proxy.Callback;
     * import net.sf.cglib.proxy.Factory;
     * import net.sf.cglib.proxy.MethodInterceptor;
     * import net.sf.cglib.proxy.MethodProxy;
     *
     * public class LandlordImpl$$EnhancerByCGLIB$$721fad33
     * extends LandlordImpl
     * implements Factory {
     *     private boolean CGLIB$BOUND;
     *     public static Object CGLIB$FACTORY_DATA;
     *     private static final ThreadLocal CGLIB$THREAD_CALLBACKS;
     *     private static final Callback[] CGLIB$STATIC_CALLBACKS;
     *     private MethodInterceptor CGLIB$CALLBACK_0;
     *     private static Object CGLIB$CALLBACK_FILTER;
     *     private static final Method CGLIB$rent$0$Method;
     *     private static final MethodProxy CGLIB$rent$0$Proxy;
     *     private static final Object[] CGLIB$emptyArgs;
     *     private static final Method CGLIB$equals$1$Method;
     *     private static final MethodProxy CGLIB$equals$1$Proxy;
     *     private static final Method CGLIB$toString$2$Method;
     *     private static final MethodProxy CGLIB$toString$2$Proxy;
     *     private static final Method CGLIB$hashCode$3$Method;
     *     private static final MethodProxy CGLIB$hashCode$3$Proxy;
     *     private static final Method CGLIB$clone$4$Method;
     *     private static final MethodProxy CGLIB$clone$4$Proxy;
     *
     *     public LandlordImpl$$EnhancerByCGLIB$$721fad33() {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = this;
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(landlordImpl$$EnhancerByCGLIB$$721fad33);
     *     }
     *
     *     static {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$STATICHOOK1();
     *     }
     *
     *     public final boolean equals(Object object) {
     *         MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
     *         if (methodInterceptor == null) {
     *             LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *             methodInterceptor = this.CGLIB$CALLBACK_0;
     *         }
     *         if (methodInterceptor != null) {
     *             Object object2 = methodInterceptor.intercept(this, CGLIB$equals$1$Method, new Object[]{object}, CGLIB$equals$1$Proxy);
     *             return object2 == null ? false : (Boolean)object2;
     *         }
     *         return super.equals(object);
     *     }
     *
     *     public final String toString() {
     *         MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
     *         if (methodInterceptor == null) {
     *             LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *             methodInterceptor = this.CGLIB$CALLBACK_0;
     *         }
     *         if (methodInterceptor != null) {
     *             return (String)methodInterceptor.intercept(this, CGLIB$toString$2$Method, CGLIB$emptyArgs, CGLIB$toString$2$Proxy);
     *         }
     *         return super.toString();
     *     }
     *
     *     public final int hashCode() {
     *         MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
     *         if (methodInterceptor == null) {
     *             LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *             methodInterceptor = this.CGLIB$CALLBACK_0;
     *         }
     *         if (methodInterceptor != null) {
     *             Object object = methodInterceptor.intercept(this, CGLIB$hashCode$3$Method, CGLIB$emptyArgs, CGLIB$hashCode$3$Proxy);
     *             return object == null ? 0 : ((Number)object).intValue();
     *         }
     *         return super.hashCode();
     *     }
     *
     *     protected final Object clone() throws CloneNotSupportedException {
     *         MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
     *         if (methodInterceptor == null) {
     *             LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *             methodInterceptor = this.CGLIB$CALLBACK_0;
     *         }
     *         if (methodInterceptor != null) {
     *             return methodInterceptor.intercept(this, CGLIB$clone$4$Method, CGLIB$emptyArgs, CGLIB$clone$4$Proxy);
     *         }
     *         return super.clone();
     *     }
     *
     *     public Object newInstance(Callback[] arrcallback) {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(arrcallback);
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = new LandlordImpl$$EnhancerByCGLIB$$721fad33();
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(null);
     *         return landlordImpl$$EnhancerByCGLIB$$721fad33;
     *     }
     *
     *     public Object newInstance(Class[] arrclass, Object[] arrobject, Callback[] arrcallback) {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33;
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(arrcallback);
     *         Class[] arrclass2 = arrclass;
     *         switch (arrclass.length) {
     *             case 0: {
     *                 landlordImpl$$EnhancerByCGLIB$$721fad33 = new LandlordImpl$$EnhancerByCGLIB$$721fad33();
     *                 break;
     *             }
     *             default: {
     *                 throw new IllegalArgumentException("Constructor not found");
     *             }
     *         }
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(null);
     *         return landlordImpl$$EnhancerByCGLIB$$721fad33;
     *     }
     *
     *     public Object newInstance(Callback callback) {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(new Callback[]{callback});
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = new LandlordImpl$$EnhancerByCGLIB$$721fad33();
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$SET_THREAD_CALLBACKS(null);
     *         return landlordImpl$$EnhancerByCGLIB$$721fad33;
     *     }
     *
     *     public final boolean rent() {
     *         MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
     *         if (methodInterceptor == null) {
     *             LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *             methodInterceptor = this.CGLIB$CALLBACK_0;
     *         }
     *         if (methodInterceptor != null) {
     *             Object object = methodInterceptor.intercept(this, CGLIB$rent$0$Method, CGLIB$emptyArgs, CGLIB$rent$0$Proxy);
     *             return object == null ? false : (Boolean)object;
     *         }
     *         return super.rent();
     *     }
     *
     *     public void setCallback(int n, Callback callback) {
     *         switch (n) {
     *             case 0: {
     *                 this.CGLIB$CALLBACK_0 = (MethodInterceptor)callback;
     *                 break;
     *             }
     *         }
     *     }
     *
     *     public void setCallbacks(Callback[] arrcallback) {
     *         Callback[] arrcallback2 = arrcallback;
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = this;
     *         this.CGLIB$CALLBACK_0 = (MethodInterceptor)arrcallback[0];
     *     }
     *
     *     public static void CGLIB$SET_STATIC_CALLBACKS(Callback[] arrcallback) {
     *         CGLIB$STATIC_CALLBACKS = arrcallback;
     *     }
     *
     *     public static void CGLIB$SET_THREAD_CALLBACKS(Callback[] arrcallback) {
     *         CGLIB$THREAD_CALLBACKS.set(arrcallback);
     *     }
     *
     *     public Callback getCallback(int n) {
     *         MethodInterceptor methodInterceptor;
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *         switch (n) {
     *             case 0: {
     *                 methodInterceptor = this.CGLIB$CALLBACK_0;
     *                 break;
     *             }
     *             default: {
     *                 methodInterceptor = null;
     *             }
     *         }
     *         return methodInterceptor;
     *     }
     *
     *     public Callback[] getCallbacks() {
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BIND_CALLBACKS(this);
     *         LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = this;
     *         return new Callback[]{this.CGLIB$CALLBACK_0};
     *     }
     *
     *     public static MethodProxy CGLIB$findMethodProxy(Signature signature) {
     *         String string = ((Object)signature).toString();
     *         switch (string.hashCode()) {
     *             case -508378822: {
     *                 if (!string.equals("clone()Ljava/lang/Object;")) break;
     *                 return CGLIB$clone$4$Proxy;
     *             }
     *             case 1092831648: {
     *                 if (!string.equals("rent()Z")) break;
     *                 return CGLIB$rent$0$Proxy;
     *             }
     *             case 1826985398: {
     *                 if (!string.equals("equals(Ljava/lang/Object;)Z")) break;
     *                 return CGLIB$equals$1$Proxy;
     *             }
     *             case 1913648695: {
     *                 if (!string.equals("toString()Ljava/lang/String;")) break;
     *                 return CGLIB$toString$2$Proxy;
     *             }
     *             case 1984935277: {
     *                 if (!string.equals("hashCode()I")) break;
     *                 return CGLIB$hashCode$3$Proxy;
     *             }
     *         }
     *         return null;
     *     }
     *
     *     static void CGLIB$STATICHOOK1() {
     *         CGLIB$THREAD_CALLBACKS = new ThreadLocal();
     *         CGLIB$emptyArgs = new Object[0];
     *         Class<?> class_ = Class.forName("com.sumkor.proxy.entity.LandlordImpl$$EnhancerByCGLIB$$721fad33");
     *         Class<?> class_2 = Class.forName("java.lang.Object");
     *         Method[] arrmethod = ReflectUtils.findMethods(new String[]{"equals", "(Ljava/lang/Object;)Z", "toString", "()Ljava/lang/String;", "hashCode", "()I", "clone", "()Ljava/lang/Object;"}, class_2.getDeclaredMethods());
     *         CGLIB$equals$1$Method = arrmethod[0];
     *         CGLIB$equals$1$Proxy = MethodProxy.create(class_2, class_, "(Ljava/lang/Object;)Z", "equals", "CGLIB$equals$1");
     *         CGLIB$toString$2$Method = arrmethod[1];
     *         CGLIB$toString$2$Proxy = MethodProxy.create(class_2, class_, "()Ljava/lang/String;", "toString", "CGLIB$toString$2");
     *         CGLIB$hashCode$3$Method = arrmethod[2];
     *         CGLIB$hashCode$3$Proxy = MethodProxy.create(class_2, class_, "()I", "hashCode", "CGLIB$hashCode$3");
     *         CGLIB$clone$4$Method = arrmethod[3];
     *         CGLIB$clone$4$Proxy = MethodProxy.create(class_2, class_, "()Ljava/lang/Object;", "clone", "CGLIB$clone$4");
     *         class_2 = Class.forName("com.sumkor.proxy.entity.LandlordImpl");
     *         CGLIB$rent$0$Method = ReflectUtils.findMethods(new String[]{"rent", "()Z"}, class_2.getDeclaredMethods())[0];
     *         CGLIB$rent$0$Proxy = MethodProxy.create(class_2, class_, "()Z", "rent", "CGLIB$rent$0");
     *     }
     *
     *     private static final void CGLIB$BIND_CALLBACKS(Object object) {
     *         block2: {
     *             Object object2;
     *             block3: {
     *                 LandlordImpl$$EnhancerByCGLIB$$721fad33 landlordImpl$$EnhancerByCGLIB$$721fad33 = (LandlordImpl$$EnhancerByCGLIB$$721fad33)object;
     *                 if (landlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BOUND) break block2;
     *                 landlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$BOUND = true;
     *                 object2 = CGLIB$THREAD_CALLBACKS.get();
     *                 if (object2 != null) break block3;
     *                 object2 = CGLIB$STATIC_CALLBACKS;
     *                 if (CGLIB$STATIC_CALLBACKS == null) break block2;
     *             }
     *             landlordImpl$$EnhancerByCGLIB$$721fad33.CGLIB$CALLBACK_0 = (MethodInterceptor)((Callback[])object2)[0];
     *         }
     *     }
     *
     *     final Object CGLIB$clone$4() throws CloneNotSupportedException {
     *         return super.clone();
     *     }
     *
     *     final boolean CGLIB$rent$0() {
     *         return super.rent();
     *     }
     *
     *     final int CGLIB$hashCode$3() {
     *         return super.hashCode();
     *     }
     *
     *     final boolean CGLIB$equals$1(Object object) {
     *         return super.equals(object);
     *     }
     *
     *     final String CGLIB$toString$2() {
     *         return super.toString();
     *     }
     * }
     */
}
