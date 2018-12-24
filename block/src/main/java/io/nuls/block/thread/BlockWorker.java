package io.nuls.block.thread;

import io.nuls.base.data.NulsDigestData;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.constant.CommandConstant;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.model.Node;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.log.Log;
import lombok.AllArgsConstructor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 区块下载器
 *
 * @author captain
 * @version 1.0
 * @date 18-12-4 下午8:29
 */
@AllArgsConstructor
public class BlockWorker implements Callable<BlockDownLoadResult> {

    private long startHeight;
    private int size;
    private int chainId;
    private Node node;

    @Override
    public BlockDownLoadResult call() {
        boolean b = false;
        long endHeight = startHeight + size - 1;
        //组装批量获取区块消息
        HeightRangeMessage message = new HeightRangeMessage(startHeight, endHeight);
        message.setCommand(CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE);
        //计算本次请求hash，用来跟踪本次异步请求
        NulsDigestData messageHash = message.getHash();
        try {
            Future<CompleteMessage> future = CacheHandler.addBatchBlockRequest(chainId, messageHash);

            //发送消息给目标节点
            boolean result = NetworkUtil.sendToNode(chainId, message, node.getId());

            //发送失败清空数据
            if (!result) {
                CacheHandler.removeRequest(chainId, messageHash);
                return new BlockDownLoadResult(messageHash, startHeight, size, node, false);
            }

            CompleteMessage completeMessage = future.get(60L, TimeUnit.SECONDS);
            b = completeMessage.isSuccess();
        } catch (Exception e) {
            Log.error(e);
        }
        return new BlockDownLoadResult(messageHash, startHeight, size, node, b);
    }
}