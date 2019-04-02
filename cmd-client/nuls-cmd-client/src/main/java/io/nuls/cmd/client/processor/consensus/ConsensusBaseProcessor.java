package io.nuls.cmd.client.processor.consensus;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.consensus.ConsensusProvider;
import io.nuls.cmd.client.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.tools.core.annotation.Autowired;

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

}
