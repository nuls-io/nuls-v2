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
package io.nuls.contract.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractConflictChecker;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ContractTxProcessorManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.service.*;
import io.nuls.contract.storage.ContractExecuteResultStorageService;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.RPCUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
        BatchInfo batchInfo = chain.getBatchInfo();
        // 清空上次批量的所有数据
        batchInfo.clear();
        batchInfo.init(blockHeight);
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
            if (Log.isDebugEnabled()) {
                Log.debug("[Invoke Contract] TxType is [{}], hash is [{}]", tx.getType(), tx.getHash().toString());
            }
            Chain chain = contractHelper.getChain(chainId);
            BatchInfo batchInfo = chain.getBatchInfo();
            if (!batchInfo.hasBegan()) {
                return getFailed();
            }
            byte[] contractAddressBytes = ContractUtil.extractContractAddressFromTxData(tx);
            String contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
            ContractContainer container = batchInfo.newAndGetContractContainer(contractAddress);
            ContractWrapperTransaction wrapperTx = ContractUtil.parseContractTransaction(tx);
            wrapperTx.setOrder(batchInfo.getAndIncreaseTxCounter());

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
    public Result beforeEnd(int chainId, long blockHeight) {
        try {
            BatchInfo batchInfo = contractHelper.getChain(chainId).getBatchInfo();
            if (!batchInfo.hasBegan()) {
                return getFailed();
            }
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
            if (!batchInfo.hasBegan()) {
                return getFailed();
            }
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
    public Result commitProcessor(int chainId, List<String> txDataList, String blockHeaderHex) {
        try {
            ContractPackageDto contractPackageDto = contractHelper.getChain(chainId).getBatchInfo().getContractPackageDto();
            if (contractPackageDto != null) {
                BlockHeader header = new BlockHeader();
                header.parse(RPCUtil.decode(blockHeaderHex), 0);
                // 保存智能合约链下交易hash
                contractOfflineTxHashListStorageService.saveOfflineTxHashList(chainId, header.getHash().getBytes(), new ContractOfflineTxHashPo(contractPackageDto.getOfflineTxHashList()));

                Map<String, ContractResult> contractResultMap = contractPackageDto.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                if (Log.isDebugEnabled()) {
                    Log.debug("contract execute txDataSize is {}, commit txDataSize is {}", contractResultMap.keySet().size(), txDataList.size());
                }
                for (String txData : txDataList) {
                    contractResult = contractResultMap.get(txData);
                    if (contractResult == null) {
                        Log.warn("empty contract result with txData: {}", txData);
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    switch (wrapperTx.getType()) {
                        case CREATE_CONTRACT:
                            contractTxProcessorManager.createCommit(chainId, wrapperTx);
                            break;
                        case CALL_CONTRACT:
                            contractTxProcessorManager.callCommit(chainId, wrapperTx);
                            break;
                        case DELETE_CONTRACT:
                            contractTxProcessorManager.deleteCommit(chainId, wrapperTx);
                            break;
                        default:
                            break;
                    }
                }
            }

            return getSuccess();
        } catch (Exception e) {
            return getFailed();
        } finally {
            // 移除临时余额, 临时区块头等当前批次执行数据
            Chain chain = contractHelper.getChain(chainId);
            BatchInfo batchInfo = chain.getBatchInfo();
            batchInfo.clear();
        }
    }

    @Override
    public Result rollbackProcessor(int chainId, List<String> txDataList, String blockHeaderHex) {
        try {
            Transaction tx;
            for (String txData : txDataList) {
                tx = new Transaction();
                tx.parse(RPCUtil.decode(txData), 0);
                switch (tx.getType()) {
                    case CREATE_CONTRACT:
                        CreateContractData create = new CreateContractData();
                        create.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.createRollback(chainId, new ContractWrapperTransaction(tx, create));
                        break;
                    case CALL_CONTRACT:
                        CallContractData call = new CallContractData();
                        call.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.callRollback(chainId, new ContractWrapperTransaction(tx, call));
                        break;
                    case DELETE_CONTRACT:
                        DeleteContractData delete = new DeleteContractData();
                        delete.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.deleteRollback(chainId, new ContractWrapperTransaction(tx, delete));
                        break;
                    default:
                        break;
                }
            }

            return getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }

    @Override
    public Result saveContractExecuteResult(int chainId, NulsHash hash, ContractResult result) {
        if (hash == null || result == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        contractHelper.updateLastedPriceForAccount(chainId, result.getSender(), result.getPrice());
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
