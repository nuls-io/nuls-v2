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

package io.nuls.account.storage;

import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AliasPo;

import java.util.List;

/**
 * @author EdwardChan
 * @date
 */
public interface AliasStorageService {

    /**
     * @auther EdwardChan
     * @date Nov.9th 2018
     */
    @Deprecated
    List<AliasPo> getAliasList(int chainId);


    /**
     * get alias
     *
     * @param chainId
     * @param alias
     * @return aliasPo
     */
    AliasPo getAlias(int chainId, String alias);


    /**
     * get alias by address
     *
     * if the alias isn't exist,return null
     *
     * @param chainId
     * @param address
     * @return
     */
    AliasPo getAliasByAddress(int chainId, String address);

    /**
     * save the alias
     *
     * @param alias
     * @return the result
     */
    boolean saveAlias(int chainId, Alias alias);

    /**
     * remove Alias by chainId and alias
     */
    boolean removeAlias(int chainId, String alias);

}
