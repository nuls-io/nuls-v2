package io.nuls.chain.cmd;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.txdata.Asset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetCmd extends BaseCmd {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;

    @CmdAnnotation(cmd = "asset", version = 1.0, preCompatible = true)
    public CmdResponse asset(List params) {
        Asset asset = assetService.getAsset(Long.valueOf(params.get(0).toString()));
        return success("success", asset);
    }

    @CmdAnnotation(cmd = "assetReg", version = 1.0, preCompatible = true)
    public CmdResponse assetReg(List params) {

        Asset asset = new Asset();
        asset.setChainId(Short.valueOf(params.get(0).toString()));
        asset.setAssetId(TimeService.currentTimeMillis());
        asset.setSymbol((String) params.get(1));
        asset.setName((String) params.get(2));
        asset.setDepositNuls((int) params.get(3));
        asset.setInitNumber(Long.valueOf(params.get(4).toString()));
        asset.setDecimalPlaces(Short.valueOf(params.get(5).toString()));
        asset.setAvailable((boolean) params.get(6));
        asset.setCreateTime(TimeService.currentTimeMillis());

        // TODO
        return success("sent newTx", asset);
    }

    @CmdAnnotation(cmd = "assetRegValidator", version = 1.0, preCompatible = true)
    public CmdResponse assetRegValidator(List params) {
        Asset asset = null;
        try {
            asset = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Asset.class);
        } catch (IOException e) {
            Log.error(e);
            return failed(CmConstants.ERROR_JSON_TO_ASSET);
        }

        Map<String, String> errMap = new HashMap<>();
        errMap.putAll(assetService.basicValidator(asset));
        errMap.putAll(assetService.uniqueValidator(asset));

        return errMap.size() == 0 ? success() : failed(ErrorCode.init("Validator error"), errMap);
    }

    @CmdAnnotation(cmd = "assetRegCommit", version = 1.0, preCompatible = true)
    public CmdResponse assetRegCommit(List params) {
        Asset asset = null;
        try {
            asset = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Asset.class);
        } catch (IOException e) {
            Log.error(e);
            return failed(CmConstants.ERROR_JSON_TO_ASSET);
        }

        return assetService.newAsset(asset) ? success() : failed("");
    }

    @CmdAnnotation(cmd = "assetRegRollback", version = 1.0, preCompatible = true)
    public CmdResponse assetRegRollback(List params) {
        return success();
    }

    @CmdAnnotation(cmd = "assetEnable", version = 1.0, preCompatible = true)
    public CmdResponse assetEnable(List params) {
        assetService.setStatus(Long.valueOf(params.get(0).toString()), true);
        return success();
    }

    @CmdAnnotation(cmd = "assetDisable", version = 1.0, preCompatible = true)
    public CmdResponse assetDisable(List params) {
        //TODO

        return success();
    }

    @CmdAnnotation(cmd = "assetDisableValidator", version = 1.0, preCompatible = true)
    public CmdResponse assetDisableValidator(List params) {
        short chainId = Short.valueOf(params.get(0).toString());
        long assetId = Long.valueOf(params.get(1).toString());

        Asset asset = assetService.getAsset(assetId);
        ChainAsset chainAsset = chainService.getChainAsset(chainId, assetId);
        if (asset == null || chainAsset == null) {
            return failed(CmConstants.ERROR_ASSET_NOT_EXIST);
        }

        if (chainId != asset.getChainId()) {
            return failed(CmConstants.ERROR_CHAIN_ASSET_NOT_MATCH);
        }

        BigDecimal initNumber = new BigDecimal(asset.getInitNumber());
        BigDecimal currentNumber = new BigDecimal(chainAsset.getCurrentNumber());
        double actual = currentNumber.divide(initNumber, 8, RoundingMode.HALF_DOWN).doubleValue();
        double config = Double.parseDouble(CmConstants.PARAM_MAP.get(CmConstants.ASSET_RECOVERY_RATE));
        if (actual < config) {
            return failed(CmConstants.ERROR_ASSET_RECOVERY_RATE);
        }

        return assetService.setStatus(assetId, false) ? success() : failed(CmConstants.ERROR_ASSET_RECOVERY_RATE);
    }

    @CmdAnnotation(cmd = "assetDisableCommit", version = 1.0, preCompatible = true)
    public CmdResponse assetDisableCommit(List params) {
        assetService.setStatus(Long.valueOf(params.get(0).toString()), false);
        return success();
    }
}
