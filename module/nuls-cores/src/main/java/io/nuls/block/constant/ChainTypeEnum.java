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

/**
 * Category of Chain
 *
 * @author captain
 * @version 1.0
 * @date 18-11-28 afternoon12:01
 */
public enum ChainTypeEnum {

    /**
     * Connected to the main chain
     * Connected to the master chain
     */
    MASTER_APPEND,

    /**
     * Fork from the main chain
     * Forked from the master chain
     */
    MASTER_FORK,

    /**
     * Connected to fork chain
     * Connected to a fork chain
     */
    FORK_APPEND,

    /**
     * Fork with the forked chain
     * Forked from a fork chain
     */
    FORK_FORK,

    /**
     * Connected to orphan chains
     * Connected to a orphan chain
     */
    ORPHAN_APPEND,

    /**
     * Splitting with Orphan Chain
     * Forked from a orphan chain
     */
    ORPHAN_FORK,

    /**
     * Repeating with the main chain
     * duplicate chain
     */
    MASTER_DUPLICATE,

    /**
     * Repeating with forked chains
     * duplicate chain
     */
    FORK_DUPLICATE,

    /**
     * Repeating with Orphan Chain
     * duplicate chain
     */
    ORPHAN_DUPLICATE,

    /**
     * Partial repetition
     */
    PARTIALLY_DUPLICATE,

    /**
     * On chain block data error
     * orphan chain
     */
    DATA_ERROR,

    /**
     * Orphan Chain
     * orphan chain
     */
    ORPHAN,

    /**
     * Forked chain
     * fork chain
     */
    FORK,

    /**
     * Main chain
     * master chain
     */
    MASTER;

    @Override
    public String toString() {
        return name();
    }
}
