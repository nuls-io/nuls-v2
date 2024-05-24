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
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.NulsDateUtils;
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

import static io.nuls.common.CommonConstant.NORMAL_PRICE_PRE_1024_BYTES_NULS;

/**
 * Asset registration and management interface
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class AssetsRegTxCmd extends BaseLedgerCmd {
    @Autowired
    NulsCoresConfig ledgerConfig;
    @Autowired
    CallRpcService rpcService;
    @Autowired
    AccountStateService accountStateService;
    @Autowired
    AssetRegMngService assetRegMngService;

    /**
     * Registration chain or asset encapsulationcoinData,x%Assets enter the black hole,y%Asset entry lock
     */
    CoinData getRegCoinData(BigInteger destroyAsset, byte[] address, int chainId, int assetId, int txSize, AccountState accountState) throws NulsRuntimeException {
        long decimal = (long) Math.pow(10, Integer.valueOf(ledgerConfig.getDecimals()));
        BigInteger destroyAssetTx = destroyAsset.multiply(BigInteger.valueOf(decimal));
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(AddressTool.getAddressByPubKeyStr(ledgerConfig.getBlackHolePublicKey(), chainId), chainId, assetId, destroyAssetTx, 0);
        coinData.addTo(to);
        //Handling fees
        CoinFrom from = new CoinFrom(address, chainId, assetId, destroyAssetTx, accountState.getNonce(), (byte) 0);
        coinData.addFrom(from);
        txSize += to.size();
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize, NORMAL_PRICE_PRE_1024_BYTES_NULS);
        BigInteger fromAmount = destroyAssetTx.add(fee);
        if (BigIntegerUtils.isLessThan(accountState.getAvailableAmount(), fromAmount)) {
            throw new NulsRuntimeException(LedgerErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(fromAmount);
        return coinData;
    }

    /**
     * In chain asset protocol registration interface
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_TX_REG, version = 1.0,
            description = "In chain asset protocol registration interface")
    @Parameters(value = {
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset Name: large、Lowercase letters、number、Underline（The underline cannot be at both ends）1~20byte"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "Initial value of assets"),
            @Parameter(parameterName = "decimalPlace", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-18]", parameterDes = "The minimum number of split digits for assets"),
            @Parameter(parameterName = "assetSymbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset unit symbol: large、Lowercase letters、number、Underline（The underline cannot be at both ends）1~20byte"),
            @Parameter(parameterName = "assetOwnerAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "Address of new asset owner"),
            @Parameter(parameterName = "txCreatorAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "Transaction creator address"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue"),
                    @Key(name = "chainId", valueType = int.class, description = "chainid")
            })
    )
    public Response chainAssetTxReg(Map params) {
        Map<String, Object> rtMap = new HashMap<>(3);
        try {
            /* assembleAsset (Asset object) */
            TxLedgerAsset asset = LedgerUtil.map2TxLedgerAsset(params);
            ErrorCode errorCode = assetRegMngService.commonRegValidator(asset);
            if (null != errorCode) {
                return failed(errorCode);
            }
            //Determine if the address is localchainIdaddress
            boolean isAddressValidate = (AddressTool.getChainIdByAddress(asset.getAddress()) == ledgerConfig.getChainId());
            if (!isAddressValidate) {
                return failed(LedgerErrorCode.ERROR_ADDRESS_ERROR);
            }
            String ledgerAddr = LedgerUtil.getRealAddressStr(params.get("txCreatorAddress").toString());
            /* Assembly transaction sending (Send transaction) */
            Transaction tx = new AssetRegTransaction();
            tx.setTxData(asset.serialize());
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            AccountState accountState = accountStateService.getAccountStateReCal(ledgerAddr, ledgerConfig.getChainId(), ledgerConfig.getChainId(), ledgerConfig.getAssetId());
            CoinData coinData = this.getRegCoinData(BigInteger.valueOf(ledgerConfig.getAssetRegDestroyAmount()), AddressTool.getAddress(params.get("txCreatorAddress").toString()), ledgerConfig.getChainId(),
                    ledgerConfig.getAssetId(), tx.size(), accountState);
            tx.setCoinData(coinData.serialize());
            /* Check if the account is correct (Determine if the signature is correct) */
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
     * View registered asset information within the chain-adoptHashvalue
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO_BY_HASH, version = 1.0,
            description = "adoptHashView registered asset information within the chain")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "txHash", requestType = @TypeDescriptor(value = String.class), parameterDes = "transactionHash")

    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "assetid"),
                    @Key(name = "assetType", valueType = int.class, description = "Asset type"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "Address of asset owner"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Asset initialization value"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "Decimal Division"),
                    @Key(name = "assetName", valueType = String.class, description = "Asset Name"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "Asset symbols"),
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue")
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
