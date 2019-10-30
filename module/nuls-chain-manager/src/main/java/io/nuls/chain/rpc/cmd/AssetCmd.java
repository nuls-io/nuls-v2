package io.nuls.chain.rpc.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.RegAssetDto;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.model.tx.AddAssetToChainTransaction;
import io.nuls.chain.model.tx.DestroyAssetAndChainTransaction;
import io.nuls.chain.model.tx.RemoveAssetFromChainTransaction;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.util.ChainManagerUtil;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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
    private NulsChainConfig nulsChainConfig;

    /**
     * 资产注册
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET_REG, version = 1.0,
            description = "资产注册")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "symbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产符号"),
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产名称"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "资产初始值"),
            @Parameter(parameterName = "decimalPlaces", requestType = @TypeDescriptor(value = short.class), parameterDes = "资产小数点位数"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "创建交易的账户地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response assetReg(Map params) {
        /* 发送到交易模块 (Send to transaction module) */
        Map<String, String> rtMap = new HashMap<>(1);
        try {
            /* 组装Asset (Asset object) */
            Asset asset = new Asset();
            asset.map2pojo(params);
            if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
                return failed(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
            }
            if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
                return failed(CmErrorCode.ERROR_ASSET_SYMBOL);
            }
            if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
                return failed(CmErrorCode.ERROR_ASSET_NAME);
            }
            asset.setDepositNuls(nulsChainConfig.getAssetDepositNuls());
            asset.setDestroyNuls(nulsChainConfig.getAssetDestroyNuls());
            asset.setAvailable(true);
            BlockChain dbChain = chainService.getChain(asset.getChainId());
            if (null == dbChain) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            }
            if (dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_CHAIN_REG_CMD);
            }
            if (assetService.assetExist(asset) && asset.isAvailable()) {
                return failed(CmErrorCode.ERROR_ASSET_ID_EXIST);
            }
            /* 组装交易发送 (Send transaction) */
            Transaction tx = new AddAssetToChainTransaction();
            if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) > 2) {
                tx.setTxData(TxUtil.parseAssetToTxV3(asset).serialize());
            } else {
                tx.setTxData(TxUtil.parseAssetToTx(asset).serialize());
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData = this.getRegCoinData(asset, CmRuntimeInfo.getMainIntChainId(),
                    CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            tx.setCoinData(coinData.serialize());

            /* 判断签名是否正确 (Determine if the signature is correct) */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }

            rtMap.put("txHash", tx.getHash().toHex());
            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 链内资产注册
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_MAIN_NET_ASSET_REG, version = 1.0,
            description = "资产注册")
    @Parameters(value = {
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "创建交易的账户地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response cm_mainNetAssetReg(Map params) {
        /* 发送到交易模块 (Send to transaction module) */
        Map<String, String> rtMap = new HashMap<>(1);
        try {
            /* 组装Asset (Asset object) */
            Asset asset = rpcService.getLocalAssetByLedger(CmRuntimeInfo.getMainIntChainId(), Integer.valueOf(params.get("assetId").toString()));
            asset.setAddress(AddressTool.getAddress((String) params.get("address")));
            if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
                return failed(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
            }
            if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
                return failed(CmErrorCode.ERROR_ASSET_SYMBOL);
            }
            if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
                return failed(CmErrorCode.ERROR_ASSET_NAME);
            }
            asset.setDepositNuls(nulsChainConfig.getAssetDepositNuls());
            asset.setDestroyNuls(nulsChainConfig.getAssetDestroyNuls());
            asset.setAvailable(true);
            BlockChain dbChain = chainService.getChain(asset.getChainId());
            if (null == dbChain) {
                return failed(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
            }
            if (dbChain.isDelete()) {
                return failed(CmErrorCode.ERROR_CHAIN_REG_CMD);
            }
            if (assetService.assetExist(asset) && asset.isAvailable()) {
                return failed(CmErrorCode.ERROR_ASSET_ID_EXIST);
            }
            /* 组装交易发送 (Send transaction) */
            Transaction tx = new AddAssetToChainTransaction();
            LoggerUtil.COMMON_LOG.debug("version= {}", ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()));
            if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) > 2) {
                tx.setTxData(TxUtil.parseAssetToTxV3(asset).serialize());
            } else {
                tx.setTxData(TxUtil.parseAssetToTx(asset).serialize());
            }
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData = this.getRegCoinData(asset, CmRuntimeInfo.getMainIntChainId(),
                    CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            tx.setCoinData(coinData.serialize());

            /* 判断签名是否正确 (Determine if the signature is correct) */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }

            rtMap.put("txHash", tx.getHash().toHex());
            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET_DISABLE, version = 1.0,
            description = "资产注销")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "创建交易的账户地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response assetDisable(Map params) {
        /* 发送到交易模块 (Send to transaction module) */
        Map<String, String> rtMap = new HashMap<>(1);
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            int assetId = Integer.parseInt(params.get("assetId").toString());
            byte[] address = AddressTool.getAddress(params.get("address").toString());
            /* 身份的校验，账户地址的校验 (Verification of account address) */
            String dealAssetKey = CmRuntimeInfo.getAssetKey(chainId, assetId);
            Asset asset = assetService.getAsset(dealAssetKey);
            if (asset == null) {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
            if (!asset.isAvailable()) {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
            if (!ByteUtils.arrayEquals(asset.getAddress(), address)) {
                return failed(CmErrorCode.ERROR_ADDRESS_ERROR);
            }

            /*
              判断链下是否只有这一个资产了，如果是，则进行带链注销交易
              Judging whether there is only one asset under the chain, and if so, proceeding with the chain cancellation transaction
             */
            BlockChain dbChain = chainService.getChain(chainId);
            if (null == dbChain) {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }

            List<String> assetKeyList = dbChain.getSelfAssetKeyList();
            int activeAssetCount = 0;
            String activeKey = "";
            for (String assetKey : assetKeyList) {
                Asset chainAsset = assetService.getAsset(assetKey);
                if (null != chainAsset && chainAsset.isAvailable()) {
                    activeKey = assetKey;
                    activeAssetCount++;
                }
                if (activeAssetCount > 1) {
                    break;
                }
            }
            Transaction tx;
            if (activeAssetCount == 1 && activeKey.equalsIgnoreCase(dealAssetKey)) {
                /* 注销资产和链 (Destroy assets and chains) */
                tx = new DestroyAssetAndChainTransaction();
                try {
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) > 2) {
                        tx.setTxData(TxUtil.parseChainToTxV3(dbChain, asset).serialize());
                    } else {
                        tx.setTxData(TxUtil.parseChainToTx(dbChain, asset).serialize());
                    }
                } catch (IOException e) {
                    LoggerUtil.logger().error(e);
                    return failed("parseToTransaction fail");
                }
            } else {
                /* 只注销资产 (Only destroy assets) */
                tx = new RemoveAssetFromChainTransaction();
                try {
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) > 2) {
                        tx.setTxData(TxUtil.parseAssetToTxV3(asset).serialize());
                    } else {
                        tx.setTxData(TxUtil.parseAssetToTx(asset).serialize());
                    }
                } catch (IOException e) {
                    LoggerUtil.logger().error(e);
                    return failed("parseToTransaction fail");
                }
            }
            AccountBalance accountBalance = new AccountBalance(null, null);
            ErrorCode ldErrorCode = rpcService.getCoinData(String.valueOf(params.get("address")), accountBalance);
            if (null != ldErrorCode) {
                return failed(ldErrorCode);
            }
            CoinData coinData = this.getDisableCoinData(asset, CmRuntimeInfo.getMainIntChainId(), CmRuntimeInfo.getMainIntAssetId(), tx.size(), accountBalance);
            tx.setCoinData(coinData.serialize());
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            /* 判断签名是否正确 (Determine if the signature is correct) */
            ErrorCode acErrorCode = rpcService.transactionSignature(CmRuntimeInfo.getMainIntChainId(), (String) params.get("address"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }
            rtMap.put("txHash", tx.getHash().toHex());
            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_GET_CHAIN_ASSET, version = 1.0,
            description = "资产查看")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = Integer.class, description = "运行链Id"),
                    @Key(name = "assetChainId", valueType = Integer.class, description = "资产链id"),
                    @Key(name = "assetId", valueType = Integer.class, description = "资产id"),
                    @Key(name = "asset", valueType = BigInteger.class, description = "资产值"),
            })
    )
    public Response getChainAsset(Map params) {
        int chainId = Integer.parseInt(params.get("chainId").toString());
        int assetChainId = Integer.parseInt(params.get("assetChainId").toString());
        int assetId = Integer.parseInt(params.get("assetId").toString());
        String assetKey = CmRuntimeInfo.getAssetKey(assetChainId, assetId);
        try {
            ChainAsset chainAsset = assetService.getChainAsset(chainId, assetKey);
            Map<String, Object> rtMap = new HashMap<>();
            rtMap.put("chainId", chainId);
            rtMap.put("assetChainId", assetChainId);
            rtMap.put("assetId", assetId);
            rtMap.put("asset", chainAsset.getInitNumber().add(chainAsset.getInNumber()).subtract(chainAsset.getOutNumber()));
            return success(rtMap);
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET, version = 1.0,
            description = "资产注册信息查询")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]")
    })
    @ResponseData(description = "返回链信息", responseType = @TypeDescriptor(value = RegAssetDto.class))
    public Response getRegAsset(Map params) {
        int chainId = Integer.parseInt(params.get("chainId").toString());
        int assetId = Integer.parseInt(params.get("assetId").toString());
        String assetKey = CmRuntimeInfo.getAssetKey(chainId, assetId);
        try {
            Asset asset = assetService.getAsset(assetKey);
            if (null == asset) {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            } else {
                RegAssetDto regAssetDto = new RegAssetDto();
                regAssetDto.setChainId(chainId);
                regAssetDto.setAssetId(assetId);
                regAssetDto.setAddress(AddressTool.getStringAddressByBytes(asset.getAddress()));
                regAssetDto.setAssetName(asset.getAssetName());
                regAssetDto.setCreateTime(asset.getCreateTime());
                regAssetDto.setDecimalPlaces(asset.getDecimalPlaces());
                regAssetDto.setDepositNuls(asset.getDepositNuls().toString());
                regAssetDto.setDestroyNuls(asset.getDestroyNuls().toString());
                regAssetDto.setEnable(asset.isAvailable());
                regAssetDto.setInitNumber(asset.getInitNumber().toString());
                regAssetDto.setTxHash(asset.getTxHash());
                regAssetDto.setSymbol(asset.getSymbol());
                return success(regAssetDto);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }
}
