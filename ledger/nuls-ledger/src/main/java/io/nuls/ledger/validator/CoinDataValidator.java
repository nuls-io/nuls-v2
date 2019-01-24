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
package io.nuls.ledger.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.TempAccountState;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.FreezeHeightState;
import io.nuls.ledger.model.po.FreezeLockTimeState;
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.utils.CoinDataUtils;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * validate Coin Data
 * Created by wangkun23 on 2018/11/22.
 * updatge  by lanjinsheng on 2018/12/28.
 */
@Component
public class CoinDataValidator {
    /**
     * key String:chainId
     * value:Map<key是交易hash  value是欲提交交易>
     *
     */
    private Map<String,Map<String,Transaction>> chainsBatchValidateTxMap = new ConcurrentHashMap<String,Map<String,Transaction>>();
    /**
     * key是账号资产 value是待确认支出列表
     */
    private Map<String,List<TempAccountState>> accountBalanceValidateTxMap = new ConcurrentHashMap<>();

    public  static final int VALIDATE_SUCCESS_CODE  = 1;
    private static final String VALIDATE_SUCCESS_DESC = "success";
    public static final int VALIDATE_ORPHAN_CODE = 2;
    private static final String VALIDATE_ORPHAN_DESC = "address {%s},nonce {%s} is orphan transaction";
    public static final int VALIDATE_DOUBLE_EXPENSES_CODE = 3;
    private static final String VALIDATE_DOUBLE_EXPENSES_DESC = "address {%s},nonce {%s} is double expenses";
    public static final int VALIDATE_FAIL_CODE = 4;
    private static final String VALIDATE_FAIL_DESC = "address {%s},nonce {%s} validate fail:{%s}";

    @Autowired
    private AccountStateService accountStateService;
    @Autowired
    private Repository repository;

    public boolean hadValidateTx(int addressChainId,Transaction tx){
        Map<String,Transaction> batchValidateTxMap = chainsBatchValidateTxMap.get(String.valueOf(addressChainId));
        if(null == batchValidateTxMap  || null == batchValidateTxMap.get(tx.getHash().toString())){
            return false;
        }
        return true;
    }
    /**
     * 开始批量校验
     */
    public boolean beginBatchPerTxValidate(int chainId){
        Map<String,Transaction> batchValidateTxMap = chainsBatchValidateTxMap.get(String.valueOf(chainId));
        if(null == batchValidateTxMap){
            batchValidateTxMap = new ConcurrentHashMap<>();
            chainsBatchValidateTxMap.put(String.valueOf(chainId),batchValidateTxMap);
        }
        batchValidateTxMap.clear();
        accountBalanceValidateTxMap.clear();
        return true;

    }

