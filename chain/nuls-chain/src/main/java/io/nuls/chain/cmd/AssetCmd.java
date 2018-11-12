package io.nuls.chain.cmd;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.service.AssetService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetCmd extends BaseCmd {

    @Autowired
    private AssetService assetService;

    @CmdAnnotation(cmd = "asset", version = 1.0, preCompatible = true)
    public CmdResponse asset(List params) {
        try {

            Asset asset = assetService.getAsset(Short.valueOf(params.get(0).toString()), Short.valueOf(params.get(1).toString()));
            return success("success", asset);

        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "assetReg", version = 1.0, preCompatible = true)
    public CmdResponse assetReg(List params) {
        try {
            Asset asset = new Asset();
            asset.setChainId(Short.valueOf(params.get(0).toString()));
            asset.setAssetId(Short.valueOf(params.get(1).toString()));
            asset.setSymbol((String) params.get(2));
            asset.setName((String) params.get(3));
            asset.setDepositNuls((int) params.get(4));
            asset.setInitNumber(Long.valueOf(params.get(5).toString()));
            asset.setCurrentNumber(asset.getInitNumber());
            asset.setDecimalPlaces(Short.valueOf(params.get(6).toString()));
            asset.setAvailable((boolean) params.get(7));
            asset.setCreateTime(TimeService.currentTimeMillis());

            assetService.saveAsset(asset);

            return success("success", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "assetEnable", version = 1.0, preCompatible = true)
    public CmdResponse assetEnable(List params) {
        try {

            assetService.setStatus(Short.valueOf(params.get(0).toString()), Short.valueOf(params.get(1).toString()), true);

            return success("success", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "assetDisable", version = 1.0, preCompatible = true)
    public CmdResponse assetDisable(List params) {
        try {

            assetService.setStatus(Short.valueOf(params.get(0).toString()), Short.valueOf(params.get(1).toString()), false);

            return success("success", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "assetCurrNumOfChain", version = 1.0, preCompatible = true)
    public CmdResponse assetCurrNumOfChain(List params) {
        try {

            assetService.setCurrentNumber(Short.valueOf(params.get(0).toString()), Short.valueOf(params.get(1).toString()), Long.valueOf(params.get(2).toString()));

            return success("success", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "assetList", version = 1.0, preCompatible = true)
    public CmdResponse assetList(List params) {
        try {

            List<Asset> assetList = assetService.getAssetListByChain(Short.valueOf(params.get(0).toString()));
            return success("success", assetList);

        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }
}
