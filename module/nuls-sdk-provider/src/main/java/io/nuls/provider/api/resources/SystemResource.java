/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.provider.api.resources;


import io.nuls.provider.api.config.Config;
import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.Key;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.rpctools.BlockTools;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author: Niels Wang
 */
@Path("/")
@Component
@Api
public class SystemResource {

    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcClientResult desc() {
        return RpcClientResult.getSuccess("支持两种方式，restful - http://{ip}:{port}/api & jsonrpc - http://{ip}:{port}/jsonrpc");
    }

    @GET
    @Path("api/info")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "获取本链相关信息", order = 001)
    @ResponseData(name = "返回值", description = "返回本链信息", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", description = "本链的ID"),
            @Key(name = "assetId", description = "本链默认主资产的ID"),
            @Key(name = "inflationAmount", description = "本链默认主资产的初始数量"),
            @Key(name = "agentChainId", description = "本链共识资产的链ID"),
            @Key(name = "agentAssetId", description = "本链共识资产的ID")
    }))
    public RpcClientResult info() {
        Result<Map> result = blockTools.getInfo(config.getChainId());
        if (result.isSuccess()) {
            Map map = result.getData();
            map.put("chainId", config.getChainId());
            map.put("assetId", config.getAssetsId());
            map.remove("awardAssetId");
            map.remove("seedNodes");
        }
        return ResultUtil.getRpcClientResult(result);
    }


}
