package io.nuls.core.storage;

import io.nuls.core.rockdb.manager.TransactionDBManager;
import org.junit.Test;

public class TransactionDBTest {

    static String filePath = "E:\\RocksDBTest";

    static String tableName = "test-table";


    @Test
    public void test() {
        try {
            TransactionDBManager.init(filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
