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
 * @Description: 功能描述
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
        // 再次交互输入构造参数
        String argsJson = CommandHelper.getArgsJson();
        argsObj = CommandHelper.parseArgsJson(argsJson);
        return argsObj;
    }


}
