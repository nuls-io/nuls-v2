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
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool class for calling consensus module interface
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 morning10:43
 */
@Component
public class ConsensusCall {
    @Autowired
    private static BlockService service;
    /**
     * Consensus verification
     *
     * @param chainId chainId/chain id
     * @param block
     * @param download 0Blocking download in progress,1Received the latest block
     * @return
     */
    public static Result verify(int chainId, Block block, int download) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(5);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("download", download);
            params.put("block", RPCUtil.encode(block.serialize()));
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_validBlock", params, 10 * 60 * 1000);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                Map v = (Map) responseData.get("cs_validBlock");
                boolean value = (Boolean) v.get("value");
                if (value) {
                    List contractList = (List) v.get("contractList");
                    return Result.getSuccess(BlockErrorCode.SUCCESS).setData(contractList);
                }
            }
            return Result.getFailed(ErrorCode.init(response.getResponseErrorCode()));
        } catch (Exception e) {
            logger.error("", e);
            return Result.getFailed(BlockErrorCode.BLOCK_VERIFY_ERROR);
        }
    }

    /**
     * Notify consensus module to enter working state or waiting state
     *
     * @param chainId chainId/chain id
     * @param status  1-work,0-wait for
     * @return
     */
    public static boolean notice(int chainId, int status) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("status", status);
            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_updateAgentStatus", params).isSuccess();
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * Notify consensus module when receiving fork blocks
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static boolean evidence(int chainId, BlockService blockService, BlockHeader forkHeader) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        long forkHeaderHeight = forkHeader.getHeight();
        if (context.getLatestHeight() < forkHeaderHeight) {
            return true;
        }
        BlockHeader masterHeader = blockService.getBlockHeader(chainId, forkHeaderHeight);
        if (masterHeader.getHash().equals(forkHeader.getHash())) {
            return true;
        }
        String masterHeaderPackingAddress = AddressTool.getStringAddressByBytes(masterHeader.getPackingAddress(chainId)) + masterHeader.getHeight();
        String forkHeaderPackingAddress = AddressTool.getStringAddressByBytes(forkHeader.getPackingAddress(chainId)) + forkHeader.getHeight();

        if (!masterHeaderPackingAddress.equals(forkHeaderPackingAddress)) {
            return true;
        }
        List<String> packingAddressList = context.getPackingAddressList();
        //May 19th 2019 EdwardChan aboutListByte arrays in cannot be usedcontainsTo make a judgment,becauseequalsMethod cannot be used to determine whether the contents of a byte array are equal
        //if (packingAddressList.contains(masterHeaderPackingAddress)) {
        //    return true;
        //}
        for (String tmp : packingAddressList) {
            if (masterHeaderPackingAddress.equals(tmp)) {
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
            logger.error("", e);
            return false;
        }
    }

    /**
     * Notify consensus module when rolling back blocks
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static boolean rollbackNotice(int chainId, long height) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_chainRollBack", params).isSuccess();
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    /**
     * Notify consensus module when adding blocks
     *
     * @param chainId chainId/chain id
     * @param localInit
     * @return
     */
    public static boolean saveNotice(int chainId, BlockHeader blockHeader, boolean localInit) {
        //Do not notify the consensus module when saving Genesis blocks
        if (localInit) {
            return true;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("blockHeader", RPCUtil.encode(blockHeader.serialize()));

            return ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_addBlock", params).isSuccess();
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

}
