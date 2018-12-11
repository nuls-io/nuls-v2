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
package io.nuls.network.rpc.external.impl;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.rpc.external.BlockRpcService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @description
 * @author lan
 * @date 2018/12/07
 **/
@Service
public class BlockRpcServiceImpl implements BlockRpcService {
    @Override
    public BestBlockInfo getBestBlockHeader(int chainId) {
        BestBlockInfo bestBlockInfo = new BestBlockInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("chainId",chainId);
        try {
            Response response =  CmdDispatcher.requestAndResponse(NetworkConstant.MODULE_ROLE, NetworkConstant.CMD_BL_BEST_BLOCK_HEADER,map );
            if(response.isSuccess()){
                Map responseData = (Map)response.getResponseData();
                bestBlockInfo.setHash(String.valueOf(responseData.get("hash")));
                bestBlockInfo.setBlockHeight(Long.valueOf(responseData.get("height").toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bestBlockInfo;
    }
}
