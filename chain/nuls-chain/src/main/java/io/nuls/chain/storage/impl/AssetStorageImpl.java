package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
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

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            RocksDBService.createTable(CmConstants.TBL_ASSET);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(CmConstants.DB_TABLE_CREATE_ERROR);
            }
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
    public boolean save(short key, Asset asset) {
        try {
            return RocksDBService.put(CmConstants.TBL_ASSET, ByteUtils.shortToBytes(key), asset.serialize());
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
    public Asset load(short key) {
        try {
            Asset asset = new Asset();
            byte[] bytes = RocksDBService.get(CmConstants.TBL_ASSET, ByteUtils.shortToBytes(key));
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
    public boolean delete(short key) {
        try {
            return RocksDBService.delete(CmConstants.TBL_ASSET, ByteUtils.shortToBytes(key));
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
        List<byte[]> bytesList = RocksDBService.valueList(CmConstants.TBL_ASSET);
        List<Asset> assetList = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            Asset asset = new Asset();
            try {
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
}
