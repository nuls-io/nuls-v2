package io.nuls.chain.cmd;

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.tx.AssetRegTransaction;
import io.nuls.chain.model.tx.txdata.AssetTx;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * @author lan
 * @date 2018/11/21
 * @description
 */
@Component
public class AssetTxCmd extends BaseCmd {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;

    private Asset buildAssetTxData(String txHex, Transaction tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            AssetTx assetTx = new AssetTx();
            assetTx.parse(tx.getTxData(),0);
            Asset asset = new Asset(assetTx);
            asset.setTxHash(tx.getHash().toString());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
    @CmdAnnotation(cmd = "cm_assetRegValidator", version = 1.0,
            description = "assetRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());

            if(assetService.assetExist(asset))
            {
                return failed(CmErrorCode.ERROR_ASSET_ID_EXIST);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002);
        }


    }

    /**
     *
     * 资产提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetRegCommit", version = 1.0,description = "assetRegCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetRegCommit(Map params) {
        try {
            Response cmdResponse = assetRegValidator(params);
            if(cmdResponse.getResponseStatus() != Constants.SUCCESS_CODE){
                return cmdResponse;
            }
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
            //获取链信息
            Chain dbChain = chainService.getChain(chainId);
            dbChain.addCreateAssetId(asset.getAssetId());
            dbChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
            //提交asset
            assetService.createAsset(asset);
            //更新chain
            chainService.updateChain(dbChain);
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.Err10002);
        }

        return success();
    }

    /**
     * 资产回滚
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetRegRollback", version = 1.0,
            description = "assetRegRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetRegRollback(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        String secondaryData = String.valueOf(params.get("secondaryData"));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        //判断库中的asset是否存在，数据正确，则删除
        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
        if(!ByteUtils.arrayEquals(asset.getAddress(),dbAsset.getAddress())){
            return failed(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        if(null != dbAsset && dbAsset.getTxHash().equalsIgnoreCase(asset.getTxHash())){
            assetService.deleteAsset(asset);
            //更新chain
            Chain dbChain = chainService.getChain(dbAsset.getChainId());
            dbChain.removeCreateAssetId(asset.getAssetId());
            dbChain.removeCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
            chainService.updateChain(dbChain);
            return success();
        }
        return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
    }

    /**
     * 资产注销校验
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetDisableValidator", version = 1.0,
            description = "assetDisableValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetDisableValidator(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        String secondaryData = String.valueOf(params.get("secondaryData"));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        return assetDisableValidator(asset);
    }
    private Response assetDisableValidator(Asset asset) {

        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
        if (asset == null) {
            return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        if(!ByteUtils.arrayEquals(asset.getAddress(),dbAsset.getAddress())){
            return failed(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        if(!asset.getTxHash().equalsIgnoreCase(dbAsset.getTxHash())){
            return failed(CmErrorCode.A10017);
        }
        if (asset.getChainId() != dbAsset.getChainId()) {
            return failed(CmErrorCode.ERROR_CHAIN_ASSET_NOT_MATCH);
        }
        ChainAsset chainAsset =assetService.getChainAsset(asset.getChainId(),dbAsset);
        BigDecimal initAsset = new BigDecimal(chainAsset.getInitNumber());
        BigDecimal inAsset = new BigDecimal(chainAsset.getInNumber());
        BigDecimal outAsset = new BigDecimal(chainAsset.getOutNumber());
        BigDecimal currentNumber =initAsset.add(inAsset).subtract(outAsset);
        double actual = currentNumber.divide(initAsset, 8, RoundingMode.HALF_DOWN).doubleValue();
        double config = Double.parseDouble(CmConstants.PARAM_MAP.get(CmConstants.ASSET_RECOVERY_RATE));
        if (actual < config) {
            return failed(CmErrorCode.ERROR_ASSET_RECOVERY_RATE);
        }
        return  success();
    }

    /**
     * 资产注销提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetDisableCommit", version = 1.0,
            description = "assetDisableCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetDisableCommit(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        String secondaryData = String.valueOf(params.get("secondaryData"));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        Response cmdResponse = assetDisableValidator(asset);
        if(cmdResponse.getResponseStatus() != Constants.SUCCESS_CODE){
            return cmdResponse;
        }
        assetService.setStatus(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()), false);
        return success();
    }

    /**
     *
     * 链注销回滚
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetDisableRollback", version = 1.0,description = "assetDisableRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetDisableRollback(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        String txHex = String.valueOf(params.get("txHex"));
        String secondaryData = String.valueOf(params.get("secondaryData"));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        /*判断资产是否可用*/
        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
        if(null == dbAsset || dbAsset.isAvailable()){
            return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        if(!dbAsset.getTxHash().equalsIgnoreCase(asset.getTxHash())){
           return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        assetService.setStatus(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()), true);
        return  success();
    }

