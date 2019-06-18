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
package io.nuls.ledger.task;

import io.nuls.base.data.Block;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.rpc.call.impl.CallRpcServiceImpl;
import io.nuls.ledger.service.impl.BlockDataServiceImpl;
import io.nuls.ledger.storage.impl.LgBlockSyncRepositoryImpl;
import io.nuls.ledger.utils.LoggerUtil;

import java.util.List;

/**
 * 进行区块的同步，统计账本缓存统计相关信息
 *
 * @author lan
 * @create 2019/06/04
 */
public class LedgerBlockSyncTask implements Runnable {
    @Override
    public void run() {

        blockSync();
    }


    private void blockSync() {
        /**
         * 1.向区块获取当前的高度+1 的区块
         * 2.判断区块的上preHash是否==缓存区块的hash
         * 不等，则进行本地回滚，直到相等
         * 3.相等进行解析存储
         *
         */
        CallRpcServiceImpl callRpcService = SpringLiteContext.getBean(CallRpcServiceImpl.class);
        BlockDataServiceImpl blockDataService = SpringLiteContext.getBean(BlockDataServiceImpl.class);
        try {
            //业务钟存在的链id
            List<ChainHeight> chainHeights = blockDataService.getChainsBlockHeight();
            for (ChainHeight chainHeight : chainHeights) {
                int chainId = chainHeight.getChainId();
                long height = blockDataService.currentSyncHeight(chainId);
                SpringLiteContext.getBean(LgBlockSyncRepositoryImpl.class).initChainDb(chainId);
                if (chainHeight.getBlockHeight() > 0) {
                    long nextHeight = height + 1;
                    Block block = callRpcService.getBlockByHeight(chainId, nextHeight);
                    while (null != block) {
                        String preHash = block.getHeader().getPreHash().toHex();
                        String currentHeightHash = blockDataService.getBlockHashByHeight(chainId, height);
                        if (nextHeight == 0 || preHash.equalsIgnoreCase(currentHeightHash)) {
                            //执行存储
                            blockDataService.syncBlockDatas(chainId, nextHeight, block);
                            LoggerUtil.logger(chainId).debug("blockSync ok chainId={},height={}", chainId, height + 1);
                            height = nextHeight;
                            nextHeight = height + 1;
                            block = callRpcService.getBlockByHeight(chainId, nextHeight);
                        } else {
                            //执行回滚
                            blockDataService.rollBackBlockDatas(chainId, height);
                            nextHeight = height;
                            height = height - 1;
                            block = callRpcService.getBlockByHeight(chainId, nextHeight);
                            LoggerUtil.logger(chainId).debug("blockSync rollback finish chainId={},height={}", chainId, height + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }

    }
}
