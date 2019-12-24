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
import io.nuls.contract.vm.code.Descriptors;
import org.objectweb.asm.tree.FieldInsnNode;

public class Getfield {

    public static void getfield(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        ObjectRef objectRef = frame.operandStack.popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        Object value = frame.heap.getField(objectRef, fieldName);
        //if(value instanceof ObjectRef) {
        //    try {
        //        ObjectRef objectRef1 = (ObjectRef) value;
        //        if(objectRef1.getVariableType().equals(VariableType.STRING_TYPE)) {
        //            Log.info("fieldName: {}, fieldDesc: {}, valueRef: {}, value: {}", fieldName, fieldDesc, value, frame.heap.runToString(objectRef1));
        //        } else if(objectRef1.getVariableType().equals(VariableType.HASH_MAP_TYPE)){
        //            Log.info("fieldName: {}, fieldDesc: {}, hash map valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().equals(VariableType.INT_TYPE)){
        //            Log.info("fieldName: {}, fieldDesc: {}, int valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().equals(VariableType.INT_WRAPPER_TYPE)){
        //            Log.info("fieldName: {}, fieldDesc: {}, integer valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().getType().toLowerCase().contains("map")) {
        //            Log.info("fieldName: {}, fieldDesc: {}, map valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().equals(VariableType.INT_ARRAY_TYPE)) {
        //            Log.info("fieldName: {}, fieldDesc: {}, int array valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().equals(VariableType.CHAR_ARRAY_TYPE)) {
        //            //Log.info("fieldName: {}, fieldDesc: {}, char array valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().equals(VariableType.STRING_ARRAY_TYPE)) {
        //            Log.info("fieldName: {}, fieldDesc: {}, string array valueRef: {}", fieldName, fieldDesc, value);
        //        } else if("integerArray".equals(fieldName) && objectRef1.getVariableType().getType().toLowerCase().contains("integer")) {
        //            Log.info("fieldName: {}, fieldDesc: {}, integer array valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().getType().toLowerCase().contains("decimal")) {
        //            Log.info("fieldName: {}, fieldDesc: {}, decimal[0] valueRef: {}", fieldName, fieldDesc, value);
        //        } else if(objectRef1.getVariableType().getDesc().toLowerCase().contains("decimal")) {
        //            Log.info("fieldName: {}, fieldDesc: {}, decimal[1] valueRef: {}", fieldName, fieldDesc, value);
        //        }
        //    } catch (Exception e) {}
        //}
        if (Descriptors.LONG_DESC.equals(fieldDesc)) {
            frame.operandStack.pushLong((long) value);
        } else if (Descriptors.DOUBLE_DESC.equals(fieldDesc)) {
            frame.operandStack.pushDouble((double) value);
        } else {
            frame.operandStack.push(value);
        }

        //Log.result(frame.getCurrentOpCode(), value, objectRef, fieldName);
    }

}
