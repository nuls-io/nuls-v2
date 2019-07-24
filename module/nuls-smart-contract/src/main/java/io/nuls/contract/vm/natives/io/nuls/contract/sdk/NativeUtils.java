/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractNewTxFromOtherModuleHandler;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.dto.BlockHeaderDto;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.FieldCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.contract.vm.program.impl.ProgramInvoke;
import io.nuls.contract.vm.util.Constants;
import io.nuls.contract.vm.util.JsonUtils;
import io.nuls.contract.vm.util.Utils;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.Sha3Hash;
import io.nuls.core.rpc.model.message.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.RPC_RESULT_KEY;
import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;
import static io.nuls.contract.vm.util.Utils.hashMapInitialCapacity;

public class NativeUtils {

    public static final String TYPE = "io/nuls/contract/sdk/Utils";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case revert:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return revert(methodCode, methodArgs, frame);
                }
            case emit:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return emit(methodCode, methodArgs, frame);
                }
            case sha3:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return sha3(methodCode, methodArgs, frame);
                }
            case sha3Bytes:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return sha3Bytes(methodCode, methodArgs, frame);
                }
            case verifySignatureData:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return verifySignatureData(methodCode, methodArgs, frame);
                }
            case getRandomSeedByCount:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getRandomSeedByCount(methodCode, methodArgs, frame);
                }
            case getRandomSeedByHeight:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getRandomSeedByHeight(methodCode, methodArgs, frame);
                }
            case getRandomSeedListByCount:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getRandomSeedListByCount(methodCode, methodArgs, frame);
                }
            case getRandomSeedListByHeight:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getRandomSeedListByHeight(methodCode, methodArgs, frame);
                }
            case invokeExternalCmd:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return invokeExternalCmd(methodCode, methodArgs, frame);
                }
            case obj2Json:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return obj2Json(methodCode, methodArgs, frame);
                }
            default:
                if (check) {
                    return NOT_SUPPORT_NATIVE;
                } else {
                    frame.nonsupportMethod(methodCode);
                    return null;
                }
        }
    }

    public static final String revert = TYPE + "." + "revert" + "(Ljava/lang/String;)V";

    /**
     * native
     *
     * @see Utils#revert(String)
     */
    private static Result revert(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String errorMessage = null;
        if (objectRef != null) {
            errorMessage = frame.heap.runToString(objectRef);
        }
        throw new ErrorException(errorMessage, frame.vm.getGasUsed(), null);
    }

    public static final String emit = TYPE + "." + "emit" + "(Lio/nuls/contract/sdk/Event;)V";

    /**
     * native
     *
     * @see Utils#emit(Event)
     */
    private static Result emit(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        //String str = frame.heap.runToString(objectRef);
        ClassCode classCode = frame.methodArea.loadClass(objectRef.getVariableType().getType());
        Map<String, Object> jsonMap = toJson(objectRef, frame);
        EventJson eventJson = new EventJson();
        eventJson.setContractAddress(frame.vm.getProgramInvoke().getAddress());
        eventJson.setBlockNumber(frame.vm.getProgramInvoke().getNumber() + 1);
        eventJson.setEvent(classCode.simpleName);
        eventJson.setPayload(jsonMap);
        String json = JsonUtils.toJson(eventJson);
        frame.vm.getEvents().add(json);
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    private static Map<String, Object> toJson(ObjectRef objectRef, Frame frame) {
        return toJson(objectRef, frame, 1);
    }

    private static Map<String, Object> toJson(ObjectRef objectRef, Frame frame, int depth) {
        if (objectRef == null) {
            return null;
        }

        Map<String, FieldCode> fields = frame.methodArea.allFields(objectRef.getVariableType().getType());
        Map<String, Object> map = frame.heap.getFields(objectRef);
        Map<String, Object> jsonMap = new LinkedHashMap<>(hashMapInitialCapacity(map.size()));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            FieldCode fieldCode = fields.get(name);
            if (fieldCode != null && !fieldCode.isSynthetic) {
                Object value = entry.getValue();
                jsonMap.put(name, toJson(fieldCode.variableType, value, frame, depth));
            }
        }
        return jsonMap;
    }

    private static Object toJson(VariableType variableType, Object value, Frame frame, int depth) {
        if (value == null) {
            return null;
        }
        if (depth > 3) {
            if (variableType.isPrimitive()) {
                return variableType.getPrimitiveValue(value);
            }
            ObjectRef ref = (ObjectRef) value;
            return frame.heap.runToString(ref);
        }
        if (variableType.isPrimitive()) {
            return variableType.getPrimitiveValue(value);
        } else if (variableType.isArray()) {
            ObjectRef ref = (ObjectRef) value;
            if (variableType.isPrimitiveType() && variableType.getDimensions() == 1) {
                return frame.heap.getObject(ref);
            } else {
                int length = ref.getDimensions()[0];
                Object[] array = new Object[length];
                for (int i = 0; i < length; i++) {
                    Object item = frame.heap.getArray(ref, i);
                    if (item != null) {
                        ObjectRef itemRef = (ObjectRef) item;
                        //item = frame.heap.runToString(itemRef);
                        item = toJson(itemRef.getVariableType(), itemRef, frame, depth + 1);
                    }
                    array[i] = item;
                }
                return array;
            }
        } else if (variableType.isWrapperType()) {
            ObjectRef ref = (ObjectRef) value;
            return frame.heap.runToString(ref);
        } else {
            String type = variableType.getType();
            boolean isCollection = false;
            boolean isMap = false;
            switch (type) {
                case "java/lang/String":
                case "java/math/BigInteger":
                case "java/math/BigDecimal":
                case "io/nuls/contract/sdk/Address":
                    ObjectRef ref = (ObjectRef) value;
                    return frame.heap.runToString(ref);
                case "java/util/Map":
                case "java/util/HashMap":
                case "java/util/LinkedHashMap":
                    isMap = true;
                    break;
                case "java/util/List":
                case "java/util/ArrayList":
                case "java/util/LinkedList":
                case "java/util/Set":
                case "java/util/HashSet":
                case "java/util/HashMap$EntrySet":
                case "java/util/LinkedHashMap$LinkedEntrySet":
                    isCollection = true;
                    break;
                default:
            }
            if (isCollection || isMap) {
                ObjectRef ref = (ObjectRef) value;
                do {
                    // 获取集合的值
                    if (isCollection) {
                        ObjectRef resultRef = frame.heap.getCollectionArrayRef(ref);
                        return toJson(resultRef.getVariableType(), resultRef, frame, depth);
                    }
                    if (isMap) {
                        ObjectRef setResultRef = frame.heap.getMapEntrySetRef(ref);
                        ObjectRef arrayResultRef = frame.heap.getCollectionArrayRef(setResultRef);
                        int length = arrayResultRef.getDimensions()[0];
                        Map<String, Object> resultMap = new HashMap<>();
                        for (int i = 0; i < length; i++) {
                            Object item = frame.heap.getArray(arrayResultRef, i);
                            if (item != null) {
                                ObjectRef itemRef = (ObjectRef) item;
                                ObjectRef keyRef = frame.heap.getMapEntryKeyRef(itemRef);
                                String key = frame.heap.runToString(keyRef);

                                ObjectRef valueRef = frame.heap.getMapEntryValueRef(itemRef);
                                resultMap.put(key, toJson(valueRef.getVariableType(), valueRef, frame, depth + 1));
                            }
                        }
                        return resultMap;
                    }
                    return frame.heap.runToString(ref);
                } while (false);
            } else {
                ObjectRef ref = (ObjectRef) value;
                return toJson(ref, frame, depth + 1);
            }
        }
    }

    static class EventJson {

        private String contractAddress;

        private long blockNumber;

        private String event;

        private Map<String, Object> payload;

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public long getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Object> payload) {
            this.payload = payload;
        }

    }

    public static final String sha3 = TYPE + "." + "sha3" + "(Ljava/lang/String;)Ljava/lang/String;";

    /**
     * native
     *
     * @see Utils#sha3(String)
     */
    private static Result sha3(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.SHA3);
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef ref = null;
        if (objectRef != null) {
            String src = frame.heap.runToString(objectRef);
            String sha3 = Sha3Hash.sha3(src);
            ref = frame.heap.newString(sha3);
        }
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String sha3Bytes = TYPE + "." + "sha3" + "([B)Ljava/lang/String;";

    /**
     * native
     *
     * @see Utils#sha3(byte[])
     */
    private static Result sha3Bytes(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.SHA3);
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef ref = null;
        if (objectRef != null) {
            byte[] bytes = (byte[]) frame.heap.getObject(objectRef);
            String sha3 = Sha3Hash.sha3(bytes);
            ref = frame.heap.newString(sha3);
        }
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String verifySignatureData = TYPE + "." + "verifySignatureData" + "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z";

    private static Result verifySignatureData(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.VERIFY_SIGNATURE);
        ObjectRef dataRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef signatureRef = (ObjectRef) methodArgs.invokeArgs[1];
        ObjectRef pubKeyRef = (ObjectRef) methodArgs.invokeArgs[2];
        String data = frame.heap.runToString(dataRef);
        String signature = frame.heap.runToString(signatureRef);
        String pubKey = frame.heap.runToString(pubKeyRef);

        boolean verify = false;
        do {
            if (data == null || signature == null || pubKey == null) {
                break;
            }
            try {
                verify = Utils.verify(data, signature, pubKey);
            } catch (Exception e) {
                verify = false;
            }
        } while (false);

        Result result = NativeMethod.result(methodCode, verify, frame);
        return result;
    }

    public static final String getRandomSeedByCount = TYPE + "." + "getRandomSeed" + "(JILjava/lang/String;)Ljava/math/BigInteger;";

    private static Result getRandomSeedByCount(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.RANDOM_COUNT_SEED);
        long endHeight = (long) methodArgs.invokeArgs[0];
        int count = (int) methodArgs.invokeArgs[1];
        ObjectRef algorithmRef = (ObjectRef) methodArgs.invokeArgs[2];
        String algorithm = frame.heap.runToString(algorithmRef);

        String seed = frame.vm.getRandomSeed(endHeight, count, algorithm);
        if (StringUtils.isBlank(seed)) {
            seed = "0";
        }
        ObjectRef objectRef = frame.heap.newBigInteger(seed);

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    public static final String getRandomSeedByHeight = TYPE + "." + "getRandomSeed" + "(JJLjava/lang/String;)Ljava/math/BigInteger;";

    private static Result getRandomSeedByHeight(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.RANDOM_HEIGHT_SEED);
        long startHeight = (long) methodArgs.invokeArgs[0];
        long endHeight = (long) methodArgs.invokeArgs[1];
        ObjectRef algorithmRef = (ObjectRef) methodArgs.invokeArgs[2];
        String algorithm = frame.heap.runToString(algorithmRef);

        String seed = frame.vm.getRandomSeed(startHeight, endHeight, algorithm);
        if (StringUtils.isBlank(seed)) {
            seed = "0";
        }
        ObjectRef objectRef = frame.heap.newBigInteger(seed);

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    public static final String getRandomSeedListByCount = TYPE + "." + "getRandomSeedList" + "(JI)Ljava/util/List;";

    private static Result getRandomSeedListByCount(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.RANDOM_COUNT_SEED);
        long endHeight = (long) methodArgs.invokeArgs[0];
        int count = (int) methodArgs.invokeArgs[1];

        List<String> seeds = frame.vm.getRandomSeedList(endHeight, count);
        int i = seeds.size() - 1;
        if (i > 0) {
            frame.vm.addGasUsed(GasCost.RANDOM_COUNT_SEED * i);
        }

        ObjectRef objectRef = newBigIntegerArrayList(frame, seeds);

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    private static ObjectRef newBigIntegerArrayList(Frame frame, List<String> seeds) {
        ObjectRef objectRef = frame.heap.newArrayList();

        MethodCode arrayListAddMethodCode = frame.vm.methodArea.loadMethod(VariableType.ARRAYLIST_TYPE.getType(), Constants.ARRAYLIST_ADD_METHOD_NAME, Constants.ARRAYLIST_ADD_METHOD_DESC);
        for (String seed : seeds) {
            frame.vm.run(arrayListAddMethodCode, new Object[]{objectRef, frame.heap.newBigInteger(seed)}, false);
        }
        return objectRef;
    }

    public static final String getRandomSeedListByHeight = TYPE + "." + "getRandomSeedList" + "(JJ)Ljava/util/List;";

    private static Result getRandomSeedListByHeight(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.RANDOM_HEIGHT_SEED);
        long startHeight = (long) methodArgs.invokeArgs[0];
        long endHeight = (long) methodArgs.invokeArgs[1];

        List<String> seeds = frame.vm.getRandomSeedList(startHeight, endHeight);
        int i = seeds.size() - 1;
        if (i > 0) {
            frame.vm.addGasUsed(GasCost.RANDOM_HEIGHT_SEED * i);
        }

        ObjectRef objectRef = newBigIntegerArrayList(frame, seeds);

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    public static final String invokeExternalCmd = TYPE + "." + "invokeExternalCmd" + "(Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Object;";

    private static Result invokeExternalCmd(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ProgramInvoke programInvoke = frame.vm.getProgramInvoke();
        if (programInvoke.isCreate()) {
            throw new ErrorException("Invoke external cmd failed. This method cannot be called when creating a contract.", frame.vm.getGasUsed(), null);
        }
        frame.vm.addGasUsed(GasCost.INVOKE_EXTERNAL_METHOD);
        ObjectRef cmdNameRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef argsRef = (ObjectRef) methodArgs.invokeArgs[1];
        String cmdName = frame.heap.runToString(cmdNameRef);
        String[] args = (String[]) frame.heap.getObject(argsRef);

        // 检查是否注册
        CmdRegisterManager cmdRegisterManager = SpringLiteContext.getBean(CmdRegisterManager.class);
        int currentChainId = frame.vm.getProgramExecutor().getCurrentChainId();
        CmdRegister cmdRegister = cmdRegisterManager.getCmdRegisterByCmdName(currentChainId, cmdName);
        if (cmdRegister == null) {
            throw new ErrorException(
                    String.format("Invoke external cmd failed. There is no registration information. chainId: [%s] cmdName: [%s]",
                            currentChainId, cmdName), frame.vm.getGasUsed(), null);
        }
        // 检查参数个数
        String moduleCode = cmdRegister.getModuleCode();
        List<String> argNames = cmdRegister.getArgNames();
        int argsSize;
        if (args == null) {
            argsSize = 0;
        } else {
            argsSize = args.length;
        }
        int argNamesSize = argNames.size();
        if (argsSize != argNamesSize) {
            throw new ErrorException(
                    String.format("Invoke external cmd failed. Inconsistent number of arguments. register size: [%s] your size: [%s]",
                            argNamesSize, argsSize), frame.vm.getGasUsed(), null);
        }
        Map argsMap = new HashMap(8);
        for (int i = 0; i < argsSize; i++) {
            argsMap.put(argNames.get(i), args[i]);
        }
        String contractAddress = programInvoke.getAddress();
        byte[] contractAddressBytes = programInvoke.getContractAddress();
        String contractSender = AddressTool.getStringAddressByBytes(programInvoke.getSender());
        // 固定参数 - chainId、合约地址、合约调用者地址(Mode: All mode)
        argsMap.put("chainId", currentChainId);
        argsMap.put("contractAddress", contractAddress);
        argsMap.put("contractSender", contractSender);
        // 固定参数 - 合约地址的当前余额和nonce, 当前打包的区块时间(Mode: NEW_TX)
        CmdRegisterMode cmdRegisterMode = cmdRegister.getCmdRegisterMode();
        if (CmdRegisterMode.NEW_TX.equals(cmdRegisterMode)) {
            BlockHeaderDto blockHeaderDto = frame.vm.getBlockHeader(programInvoke.getNumber() + 1);
            ContractHelper contractHelper = SpringLiteContext.getBean(ContractHelper.class);
            ContractBalance balance = contractHelper.getBalance(currentChainId, programInvoke.getContractAddress());
            // 使用虚拟机内部维护的合约余额
            ProgramAccount account = frame.vm.getProgramExecutor().getAccount(contractAddressBytes);
            argsMap.put("contractBalance", account.getBalance().toString());
            argsMap.put("contractNonce", account.getNonce());
            argsMap.put("blockTime", blockHeaderDto.getTime());
        }

        // 调用外部接口
        Object cmdResult = requestAndResponse(cmdRegisterManager, moduleCode, cmdName, argsMap, frame);

        // 处理返回值
        ProgramInvokeRegisterCmd invokeRegisterCmd = new ProgramInvokeRegisterCmd(cmdName, argsMap, cmdRegisterMode);
        ObjectRef objectRef = handleResult(currentChainId, contractAddressBytes, cmdResult, invokeRegisterCmd, cmdRegister, frame);
        frame.vm.getInvokeRegisterCmds().add(invokeRegisterCmd);

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    private static Object requestAndResponse(CmdRegisterManager cmdRegisterManager, String moduleCode, String cmdName, Map argsMap, Frame frame) {
        Response cmdResp;
        try {
            cmdResp = cmdRegisterManager.requestAndResponse(moduleCode, cmdName, argsMap);
        } catch (Exception e) {
            throw new ErrorException(
                    String.format("Invoke external cmd failed. error: %s",
                            e.getMessage()), frame.vm.getGasUsed(), null);
        }
        if (!cmdResp.isSuccess()) {
            String errorCode = cmdResp.getResponseErrorCode();
            String errorMsg = cmdResp.getResponseComment();
            throw new ErrorException(
                    String.format("Invoke external cmd failed. error code: %s, error message: ",
                            errorCode, errorMsg), frame.vm.getGasUsed(), null);
        }
        Map responseData = (Map) cmdResp.getResponseData();
        Map resultMap = (Map) responseData.get(cmdName);
        return resultMap.get(RPC_RESULT_KEY);
    }

    private static ObjectRef handleResult(int chainId, byte[] contractAddressBytes, Object cmdResult, ProgramInvokeRegisterCmd invokeRegisterCmd, CmdRegister cmdRegister, Frame frame) {
        ObjectRef objectRef;
        if (invokeRegisterCmd.getCmdRegisterMode().equals(CmdRegisterMode.NEW_TX)) {
            String txHash;
            String txString;
            if (cmdResult instanceof List) {
                List<String> list = (List<String>) cmdResult;
                txHash = list.get(0);
                txString = list.get(1);
            } else if (cmdResult.getClass().isArray()) {
                String[] newTxArray = (String[]) cmdResult;
                txHash = newTxArray[0];
                txString = newTxArray[1];
            } else {
                throw new ErrorException(
                        String.format("Invoke external cmd failed. Unkown return object: %s ",
                                cmdResult.getClass().getName()), frame.vm.getGasUsed(), null);
            }
            ContractNewTxFromOtherModuleHandler handler = SpringLiteContext.getBean(ContractNewTxFromOtherModuleHandler.class);
            // 处理nonce和维护虚拟机内部的合约余额，不处理临时余额，外部再处理
            Transaction tx = handler.updateNonceAndVmBalance(chainId, contractAddressBytes, txHash, txString, frame);
            invokeRegisterCmd.setProgramNewTx(new ProgramNewTx(txHash, txString, tx));
            objectRef = frame.heap.newString(txHash);
        } else {
            // 根据返回值类型解析数据
            CmdRegisterReturnType returnType = cmdRegister.getCmdRegisterReturnType();
            if (returnType.equals(CmdRegisterReturnType.STRING)) {
                // 字符串类型
                objectRef = frame.heap.newString((String) cmdResult);
            } else if (returnType.equals(CmdRegisterReturnType.STRING_ARRAY)) {
                // 字符串数组类型
                if (cmdResult instanceof List) {
                    objectRef = listToObjectRef((List) cmdResult, frame);
                } else if (cmdResult.getClass().isArray()) {
                    objectRef = frame.heap.stringArrayToObjectRef((String[]) cmdResult);
                } else {
                    throw new ErrorException(
                            String.format("Invoke external cmd failed. Unkown return object: %s ",
                                    cmdResult.getClass().getName()), frame.vm.getGasUsed(), null);
                }
            } else if (returnType.equals(CmdRegisterReturnType.STRING_TWO_DIMENSIONAL_ARRAY)) {
                // 字符串二维数组类型
                if (cmdResult instanceof List) {
                    List resultList = (List) cmdResult;
                    int size = resultList.size();
                    objectRef = frame.heap.newArray(VariableType.STRING_TWO_DIMENSIONAL_ARRAY_TYPE, size, 0);
                    Object o;
                    for (int k = 0; k < size; k++) {
                        o = resultList.get(k);
                        if (o instanceof List) {
                            frame.heap.putArray(objectRef, k, listToObjectRef((List) o, frame));
                        } else if (o instanceof String[]) {
                            frame.heap.putArray(objectRef, k, frame.heap.stringArrayToObjectRef((String[]) o));
                        }
                    }
                } else if (cmdResult.getClass().isArray()) {
                    String[][] resultArray = (String[][]) cmdResult;
                    objectRef = frame.heap.stringTwoDimensionalArrayToObjectRef(resultArray);
                } else {
                    throw new ErrorException(
                            String.format("Invoke external cmd failed. Unkown return object: %s ",
                                    cmdResult.getClass().getName()), frame.vm.getGasUsed(), null);
                }
            } else {
                throw new ErrorException(
                        String.format("Invoke external cmd failed. Unkown return type: %s ",
                                returnType), frame.vm.getGasUsed(), null);
            }
        }
        return objectRef;
    }

    private static ObjectRef listToObjectRef(List resultList, Frame frame) {
        int size = resultList.size();
        ObjectRef objectRef = frame.heap.newArray(VariableType.STRING_ARRAY_TYPE, size);
        for (int k = 0; k < size; k++) {
            frame.heap.putArray(objectRef, k, frame.heap.newString((String) resultList.get(k)));
        }
        return objectRef;
    }

    public static final String obj2Json = TYPE + "." + "obj2Json" + "(Ljava/lang/Object;)Ljava/lang/String;";

    /**
     * native
     *
     * @see Utils#obj2Json(Object)
     */
    private static Result obj2Json(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        frame.vm.addGasUsed(GasCost.OBJ_TO_JSON);
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef ref = null;
        if (objectRef != null) {
            Map<String, Object> objectMap = toJson(objectRef, frame);
            String json = JsonUtils.toJson(objectMap);
            ref = frame.heap.newString(json);
        }
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }
}
