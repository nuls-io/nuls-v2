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

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.util.Constants;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.Objects;

public class Athrow {

    public static void athrow(final Frame frame) {
        ObjectRef objectRef = frame.operandStack.popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        //Log.opcode(frame.getCurrentOpCode(), objectRef);
        while (frame.vm.isNotEmptyFrame()) {
            final Frame lastFrame = frame.vm.lastFrame();
            TryCatchBlockNode tryCatchBlockNode = getTryCatchBlockNode(lastFrame, objectRef);
            if (tryCatchBlockNode != null) {
                lastFrame.operandStack.clear();
                lastFrame.operandStack.pushRef(objectRef);
                lastFrame.jump(tryCatchBlockNode.handler);
                return;
            } else {
                frame.vm.popFrame();
            }
        }
        frame.vm.getResult().exception(objectRef);
    }

    private static TryCatchBlockNode getTryCatchBlockNode(Frame frame, ObjectRef objectRef) {
        for (TryCatchBlockNode tryCatchBlockNode : frame.methodCode.tryCatchBlocks) {
            String type = tryCatchBlockNode.type;
            int line = frame.getLine();
            int start = frame.getLine(tryCatchBlockNode.start);
            int end = frame.getLine(tryCatchBlockNode.end);
            int handler = frame.getLine(tryCatchBlockNode.handler);
            if (type != null && handler < end) {
                end = handler;
            }
            boolean result = start <= line && line < end;
            if (result && (type == null || extends_(objectRef.getVariableType().getType(), type, frame))) {
                return tryCatchBlockNode;
            }
        }
        return null;
    }

    private static boolean extends_(String refType, String className, Frame frame) {
        if (Objects.equals(refType, className)) {
            return true;
        } else {
            ClassCode classCode = frame.methodArea.loadClass(refType);
            String superName = classCode.superName;
            if (Constants.OBJECT_CLASS_NAME.equals(superName)) {
                return false;
            } else {
                return extends_(superName, className, frame);
            }
        }
    }

}
