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

package io.nuls.block.rpc.call;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.protocol.ModuleHelper;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用协议升级模块接口的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:43
 */
public class ProtocolUtil {

    /**
     * 回滚区块时通知协议升级模块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static boolean rollbackNotice(int chainId, BlockHeader blockHeader) {
        if (!ModuleHelper.isSupportProtocolUpdate()) {
            return true;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.PU.abbr, "rollbackBlock", params);
            return response.isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 新增区块时通知协议升级模块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static boolean saveNotice(int chainId, BlockHeader blockHeader) {
        if (!ModuleHelper.isSupportProtocolUpdate()) {
            return true;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.PU.abbr, "saveBlock", params);
            return response.isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }


    public static boolean checkBlockVersion(int chainId, BlockHeader blockHeader) {
        if (!ModuleHelper.isSupportProtocolUpdate()) {
            return true;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("extendsData", HexUtil.encode(blockHeader.getExtend()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.PU.abbr, "checkBlockVersion", params);
            return response.isSuccess();
        }catch (Exception e) {
            commonLog.error(e);
            return false;
        }
    }

}
