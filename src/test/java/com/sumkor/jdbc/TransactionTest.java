package com.sumkor.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Sumkor
 * @since 2021/6/24
 */
public class TransactionTest {

    /**
     * 事务隔离级别枚举：NONE、READ_UNCOMMITTED、READ_COMMITTED、REPEATABLE_READ 和 SERIALIZABLE
     *
     * @see java.sql.Connection#TRANSACTION_NONE
     * @see java.sql.Connection#READ_UNCOMMITTED
     * @see java.sql.Connection#READ_COMMITTED
     * @see java.sql.Connection#REPEATABLE_READ
     * @see java.sql.Connection#SERIALIZABLE
     *
     * Oracle 等多数数据库默认都是 READ_COMMITTED。
     * MySQL InnoDB 默认的事务隔离级别是 REPEATABLE_READ，同时 innoDB 还解决了幻读。
     *
     * MySQL 幻读的介绍
     * https://www.jianshu.com/p/c53c8ab650b5
     *
     * 幻读指的是：第一个事务在指定 where 条件下读到了所有的行，接着第二个事务 insert 了一个符合该 where 条件的行，最后第一个事务重新发起 where 查询，读到了第二个事务新插入的行。
     * 如何解决幻读？行锁只能锁住行，即使把所有的行记录都上锁，也阻止不了新插入的记录。
     * 间隙锁（Gap Lock）是 Innodb 在 REPEATABLE_READ 下为了解决幻读问题时引入的锁机制，锁的是索引叶子节点的 next 指针。
     *
     * Innodb 间隙锁的介绍
     * https://blog.csdn.net/qq_21729419/article/details/113643359
     */

    /**
     * 获取事务隔离级别
     */
    @Test
    public void getTransactionIsolation() throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out));
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        int transactionIsolation = conn.getTransactionIsolation();
        /**
         * 实际上是向数据库发起命令：
         * select @@session.tx_isolation
         *
         * @see com.mysql.cj.jdbc.ConnectionImpl#getTransactionIsolation()
         */
        System.out.println("transactionIsolation = " + transactionIsolation);
        Assert.assertEquals(transactionIsolation, Connection.TRANSACTION_REPEATABLE_READ);
    }

    @Test
    public void setTransactionIsolation() throws SQLException {
        DriverManager.setLogWriter(new PrintWriter(System.out));
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "test", "test");
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        /**
         * 实际上是向数据库发起命令，在会话级别修改事务隔离等级：
         * SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED
         *
         * @see com.mysql.cj.jdbc.ConnectionImpl#setTransactionIsolation(int)
         */

        int transactionIsolation = conn.getTransactionIsolation();
        Assert.assertEquals(transactionIsolation, Connection.TRANSACTION_READ_COMMITTED);
    }
}