    /**
     * 批量逐笔校验
     * 批量校验 非解锁交易，余额校验与coindata校验一致,从数据库获取金额校验。
     * nonce校验与coindata不一样，是从批量累计中获取，进行批量连贯性校验。
     * 解锁交易的验证与coidate一致。
     *
     * 批量校验的过程中所有错误按恶意双花来进行处理，
     * 返回VALIDATE_DOUBLE_EXPENSES_CODE
     * @param chainId
     * @param tx
     * @return ValidateResult
     */
    public ValidateResult bathValidatePerTx(int chainId,Transaction tx){
        Map<String,Transaction> batchValidateTxMap = chainsBatchValidateTxMap.get(String.valueOf(chainId));
        //先校验，再逐笔放入缓存
        //交易的 hash值如果已存在，返回false，交易的from coin nonce 如果不连续，则存在双花。
        String txHash = tx.getHash().toString();
        if(null == batchValidateTxMap || null != batchValidateTxMap.get(txHash)){
            logger.error("{} tx exist!",txHash);
            return new ValidateResult(VALIDATE_DOUBLE_EXPENSES_CODE,String.format("%s tx exist!",txHash));
        }
        CoinData coinData =  CoinDataUtils.parseCoinData(tx.getCoinData());
        if(null == coinData){
            //例如黄牌交易，直接返回
            return new ValidateResult(VALIDATE_SUCCESS_CODE,VALIDATE_SUCCESS_DESC);
        }
        List<CoinFrom> coinFroms = coinData.getFrom();
        String nonce8BytesStr = LedgerUtils.getNonceStrByTxHash(tx);
        for(CoinFrom coinFrom:coinFroms) {
            if(LedgerUtils.isNotLocalChainAccount(chainId,coinFrom.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            AccountState accountState = accountStateService.getAccountState(AddressTool.getStringAddressByBytes(coinFrom.getAddress()), chainId,coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            //判断是否是解锁操作
            if(coinFrom.getLocked() == 0 ) {
               if(!isValidateCommonTxBatch(accountState,coinFrom,nonce8BytesStr)){
                   return new ValidateResult(VALIDATE_DOUBLE_EXPENSES_CODE,String.format("validate fail"));
               }
            }else{
                //解锁交易，需要从from 里去获取需要的高度数据或时间数据，进行校验
                //解锁交易只需要从已确认的数据中去获取数据进行校验
               if (!isValidateFreezeTx(coinFrom.getLocked(),accountState,coinFrom.getAmount(),HexUtil.encode(coinFrom.getNonce()))){
                   return new ValidateResult(VALIDATE_DOUBLE_EXPENSES_CODE,String.format("validate fail"));
               }
            }
        }
        batchValidateTxMap.put(tx.getHash().toString(),tx);
        return new ValidateResult(VALIDATE_SUCCESS_CODE,VALIDATE_SUCCESS_DESC);
    }

    /**
     * 进行普通交易的coindata 校验
     * @param accountState
     * @param address
     * @param fromAmount
     * @param fromNonce
     * @return
     */
    private ValidateResult validateCommonCoinData(AccountState accountState,String address,BigInteger fromAmount,String fromNonce){
        BigInteger totalAmount = accountState.getAvailableAmount().add(accountState.getUnconfirmedAmount());
        if(totalAmount.compareTo(fromAmount)== -1 ){
            logger.info("balance is not enough");
            ValidateResult validateResult = new ValidateResult(VALIDATE_FAIL_CODE,String.format(VALIDATE_FAIL_DESC,address,fromNonce,"balance is not enough"));
            return validateResult;
        }
        //存在未确认交易
        if(accountState.getUnconfirmedNonces().size()>0){
            if(!accountState.getLatestUnconfirmedNonce().equalsIgnoreCase(fromNonce)){
                //如果存在未确认交易，而又与账户确认的nonce状态一致，则双花
                if(accountState.getNonce().equalsIgnoreCase(fromNonce)){
                    ValidateResult validateResult = new ValidateResult(VALIDATE_DOUBLE_EXPENSES_CODE,String.format(VALIDATE_DOUBLE_EXPENSES_DESC,address,fromNonce));
                    return validateResult;
                }else{
                    //未确认交易中nonce重复了(最后一个已经被排除不会相等)
                    if(accountState.getUnconfirmedNonces().size()>1){
                        for(UnconfirmedNonce unconfirmedNonce:accountState.getUnconfirmedNonces()){
                            if(unconfirmedNonce.getNonce().equalsIgnoreCase(fromNonce)){
                                ValidateResult validateResult = new ValidateResult(VALIDATE_DOUBLE_EXPENSES_CODE,String.format(VALIDATE_DOUBLE_EXPENSES_DESC,address,fromNonce));
                                return validateResult;
                            }
                        }
                    }
                    //孤儿交易了
                    ValidateResult validateResult = new ValidateResult(VALIDATE_ORPHAN_CODE,String.format(VALIDATE_ORPHAN_DESC,address,fromNonce));
                    return validateResult;
                }
            }
        }else{
            //不存在未确认交易
            if(!accountState.getNonce().equalsIgnoreCase(fromNonce)){
                //提交的nonce 不等于 已确认的账本 nonce，则可能是孤儿交易
                ValidateResult validateResult = new ValidateResult(VALIDATE_ORPHAN_CODE,String.format(VALIDATE_ORPHAN_DESC,address,fromNonce));
                return validateResult;
            }
        }
        ValidateResult validateResult = new ValidateResult(VALIDATE_SUCCESS_CODE,VALIDATE_SUCCESS_DESC);
        return validateResult;
    }

    /**
     * 进行普通交易的批量校验
     * 与单笔交易校验不同的是，批量校验要校验批量池中的nonce连续性
     * @param accountState
     * @param coinFrom
     * @param txNonce
     * @return
     */
    private boolean isValidateCommonTxBatch(AccountState accountState,CoinFrom coinFrom,String txNonce){
        String nonce =HexUtil.encode(coinFrom.getNonce());
        String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
        String assetKey = LedgerUtils.getKeyStr(address,coinFrom.getAssetsChainId(),coinFrom.getAssetsId());
        //余额判断
        if (accountState.getAvailableAmount().compareTo(coinFrom.getAmount()) == -1) {
            //余额不足
            logger.info("{}=={}=={}==balance is not enough", coinFrom.getAddress(), coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            return false;
        }
        if(nonce.equalsIgnoreCase(txNonce)){
            //nonce 重复了
            logger.info("{}=={}=={}== nonce is repeat", coinFrom.getAddress(), coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            return false;
        }
        //不是解锁操作
        //从批量校验池中获取缓存交易
        List<TempAccountState> list = accountBalanceValidateTxMap.get(assetKey);
        if (null == list) {
            //从头开始处理
            if (!accountState.getNonce().equalsIgnoreCase(nonce)) {
                logger.info("{}=={}=={}==nonce is error!{}!={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(),accountState.getNonce(),nonce);
                return false;
            }
            list = new ArrayList<>();
            BigInteger balance = accountState.getAvailableAmount().subtract(coinFrom.getAmount());
            list.add(new TempAccountState(assetKey, nonce, txNonce,balance));
            accountBalanceValidateTxMap.put(assetKey, list);
        } else {
            //从已有的缓存数据中获取对象进行操作,nonce必须连贯
            TempAccountState tempAccountState = list.get(list.size()-1);
            if(!tempAccountState.getNextNonce().equalsIgnoreCase(nonce)){
                logger.info("{}=={}=={}==nonce is error!{}!={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(),tempAccountState.getNextNonce(),nonce);
                return false;
            }
            for(TempAccountState tempAccountState1:list){
                //交易池中账户存在一样的nonce
                if(tempAccountState1.getNonce().equalsIgnoreCase(nonce)){
                    logger.info("{}=={}=={}==nonce is double expenses! nonce ={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(),nonce);
                    return false;
                }
            }
            list.add(new TempAccountState(assetKey, nonce, txNonce,tempAccountState.getBalance().subtract(coinFrom.getAmount())));
        }
        return true;
    }

    /**
     * 进行解锁交易的校验
     * @param locked
     * @param accountState
     * @param fromAmount
     * @param fromNonce
     * @return
     */
    private boolean isValidateFreezeTx(byte locked,AccountState accountState,BigInteger fromAmount,String fromNonce){
        boolean  isValidate = false;
        //解锁交易，校验是否存在该笔交易
        if(locked == -1) {
            //时间解锁
            List<FreezeLockTimeState> list = accountState.getFreezeLockTimeStates();
            for (FreezeLockTimeState freezeLockTimeState : list) {
                if (freezeLockTimeState.getNonce().equalsIgnoreCase(fromNonce) && freezeLockTimeState.getAmount().compareTo(fromAmount) == 0) {
                    //找到交易
                    isValidate = true;
                    break;
                }
            }
        }else if(locked == 1){
            //高度解锁
            List<FreezeHeightState> list = accountState.getFreezeHeightStates();
            for (FreezeHeightState freezeHeightState : list) {
                if (freezeHeightState.getNonce().equalsIgnoreCase(fromNonce) && freezeHeightState.getAmount().compareTo(fromAmount) == 0) {
                    //找到交易
                    isValidate = true;
                    break;
                }
            }
        }
        return isValidate;
    }
    /**
     * 进行coinData值的校验,在本地交易产生时候进行的校验
     * 未进行全文的nonce检索,所以不排除历史区块中的双花被当成孤儿交易返回。
     * @param addressChainId
     * @param tx
     * @return
     */
    public ValidateResult validateCoinData(int addressChainId,Transaction tx) {
        CoinData coinData =  CoinDataUtils.parseCoinData(tx.getCoinData());
        if(null == coinData){
            //例如黄牌交易，直接返回
            return new ValidateResult(VALIDATE_SUCCESS_CODE,VALIDATE_SUCCESS_DESC);
        }
        /*
         * 先校验nonce值是否正常
         */
        List<CoinFrom> coinFroms = coinData.getFrom();
        for(CoinFrom coinFrom:coinFroms){
            if(LedgerUtils.isNotLocalChainAccount(addressChainId,coinFrom.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            String nonce = HexUtil.encode(coinFrom.getNonce());
            AccountState accountState = accountStateService.getAccountState(address,addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            //初始花费交易,nonce为 fffffff;已兼容处理。
            //普通交易
            if(coinFrom.getLocked() == 0){
                return validateCommonCoinData(accountState,address,coinFrom.getAmount(),nonce);
            }else{
                  if(!isValidateFreezeTx(coinFrom.getLocked(),accountState,coinFrom.getAmount(),nonce)){
                      //未找到冻结的交易
                      ValidateResult validateResult = new ValidateResult(VALIDATE_FAIL_CODE,String.format(VALIDATE_FAIL_DESC,address,nonce," freeze tx is not exist"));
                      return validateResult;
                  }
            }
        }
        ValidateResult validateResult = new ValidateResult(VALIDATE_SUCCESS_CODE,VALIDATE_SUCCESS_DESC);
        return validateResult;
    }

}
