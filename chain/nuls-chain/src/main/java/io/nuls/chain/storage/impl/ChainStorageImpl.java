package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.Chain;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Service
public class ChainStorageImpl implements ChainStorage, InitializingBean {
    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

        try {
            RocksDBService.createTable(CmConstants.TB_NAME_CHAIN);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(CmConstants.DB_TABLE_CREATE_ERROR);
            }
        }
    }

    /**
     * Save chain information when registering a new chain
     *
     * @param chain Chain information filled in when the user registers
     * @return Number of saves
     */
    @Override
    public int save(Chain chain) {
        try {
            System.out.println("我要开始保存数据了！");
            RocksDBService.put(CmConstants.TB_NAME_CHAIN, SerializeUtils.shortToBytes(chain.getChainId()), "yifeng handsome".getBytes());
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Chain selectByName(String name) {
        byte[] bytes = RocksDBService.get(CmConstants.TB_NAME_CHAIN, name.getBytes());
        Chain chain = new Chain();
        chain.setName(new String(bytes));
        return chain;
    }

}
