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

package io.nuls.protocol.rpc;

import com.google.common.collect.Maps;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.protocol.Protocol;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.service.ProtocolService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.protocol.constant.CommandConstant.*;

/**
 * The external interface class of the module
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 afternoon2:04
 */
@Component
@NulsCoresCmd(module = ModuleE.PU)
public class ProtocolResource extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * Obtain the current main network version information
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_VERSION, version = 1.0, description = "get mainnet version")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing three properties", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "version", valueType = Short.class, description = "Protocol version number"),
            @Key(name = "effectiveRatio", valueType = Byte.class, description = "The minimum effective ratio within each statistical interval"),
            @Key(name = "continuousIntervalCount", valueType = Short.class, description = "The number of consecutive intervals that the agreement must meet in order to take effect")})
    )
    public Response getVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ProtocolContext context = ContextManager.getContext(chainId);
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        ProtocolVersion localProtocolVersion = context.getLocalProtocolVersion();
        Map<String, ProtocolVersion> result = new HashMap<>();
        result.put("currentProtocolVersion", currentProtocolVersion);
        result.put("localProtocolVersion", localProtocolVersion);
        return success(result);
    }

    /**
     * Verify if the version number of the newly received block is correct
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CHECK_BLOCK_VERSION, version = 1.0, description = "check block version")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "extendsData", requestType = @TypeDescriptor(value = String.class), parameterDes = "BlockExtendsDataSerializedhexcharacter string")
    })
    @ResponseData(name = "Return value", description = "No return value")
    public Response checkBlockVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String extendStr = map.get("extendsData").toString();
        BlockExtendsData extendsData = new BlockExtendsData(RPCUtil.decode(extendStr));

        ProtocolContext context = ContextManager.getContext(chainId);
        ProtocolVersion currentProtocol = context.getCurrentProtocolVersion();
        //The received new block does not match the local main network version, verification failed
        if (currentProtocol.getVersion() != extendsData.getMainVersion()) {
            NulsLogger logger = context.getLogger();
            logger.info("------block version error, mainVersion:" + currentProtocol.getVersion() + ",blockVersion:" + extendsData.getMainVersion());
            return failed("block version error");
        }
        return success();
    }

    /**
     * Save Block
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = SAVE_BLOCK, version = 1.0, description = "save block header")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "Block headhex")
    })
    @ResponseData(name = "Return value", description = "No return value")
    public Response save(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            if (service.save(chainId, blockHeader)) {
                return success();
            } else {
                return failed("protocol save failed!");
            }
        } catch (NulsException e) {
            return failed(e.getMessage());
        }
    }

    /**
     * Rolling back blocks
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = ROLLBACK_BLOCK, version = 1.0, description = "rollback block header")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "Block headhex")
    })
    @ResponseData(name = "Return value", description = "No return value")
    public Response rollback(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            if (service.rollback(chainId, blockHeader)) {
                return success();
            } else {
                return failed("protocol rollback failed!");
            }
        } catch (NulsException e) {
            return failed(e.getMessage());
        }
    }

    /**
     * Accept registration of multiple versions of configurations for each module
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = REGISTER_PROTOCOL, version = 1.0, description = "register protocol")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
            @Parameter(parameterName = "moduleCode", requestType = @TypeDescriptor(value = String.class), parameterDes = "Module Flag"),
            @Parameter(parameterName = "list", requestType = @TypeDescriptor(value = List.class), parameterDes = "ProtocolSerializedhexcharacter string"),
    })
    @ResponseData(name = "Return value", description = "No return value")
    public Response registerProtocol(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ProtocolContext context = ContextManager.getContext(chainId);
        Map<Short, List<Map.Entry<String, Protocol>>> protocolMap = context.getProtocolMap();
        NulsLogger logger = context.getLogger();
        String moduleCode = map.get("moduleCode").toString();
        List list = (List) map.get("list");
        logger.info("--------------------registerProtocol---------------------------");
        logger.info("moduleCode-" + moduleCode);
//        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (Object o : list) {
            Map m = (Map) o;
            Protocol protocol = JSONUtils.map2pojo(m, Protocol.class);
            short version = protocol.getVersion();
            List<Map.Entry<String, Protocol>> protocolList = protocolMap.computeIfAbsent(version, k -> new ArrayList<>());
            protocolList.add(Maps.immutableEntry(moduleCode, protocol));
            logger.info("protocol-" + protocol);
        }
        return success();
    }

}
