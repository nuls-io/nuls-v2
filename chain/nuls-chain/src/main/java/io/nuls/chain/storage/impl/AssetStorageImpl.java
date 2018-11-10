package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Service
public class AssetStorageImpl implements AssetStorage, InitializingBean {

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            RocksDBService.createTable(CmConstants.TB_NAME_ASSET);
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
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    @Override
    public int save(Asset asset) {
        try {
            return RocksDBService.put(CmConstants.TB_NAME_ASSET, SerializeUtils.shortToBytes(asset.getAssetId()), asset.serialize()) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Find assets based on key
     *
     * @param assetId key
     * @return Asset object
     */
    @Override
    public Asset load(short assetId) {
        Asset asset = new Asset();
        byte[] bytes = RocksDBService.get(CmConstants.TB_NAME_ASSET, SerializeUtils.shortToBytes(assetId));
        try {
            asset.parse(bytes, 0);
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return asset;
    }
}
