package io.nuls.chain.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.dto.CoinDataAssets;
import io.nuls.chain.model.tx.AddAssetToChainTransaction;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author lan
 * @date 2018/11/21
 * @description
 */
@Component
public class TxAssetCmd extends BaseChainCmd {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;

    private Asset buildAssetTxData(String txHex, Transaction tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(),0);
            Asset asset = new Asset(txChain);
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
            Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());

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
            if(isSuccess(cmdResponse)){
                return cmdResponse;
            }
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());
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
        Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());
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
        Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());
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
        Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());
        Response cmdResponse = assetDisableValidator(asset);
        if(isSuccess(cmdResponse)){
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
        Asset  asset = buildAssetTxData(txHex,new AddAssetToChainTransaction());
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


    private List<CoinDataAssets> getChainAssetList(Map params) throws NulsException {
        List<CoinDataAssets> list = new ArrayList<>();
        String coinDataHex=String.valueOf(params.get("coinDatas"));
        byte []coinDataByte = HexUtil.hexToByte(coinDataHex);
        CoinData coinData = new CoinData();
        int fromChainId = 0;
        int toChainId = 0;
        Map<String,String> fromAssetMap = new HashMap<>();
        Map<String,String> toAssetMap = new HashMap<>();
        coinData.parse(coinDataByte,0);
        //from 资产封装
        List<CoinFrom> listFrom = coinData.getFrom();
        for(CoinFrom coinFrom:listFrom){
            fromChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            int assetChainId =  coinFrom.getAssetsChainId();
            int assetId =  coinFrom.getAssetsId();
            String asssetKey = CmRuntimeInfo.getAssetKey(assetChainId,assetId);
            BigDecimal amount = new BigDecimal( coinFrom.getAmount());
            if(null != fromAssetMap.get(asssetKey)){
                amount = new BigDecimal(fromAssetMap.get(asssetKey)).add(amount);
            }
            fromAssetMap.put(asssetKey,amount.toString());
        }
        //to资产封装
        List<CoinTo> listTo = coinData.getTo();
        for(CoinTo coinTo:listTo){
            toChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            int assetChainId =  coinTo.getAssetsChainId();
            int assetId =  coinTo.getAssetsId();
            String asssetKey = CmRuntimeInfo.getAssetKey(assetChainId,assetId);
            BigDecimal amount = new BigDecimal( coinTo.getAmount());
            if(null != toAssetMap.get(asssetKey)){
                amount = new BigDecimal(toAssetMap.get(asssetKey)).add(amount);
            }
            toAssetMap.put(asssetKey,amount.toString());
        }
        CoinDataAssets fromCoinDataAssets = new CoinDataAssets();
        fromCoinDataAssets.setChainId(fromChainId);
        fromCoinDataAssets.setAssetsMap(fromAssetMap);
        list.add(fromCoinDataAssets);
        CoinDataAssets toCoinDataAssets = new CoinDataAssets();
        toCoinDataAssets.setChainId(toChainId);
        toCoinDataAssets.setAssetsMap(toAssetMap);
        list.add(toCoinDataAssets);
        return list;

    }


    Response assetCirculateValidator(int fromChainId,int toChainId,Map<String,String> fromAssetMap,Map<String,String> toAssetMap) {
        Chain fromChain = chainService.getChain(fromChainId);
        Chain toChain = chainService.getChain(toChainId);
        if(fromChain == toChain){
            Log.error("fromChain ==  toChain is not cross tx" +fromChain);
            return failed("fromChain ==  toChain is not cross tx");
        }
        if (fromChainId!=0 && fromChain.isDelete()) {
            Log.info("fromChain is delete,chainId=" + fromChain.getChainId());
            return failed("fromChain is delete");
        }
        if (toChainId!=0 && toChain.isDelete()) {
            Log.info("toChain is delete,chainId=" + fromChain.getChainId());
            return failed("toChain is delete");
        }
        //获取链内 资产 状态是否正常
        Set<String> toAssets = toAssetMap.keySet();
        Iterator itTo = toAssets.iterator();
        while (itTo.hasNext()) {
            String assetKey = itTo.next().toString();
            Asset asset = assetService.getAsset(assetKey);
            if (null == asset || !asset.isAvailable()) {
                return failed("asset is not exsit");
            }
        }
        //校验from 资产是否足够
        Set<String> fromAssets = fromAssetMap.keySet();
        Iterator itFrom = fromAssets.iterator();
        while (itFrom.hasNext()) {
            String assetKey = itFrom.next().toString();
            Asset asset = assetService.getAsset(assetKey);
            if (null == asset || !asset.isAvailable()) {
                return failed("asset is not exsit");
            }
            ChainAsset chainAsset = assetService.getChainAsset(fromChainId, asset);
            BigDecimal currentAsset = new BigDecimal(chainAsset.getInitNumber()).add(new BigDecimal(chainAsset.getInNumber())).subtract(new BigDecimal(chainAsset.getOutNumber()));
            if (currentAsset.subtract(new BigDecimal(fromAssetMap.get(assetKey))).doubleValue() < 0) {
                return failed("asset is not enough");
            }
        }
        return success();
    }
    /**
     * 跨链流通校验
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetCirculateValidator", version = 1.0,description = "assetCirculateValidator")
    @Parameter(parameterName = "coinDatas", parameterType = "String")
    public Response assetCirculateValidator(Map params) {
        //提取 从哪条链 转 哪条链，是否是跨链，链 手续费共多少？
        try{
            List<CoinDataAssets> list = getChainAssetList(params);
            CoinDataAssets fromCoinDataAssets = list.get(0);
            CoinDataAssets toCoinDataAssets = list.get(1);
            int fromChainId = fromCoinDataAssets.getChainId();
            int toChainId = toCoinDataAssets.getChainId();
            Map<String,String> fromAssetMap = fromCoinDataAssets.getAssetsMap();
            Map<String,String> toAssetMap = toCoinDataAssets.getAssetsMap();
            return assetCirculateValidator(fromChainId,toChainId,fromAssetMap,toAssetMap);
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return failed(CmErrorCode.Err10002);
    }

    /**
     * 跨链流通提交
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetCirculateCommit", version = 1.0,description = "assetCirculateCommit")
    @Parameter(parameterName = "coinDatas", parameterType = "String")
    public Response assetCirculateCommit(Map params) {
        //A链转B链资产X，数量N ;A链X资产减少N, B链 X资产 增加N。
        try{
            List<CoinDataAssets> list = getChainAssetList(params);
            CoinDataAssets fromCoinDataAssets = list.get(0);
            CoinDataAssets toCoinDataAssets = list.get(1);
            int fromChainId = fromCoinDataAssets.getChainId();
            int toChainId = toCoinDataAssets.getChainId();
            Map<String,String> fromAssetMap = fromCoinDataAssets.getAssetsMap();
            Map<String,String> toAssetMap = toCoinDataAssets.getAssetsMap();
            Response response =  assetCirculateValidator(fromChainId,toChainId,fromAssetMap,toAssetMap);
            if(!isSuccess(response)){
                return response;
            }
            //from 的处理
            Set<String> assetKeys = fromAssetMap.keySet();
            Iterator<String> assetKeysIt = assetKeys.iterator();
            while(assetKeysIt.hasNext()){
                String assetKey =  assetKeysIt.next();
                ChainAsset fromChainAsset = assetService.getChainAsset(fromChainId, assetKey);
                BigDecimal currentAsset = new BigDecimal(fromChainAsset.getOutNumber()).add(new BigDecimal(fromAssetMap.get(assetKey)));
                fromChainAsset.setOutNumber(currentAsset.toString());
                assetService.saveOrUpdateChainAsset(fromChainId,fromChainAsset);
            }
            if(isMainChain(toChainId)){
                //toChainId == nuls chain  不需要进行跨外链的 手续费在coinBase里增加。
            }else{
               //提取toChainId的 手续费资产，如果存将手续费放入外链给的回执，也取消 外链手续费增加。
               String mainAssetKey =CmRuntimeInfo.getMainAsset();
                String allFromMainAmount = fromAssetMap.get(mainAssetKey);
                String allToMainAmount = toAssetMap.get(mainAssetKey);
                BigDecimal feeAmount = new BigDecimal(allFromMainAmount).subtract(new BigDecimal(allToMainAmount)).multiply(BigDecimal.valueOf(0.4));
                if(null!= toAssetMap.get(mainAssetKey)){
                    feeAmount = feeAmount.add(new BigDecimal(toAssetMap.get(mainAssetKey)));
                }
                toAssetMap.put(mainAssetKey,feeAmount.toString());
            }
            //to 的处理
            Set<String> toAssetKeys = toAssetMap.keySet();
            Iterator<String> toAssetKeysIt = toAssetKeys.iterator();
            while(toAssetKeysIt.hasNext()){
               String toAssetKey =  toAssetKeysIt.next();
               ChainAsset  toChainAsset =  assetService.getChainAsset(toChainId,toAssetKey);
                if(null == toChainAsset){
//                //链下加资产，资产下增加链
                    Chain toChain = chainService.getChain(toChainId);
                    Asset asset = assetService.getAsset(CmRuntimeInfo.getMainAsset());
                    toChain.addCirculateAssetId(CmRuntimeInfo.getMainAsset());
                    asset.addChainId(toChainId);
                    chainService.updateChain(toChain);
                    assetService.updateAsset(asset);
                    //更新资产
                    toChainAsset = new ChainAsset();
                    toChainAsset.setChainId(asset.getChainId());
                    toChainAsset.setAssetId(asset.getAssetId());
                    toChainAsset.setInNumber(toAssetMap.get(toAssetKey));
                }else{
                    BigDecimal inAsset = new BigDecimal(toChainAsset.getInNumber());
                    BigDecimal inNumberBigDec =  new BigDecimal(toAssetMap.get(toAssetKey)).add(inAsset);
                    toChainAsset.setInNumber(inNumberBigDec.toString());
                }
                assetService.saveOrUpdateChainAsset(toChainId,toChainAsset);
            }
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return failed(CmErrorCode.Err10002);
    }

    /**
     * 跨链流通回滚
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetCirculateRollBack", version = 1.0,description = "assetCirculateRollBack")
    @Parameter(parameterName = "coinDatas", parameterType = "String")
    public Response assetCirculateRollBack(Map params) {
        //交易回滚，from的加，to的减
        //TODO:
       return success();
    }
}
