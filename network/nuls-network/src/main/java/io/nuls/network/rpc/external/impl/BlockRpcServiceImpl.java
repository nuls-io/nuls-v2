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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.rpc.external.BlockRpcService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

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
            long startTime = System.currentTimeMillis();
//            Log.info("start RPC Time :{}",startTime);
            Response response =  CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, NetworkConstant.CMD_BL_BEST_BLOCK_HEADER,map,500 );
            long endTime = System.currentTimeMillis();
//            Log.info("end RPC Time :{}",System.currentTimeMillis());
//            Log.info("used Time :{}",endTime-startTime);
            if(null != response && response.isSuccess()){
                Map responseData = (Map)response.getResponseData();
                String hex = (String) responseData.get("bestBlockHeader");
                BlockHeader header = new BlockHeader();
                header.parse(new NulsByteBuffer(HexUtil.decode(hex)));
                bestBlockInfo.setHash(header.getHash().getDigestHex());
                bestBlockInfo.setBlockHeight(header.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bestBlockInfo;
    }
}
