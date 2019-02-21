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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.util.HashMap;
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
    @CmdAnnotation(cmd = GET_MAIN_VERSION , version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getMainVersion(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        return success(ContextManager.getContext(chainId).getCurrentProtocolVersion().getVersion());
    }

    /**
     * 获取当前钱包版本信息
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = GET_BLOCK_VERSION , version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getBlockVersion(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        List<ProtocolVersion> list = ContextManager.getContext(chainId).getLocalVersionList();
        return success(list.get(list.size() - 1));
    }

    /**
     * 保存区块
     *
     * @param map
     * @return
     */
    @CmdAnnotation(cmd = SAVE_BLOCK , version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "string")
    public Response save(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            short i = service.save(chainId, blockHeader);
            Map<String, Short> responseData = new HashMap<>(1);
            responseData.put("version", i);
            return success(responseData);
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
    @CmdAnnotation(cmd = ROLLBACK_BLOCK , version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeader", parameterType = "string")
    public Response rollback(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String hex = map.get("blockHeader").toString();
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
            short i = service.rollback(chainId, blockHeader);
            Map<String, Short> responseData = new HashMap<>(1);
            responseData.put("version", i);
            return success(responseData);
        } catch (NulsException e) {
            return failed(e.getMessage());
        }
    }

}
