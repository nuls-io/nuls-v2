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
package io.nuls.contract.util;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.*;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.*;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.ChainManagerCall;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Response;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.FAILED;
import static io.nuls.core.constant.TxType.*;
import static io.nuls.core.model.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2018/8/25
 */
public class ContractUtil {

    public static String[][] twoDimensionalArray(Object[] args, String[] types) {
        if (args == null) {
            return null;
        } else {
            int length = args.length;
            String[][] two = new String[length][];
            Object arg;
            for (int i = 0; i < length; i++) {
                arg = args[i];
                if (arg == null) {
                    two[i] = new String[0];
                    continue;
                }
                if (arg instanceof String) {
                    String argStr = (String) arg;
                    // 非String类型参数，若传参是空字符串，则赋值为空一维数组，避免数字类型转化异常 -> 空字符串转化为数字
                    if (types != null && isBlank(argStr) && !STRING.equalsIgnoreCase(types[i])) {
                        two[i] = new String[0];
                    } else {
                        two[i] = new String[]{argStr};
                    }
                } else if (arg.getClass().isArray()) {
                    int len = Array.getLength(arg);
                    String[] result = new String[len];
                    for (int k = 0; k < len; k++) {
                        result[k] = valueOf(Array.get(arg, k));
                    }
                    two[i] = result;
                } else if (arg instanceof ArrayList) {
                    ArrayList resultArg = (ArrayList) arg;
                    int size = resultArg.size();
                    String[] result = new String[size];
                    for (int k = 0; k < size; k++) {
                        result[k] = valueOf(resultArg.get(k));
                    }
                    two[i] = result;
                } else {
                    two[i] = new String[]{valueOf(arg)};
                }
            }
            return two;
        }
    }

    public static byte[] extractContractAddressFromTxData(Transaction tx) {
        if (tx == null) {
            return null;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT
                || txType == DELETE_CONTRACT) {
            return extractContractAddressFromTxData(tx.getTxData());
        }
        return null;
    }

    private static byte[] extractContractAddressFromTxData(byte[] txData) {
        if (txData == null) {
            return null;
        }
        int length = txData.length;
        if (length < Address.ADDRESS_LENGTH * 2) {
            return null;
        }
        byte[] contractAddress = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(txData, Address.ADDRESS_LENGTH, contractAddress, 0, Address.ADDRESS_LENGTH);
        return contractAddress;
    }

    public static ContractWrapperTransaction parseContractTransaction(ContractTempTransaction tx, ChainManager chainManager) throws NulsException {
        ContractWrapperTransaction contractTransaction = null;
        ContractData contractData = null;
        boolean isContractTx = true;
        switch (tx.getType()) {
            case CALL_CONTRACT:
                CallContractData call = new CallContractData();
                call.parse(tx.getTxData(), 0);
                contractData = call;
                break;
            case CREATE_CONTRACT:
                CreateContractData create = new CreateContractData();
                create.parse(tx.getTxData(), 0);
                contractData = create;
                break;
            // add by pierre at 2019-11-02 需要协议升级
            // add by pierre at 2019-10-20
            case CROSS_CHAIN:
                contractData = parseCrossChainTx(tx, chainManager);
                if(contractData == null) {
                    isContractTx = false;
                    break;
                }
                break;
            // end code by pierre
            case DELETE_CONTRACT:
                DeleteContractData delete = new DeleteContractData();
                delete.parse(tx.getTxData(), 0);
                contractData = delete;
                break;
            default:
                Log.warn("Non-contract tx detected. Tx hash is {}, type is {}", tx.getHash().toString(), tx.getType());
                isContractTx = false;
                break;
        }
        if (isContractTx) {
            contractTransaction = new ContractWrapperTransaction(tx, tx.getTxHex(), contractData);
        }
        return contractTransaction;
    }

