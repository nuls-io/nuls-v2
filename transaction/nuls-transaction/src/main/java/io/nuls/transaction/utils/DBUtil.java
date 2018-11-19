package io.nuls.transaction.utils;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TransactionErrorCode;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
public class DBUtil {

    public static void createTable(String name){
         if(!RocksDBService.existTable(name)) {
            try {
                RocksDBService.createTable(name);
            } catch (Exception e) {
                Log.error(e);
                throw new NulsRuntimeException(TransactionErrorCode.DB_TABLE_CREATE_ERROR);
            }
        }
    }
}
