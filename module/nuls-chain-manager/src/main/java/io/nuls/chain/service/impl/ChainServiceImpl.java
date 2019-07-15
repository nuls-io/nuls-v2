package io.nuls.chain.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.model.ByteUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * 关于链的所有操作：增删改查
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
@Service
public class ChainServiceImpl implements ChainService {
    @Autowired
    private NulsChainConfig nulsChainConfig;

    @Autowired
    private ChainStorage chainStorage;

    @Autowired
    private AssetService assetService;
    @Autowired
    private RpcService rpcService;

    private static Map<String, Object> chainNetMagicNumberMap = new HashMap<>();
    private static Map<String, Object> chainNameMap = new HashMap<>();

    @Override
    public void addChainMapInfo(BlockChain blockChain) {
        chainNetMagicNumberMap.put(String.valueOf(blockChain.getMagicNumber()), 1);
        chainNameMap.put(blockChain.getChainName(), 1);
    }

    @Override
    public boolean hadExistMagicNumber(long magicNumber) {
        return (null != chainNetMagicNumberMap.get(String.valueOf(magicNumber)));
    }

    @Override
    public boolean hadExistChainName(String chainName) {
        return (null != chainNameMap.get(chainName));
    }

    @Override
    public void initRegChainDatas(long mainNetMagicNumber) throws Exception {
        chainNetMagicNumberMap.put(String.valueOf(mainNetMagicNumber), 1);
        List<BlockChain> list = getBlockList();
        for (BlockChain blockChain : list) {
            chainNetMagicNumberMap.put(String.valueOf(blockChain.getMagicNumber()), 1);
            chainNameMap.put(blockChain.getChainName(), 1);
        }
    }

    /**
     * 把Nuls2.0主网默认注册到Nuls2.0上（便于进行链资产的统一处理）
     * Register the Nuls2.0 main network to Nuls2.0 by default (Nuls2.0 main network can be considered as the first friend chain of Nurs2.0 ecosystem)
     *
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void initMainChain() throws Exception {
        int chainId = Integer.valueOf(nulsChainConfig.getMainChainId());
        BlockChain chain = getChain(chainId);
        if (chain != null) {
            return;
        }
        chain = new BlockChain();
        int assetId = Integer.parseInt(nulsChainConfig.getMainAssetId());
        chain.setChainId(chainId);
        chain.setRegAssetId(assetId);
        chain.setChainName(nulsChainConfig.getChainName());
        chain.addCreateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chainStorage.save(chainId, chain);
        Asset asset = new Asset();
        asset.setChainId(chainId);
        asset.setAssetId(assetId);
        asset.setInitNumber(new BigInteger(nulsChainConfig.getNulsAssetInitNumberMax()));
        asset.setSymbol(nulsChainConfig.getNulsAssetSymbol());
        assetService.createAsset(asset);
    }

    /**
     * 保存链信息
     * Save chain
     *
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void saveChain(BlockChain blockChain) throws Exception {
        addChainMapInfo(blockChain);
        chainStorage.save(blockChain.getChainId(), blockChain);
    }

    /**
     * 更新链信息
     * Update chain
     *
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void updateChain(BlockChain blockChain) throws Exception {
        chainStorage.update(blockChain.getChainId(), blockChain);
    }

    /**
     * @param assetMap
     * @throws Exception
     */
    @Override
    public void batchUpdateChain(Map<String, BlockChain> assetMap) throws Exception {
        Map<byte[], byte[]> kvs = new HashMap<>();
        for (Map.Entry<String, BlockChain> entry : assetMap.entrySet()) {
            kvs.put(ByteUtils.intToBytes(entry.getValue().getChainId()), entry.getValue().serialize());
        }
        chainStorage.batchUpdate(kvs);
    }