    public static CallContractData parseCrossChainTx(Transaction tx, ChainManager chainManager) throws NulsException {
        CoinData coinData = tx.getCoinDataInstance();
        // 解析交易资产ID，跨链转账to资产识别为已注册的合约跨链资产，则设置合约调用
        List<CoinTo> toList = coinData.getTo();
        CoinTo coinTo = toList.get(0);
        byte[] toAddress = coinTo.getAddress();
        int chainIdByToAddress = AddressTool.getChainIdByAddress(toAddress);
        if(chainIdByToAddress != ContractContext.MAIN_CHAIN_ID) {
            // 接收者非主链地址，不是跨链转入交易
            return null;
        }
        int assetsChainId = coinTo.getAssetsChainId();
        Chain chain = chainManager.getChainMap().get(assetsChainId);
        if(chain == null) {
            // 未知链
            return null;
        }
        int assetsId = coinTo.getAssetsId();
        Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
        String nrcContractAddress = tokenAssetsContractAddressInfoMap.get(assetsChainId + "-" + assetsId);
        if(StringUtils.isBlank(nrcContractAddress)) {
            // 没有注册资产
            return null;
        }
        boolean isCrossAssets = ChainManagerCall.isCrossAssets(assetsChainId, assetsId);
        if(!isCrossAssets) {
            // 没有注册跨链资产
            return null;
        }
        // 解析跨链转账交易，设置调用合约的参数，特殊设置 sender == null
        List<CoinFrom> fromList = coinData.getFrom();
        CoinFrom from = fromList.get(0);
        byte[] fromAddress = from.getAddress();
        BigInteger amount = coinTo.getAmount();

        CallContractData contractData = new CallContractData();
        contractData.setSender(null);
        contractData.setGasLimit(CROSS_CHAIN_GASLIMIT);
        contractData.setPrice(CONTRACT_MINIMUM_PRICE);
        contractData.setMethodName(CROSS_CHAIN_SYSTEM_CONTRACT_TRANSFER_IN_METHOD_NAME);
        contractData.setValue(BigInteger.ZERO);
        String[][] args = new String[][]{
                new String[]{nrcContractAddress},
                new String[]{AddressTool.getStringAddressByBytes(fromAddress)},
                new String[]{AddressTool.getStringAddressByBytes(toAddress)},
                new String[]{amount.toString()},
                new String[]{String.valueOf(assetsChainId)},
                new String[]{String.valueOf(assetsId)}};
        contractData.setArgsCount((short) args.length);
        contractData.setArgs(args);
        contractData.setContractAddress(CROSS_CHAIN_SYSTEM_CONTRACT);
        return contractData;
    }

    public static String[][] twoDimensionalArray(Object[] args) {
        return twoDimensionalArray(args, null);
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    public static ContractTokenTransferInfoPo convertJsonToTokenTransferInfoPo(int chainId, String event) {
        if (isBlank(event)) {
            return null;
        }
        ContractTokenTransferInfoPo po;
        try {
            Map<String, Object> eventMap = JSONUtils.json2map(event);
            String eventName = (String) eventMap.get(CONTRACT_EVENT);
            String contractAddress = (String) eventMap.get(CONTRACT_EVENT_ADDRESS);
            po = new ContractTokenTransferInfoPo();
            po.setContractAddress(contractAddress);
            if (NRC20_EVENT_TRANSFER.equals(eventName)) {
                Map<String, Object> data = (Map<String, Object>) eventMap.get(CONTRACT_EVENT_DATA);
                Collection<Object> values = data.values();
                int i = 0;
                String transferEventdata;
                byte[] addressBytes;
                for (Object object : values) {
                    transferEventdata = (String) object;
                    if (i == 0 || i == 1) {
                        if (AddressTool.validAddress(chainId, transferEventdata)) {
                            addressBytes = AddressTool.getAddress(transferEventdata);
                            if (i == 0) {
                                po.setFrom(addressBytes);
                            } else {
                                po.setTo(addressBytes);
                            }
                        }
                    }
                    if (i == 2) {
                        po.setValue(isBlank(transferEventdata) ? BigInteger.ZERO : new BigInteger(transferEventdata));
                        break;
                    }
                    i++;
                }
                return po;
            }
            return null;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public static boolean isContractTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT
                || txType == DELETE_CONTRACT
                || txType == CONTRACT_TRANSFER
                || txType == CONTRACT_RETURN_GAS) {
            return true;
        }
        return false;
    }

    public static boolean isGasCostContractTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        int txType = tx.getType();
        if (txType == CREATE_CONTRACT
                || txType == CALL_CONTRACT) {
            return true;
        }
        return false;
    }

