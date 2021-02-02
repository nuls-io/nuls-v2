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
package io.nuls.contract.vm.instructions.references;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.Constants;
import org.objectweb.asm.tree.MethodInsnNode;

public class Invokespecial {

    public static void invokespecial(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String className = methodInsnNode.owner;
        String methodName = methodInsnNode.name;
        String methodDesc = methodInsnNode.desc;

        MethodCode methodCode = frame.methodArea.loadMethod(className, methodName, methodDesc);

        MethodArgs methodArgs = new MethodArgs(methodCode.argsVariableType, frame.operandStack, false);
        ObjectRef objectRef = methodArgs.objectRef;
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }

        //Log.opcode(frame.getCurrentOpCode(), className, methodName, methodDesc);

        if (Constants.OBJECT_CLASS_NAME.equals(className) && Constants.CONSTRUCTOR_NAME.equals(methodName)) {
            return;
        }

        Result result = NativeMethod.run(methodCode, methodArgs, frame);
        if (result != null) {
            return;
        }

        if (ProtocolGroupManager.getCurrentVersion(ContractContext.CHAIN_ID) >= ContractContext.UPDATE_VERSION_CONTRACT_BALANCE) {
            if (methodCode.isMethod(GROW_CLASS_NAME, GROW_METHOD_NAME, GROW_METHOD_DESC)) {
                // ArrayList 扩容限制
                MethodCode sizeMethod = frame.vm.methodArea.loadMethod(className, Constants.SIZE, Constants.SIZE_DESC);
                frame.vm.run(sizeMethod, new Object[]{objectRef}, false);
                Object sizeResult = frame.vm.getResultValue();
                int size = (int) sizeResult;
                if (size >= Constants.LIST_MAX_CAPACITY) {
                    frame.throwRuntimeException("Max size of ArrayList is " + Constants.LIST_MAX_CAPACITY);
                    return;
                }

            }
        }

        frame.vm.run(methodCode, methodArgs.frameArgs, true);
    }

    private static final String GROW_CLASS_NAME = "java/util/ArrayList";
    private static final String GROW_METHOD_NAME = "grow";
    private static final String GROW_METHOD_DESC = "(I)V";
}
