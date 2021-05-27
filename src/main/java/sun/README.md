# 下载

rt.jar 文件中 sun 包下的源码，需要从 OpenJDK 上自行下载。

本项目使用的是 jdk1.8.0_91，但是从 OpenJDK 上找到 jdk8u91 相关的 tag 有多个。

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/

这里根据日期选择 tag 为 jdk8u91-b14 版本。

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/rev/29380f4d81bd

下载 zip 文件。

# 编译

解压 zip 文件，其中 \src\share\classes 目录下的代码即为 rt.jar 包的源码。

将 sun 目录下的代码拷贝到本工程，进行编译，标红的 AbstractPollSelectorImpl 和 PollSelectorProvider 类，直接删除即可。

# window

由于部分实现是平台相关的，如 NIO 等，因此在 window 平台下需要拷贝 \src\windows\classes 下的源码。

将 sun 目录下的代码拷贝到本工程，进行编译。