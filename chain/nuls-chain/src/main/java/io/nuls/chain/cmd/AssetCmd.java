package io.nuls.chain.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.tx.AssetDisableTransaction;
import io.nuls.chain.model.tx.AssetRegTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
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
    @Autowired
    private ChainService chainService;
    @Autowired
    private RpcService rpcService;
    public CmdResponse asset(List params) {
        Asset asset = assetService.getAsset(Long.valueOf(params.get(0).toString()));
        return success("success", asset);
    }

    public CmdResponse assetReg(List params) {
        Asset asset = new Asset();
        asset.setChainId(Integer.valueOf(params.get(0).toString()));
        asset.setAssetId(TimeService.currentTimeMillis());
        asset.setSymbol((String) params.get(1));
        asset.setName((String) params.get(2));
        asset.setDepositNuls((int) params.get(3));
        asset.setInitNumber(Long.valueOf(params.get(4).toString()));
        asset.setDecimalPlaces(Short.valueOf(params.get(5).toString()));
        asset.setAvailable((boolean) params.get(6));
        asset.setCreateTime(TimeService.currentTimeMillis());
        if(assetService.assetExist(asset))
        {
            return failed("A10005");
        }
        // 组装交易发送
        AssetRegTransaction assetRegTransaction = new AssetRegTransaction();
        assetRegTransaction.setTxData(asset.parseToTransaction());
        //TODO:coindata 未封装
        boolean rpcReslt = rpcService.newTx(assetRegTransaction);
        if(rpcReslt) {
            return success("sent asset newTx", asset);
        }else{
            return failed(new ErrorCode(),asset);
        }
    }

    public CmdResponse assetDisable(List params) {
        long assetId = Long.valueOf(params.get(0).toString());
        byte []address = AddressTool.getAddress(params.get(1).toString());
        //身份的校验，账户地址的校验
        Asset asset = assetService.getAsset(assetId);
        if (asset == null) {
            return failed("A10014");
        }
        if(!ByteUtils.arrayEquals(asset.getAddress(),address)){
            return failed("A10014");
        }
        AssetDisableTransaction assetDisableTransaction = new AssetDisableTransaction();
        assetDisableTransaction.setTxData(asset.parseToTransaction());
        //TODO:coindata 未封装
        boolean rpcReslt = rpcService.newTx(assetDisableTransaction);
        if(rpcReslt) {
            return success("sent destroy chain newTx", asset);
        }else{
            return failed(new ErrorCode(),asset);
        }
    }

}
