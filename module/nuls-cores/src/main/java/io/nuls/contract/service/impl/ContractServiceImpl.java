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
package io.nuls.contract.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.contract.callable.ContractTxCallableV14;
import io.nuls.contract.callable.ContractTxCallableV8;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.manager.ContractTxProcessorManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.contract.model.tx.*;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.*;
import io.nuls.contract.storage.ContractExecuteResultStorageService;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.constant.ContractErrorCode.FAILED;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;
import static io.nuls.core.constant.TxType.*;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Component
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractCaller contractCaller;

    @Autowired
    private ResultAnalyzer resultAnalyzer;

    @Autowired
    private ResultHanlder resultHanlder;

    @Autowired
    private ContractExecutor contractExecutor;

    @Autowired
    private ContractHelper contractHelper;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private ContractExecuteResultStorageService contractExecuteResultStorageService;

    @Autowired
    private ContractOfflineTxHashListStorageService contractOfflineTxHashListStorageService;

    @Autowired
    private ContractTxProcessorManager contractTxProcessorManager;
    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;

    @Override
    public Result begin(int chainId, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        Log.info("[Begin contract batch] packaging blockHeight is [{}], packaging address is [{}], preStateRoot is [{}]", blockHeight, packingAddress, preStateRoot);
        Chain chain = contractHelper.getChain(chainId);
        BatchInfo batchInfo = new BatchInfo(blockHeight);
        chain.setBatchInfo(null);
        // 初始化批量执行基本数据
        chain.setBatchInfo(batchInfo);
        // 准备临时余额和当前区块头
        contractHelper.createTempBalanceManagerAndCurrentBlockHeader(chainId, blockHeight, blockTime, AddressTool.getAddress(packingAddress));
        // 准备批量执行器
        ProgramExecutor batchExecutor = contractExecutor.createBatchExecute(chainId, RPCUtil.decode(preStateRoot));
        batchInfo.setBatchExecutor(batchExecutor);
        batchInfo.setPreStateRoot(preStateRoot);
        // 准备冲突检测器
        ContractConflictChecker checker = ContractConflictChecker.newInstance();
        checker.setContractSetList(new CopyOnWriteArrayList<>());
        batchInfo.setChecker(checker);
        return getSuccess();
    }

    @Override
    public Result beginV8(int chainId, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {

        Log.info("[Begin contract batch] packaging blockHeight is [{}], packaging address is [{}], preStateRoot is [{}]", blockHeight, packingAddress, preStateRoot);
        Chain chain = contractHelper.getChain(chainId);
        BatchInfoV8 batchInfo = new BatchInfoV8(blockHeight);
        // 初始化批量执行基本数据
        chain.setBatchInfoV8(batchInfo);
        // 准备临时余额和当前区块头
        ContractTempBalanceManager tempBalanceManager = ContractTempBalanceManager.newInstance(chainId);
        BlockHeader tempHeader = new BlockHeader();
        tempHeader.setHeight(blockHeight);
        tempHeader.setTime(blockTime);
        tempHeader.setPackingAddress(AddressTool.getAddress(packingAddress));
        batchInfo.setTempBalanceManager(tempBalanceManager);
        batchInfo.setCurrentBlockHeader(tempHeader);
        // 准备批量执行器
        ProgramExecutor batchExecutor = contractExecutor.createBatchExecute(chainId, RPCUtil.decode(preStateRoot));
        batchInfo.setBatchExecutor(batchExecutor);
        batchInfo.setPreStateRoot(preStateRoot);
        Map<String, String> result = new HashMap<>();
        return getSuccess().setData(result);
    }


    private Result validContractTx(int chainId, Transaction tx) {
        try {
            Result result;
            switch (tx.getType()) {
                case CREATE_CONTRACT:
                    CreateContractTransaction create = new CreateContractTransaction();
                    create.copyTx(tx);
                    result = contractTxValidatorManager.createValidator(chainId, create);
                    break;
                case CALL_CONTRACT:
                    CallContractTransaction call = new CallContractTransaction();
                    call.copyTx(tx);
                    result = contractTxValidatorManager.callValidator(chainId, call);
                    break;
                case DELETE_CONTRACT:
                    DeleteContractTransaction delete = new DeleteContractTransaction();
                    delete.copyTx(tx);
                    result = contractTxValidatorManager.deleteValidator(chainId, delete);
                    break;
                default:
                    result = getSuccess();
                    break;
            }
            return result;
        } catch (NulsException e) {
            return getFailed();
        }
    }

    @Override
    public Result invokeContractOneByOne(int chainId, ContractTempTransaction tx) {
        try {
            Log.info("[Invoke Contract] TxType is [{}], hash is [{}]", tx.getType(), tx.getHash().toString());
            tx.setChainId(chainId);
            ContractWrapperTransaction wrapperTx = ContractUtil.parseContractTransaction(tx, chainManager);
            // add by pierre at 2019-10-20
            if (wrapperTx == null) {
                return getSuccess();
            }
            // end code by pierre
            Chain chain = contractHelper.getChain(chainId);
            BatchInfo batchInfo = chain.getBatchInfo();
            wrapperTx.setOrder(batchInfo.getAndIncreaseTxCounter());
            ContractData contractData = wrapperTx.getContractData();
            byte[] contractAddressBytes = contractData.getContractAddress();
            String contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
            ContractContainer container = batchInfo.newOrGetContractContainer(contractAddress);

            // 验证合约交易
            Result validResult = this.validContractTx(chainId, tx);
            if (validResult.isFailed()) {
                return validResult;
            }
            String preStateRoot = batchInfo.getPreStateRoot();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            // 等上次的执行完
            container.loadFutureList();
            // 多线程执行合约
            Result result = contractCaller.callTx(chainId, container, batchExecutor, wrapperTx, preStateRoot);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        } catch (ExecutionException e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    @Override
    public Result invokeContractOneByOneV8(int chainId, ContractTempTransaction tx) {
        try {
            Log.info("[Invoke Contract] TxType is [{}], hash is [{}]", tx.getType(), tx.getHash().toString());
            tx.setChainId(chainId);
            ContractWrapperTransaction wrapperTx = ContractUtil.parseContractTransaction(tx, chainManager);
            // add by pierre at 2019-10-20
            if (wrapperTx == null) {
                return getSuccess();
            }
            // end code by pierre
            Chain chain = contractHelper.getChain(chainId);
            BatchInfoV8 batchInfo = chain.getBatchInfoV8();
            wrapperTx.setOrder(batchInfo.getAndIncreaseTxCounter());
            // 验证合约交易
            Result validResult = this.validContractTx(chainId, tx);
            if (validResult.isFailed()) {
                return validResult;
            }
            String preStateRoot = batchInfo.getPreStateRoot();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            // 执行合约
            Result result = callTx(chainId, batchExecutor, wrapperTx, preStateRoot, batchInfo);
            if (result.isSuccess()) {
                Map<String, Object> _result = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) result.getData();
                _result.put("success", map.get("success"));
                _result.put("gasUsed", map.get("gasUsed"));
                _result.put("txList", map.get("txList"));
                return result.setData(_result);
            }
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    @Override
    public Result invokeContractOneByOneV14(int chainId, ContractTempTransaction tx) {
        // add by pierre at 2022/6/2 p14
        try {
            Log.info("[Invoke Contract] TxType is [{}], hash is [{}]", tx.getType(), tx.getHash().toString());
            tx.setChainId(chainId);
            ContractWrapperTransaction wrapperTx = ContractUtil.parseContractTransaction(tx, chainManager);
            if (wrapperTx == null) {
                return getSuccess();
            }
            Chain chain = contractHelper.getChain(chainId);
            BatchInfoV8 batchInfo = chain.getBatchInfoV8();
            wrapperTx.setOrder(batchInfo.getAndIncreaseTxCounter());
            // 验证合约交易
            Result validResult = this.validContractTx(chainId, tx);
            if (validResult.isFailed()) {
                return validResult;
            }
            String preStateRoot = batchInfo.getPreStateRoot();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            // 执行合约
            Result result = callTxV14(chainId, batchExecutor, wrapperTx, preStateRoot, batchInfo);
            if (result.isSuccess()) {
                Map<String, Object> _result = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) result.getData();
                _result.put("success", map.get("success"));
                _result.put("gasUsed", map.get("gasUsed"));
                _result.put("txList", map.get("txList"));
                return result.setData(_result);
            }
            return result;
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        }
    }

    protected Result callTx(int chainId, ProgramExecutor batchExecutor, ContractWrapperTransaction tx, String preStateRoot, BatchInfoV8 batchInfo) {
        try {
            ContractData contractData = tx.getContractData();
            Integer blockType = Chain.currentThreadBlockType();
            byte[] contractAddressBytes = contractData.getContractAddress();
            String contract = AddressTool.getStringAddressByBytes(contractAddressBytes);
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            long blockTime = currentBlockHeader.getTime();
            long lastestHeight = currentBlockHeader.getHeight() - 1;
            ContractTxCallableV8 txCallable = new ContractTxCallableV8(chainId, blockType, blockTime, batchExecutor, contract, tx, lastestHeight, preStateRoot);
            ContractResult contractResult = txCallable.call();
            batchInfo.getContractResultMap().put(tx.getHash().toString(), contractResult);
            // 提取需要返回的结果数据
            Map<String, Object> result = this.extractDataFromContractResult(contractResult);
            batchInfo.getOfflineTxHashList().addAll((List<byte[]>)result.get("txHashList"));
            return getSuccess().setData(result);
        } catch (Exception e) {
            Log.error(e);
            return getFailed();
        }
    }

    protected Result callTxV14(int chainId, ProgramExecutor batchExecutor, ContractWrapperTransaction tx, String preStateRoot, BatchInfoV8 batchInfo) {
        try {
            ContractData contractData = tx.getContractData();
            Integer blockType = Chain.currentThreadBlockType();
            byte[] contractAddressBytes = contractData.getContractAddress();
            String contract = AddressTool.getStringAddressByBytes(contractAddressBytes);
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            long blockTime = currentBlockHeader.getTime();
            long lastestHeight = currentBlockHeader.getHeight() - 1;
            ContractTxCallableV14 txCallable = new ContractTxCallableV14(chainId, blockType, blockTime, batchExecutor, contract, tx, lastestHeight, preStateRoot);
            ContractResult contractResult = txCallable.call();
            batchInfo.getContractResultMap().put(tx.getHash().toString(), contractResult);
            // 提取需要返回的结果数据
            Map<String, Object> result = this.extractDataFromContractResult(contractResult);
            batchInfo.getOfflineTxHashList().addAll((List<byte[]>)result.get("txHashList"));
            return getSuccess().setData(result);
        } catch (Exception e) {
            Log.error(e);
            return getFailed();
        }
    }

    protected Map<String, Object> extractDataFromContractResult(ContractResult contractResult) throws IOException {
        List<byte[]> offlineTxHashList = new ArrayList<>();
        List<String> resultTxList = new ArrayList<>();
        List<ContractTransferTransaction> contractTransferList;
        List<ProgramInvokeRegisterCmd> invokeRegisterCmds;
        String newTx, newTxHash;
        ProgramNewTx programNewTx;
        // [外部模块调用生成的交易]
        invokeRegisterCmds = contractResult.getInvokeRegisterCmds();
        for (ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
            if (!invokeRegisterCmd.getCmdRegisterMode().equals(CmdRegisterMode.NEW_TX)) {
                continue;
            }
            programNewTx = invokeRegisterCmd.getProgramNewTx();
            if (StringUtils.isNotBlank(newTxHash = programNewTx.getTxHash())) {
                offlineTxHashList.add(RPCUtil.decode(newTxHash));
            }
            if (StringUtils.isNotBlank(newTx = programNewTx.getTxString())) {
                resultTxList.add(newTx);
            }
        }
        // [合约内部转账交易]
        contractTransferList = contractResult.getContractTransferList();
        for(Transaction tx : contractTransferList) {
            newTx = RPCUtil.encode(tx.serialize());
            contractResult.getContractTransferTxStringList().add(newTx);
            resultTxList.add(newTx);
            offlineTxHashList.add(tx.getHash().getBytes());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", contractResult.isSuccess());
        result.put("gasUsed", contractResult.getGasUsed());
        result.put("txList", resultTxList);
        result.put("txHashList", offlineTxHashList);
        return result;
    }



    @Override
    public Result beforeEnd(int chainId, long blockHeight) {
        try {
            Result result = contractCaller.callBatchEnd(chainId, blockHeight);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return getFailed();
        }
    }

    @Override
    public Result end(int chainId, long blockHeight) {

        try {
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            Future<ContractPackageDto> future = batchInfo.getContractPackageDtoFuture();
            // 等待before_end执行完成
            future.get();
            ContractPackageDto dto = batchInfo.getContractPackageDto();
            if (dto == null) {
                return getFailed();
            }
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            Result<byte[]> batchExecuteResult = contractExecutor.commitBatchExecute(batchExecutor);
            byte[] stateRoot = batchExecuteResult.getData();
            currentBlockHeader.setStateRoot(stateRoot);
            dto.setStateRoot(stateRoot);
            return getSuccess().setData(dto);
        } catch (Exception e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }

    @Override
    public Result packageEnd(int chainId, long blockHeight) {
        /*
         before = 0
         now0 = 100
         timeOut0 = 700
         firstCost = 200
         */
        try {
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            // 判断超时时间之前，获取此对象，不为空说明已经执行结束，可跳过直接执行后面处理结果的步骤
            ContractPackageDto dto;
            do {
                dto = batchInfo.getContractPackageDto();
                if (dto != null) {
                    break;
                }
                long beforeEndTime = batchInfo.getBeforeEndTime();
                long now0 = System.currentTimeMillis();
                long timeOut = 1200 - (now0 - beforeEndTime);
                if (timeOut <= 0) {
                    Log.warn("超过了预留的超时时间[0]: {}", timeOut);
                    break;
                }
                Log.info("预留的超时时间[0]: {}", timeOut);
                Future<ContractPackageDto> future = batchInfo.getContractPackageDtoFuture();
                try {
                    // 等待before_end执行完成
                    future.get(timeOut, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    Log.error("wait end time out[0]", e.getMessage());
                }
                dto = batchInfo.getContractPackageDto();
                if (dto != null) {
                    break;
                }
                long now1 = System.currentTimeMillis();
                Log.info("第一次花费的时间: {}", now1 - beforeEndTime);
                // 若超过了区块合约gas或者txCount限制，则中断未执行完的线程
                if (batchInfo.isExceed()) {
                    Map<String, Future<ContractResult>> contractMap = batchInfo.getContractMap();
                    if (!contractMap.isEmpty()) {
                        Set<Map.Entry<String, Future<ContractResult>>> entries = contractMap.entrySet();
                        String hash;
                        Future<ContractResult> _future;
                        int count = 0;
                        for (Map.Entry<String, Future<ContractResult>> entry : entries) {
                            hash = entry.getKey();
                            _future = entry.getValue();
                            if (_future.isDone()) {
                                continue;
                            }
                            _future.cancel(true);
                            batchInfo.addPendingTxHashList(hash);
                            count++;
                        }
                        Log.warn("超过了区块合约gas或者txCount限制，中断未执行完的交易数量: {}", count);
                    }
                }

                long now2 = System.currentTimeMillis();
                timeOut = 1500 - (now2 - beforeEndTime);
                Log.info("预留的超时时间[1]: {}", timeOut);
                if (timeOut <= 0) {
                    Log.warn("超过了预留的超时时间[1]: {}", timeOut);
                    break;
                }
                // 最终等待before_end执行完成
                future.get();
                Log.info("触发END期间 - 合约执行花费的时间: {}", System.currentTimeMillis() - beforeEndTime);
            } while (false);
            if (dto == null) {
                return getFailed();
            }
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            long s = 0L;
            if (Log.isDebugEnabled()) {
                s = System.currentTimeMillis();
            }
            Result<byte[]> batchExecuteResult = contractExecutor.commitBatchExecute(batchExecutor);
            long e;
            if (Log.isDebugEnabled()) {
                e = System.currentTimeMillis();
                Log.debug("合约提交持久化时间cost: {}", e - s);
            }
            byte[] stateRoot = batchExecuteResult.getData();
            currentBlockHeader.setStateRoot(stateRoot);
            dto.setStateRoot(stateRoot);
            return getSuccess().setData(dto);
        } catch (Exception e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }

    @Override
    public Result endV8(int chainId, long blockHeight) {
        try {
            BatchInfoV8 batchInfo = contractHelper.getChain(chainId).getBatchInfoV8();
            BlockHeader currentBlockHeader = batchInfo.getCurrentBlockHeader();
            ProgramExecutor batchExecutor = batchInfo.getBatchExecutor();
            Result<byte[]> batchExecuteResult = contractExecutor.commitBatchExecute(batchExecutor);
            byte[] stateRoot = batchExecuteResult.getData();
            currentBlockHeader.setStateRoot(stateRoot);

            List<String> txList = new ArrayList<>();
            // 生成退还剩余Gas的交易
            ContractReturnGasTransaction returnGasTx = contractHelper.makeReturnGasTx(new ArrayList<>(batchInfo.getContractResultMap().values()), batchInfo.getCurrentBlockHeader().getTime());
            if (returnGasTx != null) {
                txList.add(RPCUtil.encode(returnGasTx.serialize()));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("stateRoot", HexUtil.encode(stateRoot));
            result.put("txList", txList);
            return getSuccess().setData(result);
        } catch (Exception e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }

    @Override
    public Result packageEndV8(int chainId, long blockHeight) {
        return this.endV8(chainId, blockHeight);
    }

    @Override
    public Result saveContractExecuteResult(int chainId, NulsHash hash, ContractResult result) {
        if (hash == null || result == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        return contractExecuteResultStorageService.saveContractExecuteResult(chainId, hash, result);
    }

    @Override
    public Result deleteContractExecuteResult(int chainId, NulsHash hash) {
        if (hash == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        return contractExecuteResultStorageService.deleteContractExecuteResult(chainId, hash);
    }

    @Override
    public ContractResult getContractExecuteResult(int chainId, NulsHash hash) {
        if (hash == null) {
            return null;
        }
        return contractExecuteResultStorageService.getContractExecuteResult(chainId, hash);
    }

    @Override
    public Result<ContractOfflineTxHashPo> getContractOfflineTxHashList(Integer chainId, String blockHash) throws NulsException {
        return contractOfflineTxHashListStorageService.getOfflineTxHashList(chainId, RPCUtil.decode(blockHash));
    }


}
