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
package io.nuls.contract.vm;

public class GasCost {

    public static final int COMPARISON = 1;//Comparing Bytecodes
    public static final int CONSTANT = 1;//Simple numeric type bytecode
    public static final int LDC = 1;//Numeric constant, string constant（length * LDC）
    public static final int CONTROL = 5;//Control Bytecode
    public static final int TABLESWITCH = 2;//switchBytecode（size * TABLESWITCH）
    public static final int LOOKUPSWITCH = 2;//switchBytecode（size * LOOKUPSWITCH）
    public static final int CONVERSION = 1;//Numerical conversion
    public static final int EXTENDED = 1;//nulljudge
    public static final int MULTIANEWARRAY = 1;//Multidimensional array（size * MULTIANEWARRAY）
    public static final int LOAD = 1;//Send local variables to the top of the stack
    public static final int ARRAYLOAD = 5;//Send a certain item of the array to the top of the stack
    public static final int MATH = 1;//Mathematical operations and shift operations
    public static final int REFERENCE = 10;//Object related operations
    public static final int NEWARRAY = 1;//One-dimensional array（size * NEWARRAY）
    public static final int STACK = 2;//Stack operation
    public static final int STORE = 1;//Store the value at the top of the stack in a local variable
    public static final int ARRAYSTORE = 5;//Store the value of stack items in an array
    public static final int TRANSFER = 1000;//Transfer transaction
    public static final int SHA3 = 500;//SHA3call
    public static final int VERIFY_SIGNATURE = 500;//Verify signature
    public static final int RANDOM_COUNT_SEED = 5000;//Generate a random seed based on height and the number of original seeds
    public static final int RANDOM_HEIGHT_SEED = 5000;//Generate a random seed based on the height interval
    public static final int OBJ_TO_JSON = 2000;//Object conversion tojson

    /**
     * Calling external methods of virtual machines(Methods for registering other modules)
     */
    public static final int INVOKE_EXTERNAL_METHOD = 5000;
    public static final int CREATE_PER_BYTE = 7;
    public static final int OBJ_TO_JSON_PER_CHAR = 4;

    public static final int TRANSFER_P22 = 20000;//Transfer transaction

}
