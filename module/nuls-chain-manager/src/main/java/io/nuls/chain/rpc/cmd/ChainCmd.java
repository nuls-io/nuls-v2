package io.nuls.chain.rpc.cmd;


import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.RegChainDto;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.TimeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    NulsChainConfig nulsChainConfig;

    @CmdAnnotation(cmd = "cm_chain", version = 1.0, description = "get chain detail")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response chain(Map params) {
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            BlockChain blockChain = chainService.getChain(chainId);
            if (blockChain == null) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            } else {
                RegChainDto regChainDto = new RegChainDto();
                regChainDto.buildRegChainDto(blockChain);
                regChainDto.setSeeds(rpcService.getCrossChainSeeds());
                return success(regChainDto);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = "cm_chainReg", version = 1.0, description = "chainReg")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "chainName", parameterType = "String")
    @Parameter(parameterName = "addressType", parameterType = "String")
    @Parameter(parameterName = "magicNumber", parameterType = "long", parameterValidRange = "[1,4294967295]")
    @Parameter(parameterName = "minAvailableNodeNum", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txConfirmedBlockNum", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "symbol", parameterType = "array")
    @Parameter(parameterName = "assetName", parameterType = "String")
    @Parameter(parameterName = "initNumber", parameterType = "String")
    @Parameter(parameterName = "decimalPlaces", parameterType = "short", parameterValidRange = "[1,128]")
    @Parameter(parameterName = "password", parameterType = "String")
    public Response chainReg(Map params) {
        /* 发送到交易模块 (Send to transaction module) */
        Map<String, String> rtMap = new HashMap<>(1);
        try {
            /*判断链与资产是否已经存在*/
            /* 组装BlockChain (BlockChain object) */
            BlockChain blockChain = new BlockChain();
            blockChain.map2pojo(params);
            /* 组装Asset (Asset object) */
            /* 取消int assetId = seqService.createAssetId(blockChain.getChainId());*/
            Asset asset = new Asset();
            asset.map2pojo(params);
            asset.setChainId(blockChain.getChainId());
            asset.setDepositNuls(new BigInteger(nulsChainConfig.getAssetDepositNuls()));
            int rateToPercent = new BigDecimal(nulsChainConfig.getAssetDepositNulsDestroyRate()).multiply(BigDecimal.valueOf(100)).intValue();
            asset.setDestroyNuls(new BigInteger(nulsChainConfig.getAssetDepositNuls()).multiply(BigInteger.valueOf(rateToPercent)).divide(BigInteger.valueOf(100)));
            asset.setAvailable(true);
            BlockChain dbChain = chainService.getChain(blockChain.getChainId());
            if (null != dbChain) {
                return failed(CmErrorCode.ERROR_CHAIN_ID_EXIST);
            }
            if (chainService.hadExistMagicNumber(blockChain.getMagicNumber())) {
                return failed(CmErrorCode.ERROR_MAGIC_NUMBER_EXIST);
            }
            if (chainService.hadExistChainName(blockChain.getChainName())) {
                return failed(CmErrorCode.ERROR_CHAIN_NAME_EXIST);
            }
            /* 组装交易发送 (Send transaction) */
            Transaction tx = new RegisterChainAndAssetTransaction();
            tx.setTxData(blockChain.parseToTransaction(asset));
            tx.setTime(TimeUtils.getCurrentTimeSeconds());
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData = super.getRegCoinData(asset, CmRuntimeInfo.getMainIntChainId(),
                    CmRuntimeInfo.getMainIntAssetId(), tx.size(),
                    accountBalance);
            tx.setCoinData(coinData.serialize());

            /* 判断签名是否正确 (Determine if the signature is correct),取主网的chainid进行签名 */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }

            rtMap.put("txHash", tx.getHash().toHex());
            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger().error(e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = "getCrossChainInfos", version = 1.0, description = "getCrossChainInfos")
    public Response getCrossChainInfos(Map params) {
        List<Map<String, Object>> chainInfos = new ArrayList<>();
        Map<String, Object> rtMap = new HashMap<>();
        try {
            List<BlockChain> blockChains = chainService.getBlockList();
            for (BlockChain blockChain : blockChains) {
//                if (blockChain.getChainId() == CmRuntimeInfo.getMainIntChainId()) {
//                    continue;
//                }
                Map<String, Object> chainInfoMap = new HashMap<>();
                chainInfoMap.put("chainId", blockChain.getChainId());
                chainInfoMap.put("chainName", blockChain.getChainName());
                List<Asset> assets = assetService.getAssets(blockChain.getSelfAssetKeyList());
                List<Map<String, Object>> rtAssetList = new ArrayList<>();
                for (Asset asset : assets) {
                    Map<String, Object> assetMap = new HashMap<>();
                    assetMap.put("assetId", asset.getAssetId());
                    assetMap.put("symbol", asset.getSymbol());
                    assetMap.put("assetName", asset.getAssetName());
                    assetMap.put("usable", asset.isAvailable());
                    rtAssetList.add(assetMap);
                }
                chainInfoMap.put("assetInfoList", rtAssetList);
                chainInfos.add(chainInfoMap);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        rtMap.put("chainInfos", chainInfos);
        return success(rtMap);
    }
}
