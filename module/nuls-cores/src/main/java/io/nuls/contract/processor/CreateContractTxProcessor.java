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
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractResultDto;
import io.nuls.contract.model.dto.CreateContractDataDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;

import java.math.BigInteger;
import java.util.HashMap;
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
            // add by pierre at 2019-11-02 调用账本模块，登记资产id，当NRC20合约存在[transferCrossChain]方法时，才登记资产id 需要协议升级 done
            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_V250 ) {
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
                    Log.info("CROSS-NRC20-TOKEN contract [{}] 向账本注册合约资产", contractAddressStr);
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
            }
            // end code by pierre
        }
        return contractAddressStorageService.saveContractAddress(chainId, contractAddress, info);
    }

    public Result onRollback(int chainId, ContractWrapperTransaction tx) throws Exception {
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
        try {
            CreateContractData contractData = (CreateContractData) tx.getContractData();
            Log.info("rollback create tx, contract data is {}, result is {}", JSONUtils.obj2json(new CreateContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
        } catch (Exception e) {
            Log.warn("failed to trace create rollback log, error is {}", e.getMessage());
        }
        // add by pierre at 2019-11-02 调用账本模块，回滚已登记的资产id 需要协议升级 done
        if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_V250 && contractResult.isNrc20()) {
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
        Result result = contractAddressStorageService.deleteContractAddress(chainId, contractAddress);
        if (result.isFailed()) {
            return result;
        }
        return contractService.deleteContractExecuteResult(chainId, tx.getHash());
    }

    public Result onCommitV8(int chainId, ContractWrapperTransaction tx) throws Exception {
        BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
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
        String contractAddressStr = AddressTool.getStringAddressByBytes(contractAddress);

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
        boolean isNrc721Contract = TokenTypeStatus.NRC721.status() == contractResult.getTokenType();
        boolean acceptDirectTransfer = contractResult.isAcceptDirectTransfer();
        info.setAcceptDirectTransfer(acceptDirectTransfer);
        info.setNrc20(isNrc20Contract);
        info.setTokenType(contractResult.getTokenType());
        do {
            if (!isNrc20Contract && !isNrc721Contract) {
                break;
            }
            // 获取 token tracker
            // 处理NRC20/NRC721 token数据
            String tokenName = contractResult.getTokenName();
            String tokenSymbol = contractResult.getTokenSymbol();
            int tokenDecimals = contractResult.getTokenDecimals();
            BigInteger tokenTotalSupply = contractResult.getTokenTotalSupply();
            info.setNrc20TokenName(tokenName);
            info.setNrc20TokenSymbol(tokenSymbol);
            if (!isNrc20Contract) {
                break;
            }
            // 处理NRC20 token数据
            info.setDecimals(tokenDecimals);
            info.setTotalSupply(tokenTotalSupply);
            // add by pierre at 2019-11-02 调用账本模块，登记资产id，当NRC20合约存在[transferCrossChain]方法时，才登记资产id 需要协议升级 done
            if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_V250 ) {
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
                    Log.info("CROSS-NRC20-TOKEN contract [{}] 向账本注册合约资产", contractAddressStr);
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
            }
            // end code by pierre
        } while (false);

        return contractAddressStorageService.saveContractAddress(chainId, contractAddress, info);
    }

    public Result onRollbackV8(int chainId, ContractWrapperTransaction tx) throws Exception {
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
        try {
            CreateContractData contractData = (CreateContractData) tx.getContractData();
            Log.info("rollback create tx, contract data is {}, result is {}", JSONUtils.obj2json(new CreateContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
        } catch (Exception e) {
            Log.warn("failed to trace create rollback log, error is {}", e.getMessage());
        }
        // add by pierre at 2019-11-02 调用账本模块，回滚已登记的资产id 需要协议升级 done
        if(ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_V250 && contractResult.isNrc20()) {
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
        Result result = contractAddressStorageService.deleteContractAddress(chainId, contractAddress);
        if (result.isFailed()) {
            return result;
        }
        return contractService.deleteContractExecuteResult(chainId, tx.getHash());
    }

    // add by pierre at 2022/6/6 p14
    public Result onCommitV14(int chainId, ContractWrapperTransaction tx) throws Exception {
        BlockHeader blockHeader = contractHelper.getBatchInfoCurrentBlockHeaderV8(chainId);
        long blockHeight = blockHeader.getHeight();
        tx.setBlockHeight(blockHeight);
        ContractResult contractResult = tx.getContractResult();
        contractResult.setBlockHeight(blockHeight);
        Result saveContractExecuteResult = contractService.saveContractExecuteResult(chainId, tx.getHash(), contractResult);
        if (saveContractExecuteResult.isFailed()) {
            return saveContractExecuteResult;
        }
        // 执行失败的合约直接返回
        if (!contractResult.isSuccess()) {
            return getSuccess();
        }
        CreateContractData txData = (CreateContractData) tx.getContractData();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        String alias = txData.getAlias();
        byte[] code = txData.getCode();

        ContractCreate create = new ContractCreate();
        create.setTokenType(contractResult.getTokenType());
        create.setTokenName(contractResult.getTokenName());
        create.setTokenSymbol(contractResult.getTokenSymbol());
        create.setTokenDecimals(contractResult.getTokenDecimals());
        create.setTokenTotalSupply(contractResult.getTokenTotalSupply());
        create.setAcceptDirectTransfer(contractResult.isAcceptDirectTransfer());
        Map<String, ContractAddressInfoPo> infoPoMap = new HashMap<>();
        Result result = contractHelper.onCommitForCreateV14(chainId, blockHeader, create, tx.getHash(), tx.getTime(), contractAddress, sender, code, alias, infoPoMap);
        return result;
    }

    public Result onRollbackV14(int chainId, ContractWrapperTransaction tx) throws Exception {
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
        try {
            CreateContractData contractData = (CreateContractData) tx.getContractData();
            Log.info("rollback create tx, contract data is {}, result is {}", JSONUtils.obj2json(new CreateContractDataDto(contractData)), JSONUtils.obj2json(new ContractResultDto(chainId, contractResult, contractData.getGasLimit())));
        } catch (Exception e) {
            Log.warn("failed to trace create rollback log, error is {}", e.getMessage());
        }
        Result result = contractHelper.onRollbackForCreateV14(chainId, contractAddress, contractResult.isNrc20());
        if (result.isFailed()) {
            return result;
        }
        return contractService.deleteContractExecuteResult(chainId, tx.getHash());
    }


}
