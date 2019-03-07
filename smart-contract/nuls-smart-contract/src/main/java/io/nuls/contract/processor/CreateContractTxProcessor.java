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
import io.nuls.base.basic.TransactionProcessor;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.storage.ContractExecuteResultStorageService;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/7
 */
@Component
public class CreateContractTxProcessor {

    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private VMContext vmContext;

    @Autowired
    private ContractExecuteResultStorageService contractExecuteResultStorageService;

    @Autowired
    private ContractHelper contractHelper;

    public Result onCommit(int chainId, CreateContractTransaction tx, Object secondaryData) throws NulsException {
        ContractResult contractResult = tx.getContractResult();
        contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);

        CreateContractData txData = tx.getTxDataObj();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();

        // 执行失败的合约直接返回
        if(!contractResult.isSuccess()) {
            return getSuccess();
        }

        NulsDigestData hash = tx.getHash();
        long blockHeight = tx.getBlockHeight();
        ContractAddressInfoPo info = new ContractAddressInfoPo();
        info.setContractAddress(contractAddress);
        info.setSender(sender);
        try {
            info.setCreateTxHash(hash.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        info.setCreateTime(tx.getTime());
        info.setBlockHeight(blockHeight);

        boolean isNrc20Contract = contractResult.isNrc20();
        boolean acceptDirectTransfer = contractResult.isAcceptDirectTransfer();
        info.setAcceptDirectTransfer(acceptDirectTransfer);
        info.setNrc20(isNrc20Contract);
        // 获取 token tracker
        if(isNrc20Contract) {
            // NRC20 token 标准方法获取名称数据
            info.setNrc20TokenName(contractResult.getTokenName());
            info.setNrc20TokenSymbol(contractResult.getTokenSymbol());
            info.setDecimals(contractResult.getTokenDecimals());
            info.setTotalSupply(contractResult.getTokenTotalSupply());

            //TODO pierre  刷新创建者的token余额
            //contractHelper.refreshTokenBalance(newestStateRoot, info, senderStr, contractAddressStr);
            //TODO pierre  处理合约事件
            //contractHelper.dealEvents(newestStateRoot, tx, contractResult, info);
        }

        Result result = contractAddressStorageService.saveContractAddress(chainId, contractAddress, info);
        return result;
    }

    public Result onRollback(int chainId, CreateContractTransaction tx, Object secondaryData) throws Exception {
        CreateContractData txData = tx.getTxDataObj();
        byte[] contractAddress = txData.getContractAddress();
        contractAddressStorageService.deleteContractAddress(chainId, contractAddress);
        contractService.deleteContractExecuteResult(chainId, tx.getHash());
        return getSuccess();
    }


}
