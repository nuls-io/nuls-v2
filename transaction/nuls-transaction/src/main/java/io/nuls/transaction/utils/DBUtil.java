package io.nuls.transaction.utils;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.exception.NulsRuntimeException;
import static io.nuls.transaction.utils.LoggerUtil.Log;
import io.nuls.transaction.constant.TxErrorCode;

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
                throw new NulsRuntimeException(TxErrorCode.DB_TABLE_CREATE_ERROR);
            }
        }
    }
}
