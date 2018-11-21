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

import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块监控器，如果一段时间内区块高度没更新，通知网络模块重置节点
 * @author captain
 * @date 18-11-14 下午3:53
 * @version 1.0
 */
public class NetworkResetMonitor implements Runnable {

    private static final NetworkResetMonitor INSTANCE = new NetworkResetMonitor();

    private static Map<Integer, Long> latestHeights;

    private NetworkResetMonitor() {
        latestHeights = new HashMap<>();
    }

    public static NetworkResetMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            for (Integer chainId : ContextManager.chainIds) {
                //判断该链的运行状态，只有正常运行时才会有区块高度监控
                RunningStatusEnum status = ContextManager.getContext(chainId).getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)){
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    return;
                }
                long height = ContextManager.getContext(chainId).getLatestBlock().getHeader().getHeight();
                //1.如果没有缓存过对应chainId的最新hash，则新增
                if (latestHeights.get(chainId) == null) {
                    latestHeights.put(chainId, height);
                }
                //2.如果缓存过，对比两者的高度是否发生了变化
                if (latestHeights.get(chainId) == height) {
                    NetworkUtil.resetNetwork(chainId);
                } else {
                    latestHeights.put(chainId, height);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

}
