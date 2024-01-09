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

public class Div {

    public static void idiv(final Frame frame) {

        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        try {
            int result = value1 / value2;
            frame.operandStack.pushInt(result);
        } catch (ArithmeticException e) {
            frame.throwArithmeticException(e.getMessage());
            return;
        }
        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void ldiv(final Frame frame) {
        long value2 = frame.operandStack.popLong();
        long value1 = frame.operandStack.popLong();
        try {
            long result = value1 / value2;
            frame.operandStack.pushLong(result);
        } catch (ArithmeticException e) {
            frame.throwArithmeticException(e.getMessage());
            return;
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void fdiv(final Frame frame) {
        float value2 = frame.operandStack.popFloat();
        float value1 = frame.operandStack.popFloat();
        try {
            float result = value1 / value2;
            frame.operandStack.pushFloat(result);
        } catch (ArithmeticException e) {
            frame.throwArithmeticException(e.getMessage());
            return;
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void ddiv(final Frame frame) {
        double value2 = frame.operandStack.popDouble();
        double value1 = frame.operandStack.popDouble();
        try {
            double result = value1 / value2;
            frame.operandStack.pushDouble(result);
        } catch (ArithmeticException e) {
            frame.throwArithmeticException(e.getMessage());
            return;
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

}
