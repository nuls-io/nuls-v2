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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.SmallBlockCacher;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.BlockForwardEnum.ERROR;
import static io.nuls.block.constant.CommandConstant.*;
import static io.nuls.block.utils.LoggerUtil.COMMON_LOG;

/**
 * External Interface Class of Block Management Module
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 afternoon2:04
 */
@Component
@NulsCoresCmd(module = ModuleE.BL)
public class BlockResource extends BaseCmd {
    @Autowired
    private BlockService service;

    /**
     * Get the latest main chain height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = INFO, version = 1.0, description = "returns network node height and local node height")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing two properties", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "networkHeight", valueType = Long.class, description = "The latest block height of network nodes"),
            @Key(name = "localHeight", valueType = Long.class, description = "The latest block height of the local node")})
    )
    public Response info(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        Map<String, Long> responseData = new HashMap<>(2);
        ChainContext context = ContextManager.getContext(chainId);
        if (context == null) {
            return success();
        }
        responseData.put("networkHeight", context.getNetworkHeight());
        responseData.put("localHeight", context.getLatestHeight());
        return success(responseData);
    }

    /**
     * Get the latest main chain height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_HEIGHT, version = 1.0, description = "the latest height of master chain")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing a property", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Long.class, description = "Latest main chain height")})
    )
    public Response latestHeight(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        Map<String, Long> responseData = new HashMap<>(2);
        ChainContext context = ContextManager.getContext(chainId);
        if (context == null) {
            return success();
        }
        responseData.put("value", context.getLatestHeight());
        return success(responseData);
    }

    /**
     * Get the latest block header
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_BLOCK_HEADER, version = 1.0, description = "the latest block header of master chain")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Returns a serialized block headerHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response latestBlockHeader(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            BlockHeader blockHeader = service.getLatestBlockHeader(chainId);
            Map responseData = new HashMap<>(2);
            responseData.put("value", RPCUtil.encode(blockHeader.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the latest block headerPO
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_BLOCK_HEADER_PO, version = 1.0, description = "the latest block header po of master chain")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Return a block headerPOSerializedHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response latestBlockHeaderPo(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            BlockHeaderPo blockHeader = service.getLatestBlockHeaderPo(chainId);
            Map responseData = new HashMap<>(2);
            responseData.put("value", RPCUtil.encode(blockHeader.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the latest blocks
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = LATEST_BLOCK, version = 1.0, description = "the latest block of master chain")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Returns a serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response bestBlock(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            Block block = service.getLatestBlock(chainId);
            Map responseData = new HashMap<>(2);
            responseData.put("value", RPCUtil.encode(block.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Obtain block heads based on height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_BY_HEIGHT, version = 1.0, description = "get a block header by height")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Returns a serialized block headerHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockHeaderByHeight(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long height = Long.parseLong(map.get("height").toString());
            BlockHeader blockHeader = service.getBlockHeader(chainId, height);
            Map responseData = new HashMap<>(2);
            if (blockHeader != null) {
                responseData.put("value", RPCUtil.encode(blockHeader.serialize()));
                return success(responseData);
            } else {
                return success(responseData);
            }
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Obtain block heads based on height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_PO_BY_HEIGHT, version = 1.0, description = "get a block header po by height")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return a block headerPOSerializedHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockHeaderPoByHeight(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long height = Long.parseLong(map.get("height").toString());
            BlockHeaderPo po = service.getBlockHeaderPo(chainId, height);
            Map responseData = new HashMap<>(2);
            if (po != null) {
                responseData.put("value", RPCUtil.encode(po.serialize()));
                return success(responseData);
            } else {
                return success(responseData);
            }
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the latest block headers
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_LATEST_BLOCK_HEADERS, version = 1.0, description = "get the latest number of block headers")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "size", requestType = @TypeDescriptor(value = int.class), parameterDes = "quantity")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter stringList", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getLatestBlockHeaders(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            int size = Integer.parseInt(map.get("size").toString());
            long latestHeight = ContextManager.getContext(chainId).getLatestHeight();
            long startHeight = latestHeight - size + 1;
            startHeight = startHeight < 0 ? 0 : startHeight;
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, startHeight, latestHeight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(RPCUtil.encode(blockHeader.serialize()));
            }
            Map responseData = new HashMap<>(2);
            responseData.put("value", hexList);
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the latest block heads for several rounds,provide forPOCConsensus module usage
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_ROUND_BLOCK_HEADERS, version = 1.0, description = "get the latest several rounds of block headers")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "Starting height"),
            @Parameter(parameterName = "round", requestType = @TypeDescriptor(value = int.class), parameterDes = "Consensus round")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter stringList", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getRoundBlockHeaders(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long height = Long.parseLong(map.get("height").toString());
            int round = Integer.parseInt(map.get("round").toString());
            height = height - 1 < 0 ? 0 : height - 1;
            List<BlockHeader> blockHeaders = service.getBlockHeaderByRound(chainId, height, round);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader e : blockHeaders) {
                hexList.add(RPCUtil.encode(e.serialize()));
            }
            Map responseData = new HashMap<>(2);
            responseData.put("value", hexList);
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the latest block heads for several rounds,provide forPOCConsensus module usage
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_LATEST_ROUND_BLOCK_HEADERS, version = 1.0, description = "get the latest several rounds of block headers")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "round", requestType = @TypeDescriptor(value = int.class), parameterDes = "Consensus round")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter stringList", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getLatestRoundBlockHeaders(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            int round = Integer.parseInt(map.get("round").toString());
            List<BlockHeader> blockHeaders = service.getBlockHeaderByRound(chainId, context.getLatestHeight(), round);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader e : blockHeaders) {
                hexList.add(RPCUtil.encode(e.serialize()));
            }
            Map responseData = new HashMap<>(2);
            responseData.put("value", hexList);
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get block header,Use the protocol upgrade module
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADERS_FOR_PROTOCOL, version = 1.0, description = "get block headers for protocol upgrade module")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "interval", requestType = @TypeDescriptor(value = int.class), parameterDes = "Protocol upgrade statistics interval")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter stringList", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getBlockHeadersForProtocol(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long interval = Integer.parseInt(map.get("interval").toString());
            long latestHeight = context.getLatestHeight();
            Map responseData = new HashMap<>(2);
            if (latestHeight % interval == 0) {
                return success(responseData);
            }
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, latestHeight - (latestHeight % interval) + 1, latestHeight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(RPCUtil.encode(blockHeader.serialize()));
            }
            responseData.put("value", hexList);
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Obtain block heads based on height intervals
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADERS_BY_HEIGHT_RANGE, version = 1.0, description = "get the block headers according to the height range")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "begin", requestType = @TypeDescriptor(value = long.class), parameterDes = "Starting height"),
            @Parameter(parameterName = "end", requestType = @TypeDescriptor(value = long.class), parameterDes = "End height")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter stringList", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getBlockHeadersByHeightRange(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long startHeight = Long.parseLong(map.get("begin").toString());
            long endheight = Long.parseLong(map.get("end").toString());
            List<BlockHeader> blockHeaders = service.getBlockHeader(chainId, startHeight, endheight);
            List<String> hexList = new ArrayList<>();
            for (BlockHeader blockHeader : blockHeaders) {
                hexList.add(RPCUtil.encode(blockHeader.serialize()));
            }
            Map responseData = new HashMap<>(2);
            responseData.put("value", hexList);
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Obtain blocks based on height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_BY_HEIGHT, version = 1.0, description = "get a block by height")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter stringList", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockByHeight(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long height = Long.parseLong(map.get("height").toString());
            Block block = service.getBlock(chainId, height);
            Map<String, String> responseData = new HashMap<>(2);
            if (block == null) {
                return success(responseData);
            }
            responseData.put("value", RPCUtil.encode(block.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Obtain blocks based on height
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = "roll_back", version = 1.0, description = "Roll back a number of blocks")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "successful", responseType = @TypeDescriptor(value = String.class))
    public Response rollback(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            long count = Long.parseLong(map.get("height").toString());
            Block block = service.getLatestBlock(chainId);
            for (long height = block.getHeader().getHeight(); height > block.getHeader().getHeight() - count; height--) {
                service.rollbackBlock(chainId, height, true);
            }
            Map<String, String> responseData = new HashMap<>(2);
            if (block == null) {
                return success(responseData);
            }
            responseData.put("value", "success");
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * according tohashGet block header
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_BY_HASH, version = 1.0, description = "get a block header by hash")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Returns the serialized block headerHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockHeaderByHash(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            NulsHash hash = NulsHash.fromHex(map.get("hash").toString());
            BlockHeader blockHeader = service.getBlockHeader(chainId, hash);
            Map<String, String> responseData = new HashMap<>(2);
            if (blockHeader == null) {
                return success(responseData);
            }
            responseData.put("value", RPCUtil.encode(blockHeader.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * according tohashGet block header
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_HEADER_PO_BY_HASH, version = 1.0, description = "get a block header po by hash")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return block headerPOSerializedHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockHeaderPoByHash(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            NulsHash hash = NulsHash.fromHex(map.get("hash").toString());
            BlockHeaderPo blockHeader = service.getBlockHeaderPo(chainId, hash);
            Map<String, String> responseData = new HashMap<>(2);
            if (blockHeader == null) {
                return success(responseData);
            }
            responseData.put("value", RPCUtil.encode(blockHeader.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * according tohashGet blocks
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_BY_HASH, version = 1.0, description = "get a block by hash")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public Response getBlockByHash(Map map) {
        try {
            int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
            ChainContext context = ContextManager.getContext(chainId);
            if (context == null) {
                return success();
            }
            NulsHash hash = NulsHash.fromHex(map.get("hash").toString());
            Block block = service.getBlock(chainId, hash);
            Map<String, String> responseData = new HashMap<>(2);
            if (block == null) {
                return success(responseData);
            }
            responseData.put("value", RPCUtil.encode(block.serialize()));
            return success(responseData);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Receive new packaged blocks
     * 1.Save Block
     * 2.Broadcast Block
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = RECEIVE_PACKING_BLOCK, version = 1.0, description = "receive the new packaged block")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "block", requestType = @TypeDescriptor(value = String.class), parameterDes = "Block serializedHEXcharacter string")
    })
    @ResponseData(name = "Return value", description = "No return value")
    public Response receivePackingBlock(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ChainContext context = ContextManager.getContext(chainId);
        if (context == null) {
            return success();
        }
        NulsLogger logger = context.getLogger();
        try {
            Block block = new Block();
            block.parse(new NulsByteBuffer(RPCUtil.decode((String) map.get("block"))));
            logger.debug("recieve block from local node, height:" + block.getHeader().getHeight() + ", hash:" + block.getHeader().getHash());
            if (service.saveBlock(chainId, block, 1, true, true, false)) {
                return success();
            } else {
                SmallBlockCacher.setStatus(chainId, block.getHeader().getHash(), ERROR);
                return failed(BlockErrorCode.PARAMETER_ERROR);
            }
        } catch (Exception e) {
            logger.error("", e);
            return failed(e.getMessage());
        }
    }

    /**
     * Get the current running status
     * status-0:synchronization
     * status-1:normal operation
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_STATUS, version = 1.0, description = "receive the new packaged block")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing a property", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "status", valueType = Integer.class, description = "running state")})
    )
    public Response getStatus(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ChainContext context = ContextManager.getContext(chainId);
        if (context == null) {
            return success();
        }
        Map<String, Integer> responseData = new HashMap<>(2);
        switch (context.getStatus()) {
            case INITIALIZING:
            case WAITING:
            case SYNCHRONIZING:
                responseData.put("status", 0);
                break;
            default:
                responseData.put("status", 1);
        }
        return success(responseData);
    }
}
