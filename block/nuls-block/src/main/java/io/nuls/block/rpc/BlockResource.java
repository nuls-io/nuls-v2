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

package io.nuls.block.rpc;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.*;
import static io.nuls.block.utils.LoggerUtil.commonLog;

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
     * 获取最新主链高度
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_HEIGHT, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response latestHeight(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        Map<String, Long> responseData = new HashMap<>(1);
        responseData.put("height", ContextManager.getContext(chainId).getLatestHeight());
        return success(responseData);
    }

    /**
     * 获取最新区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_BLOCK_HEADER, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response latestBlockHeader(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            BlockHeader blockHeader = service.getLatestBlockHeader(chainId);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 获取最新区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_BLOCK, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response bestBlock(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            Block block = service.getLatestBlock(chainId);
            return success(HexUtil.encode(block.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
    public Response getBlockHeaderByHeight(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            Long height = Long.parseLong(map.get("height").toString());
            BlockHeaderPo po = service.getBlockHeader(chainId, height);
            BlockHeader blockHeader = BlockUtil.fromBlockHeaderPo(po);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
    public Response getLatestBlockHeaders(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            int size = Integer.parseInt(map.get("size").toString());
            long latestHeight = ContextManager.getContext(chainId).getLatestHeight();
            long startHeight = latestHeight - size + 1;
            startHeight = startHeight < 0 ? 0 : startHeight;
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, startHeight, latestHeight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(HexUtil.encode(blockHeader.serialize()));
            }
            return success(hexList);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * 获取区块头
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADERS_BY_HEIGHT_RANGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "begin", parameterType = "long")
    @Parameter(parameterName = "end", parameterType = "long")
    public Response getBlockHeadersByHeightRange(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            long startHeight = Long.parseLong(map.get("begin").toString());
            long endheight = Long.parseLong(map.get("end").toString());
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, startHeight, endheight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(HexUtil.encode(blockHeader.serialize()));
            }
            return success(hexList);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
    public Response getBlockByHeight(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            Long height = Long.parseLong(map.get("height").toString());
            Block block = service.getBlock(chainId, height);
            return success(HexUtil.encode(block.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
    public Response getBlockHeaderByHash(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            NulsDigestData hash = NulsDigestData.fromDigestHex(map.get("hash").toString());
            BlockHeader blockHeader = service.getBlockHeader(chainId, hash);
            return success(HexUtil.encode(blockHeader.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
    public Response getBlockByHash(Map map) {
        try {
            int chainId = Integer.parseInt(map.get("chainId").toString());
            NulsDigestData hash = NulsDigestData.fromDigestHex(map.get("hash").toString());
            Block block = service.getBlock(chainId, hash);
            return success(HexUtil.encode(block.serialize()));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
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
        int chainId = Integer.parseInt(map.get("chainId").toString());
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Block block = new Block();
            block.parse(new NulsByteBuffer(HexUtil.decode((String) map.get("block"))));
            commonLog.info("recieve block from local node, chainId:" + chainId + ", height:" + block.getHeader().getHeight() + ", hash:" + block.getHeader().getHash());
            service.broadcastBlock(chainId, block);
            if (service.saveBlock(chainId, block, 1, true)) {
                return success();
            } else {
                return failed(BlockErrorCode.PARAMETER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return failed(e.getMessage());
        }
    }
}
