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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.util.ContractUtil.asBytes;
import static io.nuls.contract.util.ContractUtil.mapAddBigInteger;

/**
 * @author: PierreLuo
 * @date: 2019-04-28
 */
@Component
public class ContractNewTxFromOtherModuleHandler {

    /**
     * 更新临时nonce和vm内维护的合约余额
     */
    public Transaction updateNonceAndVmBalance(int chainId, byte[] contractAddressBytes, String txHash, String txStr, Frame frame) {
        try {
            byte[] txBytes = RPCUtil.decode(txStr);
            Transaction tx = new Transaction();
            tx.parse(txBytes, 0);
            byte[] addressBytes;

            CoinData coinData = tx.getCoinDataInstance();

            // 扣除转出
            List<CoinFrom> fromList = coinData.getFrom();
            CoinFrom from0 = fromList.get(0);
            addressBytes = from0.getAddress();
            if (!Arrays.equals(contractAddressBytes, addressBytes)) {
                throw new RuntimeException("not contract address");
            }
            boolean isUnlockTx = from0.getLocked() == (byte) -1;
            ProgramAccount account = frame.vm.getProgramExecutor().getAccount(contractAddressBytes);

            // 普通交易，更新nonce
            if(!isUnlockTx) {
                byte[] hashBytes = HexUtil.decode(txHash);
                byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
                account.setNonce(RPCUtil.encode(currentNonceBytes));
            }

            // 更新vm balance
            LinkedHashMap<String, BigInteger> contractFromValue = MapUtil.createLinkedHashMap(4);
            LinkedHashMap<String, BigInteger> contractToValue = MapUtil.createLinkedHashMap(4);
            byte[] fromAddress, toAddress;
            long txTime;
            txTime = tx.getTime();
            List<CoinFrom> froms = coinData.getFrom();
            List<CoinTo> tos = coinData.getTo();
            for (CoinFrom from : froms) {
                fromAddress = from.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, fromAddress)) {
                    continue;
                }
                if(!isLockedAmount(txTime, from.getLocked())) {
                    mapAddBigInteger(contractFromValue, fromAddress, from.getAmount());
                }
            }
            for (CoinTo to : tos) {
                toAddress = to.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, toAddress)) {
                    continue;
                }
                if (!isLockedAmount(txTime, to.getLockTime())) {
                    mapAddBigInteger(contractToValue, toAddress, to.getAmount());
                }

            }
            byte[] contractBytes;
            ProgramExecutorImpl programExecutor = frame.vm.getProgramExecutor();
            // 扣除转出
            Set<Map.Entry<String, BigInteger>> _froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : _froms) {
                contractBytes = asBytes(from.getKey());
                programExecutor.getAccount(contractBytes).addBalance(from.getValue().negate());
            }
            // 增加转入
            Set<Map.Entry<String, BigInteger>> _tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : _tos) {
                contractBytes = asBytes(to.getKey());
                programExecutor.getAccount(contractBytes).addBalance(to.getValue());
            }
            return tx;
        } catch (NulsException e) {
            Log.error(e);
            throw new RuntimeException(e);
        }
    }

    public boolean refreshTempBalance(int chainId, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        List<ProgramInvokeRegisterCmd> invokeRegisterCmds = contractResult.getInvokeRegisterCmds();
        if (invokeRegisterCmds.isEmpty()) {
            return true;
        }
        List<ProgramNewTx> programNewTxList = new ArrayList<>();
        for (ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
            if (!CmdRegisterMode.NEW_TX.equals(invokeRegisterCmd.getCmdRegisterMode())) {
                continue;
            }
            programNewTxList.add(invokeRegisterCmd.getProgramNewTx());
        }
        if (programNewTxList.isEmpty()) {
            return true;
        }
        byte[] contractAddressBytes = contractResult.getContractAddress();
        return this.refreshTempBalance(chainId, contractAddressBytes, programNewTxList, tempBalanceManager);
    }

    private boolean refreshTempBalance(int chainId, byte[] contractAddressBytes, List<ProgramNewTx> programNewTxList, ContractTempBalanceManager tempBalanceManager) {
        try {
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, programNewTxList);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractFromLockValue = contracts[1];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[2];
            LinkedHashMap<String, BigInteger> contractToLockValue = contracts[3];
            byte[] contractBytes;
            // 增加锁定转入
            Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                contractBytes = asBytes(lockTo.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.addLockedTempBalance(contractBytes, lockTo.getValue());
            }
            // 增加转入
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                contractBytes = asBytes(to.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.addTempBalance(contractBytes, to.getValue());
            }
            // 扣除锁定转出
            Set<Map.Entry<String, BigInteger>> lockFroms = contractFromLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockFrom : lockFroms) {
                contractBytes = asBytes(lockFrom.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.minusLockedTempBalance(contractBytes, lockFrom.getValue());
            }
            // 扣除转出
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                contractBytes = asBytes(from.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.minusTempBalance(contractBytes, from.getValue());
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }

    private LinkedHashMap<String, BigInteger>[] filterContractValue(int chainId, List<ProgramNewTx> programNewTxList) throws NulsException {
        LinkedHashMap<String, BigInteger> contractFromValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractFromLockValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToLockValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger>[] contracts = new LinkedHashMap[4];
        contracts[0] = contractFromValue;
        contracts[1] = contractFromLockValue;
        contracts[2] = contractToValue;
        contracts[3] = contractToLockValue;

        byte[] fromAddress, toAddress;
        long txTime;
        Transaction tx;
        for (ProgramNewTx programNewTx : programNewTxList) {
            tx = programNewTx.getTx();
            txTime = tx.getTime();
            CoinData coinData = tx.getCoinDataInstance();

            List<CoinFrom> froms = coinData.getFrom();
            List<CoinTo> tos = coinData.getTo();

            for (CoinFrom from : froms) {
                fromAddress = from.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, fromAddress)) {
                    continue;
                }
                if(isLockedAmount(txTime, from.getLocked())) {
                    mapAddBigInteger(contractFromLockValue, fromAddress, from.getAmount());
                } else {
                    mapAddBigInteger(contractFromValue, fromAddress, from.getAmount());
                }
            }

            for (CoinTo to : tos) {
                toAddress = to.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, toAddress)) {
                    continue;
                }
                if (isLockedAmount(txTime, to.getLockTime())) {
                    mapAddBigInteger(contractToLockValue, toAddress, to.getAmount());
                } else {
                    mapAddBigInteger(contractToValue, toAddress, to.getAmount());
                }

            }

        }
        return contracts;
    }

    private boolean isLockedAmount(long time, long lockTime) {
        if(lockTime < 0) {
            return true;
        }
        if(time < lockTime) {
            return true;
        }
        return false;
    }

    public void rollbackTempBalance(int chainId, ContractResult contractResult, ContractTempBalanceManager tempBalanceManager) {
        try {
            List<ProgramInvokeRegisterCmd> invokeRegisterCmds = contractResult.getInvokeRegisterCmds();
            if (invokeRegisterCmds.isEmpty()) {
                return;
            }
            List<ProgramNewTx> programNewTxList = new ArrayList<>();
            for (ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
                if (!CmdRegisterMode.NEW_TX.equals(invokeRegisterCmd.getCmdRegisterMode())) {
                    continue;
                }
                programNewTxList.add(invokeRegisterCmd.getProgramNewTx());
            }
            if (programNewTxList.isEmpty()) {
                return;
            }
            byte[] contractAddressBytes = contractResult.getContractAddress();
            this.rollbackTempBalance(chainId, contractAddressBytes, programNewTxList, tempBalanceManager);

            contractResult.getInvokeRegisterCmds().clear();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private boolean rollbackTempBalance(int chainId, byte[] contractAddressBytes, List<ProgramNewTx> programNewTxList, ContractTempBalanceManager tempBalanceManager) {
        try {
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, programNewTxList);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractFromLockValue = contracts[1];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[2];
            LinkedHashMap<String, BigInteger> contractToLockValue = contracts[3];
            byte[] contractBytes;
            // 增加转出
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                contractBytes = asBytes(from.getKey());
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes).getData();
                if (StringUtils.isNotBlank(balance.getPreNonce())) {
                    balance.setNonce(balance.getPreNonce());
                }
                tempBalanceManager.addTempBalance(contractBytes, from.getValue());
            }
            // 增加锁定转出
            Set<Map.Entry<String, BigInteger>> lockFroms = contractFromLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockFrom : lockFroms) {
                contractBytes = asBytes(lockFrom.getKey());
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes).getData();
                if(StringUtils.isNotBlank(balance.getPreNonce())) {
                    balance.setNonce(balance.getPreNonce());
                }
                tempBalanceManager.addLockedTempBalance(contractBytes, lockFrom.getValue());
            }
            // 扣除转入
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                contractBytes = asBytes(to.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.minusTempBalance(contractBytes, to.getValue());
            }
            // 扣除锁定转入
            Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                contractBytes = asBytes(lockTo.getKey());
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes);
                tempBalanceManager.minusLockedTempBalance(contractBytes, lockTo.getValue());
            }

            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }
}
