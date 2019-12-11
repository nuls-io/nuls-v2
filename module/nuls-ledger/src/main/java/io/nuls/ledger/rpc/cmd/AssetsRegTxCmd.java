/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.rpc.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.tx.AssetRegTransaction;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;
import io.nuls.ledger.rpc.call.CallRpcService;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 资产登记与管理接口
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
public class AssetsRegTxCmd extends BaseLedgerCmd {
    @Autowired
    LedgerConfig ledgerConfig;
    @Autowired
    CallRpcService rpcService;
    @Autowired
    AccountStateService accountStateService;
    @Autowired
    AssetRegMngService assetRegMngService;

    /**
     * 注册链或资产封装coinData,x%资产进入黑洞，y%资产进入锁定
     */
    CoinData getRegCoinData(BigInteger destroyAsset, byte[] address, int chainId, int assetId, int txSize, AccountState accountState) throws NulsRuntimeException {
        long decimal = (long) Math.pow(10, Integer.valueOf(ledgerConfig.getDecimals()));
        BigInteger destroyAssetTx = destroyAsset.multiply(BigInteger.valueOf(decimal));
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(AddressTool.getAddressByPubKeyStr(ledgerConfig.getBlackHolePublicKey(), chainId), chainId, assetId, destroyAssetTx, 0);
        coinData.addTo(to);
        //手续费
        CoinFrom from = new CoinFrom(address, chainId, assetId, destroyAssetTx, accountState.getNonce(), (byte) 0);
        coinData.addFrom(from);
        txSize += to.size();
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        BigInteger fromAmount = destroyAssetTx.add(fee);
        if (BigIntegerUtils.isLessThan(accountState.getAvailableAmount(), fromAmount)) {
            throw new NulsRuntimeException(LedgerErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(fromAmount);
        return coinData;
    }

    /**
     * 链内资产协议登记接口
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_TX_REG, version = 1.0,
            description = "链内资产协议登记接口")
    @Parameters(value = {
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产名称: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "资产初始值"),
            @Parameter(parameterName = "decimalPlace", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-18]", parameterDes = "资产最小分割位数"),
            @Parameter(parameterName = "assetSymbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产单位符号: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "assetOwnerAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "新资产所有者地址"),
            @Parameter(parameterName = "txCreatorAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "交易创建者地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值"),
                    @Key(name = "chainId", valueType = int.class, description = "链id")
            })
    )
    public Response chainAssetTxReg(Map params) {
        Map<String, Object> rtMap = new HashMap<>(3);
        try {
            /* 组装Asset (Asset object) */
            TxLedgerAsset asset = LedgerUtil.map2TxLedgerAsset(params);
            ErrorCode errorCode = assetRegMngService.commonRegValidator(asset);
            if (null != errorCode) {
                return failed(errorCode);
            }
            //判断地址是否为本地chainId地址
            boolean isAddressValidate = (AddressTool.getChainIdByAddress(asset.getAddress()) == ledgerConfig.getChainId());
            if (!isAddressValidate) {
                return failed(LedgerErrorCode.ERROR_ADDRESS_ERROR);
            }
            String ledgerAddr = LedgerUtil.getRealAddressStr(params.get("txCreatorAddress").toString());
            /* 组装交易发送 (Send transaction) */
            Transaction tx = new AssetRegTransaction();
            tx.setTxData(asset.serialize());
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountState accountState = accountStateService.getAccountStateReCal(ledgerAddr, ledgerConfig.getChainId(), ledgerConfig.getChainId(), ledgerConfig.getAssetId());
            CoinData coinData = this.getRegCoinData(BigInteger.valueOf(ledgerConfig.getAssetRegDestroyAmount()), AddressTool.getAddress(params.get("txCreatorAddress").toString()), ledgerConfig.getChainId(),
                    ledgerConfig.getAssetId(), tx.size(), accountState);
            tx.setCoinData(coinData.serialize());
            /* 判断账号是否正确 (Determine if the signature is correct) */
            ErrorCode acErrorCode = rpcService.transactionSignature(ledgerConfig.getChainId(), (String) params.get("txCreatorAddress"), (String) params.get("password"), tx);
            if (null != acErrorCode) {
                return failed(acErrorCode);
            }
            rtMap.put("txHash", tx.getHash().toHex());
            rtMap.put("chainId", ledgerConfig.getChainId());
            ErrorCode txErrorCode = rpcService.newTx(tx);
            if (null != txErrorCode) {
                return failed(txErrorCode);
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 查看链内注册资产信息-通过Hash值
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO_BY_HASH, version = 1.0,
            description = "通过Hash查看链内注册资产信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "txHash", requestType = @TypeDescriptor(value = String.class), parameterDes = "交易Hash")

    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "资产id"),
                    @Key(name = "assetType", valueType = int.class, description = "资产类型"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "资产所有者地址"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "资产初始化值"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "小数点分割位数"),
                    @Key(name = "assetName", valueType = String.class, description = "资产名"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "资产符号"),
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response getAssetRegInfoByHash(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            rtMap = assetRegMngService.getLedgerRegAsset(Integer.valueOf(params.get("chainId").toString()), params.get("txHash").toString());
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

}
