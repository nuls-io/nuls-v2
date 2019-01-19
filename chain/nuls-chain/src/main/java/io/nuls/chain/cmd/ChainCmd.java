package io.nuls.chain.cmd;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
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
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

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
    private RpcService rpcService;

    @Autowired
    private SeqService seqService;

    @CmdAnnotation(cmd = "cm_chain", version = 1.0, description = "get chain detail")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response chain(Map params) {
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            BlockChain blockChain = chainService.getChain(chainId);
            if (blockChain == null) {
                return failed(ErrorCode.init("C10003"));
            }
            return success(blockChain);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainReg", version = 1.0, description = "chainReg")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "chainName", parameterType = "String")
    @Parameter(parameterName = "addressType", parameterType = "String")
    @Parameter(parameterName = "magicNumber", parameterType = "long", parameterValidRange = "[1,4294967295]")
    @Parameter(parameterName = "supportInflowAsset", parameterType = "String")
    @Parameter(parameterName = "minAvailableNodeNum", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "singleNodeMinConnectionNum", parameterType = "int")
    @Parameter(parameterName = "txConfirmedBlockNum", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "symbol", parameterType = "array")
    @Parameter(parameterName = "assetName", parameterType = "String")
    @Parameter(parameterName = "initNumber", parameterType = "String")
    @Parameter(parameterName = "decimalPlaces", parameterType = "short", parameterValidRange = "[1,128]")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response chainReg(Map params) {
        try {
            /*TODO:入参校验*/

            /*判断链与资产是否已经存在*/

            /* 组装BlockChain (BlockChain object) */
            BlockChain blockChain = JSONUtils.map2pojo(params, BlockChain.class);
            blockChain.setRegAddress(AddressTool.getAddress(String.valueOf(params.get("address"))));
            blockChain.setCreateTime(TimeService.currentTimeMillis());

            /* 组装Asset (Asset object) */
            /* 取消int assetId = seqService.createAssetId(blockChain.getChainId());*/
            Asset asset = JSONUtils.map2pojo(params, Asset.class);
            asset.setChainId(blockChain.getChainId());
            asset.setDepositNuls(Integer.valueOf(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSIT_NULS)));
            asset.setAvailable(true);
            asset.setCreateTime(TimeService.currentTimeMillis());
            asset.setAddress(blockChain.getRegAddress());

            /* 组装交易发送 (Send transaction) */
            Transaction tx = new RegisterChainAndAssetTransaction();
            tx.setTxData(blockChain.parseToTransaction(asset));
            tx.setTime(TimeService.currentTimeMillis());
            AccountBalance accountBalance = rpcService.getCoinData(String.valueOf(params.get("address")));
            CoinData coinData = super.getRegCoinData(asset.getAddress(), asset.getChainId(),
                    asset.getAssetId(), String.valueOf(asset.getDepositNuls()), tx.size(), accountBalance);
            tx.setCoinData(coinData.serialize());

            /* 判断签名是否正确 (Determine if the signature is correct) */
            tx = signDigest(asset.getChainId(), (String) params.get("address"), (String) params.get("password"), tx);

            /* 发送到交易模块 (Send to transaction module) */
            return rpcService.newTx(tx) ? success(blockChain) : failed("Register chain failed");
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
