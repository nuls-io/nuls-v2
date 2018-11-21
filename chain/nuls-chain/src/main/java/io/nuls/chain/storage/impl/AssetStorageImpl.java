package io.nuls.chain.storage.impl;

import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetStorageImpl implements AssetStorage, InitializingBean {

    private final String TBL = "asset";

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
     * Save asset
     *
     * @param key   The key
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean save(long key, Asset asset) {
        try {
            return RocksDBService.put(TBL, ByteUtils.longToBytes(key), asset.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * Find assets based on key
     *
     * @param key The key
     * @return Asset object
     */
    @Override
    public Asset load(long key) {
        byte[] bytes = RocksDBService.get(TBL, ByteUtils.longToBytes(key));
        if (bytes == null) {
            return null;
        }

        try {
            Asset asset = new Asset();
            asset.parse(bytes, 0);
            return asset;
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * Physical deletion
     *
     * @param key Asset ID
     * @return true/false
     */
    @Override
    public boolean delete(long key) {
        try {
            return RocksDBService.delete(TBL, ByteUtils.longToBytes(key));
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    @Override
    public List<Asset> getByChain(short chainId) {
        List<byte[]> bytesList = RocksDBService.valueList(TBL);
        List<Asset> assetList = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            try {
                Asset asset = new Asset();
                asset.parse(bytes, 0);
                if (asset.getChainId() == chainId) {
                    assetList.add(asset);
                }
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        return assetList;
    }

    /**
     * Get asset by symbol
     *
     * @param symbol Asset symbol
     * @return Asset object
     */
    @Override
    public Asset getBySymbol(String symbol) {
        List<byte[]> bytesList = RocksDBService.valueList(TBL);
        for (byte[] bytes : bytesList) {
            try {
                Asset asset = new Asset();
                asset.parse(bytes, 0);
                if (asset.getSymbol().equals(symbol)) {
                    return asset;
                }
            } catch (NulsException e) {
                Log.error(e);
            }
        }
        return null;
    }
}
