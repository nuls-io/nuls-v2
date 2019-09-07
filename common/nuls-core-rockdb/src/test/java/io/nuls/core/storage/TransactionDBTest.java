package io.nuls.core.storage;

import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rockdb.manager.TransactionDBManager;
import org.junit.Test;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import java.util.Arrays;

public class TransactionDBTest {

    static String filePath = "E:\\RocksDBTest";

    static String tableName = "test-table";

    static String key = "test-key";

    @Test
    public void initTest() {
        try {
            TransactionDBManager.init(filePath);
            if (existTable(tableName)) {
                destroyTable(tableName);
            }
            TransactionDBManager.createTable(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInsert() {
        String value = "test-insert-value";
        try {
            TransactionDBManager.init(filePath);
            put(key.getBytes(), value.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQuery() {
        try {
            TransactionDBManager.init(filePath);
            byte[] bytes = TransactionDBManager.get(tableName, key.getBytes());
            if (bytes != null) {
                System.out.println(new String(bytes));
            } else {
                System.out.println("----i null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTransactionDB() {
        initTest();
        Transaction tx = null;
        try {
            tx = TransactionDBManager.openSession(tableName);
            String value1 = "value2";
            put(key.getBytes(), value1.getBytes());

            Integer.parseInt("s");
        } catch (Exception e) {
            if(tx != null) {
                try {
                    TransactionDBManager.rollBack(tx);
                } catch (RocksDBException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }


    public static boolean existTable(String table) {
        String[] tables = TransactionDBManager.listTable();
        if (tables != null && Arrays.asList(tables).contains(table)) {
            return true;
        }
        return false;
    }

    public static boolean destroyTable(String table) throws Exception {
        return TransactionDBManager.destroyTable(table);
    }

    public static void put(byte[] key, byte[] value) throws Exception {
        TransactionDBManager.put(tableName, key, value);
    }
}
