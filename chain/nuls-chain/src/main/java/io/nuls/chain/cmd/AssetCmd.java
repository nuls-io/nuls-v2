package io.nuls.chain.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.model.tx.AddAssetToChainTransaction;
import io.nuls.chain.model.tx.DestroyAssetAndChainTransaction;
import io.nuls.chain.model.tx.RemoveAssetFromChainTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.SeqService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetCmd extends BaseChainCmd {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private RpcService rpcService;
    @Autowired
    private SeqService seqService;

    @CmdAnnotation(cmd = "cm_asset", version = 1.0,
            description = "asset")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    public Response asset(Map params) {
        int chainId = Integer.valueOf(params.get("chainId").toString());
        int assetId = Integer.valueOf(params.get("assetId").toString());
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId, assetId));
        return success(asset);
    }

    /**
     * 资产注册
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "cm_assetReg", version = 1.0,
            description = "assetReg")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "symbol", parameterType = "array")
    @Parameter(parameterName = "name", parameterType = "String")
    @Parameter(parameterName = "initNumber", parameterType = "String")
    @Parameter(parameterName = "decimalPlaces", parameterType = "short", parameterValidRange = "[1,128]", parameterValidRegExp = "")
    @Parameter(parameterName = "address", parameterType = "String")
    public Response assetReg(Map params) {
        int chainId = Integer.valueOf(params.get("chainId").toString());
        int assetId = seqService.createAssetId(chainId);
        Asset asset = new Asset(assetId);
        asset.setChainId(chainId);
        asset.setSymbol((String) params.get("symbol"));
        asset.setName((String) params.get("name"));
        asset.setDepositNuls(Integer.valueOf(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS)));
        asset.setInitNumber(new BigInteger(params.get("initNumber").toString()));
        asset.setDecimalPlaces(Short.valueOf(params.get("decimalPlaces").toString()));
        asset.setAvailable(true);
        asset.setCreateTime(TimeService.currentTimeMillis());
        asset.setAddress(AddressTool.getAddress(String.valueOf(params.get("address"))));
        if (assetService.assetExist(asset)) {
            return failed(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        // 组装交易发送
        AddAssetToChainTransaction assetRegTransaction = new AddAssetToChainTransaction();
        try {
            assetRegTransaction.setTxData(asset.parseToTransaction());
            AccountBalance accountBalance = rpcService.getCoinData(asset.getChainId(), asset.getAssetId(), String.valueOf(params.get("address")));
            CoinData coinData = this.getRegCoinData(asset.getAddress(), asset.getChainId(),
                    asset.getAssetId(), String.valueOf(asset.getDepositNuls()), assetRegTransaction.size(), accountBalance);
            assetRegTransaction.setCoinData(coinData.serialize());
            //todo 交易签名
        } catch (IOException e) {
            e.printStackTrace();
            return failed("parseToTransaction fail");
        }

        boolean rpcReslt = rpcService.newTx(assetRegTransaction);
        if (rpcReslt) {
            return success("sent asset newTx");
        } else {
            return failed("sent asset fail");
        }
    }

    @CmdAnnotation(cmd = "cm_assetDisable", version = 1.0, description = "assetDisable")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "address", parameterType = "String")
    public Response assetDisable(Map params) {
        int chainId = Integer.valueOf(params.get("chainId").toString());
        int assetId = Integer.valueOf(params.get("assetId").toString());
        byte[] address = AddressTool.getAddress(params.get("address").toString());
        //身份的校验，账户地址的校验
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId, assetId));
        if (asset == null) {
            return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        if (!ByteUtils.arrayEquals(asset.getAddress(), address)) {
            return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        /*
          判断链下是否只有这一个资产了，如果是，则进行带链注销交易
         */
        BlockChain dbChain = chainService.getChain(chainId);
        Transaction transaction = null;
        if (dbChain.getSelfAssetKeyList().size() == 1
                && dbChain.getSelfAssetKeyList().get(0).equals(CmRuntimeInfo.getAssetKey(chainId, asset.getAssetId()))) {
            //带链注销
            transaction = new DestroyAssetAndChainTransaction();
            try {
                transaction.setTxData(dbChain.parseToTransaction(asset));
            } catch (IOException e) {
                e.printStackTrace();
                return failed("parseToTransaction fail");
            }
        } else {
            //只走资产注销
            transaction = new RemoveAssetFromChainTransaction();
            try {
                transaction.setTxData(asset.parseToTransaction());
            } catch (IOException e) {
                e.printStackTrace();
                return failed("parseToTransaction fail");
            }
        }
        try {
            AccountBalance accountBalance = rpcService.getCoinData(asset.getChainId(), asset.getAssetId(), String.valueOf(params.get("address")));
            if (null == accountBalance) {
                return failed("get  rpc CoinData fail.");
            }
            CoinData coinData = this.getDisableCoinData(address, chainId, assetId, String.valueOf(asset.getDepositNuls()), transaction.size(), dbChain.getRegTxHash(), accountBalance);
            transaction.setCoinData(coinData.serialize());
            //TODO:交易签名
            boolean rpcReslt = rpcService.newTx(transaction);
            if (rpcReslt) {
                return success(asset);
            } else {
                return failed("sent tx fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failed("parseToTransaction fail");
        }

    }

}
