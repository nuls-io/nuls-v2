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
package io.nuls.chain.service.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.chain.model.po.CacheDatas;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.CmTransferService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;

import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/02/20
 **/
@Service
public class CmTransferServiceImpl implements CmTransferService {
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private ChainService chainService;

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            long commitHeight = blockHeader.getHeight();
            //获取回滚信息
            CacheDatas moduleTxDatas = cacheDataService.getCacheDatas(commitHeight - 1);
            //通知远程调用回滚
            chainService.rpcBlockChainRollback(txs);
            if (null == moduleTxDatas) {
                LoggerUtil.logger().info("chain module height ={} bak datas is null,maybe had rolled", commitHeight);
                return true;
            }
            //进行数据回滚
            cacheDataService.rollBlockTxs(chainId, commitHeight);
            return true;
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }
    }
}
