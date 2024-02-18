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

package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.data.Block;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.provider.model.dto.block.BlockDto;
import io.nuls.provider.model.dto.block.BlockHeaderDto;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.BlockTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;
import io.nuls.v2.util.ValidateUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class BlockController {

    BlockService blockService = ServiceManager.get(BlockService.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("getHeaderByHeight")
    @ApiOperation(description = "Query block headers based on block height", order = 201)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }

        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(height);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHeight(req);
        if (result.isSuccess() && result.getData() != null) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getHeaderByHash")
    @ApiOperation(description = "Based on blockshashQuery block header", order = 202)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is required");
        }
        GetBlockHeaderByHashReq req = new GetBlockHeaderByHashReq(hash);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHash(req);
        if (result.isSuccess() && result.getData() != null) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlockHeader")
    @ApiOperation(description = "Query the latest block header information", order = 203)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getBestBlockHeader(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        GetBlockHeaderByLastHeightReq req = new GetBlockHeaderByLastHeightReq();
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(req);
        if (result.isSuccess()) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlock")
    @ApiOperation(description = "Query the latest block", order = 204, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBestBlock(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBestBlock(Context.getChainId());
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                BeanCopierManager.beanCopier(data, dto);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBlockByHeight")
    @ApiOperation(description = "Query blocks based on block height", order = 205, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHeight(chainId, height);
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockByHash")
    @ApiOperation(description = "Based on blockshashQuery Block", order = 206, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHash(chainId, hash);
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockSerializationByHeight")
    @ApiOperation(description = "Query block serialization strings based on block height", order = 207, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public RpcResult getBlockSerializationByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.getBlockSerializationByHeight(chainId, height);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockSerializationByHash")
    @ApiOperation(description = "Based on blockshashQuery block serialization string", order = 208, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public RpcResult getBlockSerializationByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.getBlockSerializationByHash(chainId, hash);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getLatestHeight")
    @ApiOperation(description = "Get the latest main chain height", order = 209)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    })
    @ResponseData(name = "Return value", description = "Get the latest main chain height", responseType = @TypeDescriptor(value = Long.class))
    public RpcResult getLatestHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.latestHeight(chainId);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("testGetBlock")
    public RpcResult testGetBlock(List<Object> params) {
        while (true) {
            Result<Block> result = blockTools.getBestBlock(1);
            if(result.isFailed()) {
                System.out.println(result.getStatus());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
