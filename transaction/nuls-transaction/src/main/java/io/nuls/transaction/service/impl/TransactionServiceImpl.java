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
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.CrossTxData;
import io.nuls.transaction.model.bo.TxRegister;
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

    /**
     * 跨链交易固定为非解锁交易
     */
    private static final byte CORSS_TX_LOCKED = 0;

    @Override
    public Result newTx(Transaction transaction) {
        /**
         * 1.基础数据库校验
         * 2.放入队列
         */
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    @Override
    public Result register(TxRegister txRegister) {
        boolean rs = transactionManager.register(txRegister);
        return Result.getSuccess(TxErrorCode.SUCCESS).setData(rs);
    }

    @Override
    public Result getTransaction(NulsDigestData hash){
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }

    @Override
    public Result createCrossTransaction(List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        CrossTxData txData = new CrossTxData();
        txData.setChainId(TxConstant.NUlS_CHAINID);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            tx.setTxData(txData.serialize());
            List<CoinFrom> coinFromList = assemblyCoinFrom(listFrom);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            CoinData coinData = getCoinData(coinFromList, coinToList, tx.size());
            tx.setTxData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //todo 签名

            this.newTx(tx);
            return Result.getSuccess(TxErrorCode.SUCCESS);
        }
        catch (IOException e) {
            e.printStackTrace();
            return Result.getFailed(TxErrorCode.SERIALIZE_ERROR);
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(e.getErrorCode());
        }
    }


    private List<CoinFrom> assemblyCoinFrom(List<CoinDTO> listFrom) throws NulsException {

        List<CoinFrom> coinFroms = new ArrayList<>();
        for(CoinDTO coinDTO : listFrom){

            String addr = coinDTO.getAddress();
            //int chainId = AddressTool.getChainIdByAddress(coinDTO.getAddress());
            if(!AddressTool.validAddress(TxConstant.CURRENT_CHAINID, addr)){
                throw new NulsException(TxErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            byte[] address = AddressTool.getAddress(coinDTO.getAddress());
            if(AddressTool.getChainIdByAddress(address) != TxConstant.CURRENT_CHAINID){
                //转账交易转出地址必须是本链地址
                //throw new NulsException();
            }
            CoinFrom coinFrom = new CoinFrom();
            coinFrom.setAddress(address);
            coinFrom.setLocked(CORSS_TX_LOCKED);
            //todo 是否存在 chainId assetId
            //todo 查对应资产余额 是否足够
            //todo 查nonce

        }
        return null;
    }


    private List<CoinTo> assemblyCoinTo(List<CoinDTO> listTo) throws NulsException{

        return null;
    }

    public CoinData getCoinData(List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException{

        for(CoinFrom coinFrom : listFrom){
            txSize += coinFrom.size();
        }
        for(CoinTo coinTo : listTo){
            txSize += coinTo.size();
        }
        //计算并组装手续费到CoinData
        return assemblyFee(listFrom, listTo, txSize);
    }

    public boolean getFeeDirectly(List<CoinFrom> listFrom, int txSize){
        //本交易需要的手续费
        //todo 跨链交易手续费单价？？
        BigInteger targetFee = TransactionFeeCalculator.getMaxFee(txSize);
        //实际收取的手续费，从第一个from开始收，直到收取足够的手续费
        BigInteger actualFee = BigInteger.ZERO;
        boolean complete = false;
        for(CoinFrom coinFrom : listFrom){
            if(TxUtil.isNulsAsset(coinFrom)){
                //如果是nuls主网资产直接收取

            }
        }
        return complete;

    }



    public BigInteger getAvailable(byte[] address, int chainId, int assetId) throws NulsException{
        //todo 根据chainId 和资产Id 查余额;

        return BigInteger.TEN;
    }

    private CoinData assemblyFee(List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException{

        //本交易需要的手续费
        //todo 跨链交易手续费单价？？
        BigInteger targetFee = TransactionFeeCalculator.getMaxFee(txSize);
        //实际收取的手续费，从第一个from开始收，直到收取足够的手续费
        BigInteger actualFee = BigInteger.ZERO;
        List<CoinFrom> feeNewCoinfromList = new ArrayList<>();
        for(CoinFrom coinFrom : listFrom){
            //找NULS资产的余额, 用来扣手续费
            BigInteger mainAsset = getAvailable(coinFrom.getAddress(), TxConstant.NUlS_CHAINID, TxConstant.NUlS_CHAIN_ASSETID);
            //当前还差的手续费
            BigInteger current = targetFee.subtract(actualFee);
            //如果余额大于等于目标手续费，则直接收取全额手续费
            if(BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current)) {
                assemblyFeeCoinFrom(feeNewCoinfromList, coinFrom, current);
                actualFee = actualFee.add(current);
                break;
            }
            //如果余额大于0，但小于目标手续费
            if(BigIntegerUtils.isGreaterThan(mainAsset, BigInteger.ZERO)
                    && BigIntegerUtils.isLessThan(mainAsset, current)) {
                //在当前coinfrom收取的手续费,如果不够继续收下一个
                BigInteger fee = current.subtract(mainAsset);
                assemblyFeeCoinFrom(feeNewCoinfromList, coinFrom, fee);
                actualFee = actualFee.add(fee);
                //实际收取的手续费已经够了，就不再继续了
                if(BigIntegerUtils.isEqual(actualFee, targetFee)) {
                    break;
                }
            }
        }
        if(BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //所有from中账户的nuls余额总和都不够支付手续费
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        }

        if(feeNewCoinfromList.size() > 0){
            boolean hasNulsAsset = false;
            for (CoinFrom coinFrom : listFrom){
                if(coinFrom.getAssetsChainId() == TxConstant.CURRENT_CHAINID
                        && coinFrom.getAssetsId() ==TxConstant.CURRENT_CHAIN_ASSETID) {
                    for (CoinFrom feeNewCoinFrom : feeNewCoinfromList) {
                        if(Arrays.equals(coinFrom.getAddress(), feeNewCoinFrom.getAddress())) {
                            coinFrom.setAmount(coinFrom.getAmount().add(feeNewCoinFrom.getAmount()));
                            hasNulsAsset = true;
                        }
                    }

                }
            }
            if(!hasNulsAsset) {
                for (CoinFrom feeNewCoinFrom : feeNewCoinfromList) {
                    //向集合最前面添加新的coinFrom，如果再次计算首先扣取最后组装的coinfrom
                    listFrom.add(0, feeNewCoinFrom);
                    txSize += feeNewCoinFrom.size();
                }
                //新加入手续费会导致txSize变大，需要再次计算手续费
                return assemblyFee(listFrom, listTo, txSize);
            }
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    private CoinFrom createCrossTxCoinFrom(byte[] address, BigInteger current){
        CoinFrom feeCoinfrom = new CoinFrom();
        feeCoinfrom.setLocked(CORSS_TX_LOCKED);
        feeCoinfrom.setAddress(address);
        feeCoinfrom.setAssetsChainId(TxConstant.CURRENT_CHAINID);
        feeCoinfrom.setAssetsId(TxConstant.CURRENT_CHAIN_ASSETID);
        feeCoinfrom.setAmount(current);
        //todo 获取nonce;
        //feeCoinfrom.setNonce(nonce);
        return feeCoinfrom;
    }


    /**
     * 根据当前的coinfrom来组装新的手续费coinfrom，或将手续费追加到当前coinfrom中
     * @param feeNewCoinfromList 新的手续费coinfrom的集合
     * @param coinFrom 当前coinfrom
     * @param fee 当前还需要手续需
     */
    private void assemblyFeeCoinFrom(List<CoinFrom> feeNewCoinfromList, CoinFrom coinFrom, BigInteger fee){
        if(coinFrom.getAssetsChainId() == TxConstant.CURRENT_CHAINID
                && coinFrom.getAssetsId() ==TxConstant.CURRENT_CHAIN_ASSETID) {
            coinFrom.setAmount(coinFrom.getAmount().add(fee));
        }else{
            feeNewCoinfromList.add(createCrossTxCoinFrom(coinFrom.getAddress(), fee));
        }
    }



    public static void main(String[] args) {
        List list = new ArrayList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        Iterator lit = list.listIterator();
        while(lit.hasNext()){
            String str = (String)lit.next();
            if(str.equals("b")){
                ((ListIterator) lit).add("X");
                //lit.remove();
            }
            System.out.println(str);
        }


/*        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
//        TransactionServiceImpl transactionService = new TransactionServiceImpl();
//        transactionService.test(list);
        test2(list);
        System.out.println(list);*/

       /* CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAssetsId(300);*/

    }

    private void test(List<String> list){
        list.add("3");

    }
    private static void test2(List<String> list){
        list.add("4");

    }
}
