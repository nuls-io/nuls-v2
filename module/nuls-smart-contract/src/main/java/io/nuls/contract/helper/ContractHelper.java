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
package io.nuls.contract.helper;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.ContractStatus;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTempBalanceManager;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.dto.ContractConstructorInfoDto;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.ContractReturnGasTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.storage.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.Result;
import io.nuls.core.basic.VarInt;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.model.LongUtils;
import io.nuls.core.model.StringUtils;
import org.bouncycastle.util.Arrays;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.config.ContractContext.ASSET_ID;
import static io.nuls.contract.config.ContractContext.CHAIN_ID;
import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.ADDRESS_ERROR;
import static io.nuls.contract.util.ContractUtil.*;
import static io.nuls.core.constant.TxType.CROSS_CHAIN;
import static io.nuls.core.constant.TxType.DELETE_CONTRACT;
import static io.nuls.core.model.FormatValidUtils.validTokenNameOrSymbol;

@Component
public class ContractHelper {

    @Autowired
    private VMContext vmContext;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ContractAddressStorageService contractAddressStorageService;
    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;

    private static final BigInteger MAXIMUM_DECIMALS = BigInteger.valueOf(18L);
    private static final BigInteger MAXIMUM_TOTAL_SUPPLY = BigInteger.valueOf(2L).pow(256).subtract(BigInteger.ONE);


    public ProgramExecutor getProgramExecutor(int chainId) {
        Chain chain = getChain(chainId);
        if (chain == null) {
            return null;
        }
        return chain.getProgramExecutor();
    }

    public Chain getChain(int chainId) {
        return chainManager.getChainMap().get(chainId);
    }

    public ProgramMethod getMethodInfoByCode(int chainId, String methodName, String methodDesc, byte[] code) {
        if (StringUtils.isBlank(methodName) || code == null) {
            return null;
        }
        List<ProgramMethod> methods = this.getAllMethods(chainId, code);
        return this.getMethodInfo(methodName, methodDesc, methods);
    }

