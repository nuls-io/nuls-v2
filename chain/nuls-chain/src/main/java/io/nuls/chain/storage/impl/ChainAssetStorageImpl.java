package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.ChainAsset;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
@Component
public class ChainAssetStorageImpl implements ChainAssetStorage, InitializingBean {

    private final String TBL = "chain_asset";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            if (!RocksDBService.existTable(TBL)) {
                RocksDBService.createTable(TBL);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * Get specific asset values of a specific chain
     *
     * @param key chainId-assetId
     * @return ChainAsset object
     */
    @Override
    public ChainAsset load(String key) {
        byte[] bytes = RocksDBService.get(TBL, key.getBytes());
        if (bytes == null) {
            return null;
        }

        try {
            ChainAsset chainAsset = new ChainAsset();
            chainAsset.parse(bytes, 0);
            return chainAsset;
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * Save specific asset values of a specific chain
     *
     * @param key        chainId-assetId
     * @param chainAsset ChainAsset object
     * @return true/false
     */
    @Override
    public boolean save(String key, ChainAsset chainAsset) {
        try {
            return RocksDBService.put(TBL, key.getBytes(), chainAsset.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * Physical deletion
     *
     * @param key chainId-assetId
     */
    @Override
    public boolean delete(String key) {
        try {
            return RocksDBService.delete(TBL, key.getBytes());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * Get asset information by chain ID
     *
     * @param chainId The chain ID
     * @return ChainAsset object
     */
    @Override
    public List<ChainAsset> getByChain(short chainId) {
        List<byte[]> bytesList = RocksDBService.valueList(TBL);
        List<ChainAsset> chainAssetList = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            try {
                ChainAsset chainAsset = new ChainAsset();
                chainAsset.parse(bytes, 0);
                if (chainAsset.getChainId() == chainId) {
                    chainAssetList.add(chainAsset);
                }
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        return chainAssetList;
    }
}
