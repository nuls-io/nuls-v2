/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.protocol.service;

/**
 * 区块服务
 *
 * @author captain
 * @version 1.0
 * @date 18-11-6 下午4:57
 */
public interface ProtocolService {

    /**
     * todo 待实现
     * 启动一条链
     *
     * @param chainId 链ID
     * @return
     */
    boolean startChain(int chainId);

    /**
     * todo 待实现
     * 停止一条链
     *
     * @param chainId   链ID
     * @param cleanData 是否清理数据
     * @return
     */
    boolean stopChain(int chainId, boolean cleanData);

    /**
     * 初始化方法
     *
     * @param chainId
     */
    void init(int chainId);

}
