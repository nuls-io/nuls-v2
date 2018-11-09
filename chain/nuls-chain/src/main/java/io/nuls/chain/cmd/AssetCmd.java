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

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetCmd extends BaseCmd {

    @Autowired
    AssetService assetService;

    @CmdAnnotation(cmd = "getAsset", version = 1.0, preCompatible = true)
    public CmdResponse getAsset(List params) {
        try {
            if (params == null || params.get(0) == null) {
                return failed(ErrorCode.init("-100"), 1.0, "Need <asset id>");
            }

            Asset asset = assetService.getAssetById(Short.valueOf(params.get(0).toString()));
            return success(1.0, "success", asset);

        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), 1.0, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "registerAsset", version = 1.0, preCompatible = true)
    public CmdResponse registerAsset(List params) {
        try {

            Asset asset = new Asset();
            asset.setChainId(Short.valueOf(params.get(0).toString()));
            asset.setAssetId(Short.valueOf(params.get(1).toString()));
            asset.setSymbol((String) params.get(2));
            asset.setName((String) params.get(3));
            asset.setDepositNuls((int) params.get(4));
            asset.setInitCirculation(Long.valueOf(params.get(5).toString()));
            asset.setDecimalPlaces(Short.valueOf(params.get(6).toString()));
            asset.setAvailable((boolean) params.get(7));

            System.out.println(assetService.saveAsset(asset));

            return success(1.0, "success", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), 1.0, e.getMessage());
        }
    }
}
