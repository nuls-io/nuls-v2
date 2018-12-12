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
package io.nuls.ledger.service.processor;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.constant.TransactionType;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.utils.CoinDataUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * 转账交易处理
 * Created by wangkun23 on 2018/11/29.
 */
@Service
public class TransferProcessor implements TxProcessor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;

    @Override
    public void process(Transaction transaction) {
        //1 判断是否是转账交易，不是直接不处理
        if (transaction.getType() != TransactionType.TX_TYPE_TRANSFER.getValue()) {
            logger.error("transaction type:{} is not transfer type.", transaction.getType());
            return;
        }
        //2 获取coinDaData的数据
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        //3 增减用户账户的余额
        //TODO.. 收手续费
        //BigInteger txCost;
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int chainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            BigInteger amount = from.getAmount();

            //TODO 获取交易前八位
            //accountStateService.increaseNonce(address, chainId, assetId);
            String nonce = "";
            accountStateService.setNonce(address, chainId, assetId, nonce);
            accountStateService.addBalance(address, chainId, assetId, amount.negate());
        }

        List<CoinTo> tos = coinData.getTo();
        for (CoinTo to : tos) {
            String address = AddressTool.getStringAddressByBytes(to.getAddress());
            int chainId = to.getAssetsChainId();
            int assetId = to.getAssetsId();
            BigInteger amount = to.getAmount();
            accountStateService.addBalance(address, chainId, assetId, amount);
        }
    }
}
