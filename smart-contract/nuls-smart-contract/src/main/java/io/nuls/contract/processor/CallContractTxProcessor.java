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
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.vm.program.ProgramStatus;
import io.nuls.tools.basic.Result;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

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
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    @Autowired
    private ContractService contractService;

    public Result onCommit(int chainId, CallContractTransaction tx, Object secondaryData) {
        try {
            ContractResult contractResult = tx.getContractResult();

            long blockHeight = tx.getBlockHeight();

            // 保存代币交易
            CallContractData callContractData = tx.getTxDataObj();
            byte[] contractAddress = callContractData.getContractAddress();

            Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddress);
            if (contractAddressInfoPoResult.isFailed()) {
                return contractAddressInfoPoResult;
            }
            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
            if (contractAddressInfoPo == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }
            contractResult.setNrc20(contractAddressInfoPo.isNrc20());

            BlockHeader blockHeader = contractHelper.getCurrentBlockHeader(chainId);
            byte[] newestStateRoot = blockHeader.getStateRoot();


            //TODO pierre  获取合约当前状态
            //ProgramStatus status = contractHelper.getContractStatus(newestStateRoot, contractAddress);
            ProgramStatus status = null;
            boolean isTerminatedContract = ContractUtil.isTerminatedContract(status.ordinal());

            // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中获取, 若是本地创建的交易，获取到修改为失败交易
            if (isTerminatedContract || !contractResult.isSuccess()) {
                if (contractAddressInfoPo != null && contractAddressInfoPo.isNrc20() && ContractUtil.isTransferMethod(callContractData.getMethodName())) {
                    byte[] txHashBytes = tx.getHash().serialize();
                    byte[] infoKey = Arrays.concatenate(callContractData.getSender(), txHashBytes, new VarInt(0).encode());
                    Result<ContractTokenTransferInfoPo> infoResult = contractTokenTransferStorageService.getTokenTransferInfo(chainId, infoKey);
                    ContractTokenTransferInfoPo po = infoResult.getData();
                    if (po != null) {
                        po.setStatus((byte) 2);
                        contractTokenTransferStorageService.saveTokenTransferInfo(chainId, infoKey, po);

                        // 刷新token余额
                        if (isTerminatedContract) {
                            // 终止的合约，回滚token余额
                            this.rollbackContractToken(po);
                            contractResult.setError(true);
                            contractResult.setErrorMessage("this contract has been terminated");
                        } else {

                            if (po.getFrom() != null) {
                                //TODO pierre 刷新token余额
                                //contractHelper.refreshTokenBalance(newestStateRoot, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getFrom()), po.getContractAddress());
                            }
                            if (po.getTo() != null) {
                                //TODO pierre 刷新token余额
                                //contractHelper.refreshTokenBalance(newestStateRoot, contractAddressInfoPo, AddressTool.getStringAddressByBytes(po.getTo()), po.getContractAddress());
                            }
                        }
                    }
                }
            }

            if (!isTerminatedContract) {
                //TODO pierre  处理合约事件
                //vmHelper.dealEvents(newestStateRoot, tx, contractResult, contractAddressInfoPo);
            }

            // 保存合约执行结果
            contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);

        } catch (Exception e) {
            Log.error("save call contract tx error.", e);
            return getFailed();
        }
        return getSuccess();
    }

    public Result onRollback(int chainId, CallContractTransaction tx, Object secondaryData) {
        try {
            // 回滚代币转账交易
            byte[] txHashBytes = null;
            try {
                txHashBytes = tx.getHash().serialize();
            } catch (IOException e) {
                Log.error(e);
            }

            ContractResult contractResult = tx.getContractResult();
            if (contractResult == null) {
                contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
            }
            CallContractData txData = tx.getTxDataObj();
            byte[] senderContractAddressBytes = txData.getContractAddress();
            Result<ContractAddressInfoPo> senderContractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, senderContractAddressBytes);
            ContractAddressInfoPo po = senderContractAddressInfoResult.getData();
            if (po != null) {
                if (contractResult != null) {
                    // 处理合约执行失败 - 没有transferEvent的情况, 直接从数据库中删除
                    if (!contractResult.isSuccess()) {
                        if (ContractUtil.isTransferMethod(txData.getMethodName())) {
                            contractTokenTransferStorageService.deleteTokenTransferInfo(chainId, Arrays.concatenate(txData.getSender(), txHashBytes, new VarInt(0).encode()));
                        }
                    }
                    List<String> events = contractResult.getEvents();
                    int size = events.size();
                    // 目前只处理Transfer事件
                    String event;
                    ContractAddressInfoPo contractAddressInfo;
                    if (events != null && size > 0) {
                        for (int i = 0; i < size; i++) {
                            event = events.get(i);
                            // 按照NRC20标准，TransferEvent事件中第一个参数是转出地址-from，第二个参数是转入地址-to, 第三个参数是金额
                            ContractTokenTransferInfoPo tokenTransferInfoPo = ContractUtil.convertJsonToTokenTransferInfoPo(chainId, event);
                            if (tokenTransferInfoPo == null) {
                                continue;
                            }
                            String contractAddress = tokenTransferInfoPo.getContractAddress();
                            if (StringUtils.isBlank(contractAddress)) {
                                continue;
                            }
                            if (!AddressTool.validAddress(chainId, contractAddress)) {
                                continue;
                            }
                            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
                            if (Arrays.areEqual(senderContractAddressBytes, contractAddressBytes)) {
                                contractAddressInfo = po;
                            } else {
                                Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
                                contractAddressInfo = contractAddressInfoResult.getData();
                            }

                            if (contractAddressInfo == null) {
                                continue;
                            }
                            // 事件不是NRC20合约的事件
                            if (!contractAddressInfo.isNrc20()) {
                                continue;
                            }

                            // 回滚token余额
                            this.rollbackContractToken(tokenTransferInfoPo);
                            contractTokenTransferStorageService.deleteTokenTransferInfo(chainId, Arrays.concatenate(tokenTransferInfoPo.getFrom(), txHashBytes, new VarInt(i).encode()));
                            contractTokenTransferStorageService.deleteTokenTransferInfo(chainId, Arrays.concatenate(tokenTransferInfoPo.getTo(), txHashBytes, new VarInt(i).encode()));
                        }
                    }
                }
            }

            // 删除合约执行结果
            contractService.deleteContractExecuteResult(chainId, tx.getHash());
        } catch (Exception e) {
            Log.error("rollback call contract tx error.", e);
            return getFailed();
        }
        return getSuccess();
    }

    private void rollbackContractToken(ContractTokenTransferInfoPo po) {
        try {
            String contractAddressStr = po.getContractAddress();
            byte[] from = po.getFrom();
            byte[] to = po.getTo();
            BigInteger token = po.getValue();
            String fromStr = null;
            String toStr = null;
            if (from != null) {
                fromStr = AddressTool.getStringAddressByBytes(from);
            }
            if (to != null) {
                toStr = AddressTool.getStringAddressByBytes(to);
            }
            //TODO pierre 建立token余额管理器, ContractTokenBalanceManager
            //contractBalanceManager.addContractToken(fromStr, contractAddressStr, token);
            //contractBalanceManager.subtractContractToken(toStr, contractAddressStr, token);
        } catch (Exception e) {
            // skip it
        }
    }

}
