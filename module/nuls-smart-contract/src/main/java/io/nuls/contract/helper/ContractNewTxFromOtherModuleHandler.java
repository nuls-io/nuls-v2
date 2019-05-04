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
package io.nuls.contract.helper;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.ContractTransferData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.ByteArrayWrapper;
import io.nuls.tools.model.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.nuls.contract.constant.ContractErrorCode.TOO_SMALL_AMOUNT;
import static io.nuls.contract.util.ContractUtil.*;

/**
 * @author: PierreLuo
 * @date: 2019-04-28
 */
@Component
public class ContractNewTxFromOtherModuleHandler {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private VMContext vmContext;

    public void handleContractNewTxFromOtherModule(int chainId, String txHash, String txStr) {
        try {
            byte[] txBytes = RPCUtil.decode(txStr);
            Transaction tx = new Transaction();
            tx.parse(txBytes, 0);
            CoinData coinData = tx.getCoinDataInstance();
            this.refreshTempBalance(chainId, txHash, coinData, contractHelper.getBatchInfoTempBalanceManager(chainId));
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    private void refreshTempBalance(int chainId, String txHash, CoinData coinData, ContractTempBalanceManager tempBalanceManager) {
        byte[] addressBytes;
        // 增加转入
        List<CoinTo> toList = coinData.getTo();
        for(CoinTo to : toList) {
            addressBytes = to.getAddress();
            if (!ContractUtil.isLegalContractAddress(chainId, addressBytes)) {
                continue;
            }
            vmContext.getBalance(chainId, addressBytes);
            //TODO pierre 判定锁定金额 - 当前区块中
            if(to.getLockTime() != 0) {
                tempBalanceManager.addLockedTempBalance(addressBytes, to.getAmount());
            } else {
                tempBalanceManager.addTempBalance(addressBytes, to.getAmount());
            }
        }

        // 扣除转出
        List<CoinFrom> fromList = coinData.getFrom();
        CoinFrom from = fromList.get(0);
        addressBytes = from.getAddress();
        if (!ContractUtil.isLegalContractAddress(chainId, addressBytes)) {
            //TODO pierre 处理错误, 限制交易from只能有一个地址并且是合约地址
            throw new RuntimeException("not contract address");
        }
        byte[] hashBytes = HexUtil.decode(txHash);
        byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
        ContractBalance balance = contractHelper.getBalance(chainId, addressBytes);
        balance.setPreNonce(balance.getNonce());
        balance.setNonce(RPCUtil.encode(currentNonceBytes));
        tempBalanceManager.minusTempBalance(addressBytes, from.getAmount());

    }

    public void rollbackContractNewTxFromOtherModule(int chainId, ProgramNewTx programNewTx) {
        try {
            byte[] txBytes = RPCUtil.decode(programNewTx.getTxString());
            Transaction tx = new Transaction();
            tx.parse(txBytes, 0);
            CoinData coinData = tx.getCoinDataInstance();
            ContractTempBalanceManager tempBalanceManager = contractHelper.getBatchInfoTempBalanceManager(chainId);

            byte[] addressBytes;
            // 增加转出
            List<CoinFrom> fromList = coinData.getFrom();
            CoinFrom from = fromList.get(0);
            addressBytes = from.getAddress();
            ContractBalance balance = contractHelper.getBalance(chainId, addressBytes);
            if(StringUtils.isNotBlank(balance.getPreNonce())) {
                balance.setNonce(balance.getPreNonce());
            }
            tempBalanceManager.addTempBalance(addressBytes, from.getAmount());

            // 扣除转入
            List<CoinTo> toList = coinData.getTo();
            for(CoinTo to : toList) {
                addressBytes = to.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, addressBytes)) {
                    continue;
                }
                vmContext.getBalance(chainId, addressBytes);
                //TODO pierre 判定锁定金额 - 当前区块中
                if(to.getLockTime() != 0) {
                    tempBalanceManager.minusLockedTempBalance(addressBytes, to.getAmount());
                } else {
                    tempBalanceManager.minusTempBalance(addressBytes, to.getAmount());
                }
            }
        } catch (NulsException e) {
            Log.error(e);
        }
    }
}
