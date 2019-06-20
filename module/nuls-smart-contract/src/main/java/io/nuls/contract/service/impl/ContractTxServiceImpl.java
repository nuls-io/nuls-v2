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
import io.nuls.base.data.NulsHash;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractTxHelper;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.basic.VarInt;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ArraysTool;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractErrorCode.FAILED;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/22
 */
@Component
public class ContractTxServiceImpl implements ContractTxService {

    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;
    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxHelper contractTxHelper;

    @Override
    public Result contractCreateTx(int chainId, String sender, String alias, Long gasLimit, Long price,
                                   byte[] contractCode, String[][] args,
                                   String password, String remark) {
        try {
            Result<CreateContractTransaction> result = contractTxHelper.makeCreateTx(chainId, sender, alias, gasLimit, price, contractCode, args, password, remark);
            if (result.isFailed()) {
                return result;
            }
            CreateContractTransaction tx = result.getData();

            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            // 签名、发送交易到交易模块
            Result signAndBroadcastTxResult = contractTxHelper.signAndBroadcastTx(chainId, sender, password, tx);
            if(signAndBroadcastTxResult.isFailed()) {
                return signAndBroadcastTxResult;
            }
            Map<String, String> resultMap = MapUtil.createHashMap(2);
            String txHash = tx.getHash().toHex();
            String contractAddressStr = AddressTool.getStringAddressByBytes(tx.getTxDataObj().getContractAddress());
            resultMap.put("txHash", txHash);
            resultMap.put("contractAddress", contractAddressStr);
            // 保留未确认的创建合约交易到内存中
            contractHelper.getChain(chainId).getContractTxCreateUnconfirmedManager().saveLocalUnconfirmedCreateContractTransaction(sender, resultMap, tx.getTime());
            return getSuccess().setData(resultMap);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode() == null ? FAILED : e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_TX_CREATE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }


    @Override
    public Result validateContractCreateTx(int chainId, byte[] sender, Long gasLimit, Long price, byte[] contractCode, String[][] args) {
        return contractTxHelper.validateCreate(chainId, sender, null, gasLimit, price, contractCode, args);
    }


    @Override
    public Result contractCallTx(int chainId, String sender, BigInteger value, Long gasLimit, Long price, String contractAddress,
                                 String methodName, String methodDesc, String[][] args,
                                 String password, String remark) {
        try {
            Result<CallContractTransaction> result = contractTxHelper.makeCallTx(chainId, sender, value, gasLimit, price, contractAddress, methodName, methodDesc, args, password, remark);
            if (result.isFailed()) {
                return result;
            }
            CallContractTransaction tx = result.getData();

            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            // 签名、发送交易到交易模块
            Result signAndBroadcastTxResult = contractTxHelper.signAndBroadcastTx(chainId, sender, password, tx);
            if(signAndBroadcastTxResult.isFailed()) {
                return signAndBroadcastTxResult;
            }

            // 保存未确认Token转账
            Result<byte[]> unConfirmedTokenTransferResult = this.saveUnConfirmedTokenTransfer(chainId, tx, sender, contractAddress, methodName, args);
            if (unConfirmedTokenTransferResult.isFailed()) {
                return unConfirmedTokenTransferResult;
            }
            byte[] infoKey = unConfirmedTokenTransferResult.getData();

            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("txHash", tx.getHash().toHex());
            return getSuccess().setData(resultMap);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_EXECUTE_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    @Override
    public Result validateContractCallTx(int chainId, byte[] senderBytes, BigInteger value, Long gasLimit, Long price, byte[] contractAddressBytes,
                                         String methodName, String methodDesc, String[][] args) {
        return contractTxHelper.validateCall(chainId, senderBytes, contractAddressBytes, value, gasLimit, price, methodName, methodDesc, args);
    }

    private Result<byte[]> saveUnConfirmedTokenTransfer(int chainId, CallContractTransaction tx, String sender, String contractAddress, String methodName, String[][] args) {
        try {
            ContractTokenBalanceManager tokenBalanceManager = contractHelper.getChain(chainId).getContractTokenBalanceManager();
            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po != null && po.isNrc20() && ContractUtil.isTransferMethod(methodName)) {
                byte[] txHashBytes = tx.getHash().getBytes();
                byte[] infoKey = ArraysTool.concatenate(senderBytes, txHashBytes, new VarInt(0).encode());
                ContractTokenTransferInfoPo tokenTransferInfoPo = new ContractTokenTransferInfoPo();
                if (ContractConstant.NRC20_METHOD_TRANSFER.equals(methodName)) {
                    String to = args[0][0];
                    String tokenValue = args[1][0];
                    BigInteger token = new BigInteger(tokenValue);
                    Result result = tokenBalanceManager.subtractContractToken(sender, contractAddress, token);
                    if (result.isFailed()) {
                        return result;
                    }
                    tokenBalanceManager.addContractToken(to, contractAddress, token);
                    tokenTransferInfoPo.setFrom(senderBytes);
                    tokenTransferInfoPo.setTo(AddressTool.getAddress(to));
                    tokenTransferInfoPo.setValue(token);
                } else {
                    String from = args[0][0];
                    // 转出的不是自己的代币（代币授权逻辑），则不保存token待确认交易，因为有调用合约的待确认交易
                    if (!sender.equals(from)) {
                        return getSuccess();
                    }
                    String to = args[1][0];
                    String tokenValue = args[2][0];
                    BigInteger token = new BigInteger(tokenValue);
                    Result result = tokenBalanceManager.subtractContractToken(from, contractAddress, token);
                    if (result.isFailed()) {
                        return result;
                    }
                    tokenBalanceManager.addContractToken(to, contractAddress, token);
                    tokenTransferInfoPo.setFrom(AddressTool.getAddress(from));
                    tokenTransferInfoPo.setTo(AddressTool.getAddress(to));
                    tokenTransferInfoPo.setValue(token);
                }

                tokenTransferInfoPo.setName(po.getNrc20TokenName());
                tokenTransferInfoPo.setSymbol(po.getNrc20TokenSymbol());
                tokenTransferInfoPo.setDecimals(po.getDecimals());
                tokenTransferInfoPo.setTime(tx.getTime());
                tokenTransferInfoPo.setContractAddress(contractAddress);
                tokenTransferInfoPo.setBlockHeight(tx.getBlockHeight());
                tokenTransferInfoPo.setTxHash(txHashBytes);
                tokenTransferInfoPo.setStatus((byte) 0);
                Result result = contractTokenTransferStorageService.saveTokenTransferInfo(chainId, infoKey, tokenTransferInfoPo);
                if (result.isFailed()) {
                    return result;
                }
                return getSuccess().setData(infoKey);
            }
            return getSuccess();
        } catch (Exception e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_OTHER_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    @Override
    public Result contractDeleteTx(int chainId, String sender, String contractAddress,
                                   String password, String remark) {
        try {

            Result<DeleteContractTransaction> result = contractTxHelper.makeDeleteTx(chainId, sender, contractAddress, password, remark);
            if (result.isFailed()) {
                return result;
            }
            DeleteContractTransaction tx = result.getData();
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            // 签名、发送交易到交易模块
            Result signAndBroadcastTxResult = contractTxHelper.signAndBroadcastTx(chainId, sender, password, tx);
            if(signAndBroadcastTxResult.isFailed()) {
                return signAndBroadcastTxResult;
            }
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("txHash", tx.getHash().toHex());
            return getSuccess().setData(resultMap);
        } catch (IOException e) {
            Log.error(e);
            Result result = Result.getFailed(ContractErrorCode.CONTRACT_OTHER_ERROR);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    @Override
    public Result validateContractDeleteTx(int chainId, String sender, String contractAddress) {
        byte[] senderBytes = AddressTool.getAddress(sender);
        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
        return contractTxHelper.validateDelete(chainId, senderBytes, contractAddress, contractAddressBytes);
    }

    @Override
    public Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(int chainId, String address) {
        try {
            byte[] addressBytes = AddressTool.getAddress(address);
            List<ContractTokenTransferInfoPo> tokenTransferInfoListByAddress = contractTokenTransferStorageService.getTokenTransferInfoListByAddress(chainId, addressBytes);
            return getSuccess().setData(tokenTransferInfoListByAddress);
        } catch (Exception e) {
            Log.error(e);
            return getFailed();
        }
    }
}
