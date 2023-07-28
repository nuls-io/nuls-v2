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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.util.ContractUtil.asBytes;
import static io.nuls.contract.util.ContractUtil.mapAddBigInteger;

/**
 * @author: PierreLuo
 * @date: 2019-04-28
 */
@Component
public class ContractNewTxFromOtherModuleHandler {

    @Autowired
    private NulsCoresConfig contractConfig;
    /**
     * 更新临时nonce和vm内维护的合约余额
     */
    public Transaction updateNonceAndVmBalance(int chainId, byte[] contractAddressBytes, String txHash, String txStr, Frame frame) {
        try {
            byte[] txBytes = RPCUtil.decode(txStr);
            Transaction tx = new Transaction();
            tx.parse(txBytes, 0);
            byte[] addressBytes;
            CoinFrom contractFrom = null;

            CoinData coinData = tx.getCoinDataInstance();

            // 检查合约地址
            List<CoinFrom> fromList = coinData.getFrom();

            boolean existContract = false;
            for (CoinFrom from : fromList) {
                if(Arrays.equals(contractAddressBytes, from.getAddress())) {
                    contractFrom = from;
                    existContract = true;
                    break;
                }
            }
            if(!existContract) {
                throw new RuntimeException("Illegal transaction: contract address must be exist in data of coin-from.");
            }
            boolean isUnlockTx = contractFrom.getLocked() == (byte) -1;
            ProgramAccount account = frame.vm.getProgramExecutor().getAccount(contractAddressBytes, contractFrom.getAssetsChainId(), contractFrom.getAssetsId());

            // 普通交易，更新nonce
            if(!isUnlockTx) {
                byte[] hashBytes = HexUtil.decode(txHash);
                byte[] currentNonceBytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
                account.setNonce(RPCUtil.encode(currentNonceBytes));
            }

            // 更新vm balance
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, List.of(tx));
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractFromLockValue = contracts[1];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[2];
            LinkedHashMap<String, BigInteger> contractToLockValue = contracts[3];

            byte[] contractBytes;
            int assetChainId, assetId;
            ProgramExecutorImpl programExecutor = frame.vm.getProgramExecutor();
            // 增加锁定转入
            Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                String key = lockTo.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                programExecutor.getAccount(contractBytes, assetChainId, assetId).addFreeze(lockTo.getValue());
            }
            // 增加转入
            Set<Map.Entry<String, BigInteger>> _tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : _tos) {
                String key = to.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                programExecutor.getAccount(contractBytes, assetChainId, assetId).addBalance(to.getValue());
            }
            // 扣除锁定转出
            Set<Map.Entry<String, BigInteger>> lockFroms = contractFromLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockFrom : lockFroms) {
                String key = lockFrom.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                programExecutor.getAccount(contractBytes, assetChainId, assetId).addFreeze(lockFrom.getValue().negate());
            }
            // 扣除转出
            Set<Map.Entry<String, BigInteger>> _froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : _froms) {
                String key = from.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                programExecutor.getAccount(contractBytes, assetChainId, assetId).addBalance(from.getValue().negate());
            }


            return tx;
        } catch (NulsException e) {
            Log.error(e);
            throw new RuntimeException(e);
        }
    }

    public boolean refreshTempBalance(int chainId, byte[] contractAddressBytes, List<ProgramNewTx> programNewTxList, ContractTempBalanceManager tempBalanceManager) {
        try {
            List<Transaction> collect = programNewTxList.stream().map(a -> a.getTx()).collect(Collectors.toList());
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, collect);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractFromLockValue = contracts[1];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[2];
            LinkedHashMap<String, BigInteger> contractToLockValue = contracts[3];
            byte[] contractBytes;
            int assetChainId, assetId;
            // 增加锁定转入
            Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                String key = lockTo.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.addLockedTempBalance(contractBytes, lockTo.getValue(), assetChainId, assetId);
            }
            // 增加转入
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                String key = to.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.addTempBalance(contractBytes, to.getValue(), assetChainId, assetId);
            }
            // 扣除锁定转出
            Set<Map.Entry<String, BigInteger>> lockFroms = contractFromLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockFrom : lockFroms) {
                String key = lockFrom.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.minusLockedTempBalance(contractBytes, lockFrom.getValue(), assetChainId, assetId);
            }
            // 扣除转出
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                String key = from.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.minusTempBalance(contractBytes, from.getValue(), assetChainId, assetId);
            }
            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }

    private LinkedHashMap<String, BigInteger>[] filterContractValue(int chainId, List<Transaction> programNewTxList) throws NulsException {
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
        for (Transaction tx : programNewTxList) {
            txTime = tx.getTime();
            CoinData coinData = tx.getCoinDataInstance();

            List<CoinFrom> froms = coinData.getFrom();
            List<CoinTo> tos = coinData.getTo();

            for (CoinFrom from : froms) {
                fromAddress = from.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, fromAddress)) {
                    continue;
                }
                int assetChainId = from.getAssetsChainId();
                int assetId = from.getAssetsId();
                if(isLockedAmount(txTime, from.getLocked())) {
                    mapAddBigInteger(contractFromLockValue, fromAddress, assetChainId, assetId, from.getAmount());
                } else {
                    mapAddBigInteger(contractFromValue, fromAddress, assetChainId, assetId, from.getAmount());
                }
            }

            for (CoinTo to : tos) {
                toAddress = to.getAddress();
                if (!ContractUtil.isLegalContractAddress(chainId, toAddress)) {
                    continue;
                }
                int assetChainId = to.getAssetsChainId();
                int assetId = to.getAssetsId();
                if (isLockedAmount(txTime, to.getLockTime())) {
                    mapAddBigInteger(contractToLockValue, toAddress, assetChainId, assetId, to.getAmount());
                } else {
                    mapAddBigInteger(contractToValue, toAddress, assetChainId, assetId, to.getAmount());
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

    public boolean rollbackTempBalance(int chainId, byte[] contractAddressBytes, List<ProgramNewTx> programNewTxList, ContractTempBalanceManager tempBalanceManager) {
        try {
            List<Transaction> collect = programNewTxList.stream().map(a -> a.getTx()).collect(Collectors.toList());
            LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, collect);
            LinkedHashMap<String, BigInteger> contractFromValue = contracts[0];
            LinkedHashMap<String, BigInteger> contractFromLockValue = contracts[1];
            LinkedHashMap<String, BigInteger> contractToValue = contracts[2];
            LinkedHashMap<String, BigInteger> contractToLockValue = contracts[3];
            byte[] contractBytes;
            int assetChainId, assetId;
            // 增加转出
            Set<Map.Entry<String, BigInteger>> froms = contractFromValue.entrySet();
            for (Map.Entry<String, BigInteger> from : froms) {
                String key = from.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes, assetChainId, assetId).getData();
                if (StringUtils.isNotBlank(balance.getPreNonce())) {
                    balance.setNonce(balance.getPreNonce());
                }
                tempBalanceManager.addTempBalance(contractBytes, from.getValue(), assetChainId, assetId);
            }
            // 增加锁定转出
            Set<Map.Entry<String, BigInteger>> lockFroms = contractFromLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockFrom : lockFroms) {
                String key = lockFrom.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                ContractBalance balance = tempBalanceManager.getBalance(contractBytes, assetChainId, assetId).getData();
                if(StringUtils.isNotBlank(balance.getPreNonce())) {
                    balance.setNonce(balance.getPreNonce());
                }
                tempBalanceManager.addLockedTempBalance(contractBytes, lockFrom.getValue(), assetChainId, assetId);
            }
            // 扣除转入
            Set<Map.Entry<String, BigInteger>> tos = contractToValue.entrySet();
            for (Map.Entry<String, BigInteger> to : tos) {
                String key = to.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.minusTempBalance(contractBytes, to.getValue(), assetChainId, assetId);
            }
            // 扣除锁定转入
            Set<Map.Entry<String, BigInteger>> lockTos = contractToLockValue.entrySet();
            for (Map.Entry<String, BigInteger> lockTo : lockTos) {
                String key = lockTo.getKey();
                String[] keySplit = key.split(ContractConstant.LINE);
                contractBytes = asBytes(keySplit[0]);
                assetChainId = Integer.parseInt(keySplit[1]);
                assetId = Integer.parseInt(keySplit[2]);
                // 初始化临时余额
                tempBalanceManager.getBalance(contractBytes, assetChainId, assetId);
                tempBalanceManager.minusLockedTempBalance(contractBytes, lockTo.getValue(), assetChainId, assetId);
            }

            return true;
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
    }
}
