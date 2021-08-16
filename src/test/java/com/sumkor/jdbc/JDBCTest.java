package com.sumkor.jdbc;

import org.junit.Test;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ServiceLoader;

/**
 * @author Sumkor
 * @since 2021/6/17
 */
public class JDBCTest {

    /**
     * 表结构：
     create table t_student
     (
     id      bigint auto_increment
     primary key,
     address varchar(255) null,
     age     int          not null,
     name    varchar(255) null
     );
     */

    /**
     * 无参查询
     */
    @Test
    public void query() throws Exception {
//        Class.forName("com.mysql.jdbc.Driver");
//        Class.forName("com.mysql.cj.jdbc.Driver");

        // 控制台打印
        DriverManager.setLogWriter(new PrintWriter(System.out));
        /**
         * 加载 DriverManager 类，执行静态方法块，会加载数据库驱动
         * @see java.sql.DriverManager#loadInitialDrivers()
         *
         * 读取配置文件 META-INF/services/java.sql.Driver
         * @see ServiceLoader.LazyIterator#hasNext()
         * @see ServiceLoader.LazyIterator#hasNextService()
         *
         * 反射实例化 Driver 驱动类
         * @see java.util.ServiceLoader.LazyIterator#next()
         * @see ServiceLoader.LazyIterator#nextService()
         *
         * 读取到 mysql-connector-java-8.0.23.jar!\META-INF\services\java.sql.Driver 文件中的内容 "com.mysql.cj.jdbc.Driver"，反射获取得到该类，并进行实例化
         * @see com.mysql.cj.jdbc.Driver
         *
         * 实例化 mysql 数据库驱动的时候，会将该驱动注册到 DriverManager，存储在 {@link DriverManager#registeredDrivers 中}
         * @see java.sql.DriverManager#registerDriver(java.sql.Driver)
         */

        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        /**
         * @see java.sql.DriverManager#getConnection(java.lang.String, java.util.Properties, java.lang.Class)
         *
         * 其中，会遍历已注册的数据库驱动，建立数据库连接
         * @see com.mysql.cj.jdbc.NonRegisteringDriver#connect(java.lang.String, java.util.Properties)
         *
         * 根据数据库地址和用户名密码，从缓存中获取 ConnectionUrl 对象，可以看到这里用到了 LRU 缓存 {@link com.mysql.cj.util.LRUCache}
         * @see com.mysql.cj.conf.ConnectionUrl#getConnectionUrlInstance(java.lang.String, java.util.Properties)
         *
         * 创建 ConnectionUrl 实例，这里根据 "jdbc:mysql:" 和 host 数量，得到实例为 {@link com.mysql.cj.conf.url.SingleConnectionUrl}
         * @see com.mysql.cj.conf.ConnectionUrl.Type#getConnectionUrlInstance(com.mysql.cj.conf.ConnectionUrlParser, java.util.Properties)
         *
         * ConnectionUrl 实例的父类是 {@link com.mysql.cj.conf.ConnectionUrl}，它拥有多个子类。更多的对应关系可以看 {@link com.mysql.cj.conf.ConnectionUrl.Type}
         *
         * 接着，根据得到的 ConnectionUrl 实例来进一步创建真正的数据库连接 Connection 对象
         * @see com.mysql.cj.jdbc.NonRegisteringDriver#connect(java.lang.String, java.util.Properties)
         * @see com.mysql.cj.jdbc.ConnectionImpl#getInstance(com.mysql.cj.conf.HostInfo)
         * @see com.mysql.cj.jdbc.ConnectionImpl#ConnectionImpl(com.mysql.cj.conf.HostInfo)
         *
         * 调用 ConnectionImpl 构造函数来创建对象，其中会建立 Socket 连接
         * @see com.mysql.cj.jdbc.ConnectionImpl#createNewIO(boolean)
         * @see com.mysql.cj.jdbc.ConnectionImpl#connectOneTryOnly(boolean)
         * @see com.mysql.cj.NativeSession#connect(com.mysql.cj.conf.HostInfo, java.lang.String, java.lang.String, java.lang.String, int, com.mysql.cj.TransactionEventHandler)
         * @see com.mysql.cj.protocol.a.NativeSocketConnection#connect(java.lang.String, int, com.mysql.cj.conf.PropertySet, com.mysql.cj.exceptions.ExceptionInterceptor, com.mysql.cj.log.Log, int)
         * @see com.mysql.cj.protocol.StandardSocketFactory#connect(java.lang.String, int, com.mysql.cj.conf.PropertySet, int)
         */

        Statement statement = conn.createStatement();
        /**
         * @see com.mysql.cj.jdbc.ConnectionImpl#createStatement()
         * @see com.mysql.cj.jdbc.StatementImpl#StatementImpl(com.mysql.cj.jdbc.JdbcConnection, java.lang.String)
         */

        statement.setQueryTimeout(60);
        ResultSet resultSet = statement.executeQuery("select * from t_student");
        /**
         * 注意，这里会加锁，使用 ConnectionImpl 对象锁
         * @see com.mysql.cj.jdbc.StatementImpl#executeQuery(java.lang.String)
         * @see com.mysql.cj.NativeSession#execSQL(com.mysql.cj.Query, java.lang.String, int, com.mysql.cj.protocol.a.NativePacketPayload, boolean, com.mysql.cj.protocol.ProtocolEntityFactory, com.mysql.cj.protocol.ColumnDefinition, boolean)
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendQueryString(com.mysql.cj.Query, java.lang.String, java.lang.String, int, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendQueryPacket(com.mysql.cj.Query, com.mysql.cj.protocol.a.NativePacketPayload, int, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         *
         * -----------------------------
         *
         * 真正执行 SQL，实际是向 mysql 服务器发送数据包
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendCommand(com.mysql.cj.protocol.Message, boolean, int)
         * @see com.mysql.cj.protocol.a.NativeProtocol#send(com.mysql.cj.protocol.Message, int)
         * @see com.mysql.cj.protocol.a.TimeTrackingPacketSender#send(byte[], int, byte)
         * @see com.mysql.cj.protocol.a.SimplePacketSender#send(byte[], int, byte)
         *
         * 这里对二进制数据包进行 toString，很有意思
         * @see com.mysql.cj.protocol.a.NativePacketPayload#toString()
         * @see com.mysql.cj.util.StringUtils#dumpAsHex(byte[], int)
         *
         * -----------------------------
         *
         * 发送完数据包后，开始读取响应。
         *
         * 回到
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendCommand(com.mysql.cj.protocol.Message, boolean, int)
         * 获取表的列数
         * @see com.mysql.cj.protocol.a.NativeProtocol#checkErrorMessage(int)
         * @see com.mysql.cj.protocol.a.NativeProtocol#checkErrorMessage(com.mysql.cj.protocol.a.NativePacketPayload)
         *
         *
         * -----------------------------
         *
         * 继续读取响应。
         *
         * 回到
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendQueryPacket(com.mysql.cj.Query, com.mysql.cj.protocol.a.NativePacketPayload, int, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         * 继续读取 SQL 执行结果
         * @see com.mysql.cj.protocol.a.NativeProtocol#readAllResults(int, boolean, com.mysql.cj.protocol.a.NativePacketPayload, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         * @see com.mysql.cj.protocol.a.NativeProtocol#read(java.lang.Class, int, boolean, com.mysql.cj.protocol.a.NativePacketPayload, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         *
         * 读取二进制数据，并构造返回结果
         * @see com.mysql.cj.protocol.a.TextResultsetReader#read(int, boolean, com.mysql.cj.protocol.a.NativePacketPayload, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         *
         * 1. 首先读取返回结果的字段信息，构造 {@link com.mysql.cj.result.Field} 对象
         * @see com.mysql.cj.protocol.a.NativeProtocol#read(java.lang.Class, com.mysql.cj.protocol.ProtocolEntityFactory)
         * @see com.mysql.cj.protocol.a.ColumnDefinitionReader#read(com.mysql.cj.protocol.ProtocolEntityFactory)
         *
         * 再封装到 {@link com.mysql.cj.result.DefaultColumnDefinition} 对象中
         * @see com.mysql.cj.protocol.a.ColumnDefinitionFactory#createFromFields(com.mysql.cj.result.Field[])
         *
         * 2. 再读取返回结果的字段值信息，封装为 {@link com.mysql.cj.protocol.ResultsetRow} 对象
         * @see com.mysql.cj.protocol.a.NativeProtocol#read(java.lang.Class, com.mysql.cj.protocol.ProtocolEntityFactory)
         * @see com.mysql.cj.protocol.a.ResultsetRowReader#read(com.mysql.cj.protocol.ProtocolEntityFactory)
         *
         * 再封装到 {@link com.mysql.cj.jdbc.result.ResultSetImpl} 对象中
         * @see com.mysql.cj.protocol.a.TextRowFactory#createFromMessage(com.mysql.cj.protocol.a.NativePacketPayload)
         *
         * 3. 最后构造对象 ResultsetRow -> ResultsetRows -> Resultset
         * @see com.mysql.cj.jdbc.result.ResultSetFactory#createFromProtocolEntity(com.mysql.cj.protocol.ProtocolEntity)
         * @see com.mysql.cj.jdbc.result.ResultSetFactory#createFromResultsetRows(int, int, com.mysql.cj.protocol.ResultsetRows)
         * @see com.mysql.cj.jdbc.result.ResultSetImpl#ResultSetImpl(com.mysql.cj.protocol.ResultsetRows, com.mysql.cj.jdbc.JdbcConnection, com.mysql.cj.jdbc.StatementImpl)
         */

        while (resultSet.next()) {
            System.out.println("id:" + resultSet.getInt(1) + " address:" + resultSet.getString(2) + " name:" + resultSet.getString(4));
        }

    }

