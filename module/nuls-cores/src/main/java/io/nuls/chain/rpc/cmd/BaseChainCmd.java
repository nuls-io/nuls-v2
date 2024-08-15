/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;

import java.math.BigInteger;
import java.util.List;

import static io.nuls.common.CommonConstant.NORMAL_PRICE_PRE_1024_BYTES_NULS;

/**
 * @author lan
 * @date 2018/11/28
 */
public class BaseChainCmd extends BaseCmd {


    boolean isMainAsset(String assetKey) {
        return CmRuntimeInfo.getMainAssetKey().equals(assetKey);
    }

    Response parseTxs(List<String> txHexList, List<Transaction> txList) {
        for (String txHex : txHexList) {
            if (StringUtils.isBlank(txHex)) {
                return failed("txHex is blank");
            }
            byte[] txStream = RPCUtil.decode(txHex);
            Transaction tx = new Transaction();
            try {
                tx.parse(new NulsByteBuffer(txStream));
                txList.add(tx);
            } catch (NulsException e) {
                LoggerUtil.logger().error("transaction parse error", e);
                return failed("transaction parse error");
            }
        }
        return success();
    }

    /**
     * Registration chain or asset encapsulationcoinData,x%Assets enter the black hole,y%Asset entry lock
     */
    CoinData getRegCoinData(Asset asset, int nulsChainId, int nulsAssetId, int txSize, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        BigInteger lockAmount = asset.getDepositNuls().subtract(asset.getDestroyNuls());
        CoinTo to1 = new CoinTo(asset.getAddress(), nulsChainId, nulsAssetId, lockAmount, -1);
        CoinTo to2 = new CoinTo(CmConstants.BLACK_HOLE_ADDRESS, nulsChainId, nulsAssetId, asset.getDestroyNuls(), 0);
        coinData.addTo(to1);
        coinData.addTo(to2);
        //Handling fees
        CoinFrom from = new CoinFrom(asset.getAddress(), nulsChainId, nulsAssetId, asset.getDepositNuls(), accountBalance.getNonce(), (byte) 0);
        coinData.addFrom(from);
        txSize += to1.size();
        txSize += to2.size();
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize, NORMAL_PRICE_PRE_1024_BYTES_NULS);
        String fromAmount = BigIntegerUtils.bigIntegerToString(asset.getDepositNuls().add(fee));
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(BigIntegerUtils.stringToBigInteger(fromAmount));
        return coinData;
    }


    /**
     * Registration chain or asset encapsulationcoinData,x%Assets enter the black hole,y%Asset entry lock
     */
    CoinData getRegCoinDataV7(Asset asset, int nulsChainId, int nulsAssetId, int txSize, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        //Handling fees
        CoinFrom from = new CoinFrom(asset.getAddress(), nulsChainId, nulsAssetId, BigInteger.ZERO, accountBalance.getNonce(), (byte) 0);
        CoinTo to = new CoinTo(CmConstants.BLACK_HOLE_ADDRESS, nulsChainId, nulsAssetId, BigInteger.ZERO, 0);
        coinData.addFrom(from);
        coinData.addTo(to);

        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize, NORMAL_PRICE_PRE_1024_BYTES_NULS);
        String fromAmount = BigIntegerUtils.bigIntegerToString(fee);
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(BigIntegerUtils.stringToBigInteger(fromAmount));
        return coinData;
    }

    /**
     * Cancel assets for processing
     */
    CoinData getDisableCoinData(Asset asset, int nulsChainId, int nulsAssetId,
                                int txSize, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;

        BigInteger lockAmount = asset.getDepositNuls().subtract(asset.getDestroyNuls());
        CoinTo to = new CoinTo(asset.getAddress(), nulsChainId, nulsAssetId, lockAmount, 0);
        CoinData coinData = new CoinData();

        //Handling fees
        if (lockAmount.equals(BigInteger.ZERO)) {
            CoinFrom from = new CoinFrom(asset.getAddress(), nulsChainId, nulsAssetId, BigInteger.ZERO, accountBalance.getNonce(), (byte) 0);
            coinData.addFrom(from);
            coinData.addTo(to);
            txSize += from.size();
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize, NORMAL_PRICE_PRE_1024_BYTES_NULS);
            String fromAmount = BigIntegerUtils.bigIntegerToString(fee);
            if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
                throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
            }
            from.setAmount(fee);
        } else {
            CoinFrom from = new CoinFrom(asset.getAddress(), nulsChainId, nulsAssetId, lockAmount, TxUtil.getNonceByTxHash(asset.getTxHash()), (byte) -1);
            coinData.addFrom(from);
            coinData.addTo(to);
            txSize += to.size();
            txSize += from.size();
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize, NORMAL_PRICE_PRE_1024_BYTES_NULS);
            //The handling fee is deducted from the mortgage
            to.setAmount(lockAmount.subtract(fee));
        }
        return coinData;
    }

}
