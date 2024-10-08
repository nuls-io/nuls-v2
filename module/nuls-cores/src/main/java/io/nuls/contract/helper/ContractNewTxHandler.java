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

import static io.nuls.contract.config.ContractContext.LOCAL_MAIN_ASSET_ID;
import static io.nuls.contract.config.ContractContext.LOCAL_CHAIN_ID;

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
        // Maintain Temporary Balance Manager
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
                // thisnonceMaintained temporary transactions generated by calling other modules within the contractnonce, needs to be updated to the temporary balance manager and provided for internal transfer of contracts
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
                    mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                    if (!mainAsset) {
                        // Initialize temporary balances for other assets
                        tempBalanceManager.getBalance(contractAddress, assetChainId, assetId);
                        tempBalanceManager.addTempBalance(contractAddress, to.getAmount(), assetChainId, assetId);
                    }
                }
            }
        }
        // Increase the amount transferred in when calling the contractNULSmoney
        BigInteger value = contractData.getValue();
        if (value.compareTo(BigInteger.ZERO) > 0) {
            // initializationNULSTemporary balance of main assets
            tempBalanceManager.getBalance(contractAddress, LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
            tempBalanceManager.addTempBalance(contractAddress, value, LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
        }

        boolean isSuccess = true;
        do {
            // Process temporary balances for contract transfer transactions and contract call transactions generated by other modules in order of transaction generation
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
                // Rollback if processing fails
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
                // Process internal transfer transactions within contracts -> merge、Generate on chain transactions
                isSuccess = contractTransferHandler.handleContractTransferTxs(chainId, blockTime, contractResult, tempBalanceManager);
                if(!isSuccess) {
                    // If internal transfer fails, roll back other newly generated transactions in the contract - Contract balance andnonce
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
            // RollBACK - Deducting the amount transferred when calling the contract
            if (value.compareTo(BigInteger.ZERO) > 0) {
                tempBalanceManager.minusTempBalance(contractAddress, value, LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
            }
            if (toList != null && !toList.isEmpty()) {
                for (CoinTo to : toList) {
                    if (Arrays.equals(to.getAddress(), contractAddress)) {
                        assetChainId = to.getAssetsChainId();
                        assetId = to.getAssetsId();
                        mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                        if (!mainAsset) {
                            // RollBACK - Temporary balance after deducting other assets
                            tempBalanceManager.minusTempBalance(contractAddress, to.getAmount(), assetChainId, assetId);
                        }
                    }
                }
            }
        }
        return isSuccess;
    }

}
