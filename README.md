# java-source

JDK源码阅读

# 下载 JDK 源码

rt.jar 文件中 sun 包下的源码，需要从 OpenJDK 上自行下载。

本项目使用的是 jdk1.8.0_91，但是从 OpenJDK 上找到 jdk8u91 相关的 tag 有多个。

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/tags

这里根据日期选择 tag 为 jdk8u91-b14 版本。

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/rev/29380f4d81bd

下载 zip 文件。

# rt.jar 源码

解压 zip 文件，其中 \src\share\classes 目录下的代码即为 rt.jar 包的源码。

将 sun 目录下的代码拷贝到本工程，进行编译，标红的 AbstractPollSelectorImpl 和 PollSelectorProvider 类，直接删除即可。

由于部分实现是平台相关的，如 NIO 等。
在 window 平台下，\src\windows\classes 目录是 JDK 源码，\src\windows\native 目录是 JNI 相关源码。

# 下载 Hotspot 源码

对于 JDK 源码中部分 native 方法，需要查看 JVM 源码了解其实现，需要从 OpenJDK 上自行下载。

http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/tags

同样选择 tag 为 jdk8u91-b14 版本。

http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/rev/e1ea97ad19af

