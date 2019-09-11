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
package io.nuls.contract.model.bo;

import io.nuls.base.data.BlockHeader;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.enums.BatchInfoStatus;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.model.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: PierreLuo
 * @date: 2019-03-16
 */
public class BatchInfo {

    /**
     * 当前批量执行超时时间 - 10秒
     */
    private static final long TIME_OUT = 10L * 1000L;
    /**
     * 智能合约临时余额
     */
    private ContractTempBalanceManager tempBalanceManager;

    /**
     * 当前正在打包的区块头
     */
    private BlockHeader currentBlockHeader;
    /**
     * 智能合约交易打包结果
     */
    private ContractPackageDto contractPackageDto;

    /**
     * 批量执行器
     */
    private ProgramExecutor batchExecutor;
    /**
     * 打包的交易计数器
     */
    private int txCounter;
    /**
     * 打包的区块高度
     */
    private long height;
    /**
     * 批量开始执行时间
     */
    private long beginTime;
    /**
     * 停止接收交易开始时间
     */
    private long beforeEndTime;
    /**
     * 本次批量执行总共消耗的gas
     */
    private long gasCostTotal;
    /**
     * 本次批量执行总交易数
     */
    private int txTotal;
    /**
     * 0 - 未开始， 1 - 已开始
     */
    private BatchInfoStatus status;

    /**
     * 上一区块世界状态根
     */
    private String preStateRoot;

    /**
     * 并行执行的合约容器
     */
    private LinkedHashMap<String, ContractContainer> contractContainerMap;

    /**
     * 合约执行冲突检测器
     */
    private ContractConflictChecker checker;

    /**
     * 打包异步执行结果
     */
    private Future<ContractPackageDto> contractPackageDtoFuture;

    /**
     * gas变化锁
     */
    private final ReadWriteLock gasLock = new ReentrantReadWriteLock();
    /**
     * 未处理交易hash列表锁
     */
    private final ReentrantLock pendingTxListlock = new ReentrantLock();

    /**
     * 因区块GAS用尽，未处理的合约交易
     */
    private List<String> pendingTxHashList;
    private Map<String, Future<ContractResult>> contractMap;

    /**
     * 串行标记数字
     */
    private int serialOrder;

    public BatchInfo(long height) {
        this.txCounter = 0;
        this.height = height;
        this.gasCostTotal = 0L;
        this.txTotal = 0;
        this.beginTime = System.currentTimeMillis();
        this.status = BatchInfoStatus.STARTING;
        this.contractContainerMap = new LinkedHashMap<>();
        this.pendingTxHashList = new ArrayList<>();
        this.contractMap = new ConcurrentHashMap<>();
        this.serialOrder = 0;
    }

    public boolean hasBegan() {
        return status.status() > 0;
    }

    public ContractContainer newOrGetContractContainer(String contractAddress) {
        if (StringUtils.isBlank(contractAddress)) {
            return null;
        }
        ContractContainer container = contractContainerMap.get(contractAddress);
        if (container == null) {
            Set<String> commitSet = new HashSet<>();
            checker.add(commitSet);
            container = new ContractContainer(contractAddress, commitSet, new ArrayList<>());
            contractContainerMap.put(contractAddress, container);
        }
        return container;
    }

    public int getAndIncreaseTxCounter() {
        return txCounter++;
    }

    public int getSerialOrder() {
        return serialOrder;
    }

    public void setSerialOrder(int serialOrder) {
        this.serialOrder = serialOrder;
    }

    public ContractTempBalanceManager getTempBalanceManager() {
        return tempBalanceManager;
    }

    public void setTempBalanceManager(ContractTempBalanceManager tempBalanceManager) {
        this.tempBalanceManager = tempBalanceManager;
    }

    public BlockHeader getCurrentBlockHeader() {
        return currentBlockHeader;
    }

    public void setCurrentBlockHeader(BlockHeader currentBlockHeader) {
        this.currentBlockHeader = currentBlockHeader;
    }

    public ContractPackageDto getContractPackageDto() {
        return contractPackageDto;
    }

    public void setContractPackageDto(ContractPackageDto contractPackageDto) {
        this.contractPackageDto = contractPackageDto;
    }

    public ProgramExecutor getBatchExecutor() {
        return batchExecutor;
    }

    public void setBatchExecutor(ProgramExecutor batchExecutor) {
        this.batchExecutor = batchExecutor;
    }

    public int getTxCounter() {
        return txCounter;
    }

    public void setTxCounter(int txCounter) {
        this.txCounter = txCounter;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getBeforeEndTime() {
        return beforeEndTime;
    }

    public void setBeforeEndTime(long beforeEndTime) {
        this.beforeEndTime = beforeEndTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public BatchInfoStatus getStatus() {
        return status;
    }

    public void setStatus(BatchInfoStatus status) {
        this.status = status;
    }

    public String getPreStateRoot() {
        return preStateRoot;
    }

    public void setPreStateRoot(String preStateRoot) {
        this.preStateRoot = preStateRoot;
    }

    public LinkedHashMap<String, ContractContainer> getContractContainerMap() {
        return contractContainerMap;
    }

    public void setContractContainerMap(LinkedHashMap<String, ContractContainer> contractContainerMap) {
        this.contractContainerMap = contractContainerMap;
    }

    public ContractConflictChecker getChecker() {
        return checker;
    }

    public void setChecker(ContractConflictChecker checker) {
        this.checker = checker;
    }

    public Future<ContractPackageDto> getContractPackageDtoFuture() {
        return contractPackageDtoFuture;
    }

    public void setContractPackageDtoFuture(Future<ContractPackageDto> contractPackageDtoFuture) {
        this.contractPackageDtoFuture = contractPackageDtoFuture;
    }

    public Map<String, Future<ContractResult>> getContractMap() {
        return contractMap;
    }

    public void setContractMap(Map<String, Future<ContractResult>> contractMap) {
        this.contractMap = contractMap;
    }

    public long getGasCostTotal() {
        return gasCostTotal;
    }

    public List<String> getPendingTxHashList() {
        return pendingTxHashList;
    }

    public void addPendingTxHashList(String txHash) {
        pendingTxListlock.lock();
        try {
            pendingTxHashList.add(txHash);
        } finally {
            pendingTxListlock.unlock();
        }
    }

    public boolean checkGasCostTotal(String txHash) {
        gasLock.readLock().lock();
        try {
            if(isExceed()) {
                addPendingTxHashList(txHash);
                return false;
            }
            return true;
        } finally {
            gasLock.readLock().unlock();
        }
    }

    public boolean isExceed() {
        boolean exceedTx = txTotal > ContractConstant.MAX_CONTRACT_TX_IN_BLOCK;
        boolean exceedGas = gasCostTotal > ContractConstant.MAX_GAS_COST_IN_BLOCK;
        if(exceedTx || exceedGas) {
            return true;
        }
        return false;
    }

    public boolean addGasCostTotal(long gasCost, String txHash) {
        gasLock.writeLock().lock();
        try {
            if(isExceed()) {
                this.addPendingTxHashList(txHash);
                return false;
            }
            this.txTotal += 1;
            this.gasCostTotal += gasCost;
            if(isExceed()) {
                this.addPendingTxHashList(txHash);
                return false;
            } else {
                return true;
            }
        } finally {
            gasLock.writeLock().unlock();
        }
    }
}
