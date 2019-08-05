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

package io.nuls.block.thread;

import io.nuls.block.constant.NodeEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;

/**
 * 区块下载管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockDownloader implements Callable<Boolean> {

    /**
     * 链ID
     */
    private int chainId;

    BlockDownloader(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public Boolean call() {
        ChainContext context = ContextManager.getContext(chainId);
        BlockDownloaderParams downloaderParams = context.getDownloaderParams();
        List<Node> nodes = downloaderParams.getNodes();
        ThreadPoolExecutor executor = ThreadUtils.createThreadPool(nodes.size() * 2, 0, new NulsThreadFactory("worker-" + chainId));
        long netLatestHeight = downloaderParams.getNetLatestHeight();
        long startHeight = downloaderParams.getLocalLatestHeight() + 1;
        NulsLogger logger = context.getLogger();
        try {
            logger.info("BlockDownloader start work from " + startHeight + " to " + netLatestHeight + ", nodes-" + nodes);
            ChainParameters chainParameters = context.getParameters();
            long cachedBlockSizeLimit = chainParameters.getCachedBlockSizeLimit();
            int downloadNumber = chainParameters.getDownloadNumber();
            AtomicInteger cachedBlockSize = context.getCachedBlockSize();
            long limit = context.getParameters().getCachedBlockSizeLimit() * 80 / 100;
            while (startHeight <= netLatestHeight && context.isNeedSyn()) {
                int cachedSize = cachedBlockSize.get();
                while (cachedSize > cachedBlockSizeLimit) {
                    logger.info("BlockDownloader wait! cached block:" + context.getBlockMap().size() + ", total block size:" + cachedSize);
                    nodes.forEach(e -> e.setCredit(20));
                    Thread.sleep(3000L);
                    cachedSize = cachedBlockSize.get();
                }
                //下载的区块字节数达到缓存阈值的80%时，降慢下载速度
                if (cachedSize > limit) {
                    nodes.forEach(e -> e.setCredit(e.getCredit() / 2));
                }
                Node node = getNode(nodes);
                if (node == null) {
                    Thread.sleep(100L);
                    continue;
                }
                int credit = node.getCredit();
                int size = downloadNumber * credit / 100;
                size = size <= 0 ? 1 : size;
                if (startHeight + size > netLatestHeight) {
                    size = (int) (netLatestHeight - startHeight + 1);
                }
                long endHeight = startHeight + size - 1;
                //组装批量获取区块消息
                HeightRangeMessage message = new HeightRangeMessage(startHeight, endHeight);
                //发送消息给目标节点
                boolean b = NetworkCall.sendToNode(chainId, message, node.getId(), GET_BLOCKS_BY_HEIGHT_MESSAGE);
                if (b) {
                    downloaderParams.getNodeMap().get(node.getId()).setNodeEnum(NodeEnum.WORKING);
                }
                startHeight += size;
            }
            logger.info("BlockDownloader stop work, flag-" + context.isNeedSyn());
        } catch (Exception e) {
            logger.error("", e);
            context.setNeedSyn(false);
        } finally {
            executor.shutdownNow();
        }
        return context.isNeedSyn();
    }

    private Node getNode(List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getNodeEnum().equals(NodeEnum.IDLE)) {
                return node;
            }
        }
        return null;
    }

}