    public static boolean isLockContract(int chainId, long blockHeight) throws NulsException {
        if (blockHeight > 0) {
            long bestBlockHeight = BlockCall.getLatestHeight(chainId);
            long confirmCount = bestBlockHeight - blockHeight;
            if (confirmCount < 7) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLockContract(long lastestHeight, long blockHeight) throws NulsException {
        if (blockHeight > 0) {
            long confirmCount = lastestHeight - blockHeight;
            if (confirmCount < 7) {
                return true;
            }
        }
        return false;
    }

    public static byte[] getStateRoot(BlockHeader blockHeader) {
        if (blockHeader == null || blockHeader.getExtend() == null) {
            return null;
        }
        byte[] stateRoot = blockHeader.getStateRoot();
        if (stateRoot != null && stateRoot.length > 0) {
            return stateRoot;
        }
        try {
            BlockExtendsData extendsData = blockHeader.getExtendsData();
            stateRoot = extendsData.getStateRoot();
            blockHeader.setStateRoot(stateRoot);
            return stateRoot;
        } catch (Exception e) {
            Log.error("parse stateRoot error.", e);
        }
        return null;
    }

    public static String bigInteger2String(BigInteger bigInteger) {
        if (bigInteger == null) {
            return null;
        }
        return bigInteger.toString();
    }

    public static String simplifyErrorMsg(String errorMsg) {
        String resultMsg = "contract error - ";
        if (isBlank(errorMsg)) {
            return resultMsg;
        }
        if (errorMsg.contains("Exception:")) {
            String[] msgs = errorMsg.split("Exception:", 2);
            return resultMsg + msgs[1].trim();
        }
        return resultMsg + errorMsg;
    }

    public static Result checkVmResultAndReturn(String errorMessage, Result defaultResult) {
        if (isBlank(errorMessage)) {
            return defaultResult;
        }
        if (isNotEnoughGasError(errorMessage)) {
            return Result.getFailed(ContractErrorCode.CONTRACT_GAS_LIMIT).setMsg(errorMessage);
        }
        return defaultResult;
    }

    private static boolean isNotEnoughGasError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }
        if (errorMessage.contains(NOT_ENOUGH_GAS)) {
            return true;
        }
        return false;
    }

    public static boolean isNotEnoughGasError(ContractResult contractResult) {
        if (contractResult.isSuccess()) {
            return false;
        }
        return isNotEnoughGasError(contractResult.getErrorMessage());
    }

    public static boolean isTerminatedContract(int status) {
        return ContractConstant.STOP == status;
    }

    public static boolean isTransferMethod(String method) {
        return (NRC20_METHOD_TRANSFER.equals(method)
                || NRC20_METHOD_TRANSFER_FROM.equals(method));
    }

    public static String argToString(String[][] args) {
        if (args == null) {
            return "";
        }
        String result = "";
        for (String[] a : args) {
            result += Arrays.toString(a) + "| ";
        }
        return result;
    }

    public static boolean checkPrice(long price) {
        if (price < CONTRACT_MINIMUM_PRICE) {
            return false;
        }
        return true;
    }

    public static boolean checkGasLimit(long gas) {
        if (gas <= 0 || gas > MAX_GASLIMIT) {
            return false;
        }
        return true;
    }

    public static void createTable(String name) {
        if (!RocksDBService.existTable(name)) {
            try {
                RocksDBService.createTable(name);
            } catch (Exception e) {
                Log.error(e);
                throw new NulsRuntimeException(ContractErrorCode.CONTRACT_OTHER_ERROR);
            }
        }
    }

    public static boolean isLegalContractAddress(int chainId, byte[] addressBytes) {
        if (addressBytes == null) {
            return false;
        }
        return AddressTool.validContractAddress(addressBytes, chainId);
    }

