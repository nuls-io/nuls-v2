package io.nuls.chain.cmd;


import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.tx.CrossChainDestroyTransaction;
import io.nuls.chain.model.tx.CrossChainRegTransaction;
import io.nuls.chain.model.tx.txdata.ChainTx;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
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
public class ChainTxCmd extends BaseCmd {

    @Autowired
    private ChainService chainService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private RpcService rpcService;


    @CmdAnnotation(cmd = "cm_chainRegValidator", version = 1.0,description = "chainRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction(),false);
            int chainId = chain.getChainId();
            if (chainId < 0) {
                return failed(CmErrorCode.C10002);
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null ) {
                return failed(CmErrorCode.C10001);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainRegCommit", version = 1.0,description = "chainRegCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction(),false);
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null ) {
                return failed(CmErrorCode.C10001);
            }
            //进行资产存储,资产流通表存储
            Asset asset = buildAssetTxData(txHex,new CrossChainRegTransaction());
            asset.addChainId(asset.getChainId());
            assetService.createAsset(asset);

            //进行链存储:
            chain.addCreateAssetId(asset.getAssetId());
            chain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(chain.getChainId(),asset.getAssetId()));
            chainService.saveChain(chain);

            //通知网络模块创建链
            rpcService.createCrossGroup(chain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainRegRollback", version = 1.0,description = "chainRegRollback")
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
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction(),false);
            Chain dbChain = chainService.getChain(chain.getChainId());
            if ( null == chain || null == dbChain || !chain.getRegTxHash().equalsIgnoreCase(dbChain.getRegTxHash())) {
                return failed(CmErrorCode.C10001);
            }
            chainService.delChain(chain);
            int assetId = chain.getRegAssetId();
            Asset asset =  assetService.getAsset(CmRuntimeInfo.getAssetKey(chain.getChainId(),assetId));
            assetService.deleteAsset(asset);
            rpcService.destroyCrossGroup(chain);
            return success(chain);
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyValidator", version = 1.0,description = "chainDestroyValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyValidator(Map params) {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction(),true);
            return destroyValidator(chain);
    }
    private Response destroyValidator(Chain chain){
        try {
            if(null == chain) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            /*获取链下剩余的资产*/
            List<Integer> assetIds = dbChain.getAssetIds();
            if(assetIds.size()> 0){
                return failed(CmErrorCode.ERROR_CHAIN_ASSET_MUTI);
            }
            int assetId =  assetIds.get(0);
            Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chain.getChainId(),assetId));
            if(null == dbAsset){
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
            if(!ByteUtils.arrayEquals(dbAsset.getAddress(),chain.getDelAddress())){
                return failed(CmErrorCode.ERROR_ADDRESS_ERROR);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyCommit", version = 1.0,description = "chainDestroyCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction(),true);
            Response cmdResponse =  destroyValidator(chain);
            if(cmdResponse.getResponseStatus() != (Constants.SUCCESS_CODE)){
                return cmdResponse;
            }
            //更新资产
            assetService.setStatus(CmRuntimeInfo.getAssetKey(chain.getChainId(),chain.getDelAssetId()),false);
           //更新链
            Chain dbChain = chainService.getChain(chain.getChainId());
            dbChain.setDelAddress(chain.getDelAddress());
            dbChain.setDelAssetId(chain.getDelAssetId());
            dbChain.setDelTxHash(chain.getDelTxHash());
            dbChain.removeCreateAssetId(chain.getDelAssetId());
            dbChain.setDelete(true);
            chainService.updateChain(dbChain);
            rpcService.destroyCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002, e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyRollback", version = 1.0,description = "chainDestroyRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyRollback(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction(),true);
            Response cmdResponse =  destroyValidator(chain);
            if(cmdResponse.getResponseStatus() != (Constants.SUCCESS_CODE)){
                return cmdResponse;
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            if(!dbChain.isDelete()){
                return failed(CmErrorCode.ERROR_CHAIN_STATUS);
            }
            //资产回滚
            String assetKey =CmRuntimeInfo.getAssetKey(dbChain.getChainId(),dbChain.getDelAssetId());
            assetService.setStatus(assetKey,true);
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

    private Chain buildChainTxData(String txHex, Transaction tx,boolean isDelete){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            ChainTx chainTx =  new ChainTx();
            chainTx.parse(tx.getTxData(),0);
            Chain chain = new Chain(chainTx,isDelete);
            if(isDelete){
                chain.setDelTxHash(tx.getHash().toString());
            }else {
                chain.setRegTxHash(tx.getHash().toString());
            }
            return chain;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
    private Asset buildAssetTxData(String txHex, Transaction tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            ChainTx chainTx =  new ChainTx();
            chainTx.parse(tx.getTxData(),0);
            Asset asset = new Asset(chainTx);
            asset.setTxHash(tx.getHash().toString());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
