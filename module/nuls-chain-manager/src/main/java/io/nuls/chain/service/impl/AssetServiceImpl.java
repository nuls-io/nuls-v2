package io.nuls.chain.service.impl;

import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainCirculateStorage;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rpc.util.TimeUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetStorage assetStorage;

    @Autowired
    private ChainAssetStorage chainAssetStorage;

    @Autowired
    private ChainService chainService;
    @Autowired
    private NulsChainConfig nulsChainConfig;

    @Autowired
    private ChainCirculateStorage chainCirculateStorage;

    /**
     * delete asset
     *
     * @param asset Asset object that needs to be delete
     */

    @Override
    public void deleteAsset(Asset asset) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
        assetStorage.delete(assetKey);
        chainAssetStorage.delete(key);
    }

    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     */
    @Override
    public void createAsset(Asset asset) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
        asset.addChainId(asset.getChainId());
        assetStorage.save(assetKey, asset);
        ChainAsset chainAsset = new ChainAsset();
        chainAsset.setAddressChainId(asset.getChainId());
        chainAsset.setAssetChainId(asset.getChainId());
        chainAsset.setAssetId(asset.getAssetId());
        chainAsset.setInitNumber(asset.getInitNumber());
        chainAssetStorage.save(key, chainAsset);
    }

    /**
     * saveOrUpdate chainAsset
     *
     * @param chainId    Chain ID
     * @param chainAsset The ChainAsset updated
     */
    @Override
    public void saveOrUpdateChainAsset(int chainId, ChainAsset chainAsset) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(chainAsset.getAssetChainId(), chainAsset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(chainId, assetKey);
        LoggerUtil.logger().debug("key={},assetInfo:inAmount={},outAmount={}", key, chainAsset.getInNumber(), chainAsset.getOutNumber());
        chainAssetStorage.save(key, chainAsset);
    }


    @Override
    public void batchSaveOrUpdateChainAsset(Map<String, ChainAsset> chainAssets) throws Exception {
        Map<byte[], byte[]> kvs = new HashMap<>();
        for (Map.Entry<String, ChainAsset> entry : chainAssets.entrySet()) {
            kvs.put(entry.getKey().getBytes(nulsChainConfig.getEncoding()), entry.getValue().serialize());
        }
        chainAssetStorage.batchSave(kvs);
    }


    public void saveMsgChainCirculateAmount(String key, BigInteger amount) throws Exception {
        chainCirculateStorage.save(key, amount);

    }

    /**
     * update asset
     *
     * @param asset The asset updated
     */
    @Override
    public void updateAsset(Asset asset) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        assetStorage.save(assetKey, asset);
    }

    /**
     * @param assetMap
     * @throws Exception
     */
    @Override
    public void batchUpdateAsset(Map<String, Asset> assetMap) throws Exception {
        Map<byte[], byte[]> kvs = new HashMap<>();
        for (Map.Entry<String, Asset> entry : assetMap.entrySet()) {
            kvs.put(entry.getKey().getBytes(nulsChainConfig.getEncoding()), entry.getValue().serialize());
        }
        assetStorage.batchSave(kvs);
    }


    /**
     * Find asset based on key
     *
     * @param assetKey The asset key
     * @return Asset object
     */
    @Override
    public Asset getAsset(String assetKey) throws Exception {
        return assetStorage.load(assetKey);
    }

    /**
     * Set the status of asset
     *
     * @param assetKey  The asset key
     * @param available The status of asset
     */
    @Override
    public void setStatus(String assetKey, boolean available) throws Exception {
        Asset asset = assetStorage.load(assetKey);
        if (asset == null) {
            throw new Exception("assetKey not exist: " + assetKey);
        }
        asset.setAvailable(available);
        asset.setLastUpdateTime(TimeUtils.getCurrentTimeMillis());
        assetStorage.save(assetKey, asset);
    }

    @Override
    public List<Asset> getAssets(List<String> assetKeys) throws Exception {
        List<Asset> rtList = new ArrayList<>();
        for (String assetKey : assetKeys) {
            Asset asset = getAsset(assetKey);
            rtList.add(asset);
        }
        return rtList;
    }


    @Override
    public boolean assetExist(Asset asset) throws Exception {
        Asset dbAsset = assetStorage.load(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        return dbAsset != null;
    }

    @Override
    public boolean assetExist(Asset asset, Map<String, Integer> map) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        Asset dbAsset = assetStorage.load(assetKey);
        return ((dbAsset != null) || (null != map.get(assetKey)));
    }

    /**
     * @param chainId
     * @param assetKey
     * @return
     * @throws Exception
     */
    @Override
    public ChainAsset getChainAsset(int chainId, String assetKey) throws Exception {
        String chainAssetKey = CmRuntimeInfo.getChainAssetKey(chainId, assetKey);
        ChainAsset chainAsset = chainAssetStorage.load(chainAssetKey);
        if (null != chainAsset) {
            if (BigIntegerUtils.isGreaterThan(chainAsset.getInitNumber(), BigInteger.ZERO) && CmRuntimeInfo.isAddressChainEqAssetChain(chainId, assetKey)) {
                BigInteger amount = chainCirculateStorage.load(assetKey);
                if (null != amount) {
                    chainAsset.setInitNumber(amount);
                }
            }
        }
        return chainAsset;
    }

    /**
     * @param chainAssetKey
     * @return
     * @throws Exception
     */
    @Override
    public ChainAsset getChainAsset(String chainAssetKey) throws Exception {
        return chainAssetStorage.load(chainAssetKey);
    }

    /**
     * 注册资产
     * Register asset
     *
     * @param asset The registered Asset
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerAsset(Asset asset) throws Exception {

        //提交asset
        createAsset(asset);

        //获取链信息
        BlockChain dbChain = chainService.getChain(asset.getChainId());
        dbChain.addCreateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        dbChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        //更新chain
        chainService.updateChain(dbChain);
    }

    /**
     * 回滚注册资产
     * Rollback the registered Asset
     *
     * @param asset The Asset be rollback
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerAssetRollback(Asset asset) throws Exception {
        //判断库中的asset是否存在，数据正确，则删除
        Asset dbAsset = getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        if (!ByteUtils.arrayEquals(asset.getAddress(), dbAsset.getAddress())) {
            throw new Exception(CmErrorCode.ERROR_ADDRESS_ERROR.getMsg());
        }

        deleteAsset(asset);

        //更新chain
        BlockChain dbChain = chainService.getChain(dbAsset.getChainId());
        dbChain.removeCreateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        dbChain.removeCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        chainService.updateChain(dbChain);

        throw new Exception(CmErrorCode.ERROR_ASSET_NOT_EXIST.getMsg());
    }
}
