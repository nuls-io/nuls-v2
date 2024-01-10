package io.nuls.cmd.client.processor.contract;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.contract.ContractProvider;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:54
 * @Description: Function Description
 */
public abstract class ContractBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ContractProvider contractProvider = ServiceManager.get(ContractProvider.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Smart_Contract;
    }

    public Object[] getContractCallArgsJson() {
        Object[] argsObj;
        // Interacting with input again to construct parameters
        String argsJson = CommandHelper.getArgsJson();
        argsObj = CommandHelper.parseArgsJson(argsJson);
        return argsObj;
    }


}
