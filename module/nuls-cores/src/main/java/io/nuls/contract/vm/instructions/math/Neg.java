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
package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;

public class Neg {

    public static void ineg(final Frame frame) {
        int value = frame.operandStack.popInt();
        int result = -value;
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void lneg(final Frame frame) {
        long value = frame.operandStack.popLong();
        long result = -value;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void fneg(final Frame frame) {
        float value = frame.operandStack.popFloat();
        float result = -value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void dneg(final Frame frame) {
        double value = frame.operandStack.popDouble();
        double result = -value;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

}
