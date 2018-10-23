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

import java.util.List;

public interface KernelCmd {

    /**
     * 接收kernel推送的信息
     * @param params：
     * @return Object
     */
    public Object status(List params);

    /**
     * 关闭服务：在已有业务完成之后
     * @param params：
     * @return Object
     */
    public Object shutdown(List params);

    /**
     * 关闭服务：立即关闭，不管业务是否完成
     * @param params：
     * @return Object
     */
    public Object terminate(List params);

    /**
     * 提供本地配置信息
     * @param params：
     * @return Object
     */
    public Object confGet(List params);

    /**
     * 更新本地配置信息
     * @param params：
     * @return Object
     */
    public Object confSet(List params);

    /**
     * 重置本地配置信息
     * @param params：
     * @return Object
     */
    public Object confReset(List params);
}
