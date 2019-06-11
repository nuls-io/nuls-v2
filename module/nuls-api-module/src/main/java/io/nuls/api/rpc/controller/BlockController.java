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
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.BlockInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.mini.MiniBlockHeaderInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.model.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
@Controller
public class BlockController {

    @Autowired
    private BlockService blockService;

    @RpcMethod("getBestBlockHeader")
    public RpcResult getBestInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            BlockHeaderInfo localBestBlockHeader = blockService.getBestBlockHeader(chainId);
            if (localBestBlockHeader == null) {
                return RpcResult.dataNotFound();
            }
            return RpcResult.success(localBestBlockHeader);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getHeaderByHeight")
    public RpcResult getHeaderByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            BlockHeaderInfo header = blockService.getBlockHeader(chainId, height);
            if (header == null) {
                return RpcResult.dataNotFound();
            }
            return RpcResult.success(header);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getHeaderByHash")
    public RpcResult getHeaderByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            BlockHeaderInfo header = blockService.getBlockHeaderByHash(chainId, hash);
            if (header == null) {
                return RpcResult.dataNotFound();
            }
            return RpcResult.success(header);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getBlockByHash")
    public RpcResult getBlockByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (StringUtils.isBlank(hash)) {
            return RpcResult.paramError("[hash] is required");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            Result<BlockInfo> result = WalletRpcHandler.getBlockInfo(chainId, hash);
            if (result.isFailed()) {
                return RpcResult.failed(result);
            }
            if (result.getData() == null) {
                return RpcResult.dataNotFound();
            }
            RpcResult rpcResult = new RpcResult();
            rpcResult.setResult(result.getData());
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getBlockByHeight")
    public RpcResult getBlockByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            BlockHeaderInfo blockHeaderInfo = blockService.getBlockHeader(chainId, height);
            if (blockHeaderInfo == null) {
                return RpcResult.dataNotFound();
            }
            Result<BlockInfo> result = WalletRpcHandler.getBlockInfo(chainId, height);
            if (result.isFailed()) {
                return RpcResult.failed(result);
            }
            if (result.getData() == null) {
                return RpcResult.dataNotFound();
            }
            BlockInfo blockInfo = result.getData();
            blockInfo.setHeader(blockHeaderInfo);
            RpcResult rpcResult = new RpcResult();
            rpcResult.setResult(blockInfo);
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getBlockHeaderList")
    public RpcResult getBlockHeaderList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, pageIndex, pageSize;
        boolean filterEmptyBlocks;
        String packingAddress = null;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            filterEmptyBlocks = (boolean) params.get(3);
            if (params.size() > 4) {
                packingAddress = (String) params.get(4);
            }
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<MiniBlockHeaderInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            } else {
                pageInfo = blockService.pageQuery(chainId, pageIndex, pageSize, packingAddress, filterEmptyBlocks);
            }
            RpcResult result = new RpcResult();
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
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
