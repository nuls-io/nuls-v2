/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.transaction.model.bo.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Transaction module chain setting
 * @author: Charlie
 * @date: 2019/03/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigBean implements Serializable {

    /** chain id*/
    private int chainId;
    /** assets id*/
    private int assetId;
    /** 单个交易数据最大值(B)*/
    private int txMaxSize;
    /**
     * 打包时在获取交易之后留给模块统一验证的时间阈值,
     * 包括统一验证有被过滤掉的交易时需要重新验证等.
     */
    private long moduleVerifyOffset;
    /** 打包获取交易给RPC传输到共识的预留时间,超时则需要处理交易还原待打包队列*/
    private long packageRpcReserveTime;
    /** 接收新交易的文件队列最大容量**/
    private long txUnverifiedQueueSize;
    /** 孤儿交易池最大容量**/
    private int orphanContainerSize;
    /** 孤儿交易生命时间,超过会被清理**/
    private long orphanTtl;

}
