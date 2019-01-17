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

package io.nuls.block.utils.module;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 调用共识模块接口的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:43
 */
public class ConsensusUtil {

    /**
     * 共识验证
     *
     * @param chainId
     * @param block
     * @param download 0区块下载中，1接收到最新区块
     * @return
     */
    public static boolean verify(int chainId, Block block, int download) {
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("download", download);
            params.put("block", HexUtil.encode(block.serialize()));

            return CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_validBlock", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 通知共识模块进入工作状态或者进入等待状态
     *
     * @param chainId
     * @param status  1-工作,0-等待
     * @return
     */
    public static boolean notice(int chainId, int status) {
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("status", status);
            return CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 收到分叉区块时通知共识模块
     *
     * @param chainId
     * @return
     */
    public static boolean evidence(int chainId, BlockHeader masterHeader, BlockHeader forkHeader) {
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("blockHeader", HexUtil.encode(masterHeader.serialize()));
            params.put("evidenceHeader", HexUtil.encode(forkHeader.serialize()));

            return CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_addEvidenceRecord", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 回滚区块时通知共识模块
     *
     * @param chainId
     * @return
     */
    public static boolean rollbackNotice(int chainId, long height) {
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("height", height);

            return CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_chainRollBack", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 新增区块时通知共识模块
     *
     * @param chainId
     * @param localInit
     * @return
     */
    public static boolean saveNotice(int chainId, BlockHeader blockHeader, boolean localInit) {
        //创世区块保存时不通知共识模块
        if (localInit) {
            return true;
        }
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("blockHeader", HexUtil.encode(blockHeader.serialize()));

            return CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_addBlock", params).isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

}
