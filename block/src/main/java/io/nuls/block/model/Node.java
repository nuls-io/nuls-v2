/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.block.model;

import io.nuls.base.data.NulsDigestData;
import lombok.Data;

import java.util.Comparator;

/**
 * 节点
 *
 * @author captain
 * @version 1.0
 * @date 18-11-30 下午2:48
 */
@Data
public class Node {

    /**
     * 节点比较器,默认按信用值排序
     */
    public static final Comparator<Node> COMPARATOR = Comparator.comparingInt(Node::getCredit).reversed();

    /**
     * ip+port
     */
    private String id;
    /**
     * 最新区块高度
     */
    private long height;
    /**
     * 最新区块hash
     */
    private NulsDigestData hash;
    /**
     * 下载信用值
     */
    private int credit = 100;

    /**
     * 调整信用值
     */
    public void adjustCredit(boolean success){
        if (success) {
            credit = Math.min(200, credit + 20);
        } else {
            credit = Math.max(20, credit - 20);
        }

    }
}
