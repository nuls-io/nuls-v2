package io.nuls.chain.cmd;

import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.tx.AssetRegTransaction;
import io.nuls.chain.model.tx.txdata.AssetTx;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    private Asset buildAssetTxData(String txHex, Transaction<AssetTx> tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            Asset asset = new Asset(tx.getTxData());
            asset.setTxHash(tx.getHash().toString());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
    public CmdResponse assetRegValidator(List params) {
        try {
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
            Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
            if (asset.getChainId() < 0) {
                return failed("10015");
            }
            if(assetService.assetExist(asset))
            {
                return failed("A10005");
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }


    }


    public CmdResponse assetRegCommit(List params) {
        try {
            CmdResponse cmdResponse = assetRegValidator(params);
            if(!cmdResponse.getCode().equalsIgnoreCase(Constants.SUCCESS_CODE)){
                return cmdResponse;
            }
            int chainId = Integer.valueOf(String.valueOf(params.get(0)));
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
            //TODO:提交asset 以及与chain的关联
            Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
            assetService.addAsset(asset);
        } catch (Exception e) {
            Log.error(e);
            return failed("C10003");
        }

        return success();
    }

    public CmdResponse assetRegRollback(List params) {
        String txHex = String.valueOf(params.get(1));
        String secondaryData = String.valueOf(params.get(2));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        //判断库中的asset是否存在，数据正确，则删除
        Asset dbAsset = assetService.getAsset(asset.getAssetId());
        if(!ByteUtils.arrayEquals(asset.getAddress(),dbAsset.getAddress())){
            return failed("Address Error");
        }
        if(null != dbAsset && dbAsset.getTxHash().equalsIgnoreCase(asset.getTxHash())){
            assetService.setStatus(dbAsset.getAssetId(),false);
            return success();
        }
        return failed(CmConstants.ERROR_ASSET_NOT_EXIST);
    }

    public CmdResponse assetDisableValidator(List params) {
        String txHex = String.valueOf(params.get(1));
        String secondaryData = String.valueOf(params.get(2));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        return assetDisableValidator(asset);
    }
    private CmdResponse assetDisableValidator(Asset asset) {

        Asset dbAsset = assetService.getAsset(asset.getAssetId());
        ChainAsset chainAsset = chainService.getChainAsset(asset.getChainId(), asset.getAssetId());
        if (asset == null || chainAsset == null) {
            return failed(CmConstants.ERROR_ASSET_NOT_EXIST);
        }
        if(!ByteUtils.arrayEquals(asset.getAddress(),dbAsset.getAddress())){
            return failed("Address Error");
        }
        if(!asset.getTxHash().equalsIgnoreCase(dbAsset.getTxHash())){
            return failed("txHash Error");
        }
        if (asset.getChainId() != asset.getChainId()) {
            return failed(CmConstants.ERROR_CHAIN_ASSET_NOT_MATCH);
        }

        BigDecimal initNumber = new BigDecimal(asset.getInitNumber());
        BigDecimal currentNumber = new BigDecimal(chainAsset.getCurrentNumber());
        double actual = currentNumber.divide(initNumber, 8, RoundingMode.HALF_DOWN).doubleValue();
        double config = Double.parseDouble(CmConstants.PARAM_MAP.get(CmConstants.ASSET_RECOVERY_RATE));
        if (actual < config) {
            return failed(CmConstants.ERROR_ASSET_RECOVERY_RATE);
        }

        return  success();
    }

    public CmdResponse assetDisableCommit(List params) {
        String txHex = String.valueOf(params.get(1));
        String secondaryData = String.valueOf(params.get(2));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        CmdResponse cmdResponse = assetDisableValidator(asset);
        if(!cmdResponse.getCode().equalsIgnoreCase(Constants.SUCCESS_CODE)){
            return cmdResponse;
        }
        assetService.setStatus(asset.getAssetId(), false);
        return success();
    }

    public CmdResponse assetDisableRollback(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        String txHex = String.valueOf(params.get(1));
        String secondaryData = String.valueOf(params.get(2));
        Asset  asset = buildAssetTxData(txHex,new AssetRegTransaction());
        /*判断资产是否可用*/
        Asset dbAsset = assetService.getAsset(asset.getAssetId());
        if(null == dbAsset || dbAsset.isAvailable()){
            return failed("txHash Error");
        }
        if(!dbAsset.getTxHash().equalsIgnoreCase(asset.getTxHash())){
           return failed(CmConstants.ERROR_ASSET_NOT_EXIST);
        }
        assetService.setStatus(asset.getAssetId(), true);
        return  success();
    }

    /**
     * 跨链流通校验
     * @param params
     * @return
     */

    public CmdResponse assetCirculateValidator(List params) {
        //TODO:校验跨链交易上是否有该资产，并且资产金额是否充足。
        return null;
    }

    /**
     * 跨链流通提交
     * @param params
     * @return
     */


    public CmdResponse assetCirculateCommit(List params) {
        //TODO:A链转B链资产X，数量N ;A链X资产减少N, B链 X资产 增加N。

        return null;
    }

    /**
     * 跨链流通回滚
     * @param params
     * @return
     */

    public CmdResponse assetCirculateRollBack(List params) {
        //TODO:
        return null;
    }
}
