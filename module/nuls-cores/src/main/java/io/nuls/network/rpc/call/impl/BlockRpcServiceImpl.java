/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.rpc.call.BlockRpcService;
import io.nuls.network.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用区块模块的RPC接口
 *
 * @author lan
 * @description
 * @date 2018/12/07
 **/
@Component
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
            LoggerUtil.logger(chainId).debug("getBestBlockHeader begin time={}", TimeManager.currentTimeMillis());
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, NetworkConstant.CMD_BL_BEST_BLOCK_HEADER, map, 1000);
            if (null != response && response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map result = (Map) responseData.get(NetworkConstant.CMD_BL_BEST_BLOCK_HEADER);
                BlockHeader header = new BlockHeader();
                header.parse(new NulsByteBuffer(RPCUtil.decode((String) result.get("value"))));
                bestBlockInfo.setHash(header.getHash().toHex());
                bestBlockInfo.setBlockHeight(header.getHeight());
            } else {
                LoggerUtil.logger(chainId).error("getBestBlockHeader fail.response={}", JSONUtils.obj2json(response));
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error("getBestBlockHeader error,chainId={}.exception={}", chainId, e.getMessage());
        } finally {
            LoggerUtil.logger(chainId).debug("getBestBlockHeader height ={},hash={}", bestBlockInfo.getBlockHeight(), bestBlockInfo.getHash());
        }
        return bestBlockInfo;
    }
}
