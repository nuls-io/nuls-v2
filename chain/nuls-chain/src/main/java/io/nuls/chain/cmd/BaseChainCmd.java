/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.chain.cmd;

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
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.thread.TimeService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * @author lan
 * @date 2018/11/28
 */
public class BaseChainCmd extends BaseCmd {


    boolean isMainAsset(String assetKey) {
        String chainId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID);
        String assetId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID);
        return CmRuntimeInfo.getAssetKey(Integer.valueOf(chainId), Integer.valueOf(assetId)).equals(assetKey);
    }
    Response parseTxs(List<String> txHexList, List<Transaction> txList) {
        for (String txHex : txHexList) {
            if (StringUtils.isBlank(txHex)) {
                return failed("txHex is blank");
            }
            byte[] txStream = HexUtil.decode(txHex);
            Transaction tx = new Transaction();
            try {
                tx.parse(new NulsByteBuffer(txStream));
                txList.add(tx);
            } catch (NulsException e) {
                Log.error("transaction parse error", e);
                return failed("transaction parse error");
            }
        }
        return success();
    }
    /**
     * 注册链或资产封装coinData,x%资产进入黑洞，y%资产进入锁定
     */
    CoinData getRegCoinData(byte[] address, int chainId, int assetsId, String amount,
                            int txSize, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        String lockRate = CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSIT_NULS_lOCK);
        BigInteger lockAmount = new BigDecimal(amount).multiply(new BigDecimal(lockRate)).toBigInteger();
        BigInteger destroyAmount = new BigInteger(amount).subtract(lockAmount);
        CoinTo to1 = new CoinTo(address, chainId, assetsId, lockAmount, -1);
        CoinTo to2 = new CoinTo(CmConstants.BLACK_HOLE_ADDRESS, chainId, assetsId, destroyAmount, 0);
        coinData.addTo(to1);
        txSize += to1.size();
        coinData.addTo(to2);
        txSize += to2.size();
        //手续费
        CoinFrom from = new CoinFrom(address, chainId, assetsId, new BigDecimal(amount).toBigInteger(), ByteUtils.copyOf(accountBalance.getNonce().getBytes(), 8), (byte) 0);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount, fee.toString());
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(BigIntegerUtils.stringToBigInteger(fromAmount));
        coinData.addFrom(from);
        return coinData;
    }

    /**
     * 注销资产进行处理
     */
    CoinData getDisableCoinData(byte[] address, int chainId, int assetsId, String amount,
                                int txSize, String txHash, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;

        String lockRate = CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSIT_NULS_lOCK);
        BigInteger lockAmount = new BigDecimal(amount).multiply(new BigDecimal(lockRate)).toBigInteger();
        CoinTo to = new CoinTo(address, chainId, assetsId, lockAmount, 0);

        CoinData coinData = new CoinData();
        coinData.addTo(to);
        txSize += to.size();
        //手续费
        CoinFrom from = new CoinFrom(address, chainId, assetsId, new BigInteger(amount), ByteUtils.copyOf(txHash.getBytes(), 8), (byte) -1);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount, fee.toString());
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(new BigInteger(fromAmount));
        coinData.addFrom(from);
        return coinData;
    }

    BlockChain buildChainWithTxData(String txHex, Transaction tx, boolean isDelete) {
        try {
            byte[] txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes, 0);
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            BlockChain blockChain = new BlockChain(txChain);
            if (isDelete) {
                blockChain.setDelTxHash(tx.getHash().toString());
                blockChain.setRegAddress(txChain.getAddress());
                blockChain.setRegAssetId(txChain.getAssetId());
            } else {
                blockChain.setRegTxHash(tx.getHash().toString());
                blockChain.setDelAddress(txChain.getAddress());
                blockChain.setDelAssetId(txChain.getAssetId());
            }
            return blockChain;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    Asset buildAssetWithTxChain(String txHex, Transaction tx) {
        try {
            byte[] txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes, 0);
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            Asset asset = new Asset(txChain);
            asset.setTxHash(tx.getHash().toString());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

//    protected Asset buildAssetWithTxAsset(String txHex, Transaction tx) {
//        try {
//            byte[] txBytes = HexUtil.hexToByte(txHex);
//            tx.parse(txBytes, 0);
//            TxAsset txAsset = new TxAsset();
//            txAsset.parse(tx.getTxData(), 0);
//            Asset asset = new Asset();
//            asset.setTxHash(tx.getHash().toString());
//            return asset;
//        } catch (Exception e) {
//            Log.error(e);
//            return null;
//        }
//    }

    Asset setDefaultAssetValue(Asset asset) {
        asset.setDepositNuls(Integer.valueOf(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSIT_NULS)));
        asset.setAvailable(true);
        asset.setCreateTime(TimeService.currentTimeMillis());
        return asset;
    }

    Transaction signDigest(int chainId, String address, String password, Transaction tx) throws Exception {
        Map<String, Object> signDigestParam = new HashMap<>(4);
        signDigestParam.put("chainId", chainId);
        signDigestParam.put("address", address);
        signDigestParam.put("password", password);
        signDigestParam.put("dataHex", tx);

        Response response = ResponseMessageProcessor.requestAndResponse("ac", "ac_signDigest", signDigestParam);
        if (!response.isSuccess()) {
            throw new Exception("ac_signDigest error.");
        }

        Map responseData = (Map) response.getResponseData();
        String signatureHex = (String) responseData.get("signatureHex");
        tx.setTransactionSignature(HexUtil.decode(signatureHex));

        return tx;
    }
}
