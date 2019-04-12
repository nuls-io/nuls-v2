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

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.logback.NulsLogger;

import java.io.IOException;
import java.util.*;

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
     * @param download 0区块下载中，1接收到最新区块
     * @return
     */
    public static boolean verify(int chainId, Block block, int download) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("download", download);
            params.put("block", RPCUtil.encode(block.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_validBlock", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("status", status);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params).isSuccess();
            while (!success) {
                Thread.sleep(1000L);
                success = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params).isSuccess();
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    /**
     * 收到分叉区块时通知共识模块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    public synchronized static boolean evidence(int chainId, BlockService blockService, BlockHeader forkHeader) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
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
        if (packingAddressList.contains(masterHeaderPackingAddress)) {
            return true;
        }
        packingAddressList.add(masterHeaderPackingAddress);
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("blockHeader", RPCUtil.encode(masterHeader.serialize()));
            params.put("evidenceHeader", RPCUtil.encode(forkHeader.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_addEvidenceRecord", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("height", height);

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_chainRollBack", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("blockHeader", RPCUtil.encode(blockHeader.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_addBlock", params).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    /**
     * 回滚区块之前，先把共识模块的缓存区块头更新了
     *
     * @param chainId 链Id/chain id
     * @param rollBackAmount  回滚区块数量
     * @return
     */
    public static boolean sendHeaderList(int chainId, int rollBackAmount) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        try {
            Block latestBlock = context.getLatestBlock();
            long latestHeight = latestBlock.getHeader().getHeight();
            byte[] extend = latestBlock.getHeader().getExtend();
            BlockExtendsData data = new BlockExtendsData(extend);
            long roundIndex = data.getRoundIndex();
            List<String> hexList = new ArrayList<>();
            int count = 0;
            while (count < 110) {
                latestHeight--;
                if ((latestHeight <= 0)) {
                    //110轮已经回退到创世块了，不需要再给共识模块新区块
                    return true;
                }
                BlockHeaderPo blockHeader = service.getBlockHeaderPo(chainId, latestHeight);
                BlockExtendsData newData = new BlockExtendsData(blockHeader.getExtend());
                long newRoundIndex = newData.getRoundIndex();
                if (newRoundIndex != roundIndex) {
                    count++;
                    roundIndex = newRoundIndex;
                }
            }
            long start = latestHeight <= rollBackAmount ? 0 : latestHeight - rollBackAmount;
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, start, latestHeight);
            if (blockHeaders == null) {
                return true;
            }
            blockHeaders.forEach(e -> {
                try {
                    hexList.add(RPCUtil.encode(e.serialize()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("headerList", hexList);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_receiveHeaderList", params).isSuccess();
            while (!success) {
                Thread.sleep(1000L);
                success = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_receiveHeaderList", params).isSuccess();
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

}
