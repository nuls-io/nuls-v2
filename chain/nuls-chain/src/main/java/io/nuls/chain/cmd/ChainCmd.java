package io.nuls.chain.cmd;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.SeqService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@Component
public class ChainCmd extends BaseChainCmd {

    @Autowired
    private ChainService chainService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private RpcService rpcService;
    @Autowired
    private SeqService seqService;

    @CmdAnnotation(cmd = "cm_chain", version = 1.0,
            description = "chain")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    public Response chain(Map params) {
        try {
            int chainId = Integer.valueOf(params.get("chainId").toString());
            BlockChain blockChain = chainService.getChain(chainId);
            if (blockChain == null) {
                return failed("C10003");
            }
            return success(blockChain);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"));
        }
    }

    @CmdAnnotation(cmd = "cm_chainReg", version = 1.0, description = "chainReg")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "name", parameterType = "String")
    @Parameter(parameterName = "addressType", parameterType = "String")
    @Parameter(parameterName = "magicNumber", parameterType = "long", parameterValidRange = "[1,4294967295]", parameterValidRegExp = "")
    @Parameter(parameterName = "supportInflowAsset", parameterType = "String")
    @Parameter(parameterName = "minAvailableNodeNum", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "singleNodeMinConnectionNum", parameterType = "int")
    @Parameter(parameterName = "txConfirmedBlockNum", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "symbol", parameterType = "array")
    @Parameter(parameterName = "name", parameterType = "String")
    @Parameter(parameterName = "initNumber", parameterType = "String")
    @Parameter(parameterName = "decimalPlaces", parameterType = "short", parameterValidRange = "[1,128]", parameterValidRegExp = "")
    public Response chainReg(Map params) {
        try {
            BlockChain blockChain = new BlockChain();
            blockChain.setChainId(Integer.valueOf(params.get("chainId").toString()));
            blockChain.setName((String) params.get("name"));
            blockChain.setAddressType((String) params.get("addressType"));
            blockChain.setMagicNumber(Long.valueOf(params.get("magicNumber").toString()));
            blockChain.setSupportInflowAsset(Boolean.valueOf(params.get("supportInflowAsset").toString()));
            blockChain.setMinAvailableNodeNum(Integer.valueOf(params.get("minAvailableNodeNum").toString()));
            blockChain.setSingleNodeMinConnectionNum(Integer.valueOf(params.get("singleNodeMinConnectionNum").toString()));
            blockChain.setTxConfirmedBlockNum(Integer.valueOf(params.get("txConfirmedBlockNum").toString()));
            blockChain.setRegAddress(AddressTool.getAddress(String.valueOf(params.get("address"))));
            blockChain.setCreateTime(TimeService.currentTimeMillis());
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (dbChain != null) {
                return failed("C10001");
            }
            int assetId = seqService.createAssetId(blockChain.getChainId());
            Asset asset = new Asset(assetId);
            asset.setChainId(blockChain.getChainId());
            asset.setSymbol((String) params.get("symbol"));
            asset.setName((String) params.get("name"));
            asset.setDepositNuls(Integer.valueOf(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS)));
            asset.setInitNumber(new BigInteger(params.get("initNumber").toString()));
            asset.setDecimalPlaces(Short.valueOf(params.get("decimalPlaces").toString()));
            asset.setAvailable(true);
            asset.setCreateTime(TimeService.currentTimeMillis());
            asset.setAddress(AddressTool.getAddress(String.valueOf(params.get("address"))));
            // 组装交易发送
            RegisterChainAndAssetTransaction crossChainRegTransaction = new RegisterChainAndAssetTransaction();
            crossChainRegTransaction.setTxData(blockChain.parseToTransaction(asset));
            crossChainRegTransaction.setTime(TimeService.currentTimeMillis());
            AccountBalance accountBalance = rpcService.getCoinData(asset.getChainId(), asset.getAssetId(), String.valueOf(params.get("address")));
            CoinData coinData = this.getRegCoinData(asset.getAddress(), asset.getChainId(),
                    asset.getAssetId(), String.valueOf(asset.getDepositNuls()), crossChainRegTransaction.size(), accountBalance);
            crossChainRegTransaction.setCoinData(coinData.serialize());
            //todo 交易签名
            boolean rpcReslt = rpcService.newTx(crossChainRegTransaction);
            if (rpcReslt) {
                return success(blockChain);
            } else {
                Log.error("RPC fail");
                return failed(new ErrorCode());
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"));
        }
    }

}
