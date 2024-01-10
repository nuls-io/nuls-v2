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
package io.nuls.block.constant;

import io.nuls.base.data.Block;

import java.util.Comparator;

/**
 * constant
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 afternoon3:34
 */
public interface Constant {

    String ROLLBACK_HEIGHT = "rollback_height";

    /**
     * Store protocol configuration information for each chain
     */
    String PROTOCOL_CONFIG = "protocol_config";
    /**
     * Store the latest height of each chain
     */
    String CHAIN_LATEST_HEIGHT = "chain_latest_height";
    /**
     * Store block header data
     */
    String BLOCK_HEADER = "block_header_";
    /**
     * The height of storage area block heads is related tohashKey value pairs for
     */
    String BLOCK_HEADER_INDEX = "block_header_index_";
    /**
     * Forked chain、Orphan Chain Blockchain Database Prefix
     */
    String CACHED_BLOCK = "cached_block_";

    /**
     * working condition
     */
    int MODULE_WORKING = 1;
    /**
     * Waiting state
     */
    int MODULE_WAITING = 0;

    /**
     * Block Sorter
     */
    Comparator<Block> BLOCK_COMPARATOR = (o1, o2) -> (int) (o1.getHeader().getHeight() - o2.getHeader().getHeight());

}
