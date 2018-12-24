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

package io.nuls.block.rpc;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;

/**
 * 区块管理模块的对外接口类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:04
 */
@Component
public class BlockResource extends BaseCmd {
    @Autowired
    private BlockService service;

    /**
     * 获取最新区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = BEST_BLOCK_HEADER, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Object bestBlockHeader(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            BlockHeader blockHeader = service.getLatestBlockHeader(chainId);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (IOException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 获取最新区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = BEST_BLOCK, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Object bestBlock(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            Block block = service.getLatestBlock(chainId);
            return success(HexUtil.encode(block.serialize()));
        } catch (IOException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 根据高度获取区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_BY_HEIGHT, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "height", parameterType = "long")
    public Object getBlockHeaderByHeight(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            Long height = Long.parseLong(map.get("height").toString());
            BlockHeaderPo blockHeader = service.getBlockHeader(chainId, height);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (IOException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 获取区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_LATEST_BLOCK_HEADERS, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "size", parameterType = "int")
    public Object getLatestBlockHeaders(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            Integer size = Integer.parseInt(map.get("size").toString());
            long latestHeight = ContextManager.getContext(chainId).getLatestHeight();
            long startHeight = latestHeight - size + 1;
            startHeight = startHeight < 0 ? 0 : startHeight;
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, startHeight, latestHeight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(HexUtil.encode(blockHeader.serialize()));
            }
            return success(hexList);
        } catch (IOException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 根据高度获取区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_BY_HEIGHT, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "height", parameterType = "long")
    public Object getBlockByHeight(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            Long height = Long.parseLong(map.get("height").toString());
            Block block = service.getBlock(chainId, height);
            return success(HexUtil.encode(block.serialize()));
        } catch (IOException e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 根据hash获取区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_BY_HASH, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "hash", parameterType = "string")
    public Object getBlockHeaderByHash(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            NulsDigestData hash = NulsDigestData.fromDigestHex(map.get("hash").toString());
            BlockHeader blockHeader = service.getBlockHeader(chainId, hash);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 根据hash获取区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_BY_HASH, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "hash", parameterType = "string")
    public Object getBlockByHash(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            NulsDigestData hash = NulsDigestData.fromDigestHex(map.get("hash").toString());
            Block block = service.getBlock(chainId, hash);
            return success(HexUtil.encode(block.serialize()));
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 接收新打包区块
     * 1.保存区块
     * 2.广播区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = RECEIVE_PACKING_BLOCK, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "block", parameterType = "string")
    public Response receivePackingBlock(Map map) {
        try {
            Integer chainId = Integer.parseInt(map.get("chainId").toString());
            Block block = new Block();
            block.parse(HexUtil.decode((String) map.get("block")),0);
            if (service.saveBlock(chainId, block, 1)) {
                Map params = new HashMap();
                params.put("chainId",chainId );
                params.put("blockHeader",HexUtil.encode(block.getHeader().serialize()));
                CmdDispatcher.requestAndResponse(ModuleE.CS.abbr,"cs_addBlock", params);
                return success();
            } else {
                service.rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(block));
                return failed(BlockErrorCode.PARAMETER_ERROR);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
