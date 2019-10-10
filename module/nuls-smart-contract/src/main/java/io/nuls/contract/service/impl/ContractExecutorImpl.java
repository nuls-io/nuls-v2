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

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTxHelper;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.service.ContractExecutor;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2019/1/7
 */
@Component
public class ContractExecutorImpl implements ContractExecutor {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxHelper contractTxHelper;

    @Override
    public ContractResult create(ProgramExecutor executor, ContractData create, long number, String preStateRoot, byte[] publicKey) {
        byte[] contractAddress = create.getContractAddress();
        byte[] sender = create.getSender();
        long price = create.getPrice();
        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(contractAddress);
        programCreate.setSender(sender);
        programCreate.setValue(BigInteger.ZERO);
        programCreate.setPrice(price);
        programCreate.setGasLimit(create.getGasLimit());
        programCreate.setNumber(number);
        programCreate.setContractCode(create.getCode());
        programCreate.setArgs(create.getArgs());
        programCreate.setSenderPublicKey(publicKey);

        ProgramExecutor track = executor.startTracking();

        ProgramResult programResult = track.create(programCreate);

        ContractResult contractResult = new ContractResult();

        contractResult.setGasUsed(programResult.getGasUsed());
        contractResult.setPrice(price);
        contractResult.setContractAddress(contractAddress);
        contractResult.setSender(sender);
        contractResult.setRemark(ContractConstant.CREATE_REMARK);
        // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
        contractResult.setTxTrack(track);
        contractResult.setDebugEvents(programResult.getDebugEvents());

        if (!programResult.isSuccess()) {
            contractResult.setError(programResult.isError());
            contractResult.setRevert(programResult.isRevert());
            contractResult.setErrorMessage(programResult.getErrorMessage());
            contractResult.setStackTrace(programResult.getStackTrace());
            return contractResult;
        }

        // 返回已使用gas、状态根、消息事件、合约转账(从合约转出)
        contractResult.setError(false);
        contractResult.setRevert(false);
        contractResult.setEvents(programResult.getEvents());
        contractResult.setTransfers(programResult.getTransfers());
        return contractResult;
    }

    @Override
    public ContractResult call(ProgramExecutor executor, ContractData call, long number, String preStateRoot, byte[] publicKey) {
        byte[] contractAddress = call.getContractAddress();
        byte[] sender = call.getSender();
        long price = call.getPrice();
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddress);
        programCall.setSender(sender);
        programCall.setValue(call.getValue());
        programCall.setPrice(price);
        programCall.setGasLimit(call.getGasLimit());
        programCall.setNumber(number);
        programCall.setMethodName(call.getMethodName());
        programCall.setMethodDesc(call.getMethodDesc());
        programCall.setArgs(call.getArgs());
        programCall.setSenderPublicKey(publicKey);

        ProgramExecutor track = executor.startTracking();

        ProgramResult programResult = track.call(programCall);

        ContractResult contractResult = new ContractResult();

        contractResult.setGasUsed(programResult.getGasUsed());
        contractResult.setPrice(price);
        contractResult.setContractAddress(contractAddress);
        contractResult.setSender(sender);
        contractResult.setValue(programCall.getValue().longValue());
        contractResult.setRemark(ContractConstant.CALL_REMARK);
        // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
        contractResult.setTxTrack(track);
        contractResult.setDebugEvents(programResult.getDebugEvents());

        if (!programResult.isSuccess()) {
            contractResult.setError(programResult.isError());
            contractResult.setRevert(programResult.isRevert());
            contractResult.setErrorMessage(programResult.getErrorMessage());
            contractResult.setStackTrace(programResult.getStackTrace());
            return contractResult;
        }

        // 返回调用结果、已使用Gas、状态根、消息事件、合约转账(从合约转出)等
        contractResult.setError(false);
        contractResult.setRevert(false);
        contractResult.setResult(programResult.getResult());
        contractResult.setEvents(programResult.getEvents());
        contractResult.setTransfers(programResult.getTransfers());
        contractResult.setInvokeRegisterCmds(programResult.getInvokeRegisterCmds());
        contractResult.setContractAddressInnerCallSet(contractTxHelper.generateInnerCallSet(programResult.getInternalCalls()));
        contractResult.setAccounts(programResult.getAccounts());

        return contractResult;
    }

    @Override
    public ContractResult delete(ProgramExecutor executor, ContractData delete, long number, String preStateRoot) {
        byte[] contractAddress = delete.getContractAddress();
        byte[] sender = delete.getSender();

        ProgramExecutor track = executor.startTracking();

        ProgramResult programResult = track.stop(number, contractAddress, sender);

        ContractResult contractResult = new ContractResult();

        contractResult.setGasUsed(programResult.getGasUsed());
        contractResult.setContractAddress(contractAddress);
        contractResult.setSender(sender);
        contractResult.setRemark(ContractConstant.DELETE_REMARK);
        // 批量提交方式，交易track放置到外部处理合约执行结果的方法里去提交
        contractResult.setTxTrack(track);

        if (!programResult.isSuccess()) {
            contractResult.setError(programResult.isError());
            contractResult.setRevert(programResult.isRevert());
            contractResult.setErrorMessage(programResult.getErrorMessage());
            contractResult.setStackTrace(programResult.getStackTrace());
            return contractResult;
        }

        // 返回状态根
        contractResult.setError(false);
        contractResult.setRevert(false);

        return contractResult;
    }

    @Override
    public ProgramExecutor createBatchExecute(int chainId, byte[] stateRoot) {
        if (stateRoot == null) {
            return null;
        }
        ProgramExecutor executor = contractHelper.getProgramExecutor(chainId).begin(stateRoot);
        return executor;
    }

    @Override
    public Result<byte[]> commitBatchExecute(ProgramExecutor executor) {
        if (executor == null) {
            return ContractUtil.getSuccess();
        }
        executor.commit();
        byte[] stateRoot = executor.getRoot();
        if(Log.isDebugEnabled()) {
            Log.debug("after commit state is {}", HexUtil.encode(stateRoot));
        }
        return ContractUtil.getSuccess().setData(stateRoot);
    }

}