    public static void put(Map<String, Set<ContractResult>> map, String contractAddress, ContractResult result) {
        Set<ContractResult> resultSet = map.get(contractAddress);
        if (resultSet == null) {
            resultSet = new HashSet<>();
            map.put(contractAddress, resultSet);
        }
        resultSet.add(result);
    }

    public static void putAll(int chainId, Map<String, Set<ContractResult>> map, ContractResult contractResult) {
        Set<String> addressSet = collectAddress(chainId, contractResult);
        for (String address : addressSet) {
            put(map, address, contractResult);
        }
    }

    public static Set<String> collectAddress(int chainId, ContractResult result) {
        Set<String> set = new HashSet<>();
        set.add(AddressTool.getStringAddressByBytes(result.getContractAddress()));
        Set<String> innerCallSet = result.getContractAddressInnerCallSet();
        if (innerCallSet != null) {
            set.addAll(innerCallSet);
        }

        result.getTransfers().stream().forEach(transfer -> {
            if (ContractUtil.isLegalContractAddress(chainId, transfer.getFrom())) {
                set.add(AddressTool.getStringAddressByBytes(transfer.getFrom()));
            }
            if (ContractUtil.isLegalContractAddress(chainId, transfer.getTo())) {
                set.add(AddressTool.getStringAddressByBytes(transfer.getTo()));
            }
        });
        return set;
    }

    /**
     * @param contractResultList
     * @return 去掉重复的交易，并按照时间降序排列
     */
    public static List<ContractResult> deduplicationAndOrder(List<ContractResult> contractResultList) {
        return contractResultList.stream().collect(Collectors.toSet()).stream()
                .collect(Collectors.toList()).stream().sorted(CompareTxTimeDesc.getInstance()).collect(Collectors.toList());
    }

    /**
     * @param contractResultList
     * @return 收集合约执行中所有出现过的合约地址，包括内部调用合约，合约转账
     */
    public static Map<String, Set<ContractResult>> collectAddressMap(int chainId, List<ContractResult> contractResultList) {
        Map<String, Set<ContractResult>> map = new HashMap<>();
        for (ContractResult result : contractResultList) {
            put(map, AddressTool.getStringAddressByBytes(result.getContractAddress()), result);
            result.getContractAddressInnerCallSet().stream().forEach(inner -> put(map, inner, result));

            result.getTransfers().stream().forEach(transfer -> {
                if (ContractUtil.isLegalContractAddress(chainId, transfer.getFrom())) {
                    put(map, AddressTool.getStringAddressByBytes(transfer.getFrom()), result);
                }
                if (ContractUtil.isLegalContractAddress(chainId, transfer.getTo())) {
                    put(map, AddressTool.getStringAddressByBytes(transfer.getTo()), result);
                }
            });
        }
        return map;
    }

    public static void makeContractResult(ContractWrapperTransaction tx, ContractResult contractResult) {
        contractResult.setTx(tx);
        contractResult.setTxTime(tx.getTime());
        contractResult.setHash(tx.getHash().toString());
        contractResult.setTxOrder(tx.getOrder());
    }

