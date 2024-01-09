package io.nuls.chain.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.chain.util.TxUtil;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.model.ByteUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * All operations on the chain：Add, delete, modify, and check
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
@Service
public class ChainServiceImpl implements ChainService {
    @Autowired
    private NulsCoresConfig nulsChainConfig;

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

    public void removeChainMapInfo(long magicNumber, String chainName) {
        chainNetMagicNumberMap.remove(String.valueOf(magicNumber));
        chainNameMap.remove(chainName);
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
     * holdNuls2.0The main network is registered by default toNuls2.0upper（Facilitate unified processing of chain assets）
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
        int assetId = Integer.valueOf(nulsChainConfig.getMainAssetId());
        chain.setChainId(chainId);
        chain.setRegAssetId(assetId);
        chain.setChainName(nulsChainConfig.getChainName());
        chain.setAddressPrefix(nulsChainConfig.getAddressPrefix());
        chain.addCreateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chainStorage.save(chainId, chain);
        Asset asset = new Asset();
        asset.setChainId(chainId);
        asset.setAssetId(assetId);
        asset.setInitNumber(new BigInteger(nulsChainConfig.getNulsAssetInitNumberMax()));
        asset.setSymbol(nulsChainConfig.getMainSymbol());
        assetService.createAsset(asset);
        if(chainId == 1){
            //2.18.0
            BlockChain chain2 = new BlockChain();
            chain2.setChainId(2);
            chain2.setRegAssetId(1);
            chain2.setChainName("NULS-testnet");
            chain2.setAddressPrefix("tNULS");
            chain2.addCreateAssetId("2-1");
            chain2.addCirculateAssetId("2-1");
            chainStorage.save(2, chain2);
            Asset asset2 = new Asset();
            asset2.setChainId(2);
            asset2.setAssetId(1);
            asset2.setInitNumber(BigInteger.valueOf(10000000000000000L));
            asset2.setSymbol("tNULS");
            assetService.createAsset(asset2);
        }
    }

    /**
     * Save Chain Information
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
     * Update chain information
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
     * Delete Chain Information
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
     * Retrieve chain based on serial number
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
        BlockChain blockChain = getChain(chainId);
        return (null != blockChain && !blockChain.isDelete());
    }


    /**
     * Registration Chain
     * Register a new chain
     *
     * @param blockChain The BlockChain saved
     * @param asset      The Asset saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerBlockChain(BlockChain blockChain, Asset asset) throws Exception {
        /*
        1. Insert Asset Table
        2. Insert Asset Circulation Table
         */
        assetService.createAsset(asset);


