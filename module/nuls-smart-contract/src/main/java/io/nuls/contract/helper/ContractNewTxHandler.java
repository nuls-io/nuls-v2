/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.config.ContractContext.ASSET_ID;
import static io.nuls.contract.config.ContractContext.CHAIN_ID;

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

    public boolean handleContractNewTx(int chainId, long blockTime, ContractWrapperTransaction tx, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) throws NulsException {
        Map<String, ProgramAccount> accountMap = contractResult.getAccounts();
        // 维护临时余额管理器
        if(accountMap != null) {
            ProgramAccount account;
            byte[] contractBytes;
            Set<Map.Entry<String, ProgramAccount>> entrySet = accountMap.entrySet();
            accountMap.values();
            for(Map.Entry<String, ProgramAccount> accountEntry : entrySet) {
                account = accountEntry.getValue();
                contractBytes = account.getAddress();
                String nonce = account.getNonce();
                int assetChainId = account.getAssetChainId();
                int assetId = account.getAssetId();
                // 这个nonce维护了合约内部调用其他模块新生成的交易的临时nonce，需要更新到临时余额管理器中，提供给合约内部转账使用
                if (StringUtils.isNotBlank(nonce)) {
                    ContractBalance contractBalance = tempBalanceManager.getBalance(contractBytes, assetChainId, assetId).getData();
                    if (StringUtils.isBlank(contractBalance.getPreNonce())) {
                        contractBalance.setPreNonce(contractBalance.getNonce());
                    }
                    contractBalance.setNonce(nonce);
                }
            }
        }
        ContractData contractData = tx.getContractData();
        byte[] contractAddress = contractData.getContractAddress();
        CoinData coinData = tx.getCoinDataInstance();
        List<CoinTo> toList = coinData.getTo();
        int assetChainId, assetId;
        boolean mainAsset;
        if (toList != null && !toList.isEmpty()) {
            for (CoinTo to : toList) {
                if (Arrays.equals(to.getAddress(), contractAddress)) {
                    assetChainId = to.getAssetsChainId();
                    assetId = to.getAssetsId();
                    mainAsset = assetChainId == CHAIN_ID && assetId == ASSET_ID;
                    if (!mainAsset) {
                        // 初始化其他资产的临时余额
                        tempBalanceManager.getBalance(contractAddress, assetChainId, assetId);
                        tempBalanceManager.addTempBalance(contractAddress, to.getAmount(), assetChainId, assetId);
                    }
                }
            }
        }
        // 增加调用合约时转入的NULS金额
        BigInteger value = contractData.getValue();
        if (value.compareTo(BigInteger.ZERO) > 0) {
            // 初始化NULS主资产临时余额
            tempBalanceManager.getBalance(contractAddress, CHAIN_ID, ASSET_ID);
            tempBalanceManager.addTempBalance(contractAddress, value, CHAIN_ID, ASSET_ID);
        }

        boolean isSuccess = true;
        do {
            // 按交易生成顺序，依次处理合约转账交易和合约调用其他模块生成的交易的临时余额
            List<Object> orderedInnerTxs = contractResult.getOrderedInnerTxs();
            LinkedList<Object> successedOrderedInnerTxs = new LinkedList<>();
            for(Object innerTx : orderedInnerTxs) {
                if(innerTx instanceof ProgramNewTx) {
                    isSuccess = contractNewTxFromOtherModuleHandler.refreshTempBalance(chainId, contractResult.getContractAddress(), List.of((ProgramNewTx) innerTx), tempBalanceManager);
                    if(!isSuccess) {
                        contractResult.setError(true);
                        contractResult.setErrorMessage("Refresh temp balance failed about new transaction from external cmd.");
                        break;
                    }
                    successedOrderedInnerTxs.add(innerTx);
                } else if(innerTx instanceof ProgramTransfer) {
                    isSuccess = contractTransferHandler.refreshTempBalance(chainId, List.of((ProgramTransfer) innerTx), tempBalanceManager);
                    if(!isSuccess) {
                        contractResult.setError(true);
                        contractResult.setErrorMessage(String.format("Refresh temp balance failed about inner transfer out from contract[%s].", AddressTool.getStringAddressByBytes(contractResult.getContractAddress())));
                        break;
                    }
                    successedOrderedInnerTxs.add(innerTx);
                }
            }
            if(!isSuccess) {
                // 处理失败则回滚
                Iterator<Object> reverseIterator = successedOrderedInnerTxs.descendingIterator();
                Object rollbackTx;
                while (reverseIterator.hasNext()) {
                    rollbackTx = reverseIterator.next();
                    if(rollbackTx instanceof ProgramNewTx) {
                        contractNewTxFromOtherModuleHandler.rollbackTempBalance(chainId, contractResult.getContractAddress(), List.of((ProgramNewTx) rollbackTx), tempBalanceManager);
                    } else if(rollbackTx instanceof ProgramTransfer) {
                        contractTransferHandler.rollbackContractTempBalance(chainId, List.of((ProgramTransfer) rollbackTx), tempBalanceManager);
                    }
                }
                contractResult.getTransfers().clear();
                contractResult.getInvokeRegisterCmds().clear();
                break;
            } else {
                // 处理合约内部转账交易 -> 合并、生成链上交易
                isSuccess = contractTransferHandler.handleContractTransferTxs(chainId, blockTime, contractResult, tempBalanceManager);
                if(!isSuccess) {
                    // 如果内部转账失败，回滚合约新生成的其他交易 - 合约余额和nonce
                    Iterator<Object> reverseIterator = successedOrderedInnerTxs.descendingIterator();
                    Object rollbackTx;
                    while (reverseIterator.hasNext()) {
                        rollbackTx = reverseIterator.next();
                        if(rollbackTx instanceof ProgramNewTx) {
                            contractNewTxFromOtherModuleHandler.rollbackTempBalance(chainId, contractResult.getContractAddress(), List.of((ProgramNewTx) rollbackTx), tempBalanceManager);
                        }
                        //else if(rollbackTx instanceof ProgramTransfer) {
                        //    contractTransferHandler.rollbackContractTempBalance(chainId, List.of((ProgramTransfer) rollbackTx), tempBalanceManager);
                        //}
                    }
                    contractResult.getInvokeRegisterCmds().clear();
                    break;
                }
            }
        } while (false);

        if (!isSuccess) {
            // 回滚 - 扣除调用合约时转入的金额
            if (value.compareTo(BigInteger.ZERO) > 0) {
                tempBalanceManager.minusTempBalance(contractAddress, value, CHAIN_ID, ASSET_ID);
            }
            if (toList != null && !toList.isEmpty()) {
                for (CoinTo to : toList) {
                    if (Arrays.equals(to.getAddress(), contractAddress)) {
                        assetChainId = to.getAssetsChainId();
                        assetId = to.getAssetsId();
                        mainAsset = assetChainId == CHAIN_ID && assetId == ASSET_ID;
                        if (!mainAsset) {
                            // 回滚 - 扣除其他资产的临时余额
                            tempBalanceManager.minusTempBalance(contractAddress, to.getAmount(), assetChainId, assetId);
                        }
                    }
                }
            }
        }
        return isSuccess;
    }

}
