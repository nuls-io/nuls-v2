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

import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import org.ethereum.db.ByteArrayWrapper;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-04-28
 */
@Component
public class ContractNewTxHandler {

    @Autowired
    private ContractTransferHandler contractTransferHandler;
    @Autowired
    private ContractNewTxFromOtherModuleHandler contractNewTxFromOtherModuleHandler;

    public void handleContractNewTx(int chainId, long blockTime, ContractWrapperTransaction tx, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        Map<ByteArrayWrapper, ProgramAccount> accountMap = contractResult.getAccounts();
        if(accountMap != null) {
            ProgramAccount account;
            byte[] contractBytes;
            Set<Map.Entry<ByteArrayWrapper, ProgramAccount>> entrySet = accountMap.entrySet();
            accountMap.values();
            for(Map.Entry<ByteArrayWrapper, ProgramAccount> accountEntry : entrySet) {
                account = accountEntry.getValue();
                contractBytes = account.getAddress();
                String nonce = account.getNonce();
                // 这个nonce维护了合约内部调用其他模块新生成的交易的临时nonce，需要更新到临时余额管理器中，提供给合约内部转账使用
                if (StringUtils.isNotBlank(nonce)) {
                    ContractBalance contractBalance = tempBalanceManager.getBalance(contractBytes).getData();
                    if (StringUtils.isBlank(contractBalance.getPreNonce())) {
                        contractBalance.setPreNonce(contractBalance.getNonce());
                    }
                    contractBalance.setNonce(nonce);
                }
            }
        }
        ContractData contractData = tx.getContractData();
        byte[] contractAddress = contractData.getContractAddress();
        // 增加调用合约时转入的金额
        BigInteger value = contractData.getValue();
        if (value.compareTo(BigInteger.ZERO) > 0) {
            // 初始化临时余额
            tempBalanceManager.getBalance(contractAddress);
            tempBalanceManager.addTempBalance(contractAddress, value);
        }

        boolean isSuccess;
        do {
            // 处理合约调用其他模块生成的交易的临时余额
            isSuccess = contractNewTxFromOtherModuleHandler.refreshTempBalance(chainId, contractResult, tempBalanceManager);
            if (!isSuccess) {
                contractResult.setError(true);
                contractResult.setErrorMessage("Refresh temp balance failed about new transaction from external cmd.");
                // 回滚 - 清空内部转账列表
                contractResult.getTransfers().clear();
                break;
            }
            // 处理合约内部转账交易
            isSuccess = contractTransferHandler.handleContractTransfer(chainId, blockTime, contractResult, tempBalanceManager);
            // 如果内部转账失败，回滚合约新生成的其他交易 - 合约余额和nonce
            if (!isSuccess) {
                contractNewTxFromOtherModuleHandler.rollbackTempBalance(chainId, contractResult, tempBalanceManager);
                break;
            }
        } while (false);

        if (!isSuccess) {
            // 回滚 - 扣除调用合约时转入的金额
            if (value.compareTo(BigInteger.ZERO) > 0) {
                tempBalanceManager.minusTempBalance(contractAddress, value);
            }
        }

    }

}
