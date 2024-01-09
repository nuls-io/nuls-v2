/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.protocol.storage;

import io.nuls.protocol.model.po.StatisticsInfo;

import java.util.List;

/**
 * Version statistics information storage management class
 * Configuration Information Storage Management Class
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 afternoon3:02
 */
public interface StatisticsStorageService {
    /**
     * Save version statistics for the specified chain
     * Save configuration information for the specified chain
     *
     * @param statisticsInfo Version statistics class/config bean
     * @param chainId        chainId/chain id
     * @return Whether the save was successful/Is preservation successful?
     * @throws
     */
    boolean save(int chainId, StatisticsInfo statisticsInfo);

    /**
     * Query version statistics of a certain chain
     * Query the configuration information of a chain
     *
     * @param chainId chainId/chain id
     * @param height block height
     * @return Version statistics information class/config bean
     */
    StatisticsInfo get(int chainId, long height);

    /**
     * Delete version statistics for a certain chain
     * Delete configuration information for a chain
     *
     * @param chainId chainId/chain id
     * @param height block height
     * @return Whether the deletion was successful/Delete success
     */
    boolean delete(int chainId, long height);

    /**
     * Obtain all chain information of the current node
     * Get all the chain information of the current node
     *
     * @param chainId chainId/chain id
     * @return Node Information List/Node information list
     */
    List<StatisticsInfo> getList(int chainId);
}