    /**
     * 删除链信息
     * Delete chain
     *
     * @param blockChain The BlockChain deleted
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void delChain(BlockChain blockChain) throws Exception {
        chainStorage.delete(blockChain.getChainId());
    }

    /**
     * 根据序号获取链
     * Get the chain according to the ID
     *
     * @param chainId Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain getChain(int chainId) throws Exception {
        return chainStorage.load(chainId);
    }

    @Override
    public boolean chainExist(int chainId) throws Exception {
        return (null != getChain(chainId));
    }


    /**
     * 注册链
     * Register a new chain
     *
     * @param blockChain The BlockChain saved
     * @param asset      The Asset saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerBlockChain(BlockChain blockChain, Asset asset) throws Exception {
        /*
        1. 插入资产表
        2. 插入资产流通表
         */
        assetService.createAsset(asset);

        /*
          3. 插入链
         */
        blockChain.addCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
        blockChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
        saveChain(blockChain);
        /*
            通知网络模块创建链
        */
        rpcService.createCrossGroup(blockChain);
    }

    /**
     * @param txs
     * @throws Exception
     */
    @Override
    public void rpcBlockChainRollback(List<Transaction> txs) throws Exception {
        /*
            通知网络模块创建链
        */
        for (Transaction tx : txs) {
            switch (tx.getType()) {
                case TxType.REGISTER_CHAIN_AND_ASSET:
                    BlockChain blockChain = TxUtil.buildChainWithTxData(tx, false);
                    rpcService.destroyCrossGroup(blockChain);
                    break;
                case TxType.DESTROY_CHAIN_AND_ASSET:
                    BlockChain delBlockChain = TxUtil.buildChainWithTxData(tx, true);
                    BlockChain dbRegChain = this.getChain(delBlockChain.getChainId());
                    rpcService.createCrossGroup(dbRegChain);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 销毁链
     * Destroy a exist BlockChain
     *
     * @param blockChain The BlockChain destroyed
     * @return The BlockChain after destroyed
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain destroyBlockChain(BlockChain blockChain) throws Exception {
        //更新资产
        assetService.setStatus(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()), false);
        //更新链
        BlockChain dbChain = getChain(blockChain.getChainId());
        dbChain.setDelAddress(blockChain.getDelAddress());
        dbChain.setDelAssetId(blockChain.getDelAssetId());
        dbChain.setDelTxHash(blockChain.getDelTxHash());
        dbChain.removeCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()));
        dbChain.setDelete(true);
        updateChain(dbChain);
        //通知销毁链
        rpcService.destroyCrossGroup(dbChain);
        return dbChain;
    }

    @Override
    public List<BlockChain> getBlockList() throws Exception {
        return chainStorage.loadAllRegChains();
    }

    @Override
    public Map<String, Object> getBlockAssetsInfo(BlockChain blockChain) throws Exception {
        Map<String, Object> chainInfoMap = new HashMap<>();
        chainInfoMap.put("chainId", blockChain.getChainId());
        chainInfoMap.put("chainName", blockChain.getChainName());
        chainInfoMap.put("minAvailableNodeNum", blockChain.getMinAvailableNodeNum());
        chainInfoMap.put("maxSignatureCount", blockChain.getMaxSignatureCount());
        chainInfoMap.put("signatureByzantineRatio", blockChain.getSignatureByzantineRatio());
        chainInfoMap.put("verifierList", new HashSet(blockChain.getVerifierList()));
        List<Asset> assets = assetService.getAssets(blockChain.getSelfAssetKeyList());
        List<Map<String, Object>> rtAssetList = new ArrayList<>();
        for (Asset asset : assets) {
            Map<String, Object> assetMap = new HashMap<>();
            assetMap.put("assetId", asset.getAssetId());
            assetMap.put("symbol", asset.getSymbol());
            assetMap.put("assetName", asset.getAssetName());
            assetMap.put("usable", asset.isAvailable());
            assetMap.put("decimalPlaces", asset.getDecimalPlaces());
            rtAssetList.add(assetMap);
        }
        chainInfoMap.put("assetInfoList", rtAssetList);
        return chainInfoMap;
    }

}
