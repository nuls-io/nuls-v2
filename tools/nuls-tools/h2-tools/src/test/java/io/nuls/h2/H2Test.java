package io.nuls.h2;

import io.nuls.h2.dao.TransactionService;
import io.nuls.h2.dao.impl.BaseService;
import io.nuls.h2.dao.impl.TransactionServiceImpl;
import io.nuls.h2.entity.TransactionPo;
import io.nuls.h2.session.SessionManager;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public class H2Test {

    @Before
    public void before() throws Exception{
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        BaseService.sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
        //SessionManager.setSqlSessionFactory(sqlSessionFactory);
    }

    @Test
    public void init(){
        TransactionService ts = new TransactionServiceImpl();
       /* TransactionPo txPo = new TransactionPo();
        txPo.setAddress("address_ertyuighjk");
        txPo.setHash("hash_zxcvbnmasdfghjk");
        txPo.setAmount(800000L);
        txPo.setState(0);
        txPo.setType(1);
        txPo.setTime(new Date().getTime());
        ts.saveTx(txPo);*/
        //ts.createTable("transaction", "transaction_index",128);
        ts.createTxTables("transaction", "transaction_index",128);
    }

    @Test
    public void db(){
        String JDBC_URL = "jdbc:h2:file:./data/nuls;INIT=RUNSCRIPT FROM 'classpath:sql/schema-h2-bak.sql';LOG=2;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=1;DATABASE_TO_UPPER=FALSE";
        //连接数据库时使用的用户名
        String USER = "sa";
        //连接数据库时使用的密码
        String PASSWORD = "123456";
        String DRIVER_CLASS = "org.h2.Driver";
        try {
            Class.forName(DRIVER_CLASS);
            Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
