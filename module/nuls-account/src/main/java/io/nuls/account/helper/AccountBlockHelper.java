/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.helper;

import io.nuls.account.config.AccountConfig;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.AccountBlockExtend;
import io.nuls.account.model.bo.tx.AccountBlockInfo;
import io.nuls.account.model.bo.tx.txdata.AccountBlockData;
import io.nuls.account.util.TxUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.List;

import static io.nuls.account.util.TxUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2022/1/18
 */
@Component
public class AccountBlockHelper {

    @Autowired
    private AccountConfig accountConfig;


    public Result blockAccountTxValidate(Chain chain, Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        List<CoinFrom> listFrom = coinData.getFrom();
        List<CoinTo> listTo = coinData.getTo();
        if (null == listFrom || listFrom.size() == 0) {
            return Result.getFailed(AccountErrorCode.TX_COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        if (listFrom.size() > 1) {
            return Result.getFailed(AccountErrorCode.COINDATA_IS_INCOMPLETE);
        }
        CoinFrom coinFrom = listFrom.get(0);
        String fromStr = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
        if (!fromStr.equals(accountConfig.getBlockAccountManager())) {
            chain.getLogger().error("error: not manager, tx: {}, config: {}", fromStr, accountConfig.getBlockAccountManager());
            return Result.getFailed(AccountErrorCode.COINDATA_IS_INCOMPLETE);
        }
        AccountBlockData txData = new AccountBlockData();
        txData.parse(tx.getTxData(), 0);
        String[] addresses = txData.getAddresses();
        if (addresses.length == 0) {
            chain.getLogger().error("empty addresses");
            return Result.getFailed(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        // 检查白名单
        byte[] txDataExtend = txData.getExtend();
        if (txDataExtend != null) {
            AccountBlockExtend abExtend = new AccountBlockExtend();
            abExtend.parse(txDataExtend, 0);
            AccountBlockInfo[] infos = abExtend.getInfos();
            if (addresses.length != infos.length) {
                chain.getLogger().error("inconsistent address data in txData");
                return Result.getFailed(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
            }
        }
        for (String addr : addresses) {
            if (addr.equals(accountConfig.getBlockAccountManager())) {
                chain.getLogger().error("error: manager can not in it");
                return Result.getFailed(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
            }
        }
        int addrChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
        //黑洞地址不能发起转账
        if(AddressTool.isBlackHoleAddress(NulsConfig.BLACK_HOLE_PUB_KEY,addrChainId,coinFrom.getAddress())){
            return Result.getFailed(AccountErrorCode.ADDRESS_TRANSFER_BAN);
        }
        // 发送方from中地址对应的链id必须是发起链的id
        if (chainId != addrChainId) {
            return Result.getFailed(AccountErrorCode.CHAINID_ERROR);
        }

        if (null == listTo || listTo.size() == 0) {
            return Result.getFailed(AccountErrorCode.TX_COINTO_NOT_FOUND);
        }
        for (CoinTo coinTo : listTo) {
            int toAddrChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            // 接收方to中地址对应的链id必须发起链id
            if (chainId != toAddrChainId) {
                return Result.getFailed(AccountErrorCode.CHAINID_ERROR);
            }
        }
        return getSuccess();

    }
}
