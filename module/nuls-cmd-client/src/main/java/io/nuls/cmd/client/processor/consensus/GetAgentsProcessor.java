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

package io.nuls.cmd.client.processor.consensus;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.consensus.facade.AgentInfo;
import io.nuls.base.api.provider.consensus.facade.GetAgentListReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取共识节点列表
 * Get all the agent nodes
 *
 * @author: Charlie
 */
@Component
public class GetAgentsProcessor extends ConsensusBaseProcessor {


    @Override
    public String getCommand() {
        return "getagents";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<pageNumber> pageNumber - Required")
                .newLine("\t<pageSize> pageSize(1~100) - Required")
                .newLine("\t[keyWord]");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getagents <pageNumber> <pageSize> [keyWord] --get agent list";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3 && length != 4) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!StringUtils.isNumeric(args[1]) || !StringUtils.isNumeric(args[2])) {
            return false;
        }
        checkArgsNumber(args,2,3);
        checkIsNumeric(args[1],"pageNumber");
        checkIsNumeric(args[2],"pageSize");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int pageNumber = Integer.parseInt(args[1]);
        int pageSize = Integer.parseInt(args[2]);
        String keyWord = null;
        if(args.length == 4){
            keyWord = args[3];
        }
        GetAgentListReq req = new GetAgentListReq();
        req.setPageNumber(pageNumber);
        req.setPageSize(pageSize);
        req.setKeyWord(keyWord);
        Result<AgentInfo> result = consensusProvider.getAgentList(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }

        List<Map<String, Object>> list = result.getList().stream().map(this::agentToMap).collect(Collectors.toList());
        return CommandResult.getResult(CommandResult.dataTransformList(new Result(list)));
    }

}
