/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.cmd;

import io.nuls.rpc.model.CmdResponse;

import java.util.List;

/**
 * @author tangyi
 */
public interface KernelCmd {

    /**
     * receive msg from kernel
     * @param params：
     * @return Object
     */
    public CmdResponse status(List params);

    /**
     * shutdown module: after business completed
     * @param params：
     * @return Object
     */
    public CmdResponse shutdown(List params);

    /**
     * shutdown module: immediate
     * @param params：
     * @return Object
     */
    public CmdResponse terminate(List params);

    /**
     * provide local configuration
     * @param params：
     * @return Object
     */
    public CmdResponse confGet(List params);

    /**
     * update local configuration
     * @param params：
     * @return Object
     */
    public CmdResponse confSet(List params);

    /**
     * reset local configuration
     * @param params：
     * @return Object
     */
    public CmdResponse confReset(List params);
}
