package io.nuls.cmd.client.processor.consensus;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.consensus.facade.AgentInfo;
import io.nuls.base.api.provider.consensus.facade.GetAgentInfoReq;
import io.nuls.base.data.NulsHash;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.parse.MapUtils;

import java.math.BigInteger;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:50
 * @Description: 功能描述
 */
@Component
public class GetAgentInfoProcessor extends ConsensusBaseProcessor {

    @Override
    public String getCommand() {
        return "getagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentHash>  the hash of an agent -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getagent <agentHash>  -- get an agent node information According to agent hash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 1);
        checkArgs(NulsHash.validHash(args[1]), "agentHash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String agentHash = args[1];
        Result<AgentInfo> result = consensusProvider.getAgentInfo(new GetAgentInfoReq(agentHash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        AgentInfo info = result.getData();
        return CommandResult.getResult(new Result(agentToMap(info)));
    }


}

