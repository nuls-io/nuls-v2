/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.provider.api.resources;

import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.api.manager.BeanCopierManager;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.data.Block;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.Parameters;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.block.BlockDto;
import io.nuls.provider.model.dto.block.BlockHeaderDto;
import io.nuls.provider.rpctools.BlockTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.util.ValidateUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/block")
@Component
@Api
public class BlockResource {

    BlockService blockService = ServiceManager.get(BlockService.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @GET
    @Path("/header/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query block headers based on block height", order = 201)
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBlockHeaderByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(height);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHeight(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess() && clientResult.getData() != null) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/header/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Based on blockshashQuery block header", order = 202)
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBlockHeaderByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        GetBlockHeaderByHashReq req = new GetBlockHeaderByHashReq(hash);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHash(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess() && clientResult.getData() != null) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/header/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query the latest block header information", order = 203)
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcClientResult getBestBlockHeader() {
        GetBlockHeaderByLastHeightReq req = new GetBlockHeaderByLastHeightReq();
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            BlockHeaderDto dto = new BlockHeaderDto();
            Object data = clientResult.getData();
            BeanCopierManager.beanCopier(data, dto);
            clientResult.setData(dto);
        }
        return clientResult;
    }

    @GET
    @Path("/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query the latest block", order = 204, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBestBlock() {
        Result<Block> result = blockTools.getBestBlock(Context.getChainId());
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                clientResult.setData(new BlockDto((Block) clientResult.getData()));
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query blocks based on block height", order = 205, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBlockByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        Result<Block> result = blockTools.getBlockByHeight(config.getChainId(), height);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                if (clientResult.getData() != null) {
                    clientResult.setData(new BlockDto((Block) clientResult.getData()));
                }
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Based on blockshashQuery Block", order = 206, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcClientResult getBlockByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        Result<Block> result = blockTools.getBlockByHash(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            try {
                if (clientResult.getData() != null) {
                    clientResult.setData(new BlockDto((Block) clientResult.getData()));
                }
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionRpcClientResult(e);
            }
        }
        return clientResult;
    }

    @GET
    @Path("/serialization/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Query block serialization strings based on block height", order = 207, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = Long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public RpcClientResult getBlockSerializationByHeight(@PathParam("height") Long height) {
        if (height == null || height < 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "height is invalid"));
        }
        Result<String> result = blockTools.getBlockSerializationByHeight(config.getChainId(), height);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }

    @GET
    @Path("/serialization/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Based on blockshashQuery block serialization string", order = 208, detailDesc = "This interface contains all transaction information packaged in blocks. It returns a large amount of data and should be called with caution")
    @Parameters({
            @Parameter(parameterName = "hash", parameterDes = "blockhash")
    })
    @ResponseData(name = "Return value", description = "Return the serialized blockHEXcharacter string", responseType = @TypeDescriptor(value = String.class))
    public RpcClientResult getBlockSerializationByHash(@PathParam("hash") String hash) {
        if (hash == null || !ValidateUtil.validHash(hash)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "hash is invalid"));
        }
        Result<String> result = blockTools.getBlockSerializationByHash(config.getChainId(), hash);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        return clientResult;
    }
}
