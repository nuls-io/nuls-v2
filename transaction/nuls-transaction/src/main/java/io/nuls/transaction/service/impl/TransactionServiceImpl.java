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
package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.model.bo.CrossTxData;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.TxWrapper;
import io.nuls.transaction.model.dto.BlockHeaderDigestDTO;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.service.TransactionService;
import io.nuls.transaction.utils.TransactionManager;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionManager transactionManager = TransactionManager.getInstance();

    @Autowired
    private TxUnverifiedStorageService txUnverifiedStorageService;

    @Autowired
    private TxVerifiedStorageService txVerifiedStorageService;


    @Override
    public Result newTx(int chainId, Transaction tx) {
        //todo 判断已验证未打包的交易里面是否有此交易；已确认的交易中是否有此交易
        Transaction txExist = txVerifiedStorageService.getTx(chainId, tx.getHash());
        if(null != txExist){
            return Result.getSuccess(TxErrorCode.TRANSACTION_ALREADY_EXISTS);
        }
        TxWrapper txWrapper = new TxWrapper(chainId, tx);
        txUnverifiedStorageService.putTx(txWrapper);
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    @Override
    public Result register(TxRegister txRegister) {
        boolean rs = transactionManager.register(txRegister);
        return Result.getSuccess(TxErrorCode.SUCCESS).setData(rs);
    }

    @Override
    public Result getTransaction(NulsDigestData hash) {
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    @Override
    public Result createCrossTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) {
        return assemblyCrossTransaction(currentChainId, listFrom, listTo, remark, false);
    }

    @Override
    public Result createCrossMultiTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) {
        return assemblyCrossTransaction(currentChainId, listFrom, listTo, remark, true);
    }

    /**
     * 组装跨链交易
     *
     * @param currentChainId 当前链的id Current chainId
     * @param listFrom       交易的转出者数据 payer coins
     * @param listTo         交易的接收者数据 payee  coins
     * @param remark         交易备注 remark
     * @param isMultiSign    是否是多签地址交易 is Multi-sign address transaction
     * @return
     */
    private Result assemblyCrossTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, boolean isMultiSign) {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        CrossTxData txData = new CrossTxData();
        txData.setChainId(currentChainId);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            tx.setTxData(txData.serialize());
            List<CoinFrom> coinFromList = assemblyCoinFrom(currentChainId, listFrom, isMultiSign);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            if (coinFromList.size() == 0 || coinToList.size() == 0) {
                return Result.getFailed(TxErrorCode.COINDATA_IS_INCOMPLETE);
            }
            byte[] fromAddress = coinFromList.get(0).getAddress();
            int chainIdFrom = AddressTool.getChainIdByAddress(fromAddress);
            byte[] toAddress = coinToList.get(0).getAddress();
            int chainIdTo = AddressTool.getChainIdByAddress(toAddress);
            if(chainIdFrom == chainIdTo){
                return Result.getFailed(TxErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
            }
            MultiSigAccount multiSigAccount = null;
            int txSize = tx.size();
            if(isMultiSign){
                //多签交易，计算签名大小，取多签地址m来计算
                multiSigAccount = TxUtil.getMultiSigAccount(fromAddress);
                txSize += getMultiSignAddressSignatureSize(multiSigAccount.getM());
            }else{
                txSize += getSignatureSize(coinFromList);
            }
            CoinData coinData = getCoinData(coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            List<ECKey> signEcKeys = new ArrayList<>();
            for (CoinDTO coinDTO : listFrom) {
                String priKey = TxUtil.getPrikey(coinDTO.getAddress(), coinDTO.getPassword());
                ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
                signEcKeys.add(ecKey);
            }
            if(isMultiSign){
                //组装多签地址信息
                MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
                multiSignTxSignature.setM(multiSigAccount.getM());
                multiSignTxSignature.setPubKeyList(multiSigAccount.getPubKeyList());
                //多签交易有且只有一个地址的eckey
                return txMultiSignProcess(new TxWrapper(currentChainId, tx), signEcKeys.get(0), multiSignTxSignature);
            }
            SignatureUtil.createTransactionSignture(tx, signEcKeys);
            this.newTx(currentChainId, tx);
            return Result.getSuccess(TxErrorCode.SUCCESS).setData(tx.getHash().getDigestHex());
        } catch (IOException e) {
            e.printStackTrace();
            return Result.getFailed(TxErrorCode.SERIALIZE_ERROR);
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result txMultiSignProcess(TxWrapper txWrapper, ECKey ecKey){
        return txMultiSignProcess(txWrapper, ecKey, null);
    }

    @Override
    public Result txMultiSignProcess(TxWrapper txWrapper, ECKey ecKey, MultiSignTxSignature multiSignTxSignature){
        Transaction tx = txWrapper.getTx();
        try {
            if(null == multiSignTxSignature) {
                multiSignTxSignature = new MultiSignTxSignature();
                multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            }
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            if (multiSignTxSignature.getP2PHKSignatures() != null && multiSignTxSignature.getP2PHKSignatures().size() > 0) {
                p2PHKSignatures = multiSignTxSignature.getP2PHKSignatures();
            }
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.setPublicKey(ecKey.getPubKey());
            //用当前交易的hash和账户的eckey签名
            p2PHKSignature.setSignData(SignatureUtil.signDigest(tx.getHash().getDigestBytes(), ecKey));
            p2PHKSignatures.add(p2PHKSignature);
            if (p2PHKSignatures.size() == multiSignTxSignature.getM()) {
                p2PHKSignatures.sort(P2PHKSignature.PUBKEY_COMPARATOR);
                multiSignTxSignature.setP2PHKSignatures(p2PHKSignatures);
                tx.setTransactionSignature(multiSignTxSignature.serialize());
                this.newTx(txWrapper.getChainId(), tx);
                return Result.getSuccess(TxErrorCode.SUCCESS).setData(tx.getHash().getDigestHex());
            }else{
                multiSignTxSignature.setP2PHKSignatures(p2PHKSignatures);
                tx.setTransactionSignature(multiSignTxSignature.serialize());
                return Result.getSuccess(TxErrorCode.SUCCESS).setData(HexUtil.encode(tx.serialize()));
            }
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(TxErrorCode.IO_ERROR);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(TxErrorCode.DESERIALIZE_ERROR);
        }
    }

    /**
     * 通过coinfrom计算签名数据的size
     * 如果coinfrom有重复地址则只计算一次；如果有多签地址，只计算m个地址的size
     *
     * @param coinFroms
     * @return
     */
    private int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> signAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            byte[] address = coinFrom.getAddress();
            signAddress.add(AddressTool.getStringAddressByBytes(address));
        }
        size += signAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    /**
     * 获取多重签名地址，最小签名数，签名后的size
     *
     * @param signNumber m
     * @return int
     */
    private int getMultiSignAddressSignatureSize(int signNumber) {
        int size = signNumber * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }


    /**
     * assembly coinFrom
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    private List<CoinFrom> assemblyCoinFrom(int currentChainId, List<CoinDTO> listFrom, boolean isMultiSign) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        byte[] multiAddress = null;
        for (CoinDTO coinDTO : listFrom) {
            String addr = coinDTO.getAddress();
            byte[] address = AddressTool.getAddress(addr);
            if (isMultiSign) {
                //如果为多签交易，所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
                if (null == multiAddress) {
                    multiAddress = address;
                } else if (!Arrays.equals(multiAddress, address)) {
                    throw new NulsException(TxErrorCode.ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED);
                }
                if (!AddressTool.isMultiSignAddress(address)) {
                    throw new NulsException(TxErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
                }
            } else {
                //不是多签交易，from中不能有多签地址
                if (AddressTool.isMultiSignAddress(address)) {
                    throw new NulsException(TxErrorCode.IS_MULTI_SIGNATURE_ADDRESS);
                }
            }
            if (!AddressTool.validAddress(currentChainId, addr)) {
                //转账交易转出地址必须是本链地址
                throw new NulsException(TxErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            int assetChainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            if (!TxUtil.assetExist(assetChainId, assetId)) {
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }
            //检查对应资产余额 是否足够
            BigInteger amount = coinDTO.getAmount();
            BigInteger balance = TxUtil.getBalance(address, assetChainId, assetId);
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_BALANCE);
            }
            byte[] nonce = TxUtil.getNonce(address, assetChainId, assetId);
            CoinFrom coinFrom = new CoinFrom(address, assetChainId, assetId, amount, nonce, TxConstant.CORSS_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    /**
     * assembly coinTo 组装to
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    private List<CoinTo> assemblyCoinTo(List<CoinDTO> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        Integer chainIdOfTo = null;
        for (CoinDTO coinDTO : listTo) {
            byte[] address = AddressTool.getAddress(coinDTO.getAddress());
            if (null == chainIdOfTo) {
                chainIdOfTo = AddressTool.getChainIdByAddress(address);
            } else {
                if (chainIdOfTo != AddressTool.getChainIdByAddress(address)) {
                    throw new NulsException(TxErrorCode.CROSS_TX_PAYEE_CHAINID_NOT_SAME);
                }
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(address);
            int chainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            coinTo.setAmount(coinDTO.getAmount());
            if (!TxUtil.assetExist(chainId, assetId)) {
                //资产不存在 chainId assetId
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }
            coinTo.setAssetsChainId(chainId);
            coinTo.setAssetsId(assetId);
            coinTos.add(coinTo);
        }
        return coinTos;
    }


    /**
     * assembly coinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @return
     * @throws NulsException
     */
    private CoinData getCoinData(List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        BigInteger feeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            txSize += coinFrom.size();
            if (TxUtil.isNulsAsset(coinFrom)) {
                feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
            }
        }
        BigInteger feeTotalTo = BigInteger.ZERO;
        for (CoinTo coinTo : listTo) {
            txSize += coinTo.size();
            if (TxUtil.isNulsAsset(coinTo)) {
                feeTotalTo = feeTotalTo.add(coinTo.getAmount());
            }
        }
        //本交易预计收取的手续费
        BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        //实际收取的手续费, 可能自己已经组装完成
        BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
        if (BigIntegerUtils.isLessThan(actualFee, BigInteger.ZERO)) {
            //所有from中账户的nuls余额总和小于to的总和，不够支付手续费
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //只从资产为nuls的coinfrom中收取手续费
            actualFee = getFeeDirect(listFrom, targetFee, actualFee);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //如果没收到足够的手续费，则从CoinFrom中资产不是nuls的coin账户中查找nuls余额，并组装新的coinfrom来收取手续费
                if (!getFeeIndirect(listFrom, txSize, targetFee, actualFee)) {
                    //所有from中账户的nuls余额总和都不够支付手续费
                    throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
                }
            }
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * Only collect fees from CoinFrom's coins whose assets are nuls, and return the actual amount charged.
     * 只从CoinFrom中资产为nuls的coin中收取手续费，返回实际收取的数额
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param targetFee The amount of the fee that needs to be charged 需要收取的手续费数额
     * @param actualFee Actual amount charged 实际收取的数额
     * @return BigInteger The amount of the fee actually charged 实际收取的手续费数额
     * @throws NulsException
     */
    private BigInteger getFeeDirect(List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        for (CoinFrom coinFrom : listFrom) {
            if (TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = TxUtil.getBalance(coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //如果余额大于等于目标手续费，则直接收取全额手续费
                if (BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current)) {
                    coinFrom.setAmount(coinFrom.getAmount().add(current));
                    actualFee = actualFee.add(current);
                    break;
                } else if (BigIntegerUtils.isGreaterThan(mainAsset, BigInteger.ZERO)) {
                    coinFrom.setAmount(coinFrom.getAmount().add(mainAsset));
                    actualFee = actualFee.add(mainAsset);
                    continue;
                }
            }
        }
        return actualFee;
    }

    /**
     * 从CoinFrom中资产不为nuls的coin中收取nuls手续费，返回是否收取完成
     * Only collect the nuls fee from the coin in CoinFrom whose assets are not nuls, and return whether the charge is completed.
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param txSize    Current transaction size
     * @param targetFee Estimated fee
     * @param actualFee actual Fee
     * @return boolean
     * @throws NulsException
     */
    private boolean getFeeIndirect(List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        ListIterator<CoinFrom> iterator = listFrom.listIterator();
        while (iterator.hasNext()) {
            CoinFrom coinFrom = iterator.next();
            if (!TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = TxUtil.getBalance(coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
                if (BigIntegerUtils.isEqualOrLessThan(mainAsset, BigInteger.ZERO)) {
                    continue;
                }
                CoinFrom feeCoinFrom = new CoinFrom();
                byte[] address = coinFrom.getAddress();
                feeCoinFrom.setAddress(address);
                feeCoinFrom.setNonce(TxUtil.getNonce(address, TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID));
                txSize += feeCoinFrom.size();
                //新增coinfrom，重新计算本交易预计收取的手续费
                targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //此账户可以支付的手续费
                BigInteger fee = BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current) ? current : mainAsset;

                feeCoinFrom.setLocked(TxConstant.CORSS_TX_LOCKED);
                feeCoinFrom.setAssetsChainId(TxConstant.NULS_CHAINID);
                feeCoinFrom.setAssetsId(TxConstant.NULS_CHAIN_ASSETID);
                feeCoinFrom.setAmount(fee);

                iterator.add(feeCoinFrom);
                actualFee = actualFee.add(fee);
                if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
                    break;
                }
            }
        }
        //最终的实际收取数额大于等于预计收取数额，则可以正确组装CoinData
        if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
            return true;
        }
        return false;
    }


    /**
     * 跨链交易验证器
     * 交易类型为跨链交易
     * 地址和签名一一对应
     * from里面的资产是否存在，是否可以进行跨链交易
     * 必须包含NULS资产的from
     *
     * @param chainId
     * @param tx
     * @return Result
     */
    @Override
    public Result crossTransactionValidator(int chainId, Transaction tx) {
        Result result = transactionManager.baseTxValidate(chainId, tx);
        if (result.isFailed()) {
            return result;
        }
        if (null == tx.getCoinData() || tx.getCoinData().length == 0) {
            return Result.getFailed(TxErrorCode.COINDATA_NOT_FOUND);
        }
        try {
            CoinData coinData = tx.getCoinDataInstance();
            Result resultCoinFrom = validateCoinFrom(chainId, coinData.getFrom());
            if (resultCoinFrom.isFailed()) {
                return resultCoinFrom;
            }
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(TxErrorCode.DESERIALIZE_ERROR);
        }
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    /**
     * 验证跨链交易的付款方数据
     *
     * @param chainId
     * @param listFrom
     * @return
     */
    private Result validateCoinFrom(int chainId, List<CoinFrom> listFrom) {
        if (null == listFrom || listFrom.size() == 0) {
            return Result.getFailed(TxErrorCode.COINFROM_NOT_FOUND);
        }
        boolean hasNulsFrom = false;
        for (CoinFrom coinFrom : listFrom) {
            //是否有nuls(手续费)
            if (TxUtil.isNulsAsset(coinFrom)) {
                hasNulsFrom = true;
            }
        }
        if (!hasNulsFrom) {
            return Result.getFailed(TxErrorCode.INSUFFICIENT_FEE);
        }
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }


    @Override
    public Result crossTransactionCommit(int chainId, Transaction tx, BlockHeaderDigestDTO blockHeader) {
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    @Override
    public Result crossTransactionRollback(int chainId, Transaction tx, BlockHeaderDigestDTO blockHeader) {
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }
}
