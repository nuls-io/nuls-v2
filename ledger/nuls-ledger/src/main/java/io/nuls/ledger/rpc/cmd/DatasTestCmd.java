/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.model.po.BlockTxs;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description 单元测试获取数据使用，实际业务不需要
 * @date 2019/02/14
 **/
@Component
public class DatasTestCmd extends BaseCmd {
    @Autowired
    Repository repository;
    @CmdAnnotation(cmd = "getSnapshot",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response getSnapshot(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        Integer chainId = (Integer) params.get("chainId");
        long blockHeight =  Long.valueOf(params.get("blockHeight").toString());
        BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(chainId, blockHeight);
        return success(blockSnapshotAccounts);
    }
    @CmdAnnotation(cmd = "getBlock",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response getBlock(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        Integer chainId = (Integer) params.get("chainId");
        long blockHeight =  Long.valueOf(params.get("blockHeight").toString());
        BlockTxs blockTxs = repository.getBlock(chainId, blockHeight);
        return success(blockTxs);
    }

}
