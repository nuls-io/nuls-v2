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
package io.nuls.contract.storage;


import io.nuls.contract.model.bo.config.ConfigBean;

import java.util.Map;

/**
 * 配置信息存储管理类
 * Configuration Information Storage Management Class
 *
 * @author qinyifeng
 * @date 2018/12/11
 */
public interface ConfigStorageService {
    /**
     * 保存指定链的配置信息
     * Save configuration information for the specified chain
     *
     * @param bean    配置类/config bean
     * @param chainID 链ID/chain id
     * @return 保存是否成功/Is preservation successful?
     * @throws
     */
    boolean save(ConfigBean bean, int chainID) throws Exception;

    /**
     * 查询某条链的配置信息
     * Query the configuration information of a chain
     *
     * @param chainID 链ID/chain id
     * @return 配置信息类/config bean
     */
    ConfigBean get(int chainID);

    /**
     * 删除某条链的配置信息
     * Delete configuration information for a chain
     *
     * @param chainID 链ID/chain id
     * @return 删除是否成功/Delete success
     */
    boolean delete(int chainID);

    /**
     * 获取当前节点所有的链信息
     * Get all the chain information of the current node
     *
     * @return 节点信息列表/Node information list
     */
    Map<Integer, ConfigBean> getList();
}