    public ContractConstructorInfoDto getConstructor(int chainId, byte[] contractCode) {
        try {
            ContractConstructorInfoDto dto = new ContractConstructorInfoDto();
            List<ProgramMethod> programMethods = this.getAllMethods(chainId, contractCode);
            if (programMethods == null || programMethods.size() == 0) {
                return null;
            }
            for (ProgramMethod method : programMethods) {
                if (ContractConstant.CONTRACT_CONSTRUCTOR.equals(method.getName())) {
                    dto.setConstructor(method);
                    break;
                }
            }
            dto.setNrc20(this.checkNrc20Contract(programMethods));
            return dto;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public List<ProgramMethod> getAllMethods(int chainId, byte[] contractCode) {
        return getProgramExecutor(chainId).jarMethod(contractCode);
    }

    private ProgramMethod getMethodInfo(String methodName, String methodDesc, List<ProgramMethod> methods) {
        if (methods != null && methods.size() > 0) {
            boolean emptyDesc = StringUtils.isBlank(methodDesc);
            for (ProgramMethod method : methods) {
                if (methodName.equals(method.getName())) {
                    if (emptyDesc) {
                        return method;
                    } else if (methodDesc.equals(method.getDesc())) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    public ProgramMethod getMethodInfoByContractAddress(int chainId, byte[] currentStateRoot, String methodName, String methodDesc, byte[] contractAddressBytes) {
        if (StringUtils.isBlank(methodName)) {
            return null;
        }
        ProgramExecutor track = getProgramExecutor(chainId).begin(currentStateRoot);
        List<ProgramMethod> methods = track.method(contractAddressBytes);

        return this.getMethodInfo(methodName, methodDesc, methods);
    }

    private boolean checkNrc20Contract(List<ProgramMethod> methods) {
        return checkNrc20Contract(methods, null);
    }

    private boolean checkNrc20Contract(List<ProgramMethod> methods, Map<String, ProgramMethod> contractMethodsMap) {
        if (methods == null || methods.size() == 0) {
            return false;
        }
        if (contractMethodsMap == null) {
            contractMethodsMap = new HashMap<>(methods.size());
        }
        for (ProgramMethod method : methods) {
            contractMethodsMap.put(method.getName(), method);
        }

        Set<Map.Entry<String, ProgramMethod>> entries = VMContext.getNrc20Methods().entrySet();
        String methodName;
        ProgramMethod standardMethod;
        ProgramMethod mappingMethod;
        for (Map.Entry<String, ProgramMethod> entry : entries) {
            methodName = entry.getKey();
            standardMethod = entry.getValue();
            mappingMethod = contractMethodsMap.get(methodName);

            if (mappingMethod == null) {
                return false;
            }
            if (!standardMethod.equalsNrc20Method(mappingMethod)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkNrc721Contract(List<ProgramMethod> methods, Map<String, ProgramMethod> contractMethodsMap) {
        if (methods == null || methods.size() == 0) {
            return false;
        }

        Set<Map.Entry<String, ProgramMethod>> entries = VMContext.getNrc721Methods().entrySet();
        String methodName;
        ProgramMethod standardMethod;
        ProgramMethod mappingMethod;
        for (Map.Entry<String, ProgramMethod> entry : entries) {
            methodName = entry.getKey();
            standardMethod = entry.getValue();
            mappingMethod = contractMethodsMap.get(methodName);

            if (mappingMethod == null) {
                return false;
            }
            if (!standardMethod.equalsNrc721Method(mappingMethod)) {
                return false;
            }
        }
        boolean hasSafe = false;
        boolean hasSafeData = false;
        for (ProgramMethod method : methods) {
            if (NRC721_SAFETRANSFERFROM.equals(method.getName())) {
                int size = method.getArgs().size();
                if (size == 3 && VMContext.getNrc721OverloadMethodSafe().equalsNrc721Method(method)) {
                    hasSafe = true;
                    continue;
                }
                if (size == 4 && VMContext.getNrc721OverloadMethodSafeData().equalsNrc721Method(method)) {
                    hasSafeData = true;
                    continue;
                }
            }
            if (hasSafe && hasSafeData) {
                break;
            }
        }
        if (hasSafe && hasSafeData) {
            return true;
        }
        return false;
    }

    private boolean checkAcceptDirectTransfer(List<ProgramMethod> methods) {
        if (methods == null || methods.size() == 0) {
            return false;
        }
        for (ProgramMethod method : methods) {
            if (BALANCE_TRIGGER_METHOD_NAME.equals(method.getName())
                    && VOID_METHOD_DESC.equals(method.getDesc())) {
                return method.isPayable();
            }
        }
        return false;
    }

    public ProgramResult invokeViewMethod(int chainId, byte[] contractAddressBytes, String methodName, String methodDesc, Object... args) {
        return this.invokeViewMethod(chainId, contractAddressBytes, methodName, methodDesc, ContractUtil.twoDimensionalArray(args));
    }

    public ProgramResult invokeViewMethod(int chainId, byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {
        // 当前区块高度
        BlockHeader blockHeader;
        try {
            blockHeader = BlockCall.getLatestBlockHeader(chainId);
        } catch (NulsException e) {
            Log.error(e);
            return ProgramResult.getFailed(e.getMessage());
        }
        if (blockHeader == null) {
            return ProgramResult.getFailed("block header is null.");
        }
        long blockHeight = blockHeader.getHeight();
        // 当前区块状态根
        byte[] currentStateRoot = ContractUtil.getStateRoot(blockHeader);
        return this.invokeViewMethod(chainId, null, false, currentStateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, args);
    }

    public ProgramResult invokeCustomGasViewMethod(int chainId, BlockHeader blockHeader, byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {
        if (blockHeader == null) {
            return ProgramResult.getFailed("block header is null.");
        }
        long blockHeight = blockHeader.getHeight();
        // 当前区块状态根
        byte[] currentStateRoot = ContractUtil.getStateRoot(blockHeader);

        return this.invokeViewMethod(chainId, null, true, currentStateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, args);
    }

    private ProgramResult invokeViewMethod(int chainId, ProgramExecutor executor, byte[] stateRoot, long blockHeight, byte[] contractAddressBytes, String methodName, String methodDesc, Object... args) {
        return this.invokeViewMethod(chainId, executor, false, stateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, ContractUtil.twoDimensionalArray(args));
    }

    public ProgramResult invokeViewMethod(int chainId, byte[] stateRoot, long blockHeight, byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {
        return this.invokeViewMethod(chainId, null, false, stateRoot, blockHeight, contractAddressBytes, methodName, methodDesc, args);
    }

    public ProgramResult invokeViewMethod(int chainId, ProgramExecutor executor, boolean isCustomGasLimit, byte[] stateRoot, long blockHeight, byte[] contractAddressBytes, String methodName, String methodDesc, String[][] args) {

        long gasLimit;
        if (isCustomGasLimit) {
            gasLimit = vmContext.getCustomMaxViewGasLimit(chainId);
        } else {
            gasLimit = ContractConstant.CONTRACT_CONSTANT_GASLIMIT;
        }
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddressBytes);
        programCall.setValue(BigInteger.ZERO);
        programCall.setGasLimit(gasLimit);
        programCall.setPrice(ContractConstant.CONTRACT_CONSTANT_PRICE);
        programCall.setNumber(blockHeight);
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        programCall.setViewMethod(isCustomGasLimit);

        ProgramExecutor track;
        if (executor == null) {
            track = getProgramExecutor(chainId).begin(stateRoot);
        } else {
            track = executor.startTracking();
        }
        ProgramResult programResult = track.call(programCall);

        return programResult;
    }

    public Result validateNrc20Contract(int chainId, ProgramExecutor track, ContractWrapperTransaction tx, ContractResult contractResult) {
        if (contractResult == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        ContractData createContractData = tx.getContractData();
        byte[] contractAddress = contractResult.getContractAddress();
        long bestBlockHeight = vmContext.getBestHeight(chainId);
        List<ProgramMethod> methods = this.getAllMethods(chainId, createContractData.getCode());
        Map<String, ProgramMethod> contractMethodsMap = new HashMap<>();
        boolean isNrc20 = this.checkNrc20Contract(methods, contractMethodsMap);
        boolean isNrc721 = false;
        if (!isNrc20) {
            isNrc721 = this.checkNrc721Contract(methods, contractMethodsMap);
        }
        if (isNrc20) {
            contractResult.setTokenType(TokenTypeStatus.NRC20.status());
        } else if (isNrc721) {
            contractResult.setTokenType(TokenTypeStatus.NRC721.status());
        }
        boolean isAcceptDirectTransfer = this.checkAcceptDirectTransfer(methods);
        contractResult.setNrc20(isNrc20);
        contractResult.setAcceptDirectTransfer(isAcceptDirectTransfer);
        if (isNrc20 || isNrc721) {
            // NRC20 tokenName 验证代币名称格式
            ProgramResult programResult = this.invokeViewMethod(chainId, track, null, bestBlockHeight, contractAddress, NRC20_METHOD_NAME, null, null);
            if (programResult.isSuccess()) {
                String tokenName = programResult.getResult();
                if (StringUtils.isNotBlank(tokenName)) {
                    if (!validTokenNameOrSymbol(tokenName)) {
                        contractResult.setError(true);
                        contractResult.setErrorMessage("The format of the name is incorrect.");
                        return getFailed();
                    }
                    contractResult.setTokenName(tokenName);
                }
            }
            // NRC20 tokenSymbol 验证代币符号的格式
            programResult = this.invokeViewMethod(chainId, track, null, bestBlockHeight, contractAddress, NRC20_METHOD_SYMBOL, null, null);
            if (programResult.isSuccess()) {
                String symbol = programResult.getResult();
                if (StringUtils.isNotBlank(symbol)) {
                    if (!validTokenNameOrSymbol(symbol)) {
                        contractResult.setError(true);
                        contractResult.setErrorMessage("The format of the symbol is incorrect.");
                        return getFailed();
                    }
                    contractResult.setTokenSymbol(symbol);
                }
            }

            if (isNrc20) {
                programResult = this.invokeViewMethod(chainId, track, null, bestBlockHeight, contractAddress, NRC20_METHOD_DECIMALS, null, null);
                BigInteger decimalsBig = BigInteger.ZERO;
                if (programResult.isSuccess()) {
                    String decimals = programResult.getResult();
                    if (StringUtils.isNotBlank(decimals)) {
                        try {
                            decimalsBig = new BigInteger(decimals);
                            if (decimalsBig.compareTo(BigInteger.ZERO) < 0 || decimalsBig.compareTo(MAXIMUM_DECIMALS) > 0) {
                                contractResult.setError(true);
                                contractResult.setErrorMessage("The value of decimals ranges from 0 to 18.");
                                return getFailed();
                            }
                            contractResult.setTokenDecimals(decimalsBig.intValue());
                        } catch (Exception e) {
                            Log.error("Get nrc20 decimals error.", e);
                            // skip it
                        }
                    }
                }
                programResult = this.invokeViewMethod(chainId, track, null, bestBlockHeight, contractAddress, NRC20_METHOD_TOTAL_SUPPLY, null, null);
                if (programResult.isSuccess()) {
                    String totalSupply = programResult.getResult();
                    if (StringUtils.isNotBlank(totalSupply)) {
                        try {
                            BigInteger totalSupplyBig = new BigInteger(totalSupply);
                            if (totalSupplyBig.compareTo(BigInteger.ZERO) <= 0 || totalSupplyBig.compareTo(MAXIMUM_TOTAL_SUPPLY.multiply(BigInteger.TEN.pow(decimalsBig.intValue()))) > 0) {
                                contractResult.setErrorMessage("The value of totalSupply ranges from 1 to 2^256 - 1.");
                                contractResult.setError(true);
                                return getFailed();
                            }
                            contractResult.setTokenTotalSupply(totalSupplyBig);
                        } catch (Exception e) {
                            Log.error("Get nrc20 totalSupply error.", e);
                            // skip it
                        }
                    }
                }
            }
        }
        return getSuccess();
    }

    public ContractBalance getBalance(int chainId, int assetChainId, int assetId, byte[] address) {
        ContractTempBalanceManager tempBalanceManager;
        if (ProtocolGroupManager.getCurrentVersion(chainId) >= ContractContext.UPDATE_VERSION_CONTRACT_BALANCE) {
            tempBalanceManager = getBatchInfoTempBalanceManagerV8(chainId);
        } else {
            tempBalanceManager = getBatchInfoTempBalanceManager(chainId);
        }
        if (tempBalanceManager != null) {
            Result<ContractBalance> balance = tempBalanceManager.getBalance(address, assetChainId, assetId);
            if (balance.isSuccess()) {
                return balance.getData();
            } else {
                Log.error("[{}] Get balance error.", AddressTool.getStringAddressByBytes(address));
            }
        } else {
            ContractBalance realBalance = getRealBalance(chainId, assetChainId, assetId, AddressTool.getStringAddressByBytes(address));
            if (realBalance != null) {
                return realBalance;
            }
        }
        return ContractBalance.newInstance();
    }

    public ContractBalance getRealBalance(int chainId, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> balance = LedgerCall.getConfirmedBalanceAndNonce(getChain(chainId), assetChainId, assetId, address);
            ContractBalance contractBalance = ContractBalance.newInstance();
            contractBalance.setBalance(new BigInteger(balance.get("available").toString()));
            contractBalance.setFreeze(new BigInteger(balance.get("freeze").toString()));
            contractBalance.setNonce((String) balance.get("nonce"));
            return contractBalance;
        } catch (NulsException e) {
            Log.error(e);
            return ContractBalance.newInstance();
        }
    }

    public ContractBalance getUnConfirmedBalanceAndNonce(int chainId, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> balance = LedgerCall.getBalanceAndNonce(getChain(chainId), assetChainId, assetId, address);
            ContractBalance contractBalance = ContractBalance.newInstance();
            contractBalance.setBalance(new BigInteger(balance.get("available").toString()));
            contractBalance.setFreeze(new BigInteger(balance.get("freeze").toString()));
            contractBalance.setNonce((String) balance.get("nonce"));
            return contractBalance;
        } catch (NulsException e) {
            Log.error(e);
            return ContractBalance.newInstance();
        }
    }

    public void createTempBalanceManagerAndCurrentBlockHeader(int chainId, long number, long blockTime, byte[] packingAddress) {
        ContractTempBalanceManager tempBalanceManager = ContractTempBalanceManager.newInstance(chainId);
        BlockHeader tempHeader = new BlockHeader();
        tempHeader.setHeight(number);
        tempHeader.setTime(blockTime);
        tempHeader.setPackingAddress(packingAddress);
        Chain chain = getChain(chainId);
        chain.getBatchInfo().setTempBalanceManager(tempBalanceManager);
        chain.getBatchInfo().setCurrentBlockHeader(tempHeader);
    }

    public ContractTempBalanceManager getBatchInfoTempBalanceManagerV8(int chainId) {
        BatchInfoV8 batchInfo;
        if ((batchInfo = getChain(chainId).getBatchInfoV8()) == null) {
            return null;
        }
        return batchInfo.getTempBalanceManager();
    }

    public BlockHeader getBatchInfoCurrentBlockHeaderV8(int chainId) {
        BatchInfoV8 batchInfo;
        if ((batchInfo = getChain(chainId).getBatchInfoV8()) == null) {
            return null;
        }
        return batchInfo.getCurrentBlockHeader();
    }

    public ContractTempBalanceManager getBatchInfoTempBalanceManager(int chainId) {
        BatchInfo batchInfo;
        if ((batchInfo = getChain(chainId).getBatchInfo()) == null) {
            return null;
        }
        return batchInfo.getTempBalanceManager();
    }

    public BlockHeader getBatchInfoCurrentBlockHeader(int chainId) {
        BatchInfo batchInfo;
        if ((batchInfo = getChain(chainId).getBatchInfo()) == null) {
            return null;
        }
        return batchInfo.getCurrentBlockHeader();
    }

    public Result<ContractAddressInfoPo> getContractAddressInfo(int chainId, byte[] contractAddressBytes) {
        return contractAddressStorageService.getContractAddressInfo(chainId, contractAddressBytes);
    }

    public Result<ContractTokenInfo> getContractToken(int chainId, BlockHeader blockHeader, String address, String contractAddress) {
        try {
            if (StringUtils.isBlank(contractAddress) || StringUtils.isBlank(address)) {
                return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
            }

            if (!AddressTool.validAddress(chainId, contractAddress) || !AddressTool.validAddress(chainId, address)) {
                return Result.getFailed(ADDRESS_ERROR);
            }

            long blockHeight = blockHeader.getHeight();
            // 当前区块状态根
            byte[] currentStateRoot = ContractUtil.getStateRoot(blockHeader);

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = this.getContractAddressInfo(chainId, contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po == null) {
                return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }
            if (!po.isNrc20()) {
                return Result.getFailed(ContractErrorCode.CONTRACT_NOT_NRC20);
            }

            ProgramResult programResult = this.invokeViewMethod(chainId, null, false, currentStateRoot, blockHeight, contractAddressBytes, "balanceOf", null, ContractUtil.twoDimensionalArray(new Object[]{address}));
            Result<ContractTokenInfo> result;
            if (!programResult.isSuccess()) {
                result = getFailed();
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
            } else {
                result = getSuccess();
                ContractTokenInfo tokenInfo = new ContractTokenInfo(contractAddress, po.getNrc20TokenName(), po.getDecimals(), new BigInteger(programResult.getResult()), po.getNrc20TokenSymbol(), po.getBlockHeight());
                ProgramExecutor track = getProgramExecutor(chainId).begin(currentStateRoot);
                tokenInfo.setStatus(ContractStatus.getStatus(track.status(AddressTool.getAddress(tokenInfo.getContractAddress())).ordinal()));
                result.setData(tokenInfo);
            }
            return result;
        } catch (Exception e) {
            Log.error("get contract token via VM error.", e);
            return getFailed();
        }

    }

    public void dealNrc20Events(int chainId, byte[] newestStateRoot, Transaction tx, ContractResult contractResult, ContractAddressInfoPo po) {
        if (po == null) {
            return;
        }
        try {
            List<String> events = contractResult.getEvents();
            int size = events.size();
            // 目前只处理Transfer事件, 为了刷新账户的token余额
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
                    if (Arrays.areEqual(po.getContractAddress(), contractAddressBytes)) {
                        contractAddressInfo = po;
                    } else {
                        Result<ContractAddressInfoPo> contractAddressInfoResult = this.getContractAddressInfo(chainId, contractAddressBytes);
                        contractAddressInfo = contractAddressInfoResult.getData();
                    }

                    if (contractAddressInfo == null) {
                        continue;
                    }
                    // 事件不是NRC20合约的事件
                    if (!contractAddressInfo.isNrc20()) {
                        continue;
                    }
                    byte[] txHashBytes;
                    byte[] from = tokenTransferInfoPo.getFrom();
                    byte[] to = tokenTransferInfoPo.getTo();
                    tokenTransferInfoPo.setName(contractAddressInfo.getNrc20TokenName());
                    tokenTransferInfoPo.setSymbol(contractAddressInfo.getNrc20TokenSymbol());
                    tokenTransferInfoPo.setDecimals(contractAddressInfo.getDecimals());
                    tokenTransferInfoPo.setTime(tx.getTime());
                    tokenTransferInfoPo.setBlockHeight(tx.getBlockHeight());
                    txHashBytes = tx.getHash().getBytes();
                    tokenTransferInfoPo.setTxHash(txHashBytes);
                    tokenTransferInfoPo.setStatus((byte) (contractResult.isSuccess() ? 1 : 2));

                    if (from != null) {
                        this.refreshTokenBalance(chainId, newestStateRoot, tx.getBlockHeight(), contractAddressInfo, AddressTool.getStringAddressByBytes(from), contractAddress);
                        this.saveTokenTransferInfo(chainId, from, txHashBytes, new VarInt(i).encode(), tokenTransferInfoPo);
                    }
                    if (to != null) {
                        this.refreshTokenBalance(chainId, newestStateRoot, tx.getBlockHeight(), contractAddressInfo, AddressTool.getStringAddressByBytes(to), contractAddress);
                        this.saveTokenTransferInfo(chainId, to, txHashBytes, new VarInt(i).encode(), tokenTransferInfoPo);
                    }
                }
            }
        } catch (Exception e) {
            Log.warn("contract event parse error.", e);
            throw new RuntimeException(e);
        }
    }

    public void rollbackNrc20Events(int chainId, Transaction tx, ContractResult contractResult) {
        try {
            byte[] txHashBytes = null;
            txHashBytes = tx.getHash().getBytes();

            List<String> events = contractResult.getEvents();
            int size = events.size();
            // 目前只处理Transfer事件, 为了刷新账户的token余额
            String event;
            ContractAddressInfoPo contractAddressInfo;
            if (events != null && size > 0) {
                Map<String, Boolean> isNRC20Map = new HashMap<>();
                for (int i = size - 1; i >= 0; i--) {
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
                    Boolean isNRC20 = isNRC20Map.get(contractAddress);
                    if (isNRC20 == null) {
                        byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
                        Result<ContractAddressInfoPo> contractAddressInfoResult = this.getContractAddressInfo(chainId, contractAddressBytes);
                        contractAddressInfo = contractAddressInfoResult.getData();
                        if (contractAddressInfo == null) {
                            continue;
                        }
                        isNRC20 = contractAddressInfo.isNrc20();
                        isNRC20Map.put(contractAddress, isNRC20);
                    }
                    // 事件不是NRC20合约的事件
                    if (!isNRC20) {
                        continue;
                    }

                    // 回滚token余额
                    this.rollbackContractToken(chainId, tokenTransferInfoPo);
                    if (tokenTransferInfoPo.getFrom() != null) {
                        contractTokenTransferStorageService.deleteTokenTransferInfo(chainId, Arrays.concatenate(tokenTransferInfoPo.getFrom(), txHashBytes, new VarInt(i).encode()));
                    }
                    if (tokenTransferInfoPo.getTo() != null) {
                        contractTokenTransferStorageService.deleteTokenTransferInfo(chainId, Arrays.concatenate(tokenTransferInfoPo.getTo(), txHashBytes, new VarInt(i).encode()));
                    }

                }
            }
        } catch (Exception e) {
            Log.warn("contract event parse error.", e);
            throw new RuntimeException(e);
        }
    }

    public void rollbackContractToken(int chainId, ContractTokenTransferInfoPo po) {
        try {
            String contractAddressStr = po.getContractAddress();
            byte[] from = po.getFrom();
            byte[] to = po.getTo();
            BigInteger token = po.getValue();
            String fromStr;
            String toStr;
            ContractTokenBalanceManager contractTokenBalanceManager = getChain(chainId).getContractTokenBalanceManager();
            if (from != null) {
                fromStr = AddressTool.getStringAddressByBytes(from);
                contractTokenBalanceManager.addContractToken(fromStr, contractAddressStr, token);
            }
            if (to != null) {
                toStr = AddressTool.getStringAddressByBytes(to);
                contractTokenBalanceManager.subtractContractToken(toStr, contractAddressStr, token);
            }
        } catch (Exception e) {
            // skip it
            Log.error(e);
            throw new RuntimeException(e);
        }
    }

    private void saveTokenTransferInfo(int chainId, byte[] address, byte[] txHashBytes, byte[] index, ContractTokenTransferInfoPo tokenTransferInfoPo) {
        contractTokenTransferStorageService.saveTokenTransferInfo(chainId, Arrays.concatenate(address, txHashBytes, index), tokenTransferInfoPo);
    }

    public void refreshTokenBalance(int chainId, byte[] stateRoot, long blockHeight, ContractAddressInfoPo po, String address, String contractAddress) {
        this.refreshTokenBalance(chainId, null, stateRoot, blockHeight, po, address, contractAddress);
    }

    private void refreshTokenBalance(int chainId, ProgramExecutor executor, byte[] stateRoot, long blockHeight, ContractAddressInfoPo po, String address, String contractAddress) {
        byte[] contractAddressBytes = po.getContractAddress();
        ProgramResult programResult = this.invokeViewMethod(chainId, executor, stateRoot, blockHeight, contractAddressBytes, NRC20_METHOD_BALANCE_OF, null, address);
        if (!programResult.isSuccess()) {
            return;
        } else {
            getChain(chainId).getContractTokenBalanceManager().refreshContractToken(address, contractAddress, po, new BigInteger(programResult.getResult()));
        }

    }

    public ProgramStatus getContractStatus(int chainId, byte[] stateRoot, byte[] contractAddress) {
        ProgramExecutor track = getProgramExecutor(chainId).begin(stateRoot);
        return track.status(contractAddress);
    }

    public ContractResult makeFailedContractResult(int chainId, ContractWrapperTransaction tx, CallableResult callableResult, String errorMsg) {
        ContractResult contractResult = ContractResult.genFailed(tx.getContractData(), errorMsg);
        makeContractResult(tx, contractResult);
        if (callableResult != null) {
            callableResult.putFailed(chainId, contractResult);
        }
        return contractResult;
    }

    /**
     * 提取合约多资产转入的信息
     */
    public void extractAssetInfoFromCallTransaction(CallContractData contractData, Transaction tx) throws NulsException {
        // 过滤特殊的交易，token跨链转入交易(to中包含其他资产)
        if (CROSS_CHAIN == tx.getType()) {
            return;
        }
        CoinData coinData = tx.getCoinDataInstance();
        List<ProgramMultyAssetValue> list = extractMultyAssetInfoFromCallTransaction(coinData);
        contractData.setMultyAssetValues(list);
    }

    public ContractReturnGasTransaction makeReturnGasTx(List<ContractResult> resultList, long time) throws IOException {
        ContractWrapperTransaction wrapperTx;
        ContractData contractData;
        Map<ByteArrayWrapper, BigInteger> returnMap = new HashMap<>();
        for (ContractResult contractResult : resultList) {
            wrapperTx = contractResult.getTx();
            // 终止合约不消耗Gas，跳过
            if (wrapperTx.getType() == DELETE_CONTRACT) {
                continue;
            }
            // add by pierre at 2019-12-03 代币跨链交易的合约调用是系统调用，不计算Gas消耗，跳过
            if (wrapperTx.getType() == CROSS_CHAIN) {
                continue;
            }
            // end code by pierre
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
        if (!returnMap.isEmpty()) {
            CoinData coinData = new CoinData();
            List<CoinTo> toList = coinData.getTo();
            Set<Map.Entry<ByteArrayWrapper, BigInteger>> entries = returnMap.entrySet();
            CoinTo returnCoin;
            for (Map.Entry<ByteArrayWrapper, BigInteger> entry : entries) {
                returnCoin = new CoinTo(entry.getKey().getBytes(), CHAIN_ID, ASSET_ID, entry.getValue(), 0L);
                toList.add(returnCoin);
            }
            ContractReturnGasTransaction tx = new ContractReturnGasTransaction();
            tx.setTime(time);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            return tx;
        }
        return null;
    }
}
