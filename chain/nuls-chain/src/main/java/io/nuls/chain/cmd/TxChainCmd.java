package io.nuls.chain.cmd;


import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.model.tx.DestroyAssetAndChainTransaction;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.Map;

/**
 * @author lan
 * @date 2018/11/21
 * @description
 */
@Component
public class TxChainCmd extends BaseChainCmd {

    @Autowired
    private ChainService chainService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private RpcService rpcService;


    @CmdAnnotation(cmd = "cm_chainRegValidator", version = 1.0, description = "chainRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            BlockChain blockChain = buildChainWithTxData(txHex, new RegisterChainAndAssetTransaction(), false);
            int chainId = blockChain.getChainId();
            if (chainId < 0) {
                return failed(CmErrorCode.C10002);
            }
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (dbChain != null) {
                return failed(CmErrorCode.C10001);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainRegCommit", version = 1.0, description = "chainRegCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            BlockChain blockChain = buildChainWithTxData(txHex, new RegisterChainAndAssetTransaction(), false);
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (dbChain != null) {
                return failed(CmErrorCode.C10001);
            }
            //进行资产存储,资产流通表存储
            Asset asset = buildAssetWithTxChain(txHex, new RegisterChainAndAssetTransaction());
            asset.addChainId(asset.getChainId());
            assetService.createAsset(asset);

            //进行链存储:
            blockChain.addCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
            blockChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
            chainService.saveChain(blockChain);

            //通知网络模块创建链
            rpcService.createCrossGroup(blockChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainRegRollback", version = 1.0, description = "chainRegRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    /**
     * 尽量不要让链中注册提交后，执行回滚。否则如果期间有跨连业务了，回滚很容易造成问题。
     *
     * @param params Map
     * @return Response object
     */
    public Response chainRegRollback(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            BlockChain blockChain = buildChainWithTxData(txHex, new RegisterChainAndAssetTransaction(), false);
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (null == blockChain || null == dbChain || !blockChain.getRegTxHash().equalsIgnoreCase(dbChain.getRegTxHash())) {
                return failed(CmErrorCode.C10001);
            }
            chainService.delChain(blockChain);
            int assetId = blockChain.getRegAssetId();
            Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), assetId));
            assetService.deleteAsset(asset);
            rpcService.destroyCrossGroup(blockChain);
            return success(blockChain);
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainDestroyValidator", version = 1.0, description = "chainDestroyValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyValidator(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        String secondaryData = String.valueOf(params.get("secondaryData"));
        BlockChain blockChain = buildChainWithTxData(txHex, new DestroyAssetAndChainTransaction(), true);
        return destroyValidator(blockChain);
    }

    private Response destroyValidator(BlockChain blockChain) {
        try {
            if (null == blockChain) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            }
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            /*获取链下剩余的资产*/
            List<String> keys = dbChain.getSelfAssetKeyList();
            if (keys.size() == 0) {
                return failed(CmErrorCode.ERROR_CHAIN_ASSET_MUTI);
            }
            String key = keys.get(0);
            Asset dbAsset = assetService.getAsset(key);
            if (null == dbAsset) {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
            if (!ByteUtils.arrayEquals(dbAsset.getAddress(), blockChain.getDelAddress())) {
                return failed(CmErrorCode.ERROR_ADDRESS_ERROR);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainDestroyCommit", version = 1.0, description = "chainDestroyCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            BlockChain blockChain = buildChainWithTxData(txHex, new DestroyAssetAndChainTransaction(), true);
            Response cmdResponse = destroyValidator(blockChain);
            if (cmdResponse.isSuccess()) {
                return cmdResponse;
            }
            //更新资产
            assetService.setStatus(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()), false);
            //更新链
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            dbChain.setDelAddress(blockChain.getDelAddress());
            dbChain.setDelAssetId(blockChain.getDelAssetId());
            dbChain.setDelTxHash(blockChain.getDelTxHash());
            dbChain.removeCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()));
            dbChain.setDelete(true);
            chainService.updateChain(dbChain);
            rpcService.destroyCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainDestroyRollback", version = 1.0, description = "chainDestroyRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyRollback(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            BlockChain blockChain = buildChainWithTxData(txHex, new DestroyAssetAndChainTransaction(), true);
            Response cmdResponse = destroyValidator(blockChain);
            if (cmdResponse.isSuccess()) {
                return cmdResponse;
            }
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (!dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_CHAIN_STATUS);
            }
            //资产回滚
            String assetKey = CmRuntimeInfo.getAssetKey(dbChain.getChainId(), dbChain.getDelAssetId());
            assetService.setStatus(assetKey, true);
            //链回滚
            dbChain.setDelete(false);
            chainService.updateChain(dbChain);
            rpcService.createCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }


}
