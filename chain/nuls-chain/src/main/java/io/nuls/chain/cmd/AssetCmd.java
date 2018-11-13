package io.nuls.chain.cmd;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.service.AssetService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
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
            return failed("A10003");
        }

        if (asset.getSymbol().length() > 5) {
            return failed("A10001");
        }
        if (assetService.getAsset(asset.getAssetId()) != null) {
            return failed("A10005");
        }
        if (assetService.getAssetBySymbol(asset.getSymbol()) != null) {
            return failed("A10002");
        }


        return success();
    }

    @CmdAnnotation(cmd = "assetRegCommit", version = 1.0, preCompatible = true)
    public CmdResponse assetRegCommit(List params) {
        Asset asset = null;
        try {
            asset = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Asset.class);
        } catch (IOException e) {
            Log.error(e);
            return failed("A10003");
        }

        return assetService.newAsset(asset) ? success() : failed("");
    }

    @CmdAnnotation(cmd = "assetRegRollback", version = 1.0, preCompatible = true)
    public CmdResponse assetRegRollback(List params) {
        return success();
    }

    @CmdAnnotation(cmd = "assetEnable", version = 1.0, preCompatible = true)
    public CmdResponse assetEnable(List params) {
        return assetService.setStatus(Long.valueOf(params.get(0).toString()), true) ? success() : failed("A10004");
    }

    @CmdAnnotation(cmd = "assetDisable", version = 1.0, preCompatible = true)
    public CmdResponse assetDisable(List params) {
        return assetService.setStatus(Long.valueOf(params.get(0).toString()), false) ? success() : failed("A10004");
    }
}
