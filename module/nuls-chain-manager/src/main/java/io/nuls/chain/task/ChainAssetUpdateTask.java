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
package io.nuls.chain.task;


import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainAssetTotalCirculate;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.impl.RpcServiceImpl;
import io.nuls.chain.service.impl.ChainServiceImpl;
import io.nuls.chain.service.impl.MessageServiceImpl;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.List;

/**
 * Group event monitor
 * 测试 定时打印连接信息
 *
 * @author lan
 * @create 2018/11/14
 */
public class ChainAssetUpdateTask implements Runnable {
    @Override
    public void run() {
        ChainServiceImpl chainService = SpringLiteContext.getBean(ChainServiceImpl.class);
        RpcServiceImpl rpcService = SpringLiteContext.getBean(RpcServiceImpl.class);
        MessageServiceImpl messageService = SpringLiteContext.getBean(MessageServiceImpl.class);
        //查询所有链列表
        try {
            List<BlockChain> blockChainList = chainService.getBlockList();
            for (BlockChain blockChain : blockChainList) {
                if (!blockChain.isDelete()) {
                    List<String> assetKeys = blockChain.getSelfAssetKeyList();
                    StringBuilder assets = new StringBuilder();
                    for (String assetKey : assetKeys) {
                        assets.append(CmRuntimeInfo.getAssetIdByAssetKey(assetKey)).append(",");
                    }
                    LoggerUtil.logger().debug("chainId={}=====assets={}",blockChain.getChainId(),assets);
                    if (blockChain.getChainId() == CmRuntimeInfo.getMainIntChainId()) {
                        //处理主网值更新
                        if(assets.length()>0) {
                            List<ChainAssetTotalCirculate> mainChainAssets = rpcService.getLgAssetsById(blockChain.getChainId(), assets.substring(0, assets.length() - 1));
                            messageService.dealMainChainIssuingAssets(mainChainAssets);
                        }
                        continue;
                    }
                    messageService.initChainIssuingAssets(blockChain.getChainId());
                    //发送链请求信息，并且增加30s后的处理线程，sleep30s.
                    if (assets.length() > 0) {
                        rpcService.requestCrossIssuingAssets(blockChain.getChainId(), assets.substring(0, assets.length() - 1));
                    }
                    //30s交互时间
                    Thread.sleep(30000);
                    messageService.dealChainIssuingAssets(blockChain.getChainId());
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }

    }
}