    /**
     * 跨链流通校验
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetCirculateValidator", version = 1.0,description = "assetCirculateValidator")
    @Parameter(parameterName = "fromChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "toChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "amount", parameterType = "String")
    public Response assetCirculateValidator(Map params) {
        //校验跨链交易上是否有该资产，并且资产金额是否充足。
        int fromChainId = Integer.valueOf(params.get("fromChainId").toString());
        int toChainId = Integer.valueOf(params.get("toChainId").toString());
        int assetId = Integer.valueOf(params.get("assetId").toString());
        int chainId = Integer.valueOf(params.get("chainId").toString());
        Chain fromChain = chainService.getChain(fromChainId);
        Chain toChain = chainService.getChain(toChainId);
        if(fromChain.isDelete()){
            Log.info("fromChain is delete,chainId="+fromChain.getChainId());
            return failed("fromChain is delete");
        }
        if(toChain.isDelete()){
            Log.info("toChain is delete,chainId="+fromChain.getChainId());
            return failed("toChain is delete");
        }
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId,assetId));
        if(null == asset || !asset.isAvailable()){
            return failed("asset is not exsit");
        }
        ChainAsset chainAsset =  assetService.getChainAsset(fromChainId,asset);
        if(null == chainAsset){
            return failed("from asset is not exsit");
        }
        BigDecimal currentAsset =new BigDecimal(chainAsset.getInitNumber()).add(new BigDecimal(chainAsset.getInNumber())).subtract(new BigDecimal(chainAsset.getOutNumber()));
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        if(currentAsset.doubleValue()>= amount.doubleValue()){
            return success();
        }
        return failed(CmErrorCode.Err10002);
    }

    /**
     * 跨链流通提交
     * @param params
     * @return
     */

    @CmdAnnotation(cmd = "cm_assetCirculateCommit", version = 1.0,description = "assetCirculateCommit")
    @Parameter(parameterName = "fromChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "toChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "amount", parameterType = "String")
    public Response assetCirculateCommit(Map params) {
        //A链转B链资产X，数量N ;A链X资产减少N, B链 X资产 增加N。
        int fromChainId = Integer.valueOf(params.get("fromChainId").toString());
        int toChainId = Integer.valueOf(params.get("toChainId").toString());
        int assetId = Integer.valueOf(params.get("assetId").toString());
        int chainId = Integer.valueOf(params.get("chainId").toString());
        Chain fromChain = chainService.getChain(fromChainId);
        Chain toChain = chainService.getChain(toChainId);
        if(fromChain.isDelete()){
            Log.info("fromChain is delete,chainId="+fromChain.getChainId());
            return failed("fromChain is delete");
        }
        if(toChain.isDelete()){
            Log.info("toChain is delete,chainId="+fromChain.getChainId());
            return failed("toChain is delete");
        }
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId,assetId));
        if(null == asset || !asset.isAvailable()){
            return failed("asset is not exsit");
        }
        ChainAsset fromChainAsset =  assetService.getChainAsset(fromChainId,asset);
        ChainAsset toChainAsset =  assetService.getChainAsset(toChainId,asset);
        if(null == fromChainAsset){
            return failed("from asset is not exsit");
        }
        BigDecimal currentAsset = new BigDecimal(fromChainAsset.getInitNumber()).add(new BigDecimal(fromChainAsset.getInNumber())).subtract(new BigDecimal(fromChainAsset.getOutNumber()));
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        if(currentAsset.doubleValue() >=  amount.doubleValue()){
            BigDecimal out =  new BigDecimal(fromChainAsset.getOutNumber()).add(amount);
            fromChainAsset.setOutNumber(String.valueOf(out.doubleValue()));
            if(null == toChainAsset){
                //链下加资产，资产下增加链
                toChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
                asset.addChainId(toChainId);
                chainService.updateChain(toChain);
                assetService.updateAsset(asset);
                //更新资产
                toChainAsset = new ChainAsset();
                toChainAsset.setChainId(asset.getChainId());
                toChainAsset.setAssetId(asset.getAssetId());
                toChainAsset.setInNumber(String.valueOf(amount.doubleValue()));
            }else{
                BigDecimal inAsset = new BigDecimal(toChainAsset.getInNumber());
                String inNumberStr = String.valueOf(inAsset.add(amount).doubleValue());
                toChainAsset.setInNumber(inNumberStr);
            }
            assetService.saveOrUpdateChainAsset(fromChainId,fromChainAsset);
            assetService.saveOrUpdateChainAsset(toChainId,toChainAsset);
            return success();
        }
        return failed(CmErrorCode.Err10002);
    }

    /**
     * 跨链流通回滚
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetCirculateRollBack", version = 1.0,description = "assetCirculateRollBack")
    @Parameter(parameterName = "fromChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "toChainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "amount", parameterType = "String")
    public Response assetCirculateRollBack(Map params) {
        //交易回滚，from的加，to的减
        int fromChainId = Integer.valueOf(params.get("fromChainId").toString());
        int toChainId = Integer.valueOf(params.get("toChainId").toString());
        int assetId = Integer.valueOf(params.get("assetId").toString());
        int chainId = Integer.valueOf(params.get("chainId").toString());
        Chain fromChain = chainService.getChain(fromChainId);
        Chain toChain = chainService.getChain(toChainId);
        if(null == fromChain){
            Log.info("fromChain is delete,chainId="+fromChain.getChainId());
            return failed("fromChain is delete");
        }
        if(null == toChain){
            Log.info("toChain is delete,chainId="+fromChain.getChainId());
            return failed("toChain is delete");
        }
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId,assetId));
        if(null == asset){
            return failed("asset is not exsit");
        }
        ChainAsset fromChainAsset =  assetService.getChainAsset(fromChainId,asset);
        ChainAsset toChainAsset =  assetService.getChainAsset(toChainId,asset);
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
            BigDecimal out =  new BigDecimal(fromChainAsset.getOutNumber()).subtract(amount);
            fromChainAsset.setOutNumber(String.valueOf(out.doubleValue()));
            BigDecimal inAsset = new BigDecimal(toChainAsset.getInNumber());
            String inNumberStr = String.valueOf(inAsset.subtract(amount).doubleValue());
            toChainAsset.setInNumber(inNumberStr);
            assetService.saveOrUpdateChainAsset(fromChainId,fromChainAsset);
            assetService.saveOrUpdateChainAsset(toChainId,toChainAsset);
            return success();
    }
}
