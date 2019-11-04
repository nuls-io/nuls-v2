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
import io.nuls.base.data.NulsHash;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractTokenAssetsInfo;
import io.nuls.contract.model.bo.ContractWrapperTransaction;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.storage.ContractTokenAddressStorageService;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

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
    private ContractTokenAddressStorageService contractTokenAddressStorageService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractTxService contractTxService;
    @Autowired
    private ContractHelper contractHelper;

    public Result onCommit(int chainId, ContractWrapperTransaction tx) throws Exception {
        BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeader(chainId);
        long blockHeight = blockHeader.getHeight();
        ContractResult contractResult = tx.getContractResult();
        contractResult.setBlockHeight(blockHeight);
        Result saveContractExecuteResult = contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        if (saveContractExecuteResult.isFailed()) {
            return saveContractExecuteResult;
        }

        CreateContractData txData = (CreateContractData) tx.getContractData();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        String senderStr = AddressTool.getStringAddressByBytes(sender);
        String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);
        // 移除未确认的创建合约交易
        contractHelper.getChain(chainId).getContractTxCreateUnconfirmedManager().removeLocalUnconfirmedCreateContractTransaction(senderStr, contractAddressStr, contractResult);

        // 执行失败的合约直接返回
        if (!contractResult.isSuccess()) {
            return getSuccess();
        }


        NulsHash hash = tx.getHash();
        tx.setBlockHeight(blockHeight);
        ContractAddressInfoPo info = new ContractAddressInfoPo();
        info.setContractAddress(contractAddress);
        info.setSender(sender);
        info.setCreateTxHash(hash.getBytes());
        info.setAlias(txData.getAlias());
        info.setCreateTime(tx.getTime());
        info.setBlockHeight(blockHeight);

        boolean isNrc20Contract = contractResult.isNrc20();
        boolean acceptDirectTransfer = contractResult.isAcceptDirectTransfer();
        info.setAcceptDirectTransfer(acceptDirectTransfer);
        info.setNrc20(isNrc20Contract);
        info.setTokenType(contractResult.getTokenType());
        // 获取 token tracker
        if (isNrc20Contract) {
            // NRC20 token 标准方法获取名称数据
            String tokenName = contractResult.getTokenName();
            String tokenSymbol = contractResult.getTokenSymbol();
            int tokenDecimals = contractResult.getTokenDecimals();
            BigInteger tokenTotalSupply = contractResult.getTokenTotalSupply();

            info.setNrc20TokenName(tokenName);
            info.setNrc20TokenSymbol(tokenSymbol);
            info.setDecimals(tokenDecimals);
            info.setTotalSupply(tokenTotalSupply);
            byte[] newestStateRoot = blockHeader.getStateRoot();
            //处理NRC20合约事件
            contractHelper.dealNrc20Events(chainId, newestStateRoot, tx, contractResult, info);
            // 保存NRC20-token地址
            Result result = contractTokenAddressStorageService.saveTokenAddress(chainId, contractAddress);
            if (result.isFailed()) {
                return result;
            }
            // add by pierre at 2019-11-02 需要协议升级
            // add by pierre at 2019-10-21 调用账本模块，登记资产id
            // 当NRC20合约存在[transferCrossChain]方法时，才登记资产id
            List<ProgramMethod> methods = contractHelper.getAllMethods(chainId, txData.getCode());
            boolean isNewNrc20 = false;
            for(ProgramMethod method : methods) {
                if(ContractConstant.CROSS_CHAIN_NRC20_CONTRACT_TRANSFER_OUT_METHOD_NAME.equals(method.getName()) &&
                        ContractConstant.CROSS_CHAIN_NRC20_CONTRACT_TRANSFER_OUT_METHOD_DESC.equals(method.getDesc())) {
                    isNewNrc20 = true;
                    break;
                }
            }
            if(isNewNrc20) {
                Map resultMap = LedgerCall.commitNRC20Assets(chainId, tokenName, tokenSymbol, (short) tokenDecimals, tokenTotalSupply, contractAddressStr);
                if(resultMap != null) {
                    // 缓存合约地址和合约资产ID
                    int assetId = Integer.parseInt(resultMap.get("assetId").toString());
                    Chain chain = contractHelper.getChain(chainId);
                    Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = chain.getTokenAssetsInfoMap();
                    Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
                    tokenAssetsInfoMap.put(contractAddressStr, new ContractTokenAssetsInfo(chainId, assetId));
                    tokenAssetsContractAddressInfoMap.put(chainId + "-" + assetId, contractAddressStr);
                }
            }
            // end code by pierre
        }
        return contractAddressStorageService.saveContractAddress(chainId, contractAddress, info);
    }

    public Result onRollback(int chainId, ContractWrapperTransaction tx) throws Exception {
        Log.info("rollback create tx, hash is {}", tx.getHash().toString());
        ContractData txData = tx.getContractData();
        byte[] contractAddress = txData.getContractAddress();

        // 回滚代币转账交易
        ContractResult contractResult = tx.getContractResult();
        if (contractResult == null) {
            contractResult = contractService.getContractExecuteResult(chainId, tx.getHash());
        }
        if (contractResult == null) {
            return Result.getSuccess(null);
        }
        // add by pierre at 2019-11-02 需要协议升级
        // add by pierre at 2019-10-21 调用账本模块，回滚已登记的资产id
        if(contractResult.isNrc20()) {
            LedgerCall.rollBackNRC20Assets(chainId, AddressTool.getStringAddressByBytes(contractAddress));
            // 清理缓存
            Chain chain = contractHelper.getChain(chainId);
            Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = chain.getTokenAssetsInfoMap();
            ContractTokenAssetsInfo tokenAssetsInfo = tokenAssetsInfoMap.remove(contractAddress);
            if(tokenAssetsInfo != null) {
                Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
                tokenAssetsContractAddressInfoMap.remove(chainId + "-" + tokenAssetsInfo.getAssetId());
            }
        }
        // end code by pierre
        contractHelper.rollbackNrc20Events(chainId, tx, contractResult);
        Result result = contractAddressStorageService.deleteContractAddress(chainId, contractAddress);
        if (result.isFailed()) {
            return result;
        }
        result = contractTokenAddressStorageService.deleteTokenAddress(chainId, contractAddress);
        if (result.isFailed()) {
            return result;
        }
        return contractService.deleteContractExecuteResult(chainId, tx.getHash());
    }


}
