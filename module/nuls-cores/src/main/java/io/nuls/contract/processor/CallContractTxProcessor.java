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
package io.nuls.contract.processor;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractInternalCreate;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.dto.CallContractDataDto;
import io.nuls.contract.model.dto.ContractResultDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.getFailed;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/8
 */
@Component
public class CallContractTxProcessor {

    @Autowired
    private ContractHelper contractHelper;

    @Autowired
    private ContractService contractService;

    public Result onCommit(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);
            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onRollback(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onCommitV8(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);
            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onRollbackV8(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }

    // add by pierre at 2022/6/6 p14
    public Result onCommitV14(int chainId, ContractWrapperTransaction tx) {
        try {
            BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
            byte[] stateRoot = blockHeader.getStateRoot();
            long blockHeight = blockHeader.getHeight();
            ContractResult contractResult = tx.getContractResult();
            contractResult.setBlockHeight(blockHeight);

            // 保存代币交易
            ContractData callContractData = tx.getContractData();
            byte[] contractAddress = callContractData.getContractAddress();
            String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());
            tx.setBlockHeight(blockHeight);

            Map<String, ContractAddressInfoPo> infoPoMap = new HashMap<>();
            infoPoMap.put(contractAddressStr, contractAddressInfoPo);
            // 处理内部创建合约
            List<ContractInternalCreate> internalCreates = contractResult.getInternalCreates();
            if (internalCreates != null && !internalCreates.isEmpty()) {
                for (ContractInternalCreate internalCreate : internalCreates) {
                    Result result = contractHelper.onCommitForCreateV14(chainId, blockHeader, internalCreate, tx.getHash(), tx.getTime(), internalCreate.getContractAddress(),
                            internalCreate.getSender(), contractHelper.getContractCode(chainId, stateRoot, internalCreate.getCodeCopyBy()), "internal_create", infoPoMap);
                    if (result.isFailed()) {
                        return result;
                    }
                }
            }
            // 保存合约执行结果
            return contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
    }

    public Result onRollbackV14(int chainId, ContractWrapperTransaction tx) {
        try {
            // 回滚代币转账交易
            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            if (contractResult == null) {
                return ContractUtil.getSuccess();
            }
            try {
                CallContractData contractData = (CallContractData) tx.getContractData();
                Log.info("rollback call tx, contract data is {}, result is {}", JSONUtils.obj2json(new CallContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
            } catch (Exception e) {
                Log.warn("failed to trace call rollback log, error is {}", e.getMessage());
            }
            // 处理内部创建合约
            List<ContractInternalCreate> internalCreates = contractResult.getInternalCreates();
            if (internalCreates != null && !internalCreates.isEmpty()) {
                for (ContractInternalCreate internalCreate : internalCreates) {
                    Result result = contractHelper.onRollbackForCreateV14(chainId, internalCreate.getContractAddress(), internalCreate.getTokenType() == TokenTypeStatus.NRC20.status());
                    if (result.isFailed()) {
                        return result;
                    }
                }
            }
            // 删除合约执行结果
            return contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
    }


}
