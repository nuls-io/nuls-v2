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

import io.nuls.contract.sdk.Consensus;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.ProgramCancelDepositAgent;
import io.nuls.contract.vm.program.ProgramJoinAgent;
import io.nuls.contract.vm.program.ProgramRegisterAgent;
import io.nuls.contract.vm.program.ProgramStopAgent;

import java.math.BigInteger;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeConsensus {

    public static final String TYPE = "io/nuls/contract/sdk/Consensus";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case registerAgent:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return registerAgent(methodCode, methodArgs, frame);
                }
            case stopAgent:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return stopAgent(methodCode, methodArgs, frame);
                }
            case joinAgent:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return joinAgent(methodCode, methodArgs, frame);
                }
            case cancelDepositAgent:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return cancelDepositAgent(methodCode, methodArgs, frame);
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

    public static final String registerAgent = TYPE + "." + "registerAgent" + "(Ljava/lang/String;Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Consensus#registerAgent(String, BigInteger)
     */
    private static Result registerAgent(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef packageAddressRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef depositAmountRef = (ObjectRef) methodArgs.invokeArgs[1];

        String packageAddress = frame.heap.runToString(packageAddressRef);
        BigInteger depositAmount = frame.heap.toBigInteger(depositAmountRef);
        if (depositAmount == null) {
            depositAmount = BigInteger.ZERO;
        }
        byte[] rewardAddress = frame.vm.getProgramInvoke().getContractAddress();
        byte[] agentAddress = frame.vm.getProgramInvoke().getSender();

        ProgramRegisterAgent programRegisterAgent = new ProgramRegisterAgent(agentAddress, NativeAddress.toBytes(packageAddress), rewardAddress, depositAmount, 100);
        frame.vm.setProgramRegisterAgent(programRegisterAgent);

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }
    public static final String stopAgent = TYPE + "." + "stopAgent" + "()";

    /**
     * native
     *
     * @see Consensus#stopAgent()
     */
    private static Result stopAgent(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {

        byte[] agentAddress = frame.vm.getProgramInvoke().getSender();
        ProgramStopAgent programStopAgent = new ProgramStopAgent(agentAddress);
        frame.vm.setProgramStopAgent(programStopAgent);

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String joinAgent = TYPE + "." + "joinAgent" + "(Ljava/lang/String;Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Consensus#joinAgent(String, BigInteger)
     */
    private static Result joinAgent(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef agentHashRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef depositAmountRef = (ObjectRef) methodArgs.invokeArgs[1];

        byte[] joinAddress = frame.vm.getProgramInvoke().getSender();
        String agentHash = frame.heap.runToString(agentHashRef);
        BigInteger depositAmount = frame.heap.toBigInteger(depositAmountRef);
        if (depositAmount == null) {
            depositAmount = BigInteger.ZERO;
        }
        ProgramJoinAgent programJoinAgent = new ProgramJoinAgent(joinAddress, agentHash, depositAmount);
        frame.vm.setProgramJoinAgent(programJoinAgent);

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String cancelDepositAgent = TYPE + "." + "cancelDepositAgent" + "(Ljava/lang/String;)V";

    /**
     * native
     *
     * @see Consensus#cancelDepositAgent(String)
     */
    private static Result cancelDepositAgent(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef agentHashRef = (ObjectRef) methodArgs.invokeArgs[0];

        byte[] cancelDepositAddress = frame.vm.getProgramInvoke().getSender();
        String agentHash = frame.heap.runToString(agentHashRef);
        ProgramCancelDepositAgent programCancelDepositAgent = new ProgramCancelDepositAgent(cancelDepositAddress, agentHash);
        frame.vm.setProgramCancelDepositAgent(programCancelDepositAgent);

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

}
