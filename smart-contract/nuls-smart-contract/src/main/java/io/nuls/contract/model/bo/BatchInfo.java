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
package io.nuls.contract.model.bo;

import io.nuls.base.data.BlockHeader;
import io.nuls.contract.enums.BatchInfoStatus;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.thread.TimeService;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author: PierreLuo
 * @date: 2019-03-16
 */
@Data
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
     * 打包的区块高度
     */
    private long height;
    /**
     * 批量开始执行时间
     */
    private long beginTime;
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


    public BatchInfo() {
        this.status = BatchInfoStatus.NOT_STARTING;
        this.contractContainerMap = new LinkedHashMap<>();
    }

    public boolean hasBegan() {
        return status.status() > 0;
    }

    public boolean isTimeOut() {
        long time = TimeService.currentTimeMillis() - this.beginTime;
        return time > TIME_OUT;
    }

    public void init(long height) {
        this.clear();
        this.height = height;
        this.beginTime = TimeService.currentTimeMillis();
        this.status = BatchInfoStatus.STARTING;
    }

    public void clear() {
        this.tempBalanceManager = null;
        this.currentBlockHeader = null;
        this.contractPackageDto = null;
        this.batchExecutor = null;
        this.height = -1L;
        this.beginTime = -1L;
        this.status = BatchInfoStatus.NOT_STARTING;
        this.preStateRoot = null;
        this.checker = null;
        this.contractContainerMap = new LinkedHashMap<>();
    }

    public ContractContainer newAndGetContractContainer(String contractAddress) {
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
}
