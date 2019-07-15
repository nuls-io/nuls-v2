package io.nuls.chain.rpc.cmd;


import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.ChainDto;
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
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.NulsDateUtils;

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


    @CmdAnnotation(cmd = RpcConstants.CMD_CHAIN, version = 1.0,
            description = "查看链信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class),  parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
    })
    @ResponseData(description = "返回链信息", responseType = @TypeDescriptor(value = RegChainDto.class))
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


    @CmdAnnotation(cmd = RpcConstants.CMD_CHAIN_REG, version = 1.0,
            description = "链注册-用于平行链的跨链注册")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "chainName", requestType = @TypeDescriptor(value = String.class), parameterDes = "链名称"),
            @Parameter(parameterName = "addressType", requestType = @TypeDescriptor(value = int.class), parameterDes = "1 使用NULS框架构建的链 生态内，2生态外"),
            @Parameter(parameterName = "magicNumber", requestType = @TypeDescriptor(value = long.class), parameterDes = "网络魔法参数"),
            @Parameter(parameterName = "minAvailableNodeNum", requestType = @TypeDescriptor(value = int.class), parameterDes = "最小连接数"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "symbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产符号"),
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产名称"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产初始值"),
            @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = short.class), parameterDes = "资产小数点位数"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "创建交易的账户地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码"),
            @Parameter(parameterName = "verifierList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "验证者名单列表"),
            @Parameter(parameterName = "signatureBFTRatio", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "拜占庭比例,大于等于该值为有效确认"),
            @Parameter(parameterName = "maxSignatureCount", requestType = @TypeDescriptor(value = Integer.class), parameterDes = "最大签名数量,限制验证者签名列表的最大数")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
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
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
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

    @CmdAnnotation(cmd = RpcConstants.CMD_GET_CROSS_CHAIN_INFOS, version = 1.0,
            description = "获取跨链注册资产信息")

    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, collectionElement = List.class, mapKeys = {
                    @Key(name = "chainInfos", valueType = List.class, valueElement = ChainDto.class, description = "资产信息列表")
            })
    )
    public Response getCrossChainInfos(Map params) {
        List<Map<String, Object>> chainInfos = new ArrayList<>();
        Map<String, Object> rtMap = new HashMap<>();
        try {
            List<BlockChain> blockChains = chainService.getBlockList();
            for (BlockChain blockChain : blockChains) {
                chainInfos.add(chainService.getBlockAssetsInfo(blockChain));
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        rtMap.put("chainInfos", chainInfos);
        return success(rtMap);
    }

}
