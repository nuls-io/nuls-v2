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

import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.HashMap;
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
    @CmdAnnotation(cmd = GET_PROTOCOL_VERSION , version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getProtocolVersion(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        Map<String, Short> responseData = new HashMap<>(1);
        responseData.put("value", ContextManager.getContext(chainId).getProtocolVersion().getVersion());
        return success(responseData);
    }

}
