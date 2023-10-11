package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainCirculateStorage;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

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
    private RpcService rpcService;
    @Autowired
    private NulsCoresConfig nulsChainConfig;

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
        asset.setAvailable(false);
        assetStorage.save(assetKey, asset);
//        assetStorage.delete(assetKey);
//        chainAssetStorage.delete(key);
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

    /**
     * 全网的可流通资产数量，含跨链转出的资产数
     *
     * @param key
     * @param amount
     * @throws Exception
     */
    @Override
    public void saveMsgChainCirculateAmount(String key, BigInteger amount) throws Exception {
        chainCirculateStorage.save(key, amount);

    }

    @Override
    public void registerAsset(Asset asset) throws Exception {
        //提交asset
        createAsset(asset);
        //获取链信息
        BlockChain dbChain = chainService.getChain(asset.getChainId());
        List<String> selfAssets = dbChain.getSelfAssetKeyList();
        boolean notExist = true;
        for (String assetKey : selfAssets) {
            String addAssetkey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
            if (assetKey.equalsIgnoreCase(addAssetkey)) {
                notExist = false;
                break;
            }
        }
        if (notExist) {
            dbChain.addCreateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
            dbChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        } else {
            dbChain.setSelfAssetKeyList(TxUtil.moveRepeatInfo(dbChain.getSelfAssetKeyList()));
            dbChain.setTotalAssetKeyList(TxUtil.moveRepeatInfo(dbChain.getTotalAssetKeyList()));
        }
        //更新chain
        chainService.updateChain(dbChain);
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
        Asset asset =  assetStorage.load(assetKey);
        //todo 临时测试用
//        if(assetKey.equals("2-0")&&asset==null){
//            asset = new Asset();
//            asset.setAssetId(0);
//            asset.setAssetName("TEST");
//            asset.setChainId(2);
//            asset.setCreateTime(0);
//            asset.setDecimalPlaces((short) 4);
//            asset.setAddress(AddressTool.getAddress("tNULSeBaMmShSTVwbU4rHkZjpD98JgFgg6rmhF"));
//            asset.setAvailable(true);
//            asset.setDepositNuls(BigInteger.ZERO);
//            asset.setInitNumber(BigInteger.valueOf(1000000000000L));
//            asset.setSymbol("TEST");
//        }
        return asset;
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
        asset.setLastUpdateTime(NulsDateUtils.getCurrentTimeMillis());
        assetStorage.save(assetKey, asset);
    }

    @Override
    public List<Asset> getAssets(List<String> assetKeys) throws Exception {
        List<Asset> rtList = new ArrayList<>();
        for (String assetKey : assetKeys) {
            Asset asset = getAsset(assetKey);
            if (null != asset) {
                rtList.add(asset);
            }
        }
        return rtList;
    }


    @Override
    public boolean assetExist(Asset asset) throws Exception {
        Asset dbAsset = assetStorage.load(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        return dbAsset != null;
    }

    @Override
    public boolean assetExistAndAvailable(Asset asset) throws Exception {
        Asset dbAsset = assetStorage.load(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        return dbAsset != null&&dbAsset.isAvailable();
    }

    @Override
    public boolean assetExist(Asset asset, Map<String, Integer> map) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        Asset dbAsset = assetStorage.load(assetKey);
        return ((dbAsset != null) || (null != map.get(assetKey)));
    }

    @Override
    public boolean regChainAssetExist(Asset asset, Map<String, Integer> map) throws Exception {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        Asset dbAsset = assetStorage.load(assetKey);
        return ((dbAsset != null && dbAsset.isAvailable()) || (null != map.get(assetKey)));
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

}
