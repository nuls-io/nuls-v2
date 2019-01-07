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
package io.nuls.ledger.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.processor.CommontTransactionProcessor;
import io.nuls.ledger.service.processor.LockedTransactionProcessor;
import io.nuls.ledger.utils.CoinDataUtils;
import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wangkun23 on 2018/11/28.
 * update by lanjinsheng on 2019/01/02
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;
    @Autowired
    CoinDataValidator coinDataValidator;
    @Autowired
    LockedTransactionProcessor lockedTransactionProcessor;
    @Autowired
    CommontTransactionProcessor commontTransactionProcessor;
    /**
     * 未确认交易数据处理
     *
     * @param transaction
     */

    @Override
    public boolean unConfirmTxProcess(Transaction transaction) {
        //直接更新未确认交易
        //TODO:要按用户账户来锁定处理(待处理)
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        byte [] nonce8Bytes = ByteUtils.copyOf(transaction.getHash().getDigestBytes(), 8);
        String nonce8BytesStr =  HexUtil.encode(nonce8Bytes);
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            if(from.getLocked()>0){
                //解锁交易处理

            }else {
                //非解锁交易处理
                accountStateService.setUnconfirmNonce(address, from.getAssetsChainId(), from.getAssetsId(), nonce8BytesStr);
            }
        }
        return true;
    }

    /**
     * 已确认交易数据处理
     *
     * @param transaction
     */
    @Override
    public boolean confirmTxProcess(Transaction transaction) {
        //从缓存校验交易
        if(coinDataValidator.hadValidateTx(transaction)){
            //提交交易：1.交易存库（最近100区块交易） 2.更新账户
            //TODO:存库

            //更新账户状态
            CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
            byte [] nonce8Bytes = ByteUtils.copyOf(transaction.getHash().getDigestBytes(), 8);
            String nonce8BytesStr = HexUtil.encode(nonce8Bytes);
            List<CoinFrom> froms = coinData.getFrom();
            for (CoinFrom from : froms) {

                if(from.getLocked() > 0){
                    lockedTransactionProcessor.processFromCoinData(from,nonce8BytesStr,transaction.getHash().toString());
                }else {
                    //非解锁交易处理
                    commontTransactionProcessor.processFromCoinData(from,nonce8BytesStr,transaction.getHash().toString());
                }
            }

            List<CoinTo> tos = coinData.getTo();
            for (CoinTo to : tos) {
                String address = AddressTool.getStringAddressByBytes(to.getAddress());
                int assetChainId = to.getAssetsChainId();
                int assetId = to.getAssetsId();
                if(to.getLockTime() > 0){
                    //锁定交易处理
                    lockedTransactionProcessor.processToCoinData(to,nonce8BytesStr,transaction.getHash().toString());
                }else {
                    //非锁定交易处理
                    commontTransactionProcessor.processToCoinData(to,nonce8BytesStr,transaction.getHash().toString());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 交易回滚
     * @param transaction
     */
    @Override
    public void rollBackConfirmTx(Transaction transaction) {
        //TODO:
    }
}
