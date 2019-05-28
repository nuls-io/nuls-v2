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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用共识模块接口的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:43
 */
@Component
public class ConsensusUtil {
    @Autowired
    private static BlockService service;
    /**
     * 共识验证
     *
     * @param chainId 链Id/chain id
     * @param block
     * @param download 0区块下载中,1接收到最新区块
     * @return
     */
    public static boolean verify(int chainId, Block block, int download) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("download", download);
            params.put("block", RPCUtil.encode(block.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_validBlock", params).isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 通知共识模块进入工作状态或者进入等待状态
     *
     * @param chainId 链Id/chain id
     * @param status  1-工作,0-等待
     * @return
     */
    public static boolean notice(int chainId, int status) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("status", status);
            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params).isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 收到分叉区块时通知共识模块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static synchronized boolean evidence(int chainId, BlockService blockService, BlockHeader forkHeader) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        long forkHeaderHeight = forkHeader.getHeight();
        if (context.getLatestHeight() < forkHeaderHeight) {
            return true;
        }
        BlockHeader masterHeader = blockService.getBlockHeader(chainId, forkHeaderHeight);
        if (masterHeader.getHash().equals(forkHeader.getHash())) {
            return true;
        }
        byte[] masterHeaderPackingAddress = masterHeader.getPackingAddress(chainId);
        byte[] forkHeaderPackingAddress = forkHeader.getPackingAddress(chainId);
        if (!Arrays.equals(masterHeaderPackingAddress, forkHeaderPackingAddress)) {
            return true;
        }
        List<byte[]> packingAddressList = context.getPackingAddressList();
        //May 19th 2019 EdwardChan 对于List中的字节数组不能使用contains来进行判断,因为equals方法不能用来判断字节数组中的内容是否相等
        //if (packingAddressList.contains(masterHeaderPackingAddress)) {
        //    return true;
        //}
        for (byte[] tmp : packingAddressList) {
            if (Arrays.equals(tmp,masterHeaderPackingAddress)) {
                return true;
            }
        }
        packingAddressList.add(masterHeaderPackingAddress);
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("blockHeader", RPCUtil.encode(masterHeader.serialize()));
            params.put("evidenceHeader", RPCUtil.encode(forkHeader.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_addEvidenceRecord", params).isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 回滚区块时通知共识模块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public static boolean rollbackNotice(int chainId, long height) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_chainRollBack", params).isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

    /**
     * 新增区块时通知共识模块
     *
     * @param chainId 链Id/chain id
     * @param localInit
     * @return
     */
    public static boolean saveNotice(int chainId, BlockHeader blockHeader, boolean localInit) {
        //创世区块保存时不通知共识模块
        if (localInit) {
            return true;
        }
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("blockHeader", RPCUtil.encode(blockHeader.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_addBlock", params).isSuccess();
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
    }

}
