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
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.program.impl.ProgramInvoke;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static io.nuls.contract.config.ContractContext.ASSET_ID;
import static io.nuls.contract.config.ContractContext.CHAIN_ID;
import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeAddress {

    public static final String TYPE = "io/nuls/contract/sdk/Address";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case balance:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return balance(methodCode, methodArgs, frame);
                }
            case balanceOfDesignatedAsset:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return balanceOfDesignatedAsset(methodCode, methodArgs, frame);
                }
            case totalBalance:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return totalBalance(methodCode, methodArgs, frame);
                }
            case totalBalanceOfDesignatedAsset:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return totalBalanceOfDesignatedAsset(methodCode, methodArgs, frame);
                }
            case transfer:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return transfer(methodCode, methodArgs, frame);
                }
            case transferLocked:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return transferLocked(methodCode, methodArgs, frame);
                }
            case transferOfDesignatedAsset:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return transferOfDesignatedAsset(methodCode, methodArgs, frame);
                }
            case call:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return call(methodCode, methodArgs, frame);
                }
            case callWithReturnValue:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return callWithReturnValue(methodCode, methodArgs, frame);
                }
            case callWithReturnValueAndAssetInfo:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return callWithReturnValueAndAssetInfo(methodCode, methodArgs, frame);
                }
            case valid:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return valid(methodCode, methodArgs, frame);
                }
            case isContract:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return isContract(methodCode, methodArgs, frame);
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

    private static BigInteger balance(byte[] address, Frame frame) {
        return frame.vm.getProgramExecutor().getAccount(address, CHAIN_ID, ASSET_ID).getBalance();
    }

    private static BigInteger totalBalance(byte[] address, Frame frame) {
        return frame.vm.getProgramExecutor().getAccount(address, CHAIN_ID, ASSET_ID).getTotalBalance();
    }

    public static final String balance = TYPE + "." + "balance" + "()Ljava/math/BigInteger;";
    public static final String balanceOfDesignatedAsset = TYPE + "." + "balance" + "(II)Ljava/math/BigInteger;";

    /**
     * native
     *
     * see Address#balance()
     */
    private static Result balance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger balance = balance(NativeAddress.toBytes(address), frame);
        ObjectRef balanceRef = frame.heap.newBigInteger(balance.toString());
        Result result = NativeMethod.result(methodCode, balanceRef, frame);
        return result;
    }

    private static Result balanceOfDesignatedAsset(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int assetChainId = (int) methodArgs.invokeArgs[0];
        int assetId = (int) methodArgs.invokeArgs[1];
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger balance = frame.vm.getProgramExecutor().getAccount(NativeAddress.toBytes(address), assetChainId, assetId).getBalance();
        ObjectRef balanceRef = frame.heap.newBigInteger(balance.toString());
        Result result = NativeMethod.result(methodCode, balanceRef, frame);
        return result;
    }

    public static final String totalBalance = TYPE + "." + "totalBalance" + "()Ljava/math/BigInteger;";
    public static final String totalBalanceOfDesignatedAsset = TYPE + "." + "totalBalance" + "(II)Ljava/math/BigInteger;";

    /**
     * native
     *
     * see Address#totalBalance()
     */
    private static Result totalBalance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger totalBalance = totalBalance(NativeAddress.toBytes(address), frame);
        ObjectRef totalBalanceRef = frame.heap.newBigInteger(totalBalance.toString());
        Result result = NativeMethod.result(methodCode, totalBalanceRef, frame);
        return result;
    }

    private static Result totalBalanceOfDesignatedAsset(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int assetChainId = (int) methodArgs.invokeArgs[0];
        int assetId = (int) methodArgs.invokeArgs[1];
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger totalBalance = frame.vm.getProgramExecutor().getAccount(NativeAddress.toBytes(address), assetChainId, assetId).getTotalBalance();
        ObjectRef totalBalanceRef = frame.heap.newBigInteger(totalBalance.toString());
        Result result = NativeMethod.result(methodCode, totalBalanceRef, frame);
        return result;
    }

    public static final String transfer = TYPE + "." + "transfer" + "(Ljava/math/BigInteger;)V";
    public static final String transferLocked = TYPE + "." + "transferLocked" + "(Ljava/math/BigInteger;J)V";
    public static final String transferOfDesignatedAsset = TYPE + "." + "transferLocked" + "(Ljava/math/BigInteger;IIJ)V";

    /**
     * native
     *
     * see Address#transfer(BigInteger)
     */
    private static Result transfer(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return transferBase(methodCode, methodArgs, frame, CHAIN_ID, ASSET_ID, 0);
    }

    private static Result transferLocked(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long lockedTime = (long) methodArgs.invokeArgs[1];
        return transferBase(methodCode, methodArgs, frame, CHAIN_ID, ASSET_ID, lockedTime);
    }

    private static Result transferOfDesignatedAsset(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int assetChainId = (int) methodArgs.invokeArgs[1];
        int assetId = (int) methodArgs.invokeArgs[2];
        long lockedTime = (long) methodArgs.invokeArgs[3];
        return transferBase(methodCode, methodArgs, frame, assetChainId, assetId, lockedTime);
    }

    private static Result transferBase(MethodCode methodCode, MethodArgs methodArgs, Frame frame, int assetChainId, int assetId, long lockedTime) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[0];
        String address = frame.heap.runToString(addressRef);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        byte[] from = frame.vm.getProgramInvoke().getContractAddress();
        byte[] to = NativeAddress.toBytes(address);
        if (Arrays.equals(from, to)) {
            throw new ErrorException(String.format("Cannot transfer from %s to %s", NativeAddress.toString(from), address), frame.vm.getGasUsed(), null);
        }
        checkBalance(from, assetChainId, assetId, value, frame);

        frame.vm.addGasUsed(GasCost.TRANSFER);

        boolean mainAsset = assetChainId == CHAIN_ID && assetId == ASSET_ID;
        if (frame.heap.existContract(to)) {
            if (lockedTime > 0) {
                throw new ErrorException(String.format("Cannot transfer the locked amount to the contract address %s", address), frame.vm.getGasUsed(), null);
            }
            String methodName;
            if (mainAsset) {
                methodName = "_payable";
            } else {
                methodName = "_payableMultyAsset";
            }
            String methodDesc = "()V";
            String[][] args = null;
            //BigInteger value;
            if (!mainAsset) {
                ProgramMultyAssetValue[] multyAssetValues = new ProgramMultyAssetValue[] {new ProgramMultyAssetValue(value, assetChainId, assetId)};
                call(address, methodName, methodDesc, args, BigInteger.ZERO, frame, multyAssetValues);
            } else {
                call(address, methodName, methodDesc, args, value, frame, null);
            }
        } else {
            frame.vm.getProgramExecutor().getAccount(from, assetChainId, assetId).addBalance(value.negate());
            ProgramTransfer programTransfer = new ProgramTransfer(from, to, value, assetChainId, assetId, lockedTime);
            frame.vm.getTransfers().add(programTransfer);
            // add by pierre at 2019-11-23 标记 按合约执行顺序添加合约生成交易，按此顺序处理合约生成交易的业务 不确定 需要协议升级
            frame.vm.getOrderedInnerTxs().add(programTransfer);
            // end code by pierre
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String call = TYPE + "." + "call" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * see Address#call(String, String, String[][], BigInteger)
     */
    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, false, null);
    }

    public static final String callWithReturnValue = TYPE + "." + "callWithReturnValue" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)Ljava/lang/String;";
    private static Result callWithReturnValue(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, true, null);
    }

    public static final String callWithReturnValueAndAssetInfo = TYPE + "." + "callWithReturnValue" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;[Lio/nuls/contract/sdk/MultyAssetValue;)Ljava/lang/String;";
    private static Result callWithReturnValueAndAssetInfo(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef multyAssetValuesRef = (ObjectRef) methodArgs.invokeArgs[4];
        ProgramMultyAssetValue[] multyAssetValues = null;
        if (multyAssetValuesRef != null) {
            int length = multyAssetValuesRef.getDimensions()[0];
            multyAssetValues = new ProgramMultyAssetValue[length];
            for (int i = 0; i < length; i++) {
                Object item = frame.heap.getArray(multyAssetValuesRef, i);
                if (item == null) {
                    throw new ErrorException(String.format("Empty elements in array"), frame.vm.getGasUsed(), null);
                }
                ObjectRef itemRef = (ObjectRef) item;
                ObjectRef value = (ObjectRef) frame.heap.getField(itemRef, "value");
                Integer assetChainId = (Integer) frame.heap.getField(itemRef, "assetChainId");
                if (assetChainId == null || assetChainId.intValue() == 0) {
                    throw new ErrorException(String.format("Zero assetChainId"), frame.vm.getGasUsed(), null);
                }
                Integer assetId = (Integer) frame.heap.getField(itemRef, "assetId");
                if (assetId == null || assetId.intValue() == 0) {
                    throw new ErrorException(String.format("Zero assetId"), frame.vm.getGasUsed(), null);
                }
                multyAssetValues[i] = new ProgramMultyAssetValue(frame.heap.toBigInteger(value), assetChainId, assetId);
            }
        }
        return call(methodCode, methodArgs, frame, true, multyAssetValues);
    }

    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean returnResult, ProgramMultyAssetValue[] multyAssetValues) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef methodNameRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef methodDescRef = (ObjectRef) methodArgs.invokeArgs[1];
        ObjectRef argsRef = (ObjectRef) methodArgs.invokeArgs[2];
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[3];

        String address = frame.heap.runToString(addressRef);
        String methodName = frame.heap.runToString(methodNameRef);
        String methodDesc = frame.heap.runToString(methodDescRef);
        String[][] args = getArgs(argsRef, frame);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        if (value == null) {
            value = BigInteger.ZERO;
        }
        ProgramResult programResult = call(address, methodName, methodDesc, args, value, frame, multyAssetValues);

        if (!programResult.isSuccess()) {
            return new Result();
        }

        Object resultValue = null;
        if (returnResult && programResult.isSuccess()) {
            resultValue = frame.heap.newString(programResult.getResult());
        }

        Result result = NativeMethod.result(methodCode, resultValue, frame);
        return result;
    }

    private static String[][] getArgs(ObjectRef argsRef, Frame frame) {
        if (argsRef == null) {
            return null;
        }

        int length = argsRef.getDimensions()[0];
        String[][] array = new String[length][0];
        for (int i = 0; i < length; i++) {
            ObjectRef objectRef = (ObjectRef) frame.heap.getArray(argsRef, i);
            String[] ss = (String[]) frame.heap.getObject(objectRef);
            array[i] = ss;
        }

        return array;
    }

    public static ProgramResult call(String address, String methodName, String methodDesc, String[][] args, BigInteger value, Frame frame, ProgramMultyAssetValue[] multyAssetValues) {
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new ErrorException(String.format("amount less than zero, value=%s", value), frame.vm.getGasUsed(), null);
        }

        ProgramInvoke programInvoke = frame.vm.getProgramInvoke();
        ProgramCall programCall = new ProgramCall();
        programCall.setNumber(programInvoke.getNumber());
        programCall.setSender(programInvoke.getContractAddress());
        programCall.setValue(value != null ? value : BigInteger.ZERO);
        programCall.setMultyAssetValues(multyAssetValues != null ? List.of(multyAssetValues) : null);
        programCall.setGasLimit(programInvoke.getGasLimit() - frame.vm.getGasUsed());
        programCall.setPrice(programInvoke.getPrice());
        programCall.setContractAddress(NativeAddress.toBytes(address));
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        programCall.setEstimateGas(programInvoke.isEstimateGas());
        programCall.setViewMethod(programInvoke.isViewMethod());
        programCall.setInternalCall(true);

        if (programCall.getValue().compareTo(BigInteger.ZERO) > 0) {
            checkBalance(programCall.getSender(), CHAIN_ID, ASSET_ID, programCall.getValue(), frame);
            frame.vm.getProgramExecutor().getAccount(programCall.getSender(), CHAIN_ID, ASSET_ID).addBalance(programCall.getValue().negate());
            ProgramTransfer programTransfer = new ProgramTransfer(programCall.getSender(), programCall.getContractAddress(), programCall.getValue(), CHAIN_ID, ASSET_ID, 0);
            frame.vm.getTransfers().add(programTransfer);
            // add by pierre at 2019-11-23 标记 按合约执行顺序添加合约生成交易，按此顺序处理合约生成交易的业务 不确定 需要协议升级
            frame.vm.getOrderedInnerTxs().add(programTransfer);
            // end code by pierre
        }
        if (multyAssetValues != null && multyAssetValues.length > 0) {
            for (ProgramMultyAssetValue multyAssetValue : multyAssetValues) {
                int assetChainId = multyAssetValue.getAssetChainId();
                int assetId = multyAssetValue.getAssetId();
                checkBalance(programCall.getSender(), assetChainId, assetId, multyAssetValue.getValue(), frame);
                frame.vm.getProgramExecutor().getAccount(programCall.getSender(), assetChainId, assetId).addBalance(multyAssetValue.getValue().negate());
                ProgramTransfer programTransfer = new ProgramTransfer(programCall.getSender(), programCall.getContractAddress(), multyAssetValue.getValue(), assetChainId, assetId, 0);
                frame.vm.getTransfers().add(programTransfer);
                // add by pierre at 2019-11-23 标记 按合约执行顺序添加合约生成交易，按此顺序处理合约生成交易的业务 不确定 需要协议升级
                frame.vm.getOrderedInnerTxs().add(programTransfer);
                // end code by pierre
            }
        }

        ProgramInternalCall programInternalCall = new ProgramInternalCall();
        programInternalCall.setSender(programCall.getSender());
        programInternalCall.setValue(programCall.getValue());
        programInternalCall.setMultyAssetValues(programCall.getMultyAssetValues());
        programInternalCall.setContractAddress(programCall.getContractAddress());
        programInternalCall.setMethodName(programCall.getMethodName());
        programInternalCall.setMethodDesc(programCall.getMethodDesc());
        programInternalCall.setArgs(programCall.getArgs());

        frame.vm.getInternalCalls().add(programInternalCall);

        ProgramResult programResult = frame.vm.getProgramExecutor().callProgramExecutor().call(programCall);


        frame.vm.addGasUsed(programResult.getGasUsed());
        // add by pierre at 2020-11-03 从`isSuccess`代码段中移出，可能影响兼容性，考虑协议升级
        frame.vm.getDebugEvents().addAll(programResult.getDebugEvents());
        // end code by pierre
        if (programResult.isSuccess()) {
            frame.vm.getTransfers().addAll(programResult.getTransfers());
            frame.vm.getInternalCalls().addAll(programResult.getInternalCalls());
            frame.vm.getEvents().addAll(programResult.getEvents());
            frame.vm.getInvokeRegisterCmds().addAll(programResult.getInvokeRegisterCmds());
            frame.vm.getOrderedInnerTxs().addAll(programResult.getOrderedInnerTxs());
            // add by pierre at 2022/7/18 p14
            int currentChainId = frame.vm.getProgramExecutor().getCurrentChainId();
            if(ProtocolGroupManager.getCurrentVersion(currentChainId) >= ContractContext.PROTOCOL_15 ) {
                frame.vm.getInternalCreates().addAll(programResult.getInternalCreates());
            }
            // end code by pierre
            return programResult;
        } else {
            // add by pierre at 2020-11-03 可能影响兼容性，考虑协议升级
            Iterator<String> descendingIterator = programResult.getStackTraces().descendingIterator();
            while (descendingIterator.hasNext()) {
                frame.vm.getStackTraces().addFirst(descendingIterator.next());
            }
            // end code by pierre
            frame.throwRuntimeException(programResult.getErrorMessage());
            return programResult;
        }
    }

    private static void checkBalance(byte[] address, int assetChainId, int assetId, BigInteger value, Frame frame) {
        if (value == null || value.compareTo(BigInteger.ZERO) <= 0) {
            throw new ErrorException(String.format("transfer amount error, value=%s", value), frame.vm.getGasUsed(), null);
        }
        BigInteger balance = frame.vm.getProgramExecutor().getAccount(address, assetChainId, assetId).getBalance();
        if (balance.compareTo(value) < 0) {
            if (frame.vm.getProgramContext().isEstimateGas()) {
                balance = value;
            } else {
                throw new ErrorException(String.format("contract[%s] not enough balance", toString(address)), frame.vm.getGasUsed(), null);
            }
        }
    }

    public static final String valid = TYPE + "." + "valid" + "(Ljava/lang/String;)V";

    /**
     * native
     *
     * see Address#valid(String)
     */
    private static Result valid(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String str = frame.heap.runToString(objectRef);
        boolean valided = validAddress(frame.vm.getProgramExecutor().getCurrentChainId(), str);
        if (!valided) {
            frame.throwRuntimeException(String.format("address[%s] error", str));
        }
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String isContract = TYPE + "." + "isContract" + "()Z";

    private static Result isContract(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.objectRef;
        String address = frame.heap.runToString(addressRef);
        boolean verify = isContract(NativeAddress.toBytes(address), frame);
        Result result = NativeMethod.result(methodCode, verify, frame);
        return result;
    }

    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return AddressTool.getStringAddressByBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static byte[] toBytes(String str) {
        if (str == null) {
            return null;
        }
        try {
            return AddressTool.getAddress(str);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static boolean isContract(byte[] address, Frame frame) {
        byte[] contractAddress = frame.vm.getProgramInvoke().getContractAddress();
        if (Arrays.equals(contractAddress, address)) {
            return true;
        }
        if (frame.heap.existContract(address)) {
            return true;
        }
        return false;
    }

    public static boolean validAddress(int chainId, String str) {
        return AddressTool.validAddress(chainId, str);
    }

    public static ProgramResult call(String address, String methodName, String methodDesc, String[][] args, BigInteger value, Frame frame) {
        return call(address, methodName, methodDesc, args, value, frame, null);
    }
}
