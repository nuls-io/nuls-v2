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
import io.nuls.contract.enums.BatchInfoStatus;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.vm.program.ProgramExecutor;

import java.util.*;

/**
 * @author: PierreLuo
 * @date: 2019-03-16
 */
public class BatchInfoV8 {

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

    private Map<String, ContractResult> contractResultMap;

    private Set<String> deleteSet = new HashSet<>();
    private Set<String> createSet = new HashSet<>();
    private List<byte[]> offlineTxHashList;
    /**
     * 串行标记数字
     */
    private int serialOrder;

    public BatchInfoV8(long height) {
        this.txCounter = 0;
        this.height = height;
        this.gasCostTotal = 0L;
        this.txTotal = 0;
        this.beginTime = System.currentTimeMillis();
        this.status = BatchInfoStatus.STARTING;
        this.contractResultMap = new HashMap<>();
        this.offlineTxHashList = new ArrayList<>();
        this.serialOrder = 0;
    }

    public List<byte[]> getOfflineTxHashList() {
        return offlineTxHashList;
    }

    public void setOfflineTxHashList(List<byte[]> offlineTxHashList) {
        this.offlineTxHashList = offlineTxHashList;
    }

    public Map<String, ContractResult> getContractResultMap() {
        return contractResultMap;
    }

    public void setContractResultMap(Map<String, ContractResult> contractResultMap) {
        this.contractResultMap = contractResultMap;
    }

    public int getAndIncreaseTxCounter() {
        return txCounter++;
    }

    public static long getTimeOut() {
        return TIME_OUT;
    }

    public Set<String> getDeleteSet() {
        return deleteSet;
    }

    public Set<String> getCreateSet() {
        return createSet;
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

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getBeforeEndTime() {
        return beforeEndTime;
    }

    public void setBeforeEndTime(long beforeEndTime) {
        this.beforeEndTime = beforeEndTime;
    }

    public long getGasCostTotal() {
        return gasCostTotal;
    }

    public void setGasCostTotal(long gasCostTotal) {
        this.gasCostTotal = gasCostTotal;
    }

    public int getTxTotal() {
        return txTotal;
    }

    public void setTxTotal(int txTotal) {
        this.txTotal = txTotal;
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

    public void setDeleteSet(Set<String> deleteSet) {
        this.deleteSet = deleteSet;
    }

    public void setCreateSet(Set<String> createSet) {
        this.createSet = createSet;
    }

    public int getSerialOrder() {
        return serialOrder;
    }

    public void setSerialOrder(int serialOrder) {
        this.serialOrder = serialOrder;
    }
}