        saveChain(blockChain);
        /*
            Notify the network module to create a chain
        */
        rpcService.createCrossGroup(blockChain);
    }

    /**
     * @param txs
     * @throws Exception
     */
    @Override
    public void rpcBlockChainRollback(List<Transaction> txs, long time) throws Exception {
        /*
            Notify the network module to create a chain
        */
        for (Transaction tx : txs) {
            switch (tx.getType()) {
                case TxType.REGISTER_CHAIN_AND_ASSET:
                    BlockChain blockChain = TxUtil.buildChainWithTxData(tx, false);
                    rpcService.destroyCrossGroup(blockChain);
                    Map<String, Object> chainAssetId = new HashMap<>();
                    chainAssetId.put("chainId", blockChain.getChainId());
                    chainAssetId.put("assetId", 0);
                    rpcService.cancelCrossChain(chainAssetId, time);
                    removeChainMapInfo(blockChain.getMagicNumber(), blockChain.getChainName());
                    break;
                case TxType.DESTROY_CHAIN_AND_ASSET:
                    BlockChain delBlockChain = TxUtil.buildChainWithTxData(tx, true);
                    BlockChain dbRegChain = this.getChain(delBlockChain.getChainId());
                    rpcService.createCrossGroup(dbRegChain);
                    rpcService.registerCrossChain(delBlockChain);
                    addChainMapInfo(delBlockChain);
                    break;
                case TxType.ADD_ASSET_TO_CHAIN:
                    Asset asset = TxUtil.buildAssetWithTxChain(tx);
                    Map<String, Object> chainAssetIdAdd = new HashMap<>();
                    chainAssetIdAdd.put("chainId", asset.getChainId());
                    chainAssetIdAdd.put("assetId", asset.getAssetId());
                    rpcService.cancelCrossChain(chainAssetIdAdd, time);
                    break;
                case TxType.REMOVE_ASSET_FROM_CHAIN:
                    Asset assetDel = TxUtil.buildAssetWithTxChain(tx);
                    rpcService.registerCrossAsset(assetDel, time);
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * @param txs
     * @throws Exception
     */
    @Override
    public void rpcBlockChainRollbackV4(List<Transaction> txs, long time) throws Exception {
        /*
            Notify the network module to create a chain
        */
        for (Transaction tx : txs) {
            switch (tx.getType()) {
                case TxType.REGISTER_CHAIN_AND_ASSET:
                    BlockChain blockChain = TxUtil.buildChainWithTxDataV4(tx, false);
                    rpcService.destroyCrossGroup(blockChain);
                    Map<String, Object> chainAssetId = new HashMap<>();
                    chainAssetId.put("chainId", blockChain.getChainId());
                    chainAssetId.put("assetId", 0);
                    rpcService.cancelCrossChain(chainAssetId, time);
                    removeChainMapInfo(blockChain.getMagicNumber(), blockChain.getChainName());
                    break;
                case TxType.DESTROY_CHAIN_AND_ASSET:
                    BlockChain delBlockChain = TxUtil.buildChainWithTxDataV4(tx, true);
                    BlockChain dbRegChain = this.getChain(delBlockChain.getChainId());
                    rpcService.createCrossGroup(dbRegChain);
                    rpcService.registerCrossChain(delBlockChain);
                    addChainMapInfo(delBlockChain);
                    break;
                case TxType.ADD_ASSET_TO_CHAIN:
                    Asset asset = TxUtil.buildAssetWithTxChainV4(tx);
                    Map<String, Object> chainAssetIdAdd = new HashMap<>();
                    chainAssetIdAdd.put("chainId", asset.getChainId());
                    chainAssetIdAdd.put("assetId", asset.getAssetId());
                    rpcService.cancelCrossChain(chainAssetIdAdd,time);
                    break;
                case TxType.REMOVE_ASSET_FROM_CHAIN:
                    Asset assetDel = TxUtil.buildAssetWithTxChainV4(tx);
                    rpcService.registerCrossAsset(assetDel,time);
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * Destruction chain
     * Destroy a exist BlockChain
     *
     * @param blockChain The BlockChain destroyed
     * @return The BlockChain after destroyed
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain destroyBlockChain(BlockChain blockChain) throws Exception {
        //Update assets
        assetService.setStatus(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()), false);
        //Update Chain
        BlockChain dbChain = getChain(blockChain.getChainId());
        dbChain.setDelAddress(blockChain.getDelAddress());
        dbChain.setDelAssetId(blockChain.getDelAssetId());
        dbChain.setDelTxHash(blockChain.getDelTxHash());
        dbChain.setDelete(true);
        updateChain(dbChain);
        //Notify to destroy the chain
        rpcService.destroyCrossGroup(dbChain);
        removeChainMapInfo(blockChain.getMagicNumber(), blockChain.getChainName());
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
        chainInfoMap.put("addressPrefix", blockChain.getAddressPrefix());
        chainInfoMap.put("signatureByzantineRatio", blockChain.getSignatureByzantineRatio());
        chainInfoMap.put("verifierList", new HashSet(blockChain.getVerifierList()));
        chainInfoMap.put("registerTime", blockChain.getCreateTime());
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

    @Override
    public Map<String, Object> getChainAssetsSimpleInfo(BlockChain blockChain) throws Exception {
        Map<String, Object> chainInfoMap = new HashMap<>();
        chainInfoMap.put("chainId", blockChain.getChainId());
        chainInfoMap.put("chainName", blockChain.getChainName());
        chainInfoMap.put("addressPrefix", blockChain.getAddressPrefix());
        chainInfoMap.put("registerTime", blockChain.getCreateTime());
        chainInfoMap.put("enable", !blockChain.isDelete());
        return chainInfoMap;
    }

}
