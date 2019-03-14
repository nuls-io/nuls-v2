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

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ContractTxProcessorManager;
import io.nuls.contract.manager.ContractTxValidatorManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.model.tx.ContractReturnGasTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.service.*;
import io.nuls.contract.storage.ContractExecuteResultStorageService;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.ByteArrayWrapper;
import io.nuls.tools.model.LongUtils;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private AddressDistribution addressDistribution;

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
    private ContractTxProcessorManager contractTxProcessorManager;
    @Autowired
    private ContractTxValidatorManager contractTxValidatorManager;

    @Override
    public Result invokeContract(int chainId, List<ContractTempTransaction> txList, long number, long blockTime, String packingAddress, String preStateRoot) {
        try {
            // 准备临时余额和当前区块头
            contractHelper.createTempBalanceManagerAndCurrentBlockHeader(chainId, number, blockTime, Hex.decode(packingAddress));
            // 准备批量执行器
            ProgramExecutor batchExecutor = contractExecutor.createBatchExecute(chainId, Hex.decode(preStateRoot));

            // 交易按合约地址分组
            Map<String, List<ContractWrapperTransaction>> listMap = addressDistribution.distribution(txList);
            // 多线程执行合约
            CallerResult callerResult = contractCaller.caller(chainId, batchExecutor, listMap, preStateRoot);
            // 合约执行结果归类
            AnalyzerResult analyzerResult = resultAnalyzer.analysis(callerResult.getCallableResultList());
            // 重新执行冲突合约，处理失败合约的金额退还
            List<ContractResult> contractResultList = resultHanlder.handleAnalyzerResult(chainId, batchExecutor, analyzerResult, preStateRoot);
            // 归集合约内部转账交易
            List<Transaction> resultTxList = new ArrayList<>();
            for(ContractResult contractResult : contractResultList) {
                resultTxList.addAll(contractResult.getContractTransferList());
            }
            // 生成退还剩余Gas的交易
            ContractReturnGasTransaction contractReturnGasTx = this.makeReturnGasTx(chainId, contractResultList, resultTxList.get(resultTxList.size() - 1).getTime() + 1);
            resultTxList.add(contractReturnGasTx);
            Result<byte[]> batchExecuteResult = contractCaller.commitBatchExecute(batchExecutor);
            byte[] stateRoot = batchExecuteResult.getData();
            ContractPackageDto dto = new ContractPackageDto(stateRoot, resultTxList);
            dto.makeContractResultMap(contractResultList);
            contractHelper.getChain(chainId).setContractPackageDto(dto);
            return getSuccess().setData(dto);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }

    public Result commitProcessor(int chainId, List<String> txHexList, String blockHeaderHex) {
        try {
            ContractPackageDto contractPackageDto = contractHelper.getChain(chainId).getContractPackageDto();
            if(contractPackageDto != null) {
                Map<String, ContractResult> contractResultMap = contractPackageDto.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                for(String txHex : txHexList) {
                    contractResult = contractResultMap.get(txHex);
                    if(contractResult == null) {
                        Log.warn("empty contract result with txHex: {}", txHex);
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    switch (wrapperTx.getType()) {
                        case TX_TYPE_CREATE_CONTRACT:
                            contractTxProcessorManager.createCommit(chainId, wrapperTx);
                            break;
                        case TX_TYPE_CALL_CONTRACT:
                            contractTxProcessorManager.callCommit(chainId, wrapperTx);
                            break;
                        case TX_TYPE_DELETE_CONTRACT:
                            contractTxProcessorManager.callCommit(chainId, wrapperTx);
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
            // 移除临时余额
            contractHelper.removeTempBalanceManagerAndCurrentBlockHeader(chainId);
        }
    }

    public Result rollbackProcessor(int chainId, List<String> txHexList, String blockHeaderHex) {
        try {
            Transaction tx;
            for(String txHex : txHexList) {
                tx = new Transaction();
                tx.parse(Hex.decode(txHex), 0);
                switch (tx.getType()) {
                    case TX_TYPE_CREATE_CONTRACT:
                        CreateContractData create = new CreateContractData();
                        create.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.createRollback(chainId, new ContractWrapperTransaction(tx, null, create));
                        break;
                    case TX_TYPE_CALL_CONTRACT:
                        CallContractData call = new CallContractData();
                        call.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.callRollback(chainId, new ContractWrapperTransaction(tx, null, call));
                        break;
                    case TX_TYPE_DELETE_CONTRACT:
                        DeleteContractData delete = new DeleteContractData();
                        delete.parse(tx.getTxData(), 0);
                        contractTxProcessorManager.deleteRollback(chainId, new ContractWrapperTransaction(tx, null, delete));
                        break;
                    default:
                        break;
                }
            }

            return getSuccess();
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            return getFailed().setMsg(e.getMessage());
        }
    }


    private ContractReturnGasTransaction makeReturnGasTx(int chainId, List<ContractResult> resultList, long time) throws IOException {
        int assetsId = contractHelper.getChain(chainId).getConfig().getAssetsId();
        ContractWrapperTransaction wrapperTx;
        ContractData contractData;
        Map<ByteArrayWrapper, BigInteger> returnMap = new HashMap<>();
        for(ContractResult contractResult : resultList) {
            wrapperTx = contractResult.getTx();
            // 终止合约不消耗Gas，跳过
            if(wrapperTx.getType() == TX_TYPE_DELETE_CONTRACT) {
                continue;
            }
            contractData = wrapperTx.getContractData();
            long realGasUsed = contractResult.getGasUsed();
            long txGasUsed = contractData.getGasLimit();
            long returnGas;

            BigInteger returnValue;
            if (txGasUsed > realGasUsed) {
                returnGas = txGasUsed - realGasUsed;
                returnValue = BigInteger.valueOf(LongUtils.mul(returnGas, contractData.getPrice()));

                ByteArrayWrapper sender = new ByteArrayWrapper(contractData.getSender());
                BigInteger senderValue = returnMap.get(sender);
                if (senderValue == null) {
                    senderValue = returnValue;
                } else {
                    senderValue = senderValue.add(returnValue);
                }
                returnMap.put(sender, senderValue);
            }
        }
        if(!returnMap.isEmpty()) {
            CoinData coinData = new CoinData();
            List<CoinTo> toList = coinData.getTo();
            Set<Map.Entry<ByteArrayWrapper, BigInteger>> entries = returnMap.entrySet();
            CoinTo returnCoin;
            for (Map.Entry<ByteArrayWrapper, BigInteger> entry : entries) {
                returnCoin = new CoinTo(entry.getKey().getBytes(), chainId, assetsId, entry.getValue(), 0L);
                toList.add(returnCoin);
            }
            ContractReturnGasTransaction tx = new ContractReturnGasTransaction();
            tx.setTime(time);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            return tx;
        }

        return null;
    }

    @Override
    public Result saveContractExecuteResult(int chainId, NulsDigestData hash, ContractResult result) {
        if (hash == null || result == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        contractHelper.updateLastedPriceForAccount(chainId, result.getSender(), result.getPrice());
        return contractExecuteResultStorageService.saveContractExecuteResult(chainId, hash, result);
    }

    @Override
    public Result deleteContractExecuteResult(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        return contractExecuteResultStorageService.deleteContractExecuteResult(chainId, hash);
    }

    @Override
    public ContractResult getContractExecuteResult(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return contractExecuteResultStorageService.getContractExecuteResult(chainId, hash);
    }



}
