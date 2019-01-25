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

package io.nuls.protocol.thread.monitor;

import io.nuls.protocol.constant.RunningStatusEnum;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 协议升级监视器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class ProtocolMonitor implements Runnable {

    private static final ProtocolMonitor INSTANCE = new ProtocolMonitor();

    private ProtocolMonitor() {

    }

    public static ProtocolMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            ProtocolContext context = ContextManager.getContext(chainId);
            try {
                //判断该链的运行状态,只有正常运行时才会有分叉链的处理
                RunningStatusEnum status = context.getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)) {
                    commonLog.debug("skip process, status is " + status + ", chainId-" + chainId);
                    continue;
                }

                StampedLock lock = context.getLock();
                long stamp = lock.tryOptimisticRead();
                try {
                    for (; ; stamp = lock.writeLock()) {
                        if (stamp == 0L) {
                            continue;
                        }
                        // possibly racy reads
                        List<ProtocolVersion> versionList = context.getVersionList();
                        if (!lock.validate(stamp)) {
                            continue;
                        }
                        if (true) {
                            break;
                        }
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp == 0L) {
                            continue;
                        }
                        // exclusive access
                        //进行切换,切换前变更模块运行状态
                        break;
                    }
                } finally {
                    context.setStatus(RunningStatusEnum.RUNNING);
                    if (StampedLock.isWriteLockStamp(stamp)) {
                        lock.unlockWrite(stamp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.setStatus(RunningStatusEnum.RUNNING);
                commonLog.error("chainId-" + chainId + ", switchChain fail, auto rollback fail");
            }
        }
    }

}
