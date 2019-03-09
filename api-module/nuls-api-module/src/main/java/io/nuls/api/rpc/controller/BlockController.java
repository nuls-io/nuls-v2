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

package io.nuls.api.rpc.controller;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.MongoDBService;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.BlockInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.service.RollbackService;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.data.StringUtils;


import java.util.List;

import static io.nuls.api.model.rpc.RpcErrorCode.DATA_NOT_EXISTS;

/**
 * @author Niels
 */
@Controller
public class BlockController {

    @Autowired
    private MongoDBService dbService;

//    @Autowired
//    private WalletRPCHandler rpcHandler;

    @Autowired
    private BlockService blockService;
    @Autowired
    private RollbackService rollbackBlock;

    @RpcMethod("getBestBlockHeader")
    public RpcResult getBestInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId = (int) params.get(0);
        BlockHeaderInfo localBestBlockHeader = blockService.getBestBlockHeader(chainId);
        return new RpcResult().setResult(localBestBlockHeader);
    }

    @RpcMethod("getHeaderByHeight")
    public RpcResult getHeaderByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        long height = Long.parseLong("" + params.get(0));
        int chainId = (int) params.get(1);
        if (height < 0) {
            height = 0;
        }
        BlockHeaderInfo header = blockService.getBlockHeader(chainId, height);
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(header);

        return rpcResult;
    }

    @RpcMethod("getHeaderByHash")
    public RpcResult getHeaderByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        String hash = (String) params.get(0);
        int chainId = (int) params.get(1);

        if (StringUtils.isBlank(hash)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[hash] is required"));
        }
        BlockHeaderInfo header = blockService.getBlockHeaderByHash(chainId, hash);

        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(header);
        return rpcResult;
    }

    @RpcMethod("getBlockByHash")
    public RpcResult getBlockByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        String hash = (String) params.get(0);
        int chainId = (int) params.get(1);

        if (StringUtils.isBlank(hash)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[hash] is required"));
        }

        BlockInfo blockInfo = WalletRpcHandler.getBlockInfo(chainId, hash);
        if (blockInfo == null) {
            throw new JsonRpcException(new RpcResultError(DATA_NOT_EXISTS.getCode(), DATA_NOT_EXISTS.getMessage(), null));
        }

        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(blockInfo);
        return rpcResult;
    }

    @RpcMethod("getBlockByHeight")
    public RpcResult getBlockByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        long height = Long.parseLong("" + params.get(0));
        int chainId = (int) params.get(1);
        if (height < 0) {
            height = 0;
        }
        BlockHeaderInfo blockHeaderInfo = blockService.getBlockHeader(chainId, height);
        if (blockHeaderInfo == null) {
            throw new JsonRpcException(new RpcResultError(DATA_NOT_EXISTS.getCode(), DATA_NOT_EXISTS.getMessage(), null));
        }

        BlockInfo blockInfo = WalletRpcHandler.getBlockInfo(chainId, height);
        blockInfo.setHeader(blockHeaderInfo);
        RpcResult rpcResult = new RpcResult();
        rpcResult.setResult(blockInfo);
        return rpcResult;
    }

    @RpcMethod("getBlockHeaderList")
    public RpcResult getBlockHeaderList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        int chainId = (int) params.get(2);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        // Whether to filter empty blocks
        boolean filterEmptyBlocks = (boolean) params.get(3);
        ;
        String packingAddress = null;
        if (params.size() > 4) {
            packingAddress = (String) params.get(4);
        }
        PageInfo<BlockHeaderInfo> pageInfo = blockService.pageQuery(chainId, pageIndex, pageSize, packingAddress, filterEmptyBlocks);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

//
//    @RpcMethod("rollbackBestBlocks")
//    public RpcResult rollbackBestBlocks(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        int count = (int) params.get(0);
//        BlockHeaderInfo localBestBlockHeader;
//        long useNanoTime = 0;
//        for (; count > 0; count--) {
//            localBestBlockHeader = blockHeaderService.getBestBlockHeader();
//            if (null != localBestBlockHeader && localBestBlockHeader.getHeight() >= 0L) {
//                try {
//                    long start = System.nanoTime();
//                    rollbackBlock.rollbackBlock(localBestBlockHeader.getHeight());
//                    useNanoTime += System.nanoTime() - start;
//                } catch (Exception e) {
//                    Log.error(e);
//                    throw new JsonRpcException(new RpcResultError(RpcErrorCode.SYS_UNKNOWN_EXCEPTION, "Rollback is failed"));
//                }
//            }
//        }
//        Log.info("rollback " + count + " use:" + useNanoTime/1000000 + "ms.");
//
//        RpcResult rpcResult = new RpcResult();
//        rpcResult.setResult(true);
//        return rpcResult;
//    }
//
//    @RpcMethod("stopSync")
//    public RpcResult stopSync(List<Object> params) {
//        ApiContext.doSync = false;
//        RpcResult result = new RpcResult();
//        result.setResult(true);
//        return result;
//    }
//
//    @RpcMethod("recoverySync")
//    public RpcResult recoverySync(List<Object> params) {
//        ApiContext.doSync = true;
//        RpcResult result = new RpcResult();
//        result.setResult(true);
//        return result;
//    }

}
