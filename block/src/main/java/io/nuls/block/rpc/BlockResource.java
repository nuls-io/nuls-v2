/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.block.service.BlockService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * 区块管理模块的对外接口类
 * @author captain
 * @date 18-11-9 下午2:04
 * @version 1.0
 */
@Component
public class BlockResource extends BaseCmd{
    @Autowired
    private BlockService service;
//    /**
//     * 创建节点
//     * */
//    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0)
//    public CmdResponse createAgent(List<Object> params){
//        return service.createAgent(params);
//    }
//
//    /**
//     * 节点验证
//     * */
//    @CmdAnnotation(cmd = "cs_createAgentValid", version = 1.0)
//    public CmdResponse createAgentValid(List<Object> params){
//        return null;
//    }

}
