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
 * Module operation status enumeration
 * The module runs state enumeration.
 *
 * @author captain
 * @version 1.0
 * @date 18-11-28 afternoon5:58
 */
public enum StatusEnum {

    /**
     * Module is initializing
     * initializing
     */
    INITIALIZING,

    /**
     * Module is waiting
     * initializing
     */
    WAITING,

    /**
     * In synchronized blocks
     * synchronizing
     */
    SYNCHRONIZING,

    /**
     * Switching forked chains
     * switching
     */
    SWITCHING,

    /**
     * Module is running normally
     * running
     */
    RUNNING,

    /**
     * Maintaining orphan chains
     * Clean up the database
     */
    UPDATE_ORPHAN_CHAINS,

    /**
     * Maintaining orphan chains
     * Clean up the database
     */
    MAINTAIN_ORPHAN_CHAINS,

    /**
     * Clean up the database
     * Clean up the database
     */
    STORAGE_CLEANING,
    ;

    @Override
    public String toString() {
        return name();
    }
}