    /**
     * PreparedStatement 的功能类似 Statement，但不同的是 PreparedStatement 可以使用占位符，它是由占位符标识需要输入数据的位置，然后再逐一填入数据。
     * 当然，PreparedStatement 也可以执行没有占位符的 sql 语句。
     * 对于有 sql 缓存池的数据库，PreparedStatement 的效率要高于 Statement。
     *
     * https://www.cnblogs.com/progor/p/9096463.html#navigator
     */
    @Test
    public void queryByParam() throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out));
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        PreparedStatement preparedStatement = conn.prepareStatement("select * from t_student where id = ? and age = ?");
        /**
         * @see com.mysql.cj.jdbc.ConnectionImpl#prepareStatement(java.lang.String)
         * @see com.mysql.cj.jdbc.ConnectionImpl#clientPrepareStatement(java.lang.String, int, int, boolean)
         * @see com.mysql.cj.jdbc.ClientPreparedStatement#getInstance(com.mysql.cj.jdbc.JdbcConnection, java.lang.String, java.lang.String)
         */

        preparedStatement.setString(1, "1");
        preparedStatement.setInt(2, 11);
        /**
         * @see com.mysql.cj.jdbc.ClientPreparedStatement#setString(int, java.lang.String)
         */

        ResultSet resultSet = preparedStatement.executeQuery();
        /**
         * @see com.mysql.cj.jdbc.ClientPreparedStatement#executeQuery()
         *
         * 对 SQL 中的占位符进行填补
         * @see com.mysql.cj.AbstractPreparedQuery#fillSendPacket()
         * @see com.mysql.cj.AbstractPreparedQuery#fillSendPacket(com.mysql.cj.QueryBindings)
         *
         * 将原始的 SQL 根据占位符拆分成三个字符串：
         * 1. select * from t_student where id =
         * 2.  and age =
         * 3. 空字符串
         *
         * 拿到完整的 SQL 之后，进行数据发送
         * @see com.mysql.cj.jdbc.ClientPreparedStatement#executeInternal(int, com.mysql.cj.protocol.Message, boolean, boolean, com.mysql.cj.protocol.ColumnDefinition, boolean)
         *
         * 后续流程，与使用 Statement 是一样的
         * @see com.mysql.cj.protocol.a.NativeProtocol#sendQueryPacket(com.mysql.cj.Query, com.mysql.cj.protocol.a.NativePacketPayload, int, boolean, com.mysql.cj.protocol.ColumnDefinition, com.mysql.cj.protocol.ProtocolEntityFactory)
         * @see com.mysql.cj.NativeSession#execSQL(com.mysql.cj.Query, java.lang.String, int, com.mysql.cj.protocol.a.NativePacketPayload, boolean, com.mysql.cj.protocol.ProtocolEntityFactory, com.mysql.cj.protocol.ColumnDefinition, boolean)
         */

        while (resultSet.next()) {
            System.out.println("id:" + resultSet.getInt(1) + " address:" + resultSet.getString(2) + " name:" + resultSet.getString(4));
        }

    }

    /**
     * 批量操作
     */
    @Test
    public void updateBatch() throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out));
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        PreparedStatement preparedStatement = conn.prepareStatement("update t_student set age = ? where id = ?");

        preparedStatement.setInt(1, 10);
        preparedStatement.setString(2, "1");
        preparedStatement.addBatch();

        preparedStatement.setInt(1, 10);
        preparedStatement.setString(2, "2");
        preparedStatement.addBatch();

        int[] result = preparedStatement.executeBatch();
        System.out.println("result = " + result.length);
    }

    @Test
    public void ping() throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out));
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        Statement statement = conn.createStatement();
        statement.executeQuery("/* ping */");
//        ResultSet resultSet = statement.executeQuery("/* ping */");
//        Object object = resultSet.getObject(1);
//        System.out.println("object = " + object);
    }

    @Test
    public void byteToString() {
        String str = "hello world";
        byte[] bytes = str.getBytes();
        String result = com.mysql.cj.util.StringUtils.dumpAsHex(bytes, str.length());
        System.out.println("result = \r\n" + result);
    }


}
