package io.nuls.cmd.client.processor.transaction;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:10
 * @Description: 功能描述
 */
public abstract class TransactionBaseProcessor implements CommandProcessor {

    TransferService transferService = ServiceManager.get(TransferService.class);

    @Override
    public CommandGroup getGroup(){
        return CommandGroup.Transaction;
    }

}