    public static boolean makeContractResultAndCheckGasSerial(ContractWrapperTransaction tx, ContractResult contractResult, BatchInfo batchInfo) {
        int i = 0;
        // 所以交易都按顺序串行执行checkGas
        while (true) {
            synchronized (batchInfo) {
                int txOrder = tx.getOrder();
                int serialOrder = batchInfo.getSerialOrder();
                if(serialOrder == txOrder) {
                    if(Log.isDebugEnabled()) {
                        Log.debug("串行交易order - [{}]", txOrder);
                    }
                    batchInfo.setSerialOrder(serialOrder + 1);
                    contractResult.setTx(tx);
                    contractResult.setTxTime(tx.getTime());
                    contractResult.setHash(tx.getHash().toString());
                    contractResult.setTxOrder(tx.getOrder());
                    boolean checkGas = checkGas(contractResult, batchInfo);
                    batchInfo.notifyAll();
                    return checkGas;
                } else {
                    i++;
                    if(Log.isDebugEnabled()) {
                        Log.debug("等待的交易order - [{}], [{}]线程等待次数 - [{}]", txOrder, Thread.currentThread().getName(), i);
                    }
                    try {
                        batchInfo.wait(5000);
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                    // 防止唤醒线程意外终止，导致等待线程永远等待
                    if(i > 4) {
                        return false;
                    }
                }
            }
        }
    }

    private static boolean checkGas(ContractResult contractResult, BatchInfo batchInfo) {
        long gasUsed = contractResult.getGasUsed();
        boolean isAdded = batchInfo.addGasCostTotal(gasUsed, contractResult.getHash());
        if(!isAdded) {
            contractResult.setError(true);
            contractResult.setErrorMessage("Exceed tx count [600] or gas limit of block [13,000,000 gas], the contract transaction ["+ contractResult.getHash() +"] revert to package queue.");
        }
        return isAdded;
    }

    public static Result getSuccess() {
        return Result.getSuccess(ContractErrorCode.SUCCESS);
    }

    public static Result getFailed() {
        return Result.getFailed(FAILED);
    }

    public static String asString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] asBytes(String string) {
        return Base64.getDecoder().decode(string);
    }

    public static BigInteger minus(BigInteger a, BigInteger b) {
        BigInteger result = a.subtract(b);
        if (result.compareTo(BigInteger.ZERO) < 0) {
            throw new RuntimeException("Negative number detected.");
        }
        return result;
    }

    public static ContractBaseTransaction convertContractTx(Transaction tx) {
        ContractBaseTransaction resultTx = null;
        switch (tx.getType()) {
            case CREATE_CONTRACT:
                resultTx = new CreateContractTransaction();
                break;
            case CALL_CONTRACT:
                resultTx = new CallContractTransaction();
                break;
            case DELETE_CONTRACT:
                resultTx = new DeleteContractTransaction();
                break;
            case CONTRACT_TRANSFER:
                resultTx = new ContractTransferTransaction();
                break;
            case CONTRACT_RETURN_GAS:
                resultTx = new ContractReturnGasTransaction();
                break;
            default:
                break;
        }
        if (resultTx != null) {
            resultTx.copyTx(tx);
        }
        return resultTx;
    }

    public static Response wrapperFailed(Result result) {
        String msg;
        ErrorCode errorCode;
        if (result != null) {
            errorCode = result.getErrorCode();
            msg = result.getMsg();
            if (StringUtils.isBlank(msg)) {
                msg = errorCode.getMsg();
            }
            Response res = MessageUtil.newFailResponse("",msg);
            res.setResponseErrorCode(errorCode.getCode());
            return res;
        } else {
            return MessageUtil.newFailResponse("", FAILED);
        }
    }

    public static int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }

    public static byte[] extractPublicKey(Transaction tx) {
        if(tx.getTransactionSignature() == null) {
            return null;
        }
        TransactionSignature signature = new TransactionSignature();
        try {
            signature.parse(tx.getTransactionSignature(), 0);
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
        List<P2PHKSignature> p2PHKSignatures = signature.getP2PHKSignatures();
        P2PHKSignature p2PHKSignature = p2PHKSignatures.get(0);
        byte[] publicKey = p2PHKSignature.getPublicKey();
        return publicKey;
    }

    public static void mapAddBigInteger(LinkedHashMap<String, BigInteger> map, byte[] address, BigInteger amount) {
        String strAddress = asString(address);
        BigInteger currentAmount = map.get(strAddress);
        if (currentAmount == null) {
            map.put(strAddress, amount);
        } else {
            map.put(strAddress, currentAmount.add(amount));
        }
    }

    public static String toString(String[][] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(Arrays.toString(a[i]));
            if (i == iMax) {
                b.append(']');
                break;
            }
            b.append(", ");
        }
        return b.toString();
    }

    public static void addDebugEvents(List<String> debugEvents, Result result) {
        if(debugEvents.isEmpty()) {
            return;
        }
        String msg = result.getMsg();
        if(msg == null) {
            msg = EMPTY;
        }
        msg += ", debugEvents: " + debugEvents.toString();
        result.setMsg(msg);
    }
}
