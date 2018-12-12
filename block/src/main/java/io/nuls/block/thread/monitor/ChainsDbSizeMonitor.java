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

package io.nuls.block.thread.monitor;

import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.tools.log.Log;

import java.util.SortedSet;

import static io.nuls.block.constant.Constant.CLEAN_PARAM;
import static io.nuls.block.constant.RunningStatusEnum.RUNNING;
import static io.nuls.block.constant.RunningStatusEnum.SYNCHRONIZING;

/**
 * 分叉链、孤儿链数据库定时清理器
 * 因为使用了rocksDb，清理记录后，数据文件大小不能实时变化，所以不能按数据库文件大小来做判断标准，每次按区块的百分比清理
 * 触发条件:某链ID的数据库缓存的区块总数超过超出cacheSize(可配置)
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class ChainsDbSizeMonitor implements Runnable {

    private static final ChainsDbSizeMonitor INSTANCE = new ChainsDbSizeMonitor();

    private ChainsDbSizeMonitor() {

    }

    public static ChainsDbSizeMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            try {
                //判断该链的运行状态，只有正常运行时才会有数据库的处理
                RunningStatusEnum status = ContextManager.getContext(chainId).getStatus();
                if (!status.equals(RUNNING)) {
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    return;
                }
                //获取配置项
                int cacheSize = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.CACHE_SIZE));
                //1.获取某链ID的数据库缓存的所有区块数量
                int actualSize = ChainManager.getForkChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                actualSize += ChainManager.getOrphanChains(chainId).stream().mapToInt(e -> e.getHashList().size()).sum();
                Log.info("chainId:{}, cacheSize:{}, actualSize:{}", chainId, cacheSize, actualSize);
                //与阈值比较
                while (actualSize > cacheSize) {
                    Log.info("before clear, chainId:{}, cacheSize:{}, actualSize:{}", chainId, cacheSize, actualSize);
                    ContextManager.getContext(chainId).setStatus(RunningStatusEnum.DATABASE_CLEANING);
                    //2.按顺序清理分叉链和孤儿链
                    SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
                    int forkSize = forkChains.size();
                    if (forkSize > 0) {
                        int i = forkSize / CLEAN_PARAM;
                        //最少清理一个链
                        i = i == 0 ? 1 : i;
                        for (int j = 0; j < i; j++) {
                            Chain chain = forkChains.first();
                            boolean b = ChainManager.removeForkChain(chainId, chain);
                            if (!b) {
                                Log.error("remove fork chain fail, chain:", chain);
                            } else {
                                actualSize -= chain.getHashList().size();
                            }
                        }
                    }

                    SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
                    int orphanSize = orphanChains.size();
                    if (orphanSize > 0) {
                        int i = orphanSize / CLEAN_PARAM;
                        //最少清理一个链
                        i = i == 0 ? 1 : i;
                        for (int j = 0; j < i; j++) {
                            Chain chain = orphanChains.first();
                            boolean b = ChainManager.removeOrphanChain(chainId, chain);
                            if (!b) {
                                Log.error("remove orphan chain fail, chain:", chain);
                            } else {
                                actualSize -= chain.getHashList().size();
                            }
                        }
                    }
                    Log.info("after clear, chainId:{}, cacheSize:{}, actualSize:{}", chainId, cacheSize, actualSize);
                    ContextManager.getContext(chainId).setStatus(RUNNING);
                }
            } catch (Exception e) {
                ContextManager.getContext(chainId).setStatus(RunningStatusEnum.EXCEPTION);
                Log.error(e);
            }
        }
    }

}
