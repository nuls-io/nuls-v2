package io.nuls.cmd.client.processor.consensus;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.base.api.provider.consensus.facade.AgentInfo;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.math.BigInteger;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:07
 * @Description: 功能描述
 */
public abstract class ConsensusBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Consensus;
    }

    public Map<String, Object> agentToMap(AgentInfo info) {
        Map<String, Object> map = MapUtils.beanToMap(info);
        map.put("deposit", config.toBigUnit(new BigInteger(info.getDeposit())));
        map.put("totalDeposit", config.toBigUnit(new BigInteger(info.getTotalDeposit())));
        map.put("time", NulsDateUtils.timeStamp2DateStr(info.getTime()));
        map.put("status", CommandHelper.consensusExplain((Integer) map.get("status")));
        return map;
    }
}
