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
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.Constants;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Objects;

public class Invokevirtual {

    public static void invokevirtual(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String className = methodInsnNode.owner;
        String methodName = methodInsnNode.name;
        String methodDesc = methodInsnNode.desc;

        List<VariableType> variableTypes = VariableType.parseArgs(methodDesc);
        MethodArgs methodArgs = new MethodArgs(variableTypes, frame.operandStack, false);
        ObjectRef objectRef = methodArgs.objectRef;
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }

        String type = objectRef.getVariableType().getType();

        if (!Objects.equals(className, type)) {
            if (objectRef.getVariableType().isPrimitiveType()) {

            } else {
                className = type;
            }
        }

        if (objectRef.isArray()) {
            className = Constants.OBJECT_CLASS_NAME;
        }

        MethodCode methodCode = frame.methodArea.loadMethod(className, methodName, methodDesc);

        //Log.opcode(frame.getCurrentOpCode(), objectRef, methodName, methodDesc);

        Result result = NativeMethod.run(methodCode, methodArgs, frame);
        if (result != null) {
            return;
        }

        if (ProtocolGroupManager.getCurrentVersion(ContractContext.CHAIN_ID) >= ContractContext.UPDATE_VERSION_CONTRACT_BALANCE) {
            if (methodCode.isMethod(RESIZE_CLASS_NAME, RESIZE_METHOD_NAME, RESIZE_METHOD_DESC)) {
                // HashMap 扩容限制
                MethodCode sizeMethod = frame.vm.methodArea.loadMethod(className, Constants.SIZE, Constants.SIZE_DESC);
                frame.vm.run(sizeMethod, methodArgs.frameArgs, false);
                Object sizeResult = frame.vm.getResultValue();
                int size = (int) sizeResult;
                if (size > Constants.MAP_MAX_CAPACITY) {
                    frame.throwRuntimeException("Max size of map is " + Constants.MAP_MAX_CAPACITY);
                    return;
                }
                if (size > Constants.MAP_MIN_TRIGGER_RESIZE_CAPACITY) {
                    // 扩容机制
                    MethodCode capacityMethod = frame.vm.methodArea.loadMethod(className, CAPACITY_METHOD_NAME, CAPACITY_METHOD_DESC);
                    frame.vm.run(capacityMethod, methodArgs.frameArgs, false);
                    Object capacityResult = frame.vm.getResultValue();
                    int capacity = (int) capacityResult;
                    int resizeCount = log2(Constants.MAP_MAX_CAPACITY / capacity) - 1;
                    if (resizeCount > 0) {
                        for (int i = 0; i < resizeCount; i++) {
                            frame.vm.run(methodCode, methodArgs.frameArgs, true);
                        }
                    }
                }
            }
        }
        
        frame.vm.run(methodCode, methodArgs.frameArgs, true);
    }

    private static final String RESIZE_CLASS_NAME = "java/util/HashMap";
    private static final String RESIZE_METHOD_NAME = "resize";
    private static final String RESIZE_METHOD_DESC = "()[Ljava/util/HashMap$Node;";

    private static final String CAPACITY_METHOD_NAME = "capacity";
    private static final String CAPACITY_METHOD_DESC = "()I";

    static int log2(int digit) {
        int count = 0;
        while ((digit = digit / 2) != 0) {
            count++;
        }
        return count;
    }

}
