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
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.service.ProtocolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.protocol.constant.CommandConstant.*;

/**
 * 模块的对外接口类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:04
 */
@Component
public class ProtocolResource extends BaseCmd {
    @Autowired
    private ProtocolService service;

    /**
     * 获取当前主网版本信息
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_MAIN_VERSION, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getMainVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        return success(ContextManager.getContext(chainId).getCurrentProtocolVersion());
    }

    /**
     * 获取当前钱包版本信息
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_VERSION, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getBlockVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        List<ProtocolVersion> list = ContextManager.getContext(chainId).getLocalVersionList();
        return success(list.get(list.size() - 1));

    }

    /**
     * 验证新收到区块的版本号是否正确
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = CHECK_BLOCK_VERSION, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response checkBlockVersion(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String extendStr = map.get("extendsData").toString();
        BlockExtendsData extendsData = new BlockExtendsData(RPCUtil.decode(extendStr));

        ProtocolContext context = ContextManager.getContext(chainId);
        ProtocolVersion currentProtocol = context.getCurrentProtocolVersion();
        //收到的新区块和本地主网版本不一致，验证不通过
        if (currentProtocol.getVersion() != extendsData.getMainVersion()) {
            NulsLogger commonLog = context.getLogger();
            commonLog.info("------block version error, mainVersion:" + currentProtocol.getVersion() + ",blockVersion:" + extendsData.getMainVersion());
            return failed("block version error");
        }
        return success();
    }

    /**
     * 保存区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = SAVE_BLOCK, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "string")
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
     * 回滚区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = ROLLBACK_BLOCK, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "string")
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
     * 接受各模块注册多版本配置
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = REGISTER_PROTOCOL, version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "list", parameterType = "List")
    public Response registerProtocol(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        ProtocolContext context = ContextManager.getContext(chainId);
        Map<Short, List<Map.Entry<String, Protocol>>> protocolMap = context.getProtocolMap();
        NulsLogger commonLog = context.getLogger();
        String moduleCode = map.get("moduleCode").toString();
        List list = (List) map.get("list");
        commonLog.info("--------------------registerProtocol---------------------------");
        commonLog.info("moduleCode-" + moduleCode);
//        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (Object o : list) {
            Map m = (Map) o;
            Protocol protocol = JSONUtils.map2pojo(m, Protocol.class);
            short version = protocol.getVersion();
            List<Map.Entry<String, Protocol>> protocolList = protocolMap.computeIfAbsent(version, k -> new ArrayList<>());
            protocolList.add(Maps.immutableEntry(moduleCode, protocol));
            commonLog.info("protocol-" + protocol);
        }
        return success();

    }

}
