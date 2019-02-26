package io.nuls.h2.utils;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class MybatisDbHelper {

    private static SqlSessionFactory sqlSessionFactory;

    private static ThreadLocal<SqlSession> sessionHolder = new ThreadLocal<>();


    public static void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        MybatisDbHelper.sqlSessionFactory = sqlSessionFactory;
    }

    public static SqlSession get() {
        SqlSession sqlSession = sessionHolder.get();
        if (sqlSession == null) {

            sqlSession = sqlSessionFactory.openSession(false);
            sessionHolder.set(sqlSession);
        }
        return sqlSession;
    }

    public void commit() {
        SqlSession sqlSession = sessionHolder.get();
        sqlSession.commit();
    }

    public static void close(SqlSession sqlSession) {
        if (sqlSession == sessionHolder.get()) {
            sqlSession.close();
            sessionHolder.remove();
        }
    }
}
