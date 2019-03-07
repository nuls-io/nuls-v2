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
package io.nuls.network.rpc.call.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.rpc.call.BlockRpcService;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用区块模块的RPC接口
 *
 * @author lan
 * @description
 * @date 2018/12/07
 **/
@Service
public class BlockRpcServiceImpl implements BlockRpcService {
    /**
     * 获取最近区块高度与hash
     *
     * @param chainId chainId
     * @return
     */
    @Override
    public BestBlockInfo getBestBlockHeader(int chainId) {
        BestBlockInfo bestBlockInfo = new BestBlockInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("chainId", chainId);
        try {
            LoggerUtil.Log.debug("getBestBlockHeader begin time={}", TimeManager.currentTimeMillis());
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, NetworkConstant.CMD_BL_BEST_BLOCK_HEADER, map, 2000);
            if (null != response && response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                String hex = (String) responseData.get(NetworkConstant.CMD_BL_BEST_BLOCK_HEADER);
                BlockHeader header = new BlockHeader();
                header.parse(new NulsByteBuffer(HexUtil.decode(hex)));
                bestBlockInfo.setHash(header.getHash().getDigestHex());
                bestBlockInfo.setBlockHeight(header.getHeight());
            }
        } catch (Exception e) {
            LoggerUtil.Log.error("getBestBlockHeader error,chainId={}.exception={}", chainId, e.getMessage());
        } finally {
            LoggerUtil.Log.info("getBestBlockHeader end time={}", TimeManager.currentTimeMillis());
            LoggerUtil.Log.debug("getBestBlockHeader height ={},hash={}", bestBlockInfo.getBlockHeight(), bestBlockInfo.getHash());
        }

        return bestBlockInfo;
    }
}
