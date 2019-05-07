package io.nuls.cmd.client.processor;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:01
 * @Description:
 *   命令组
 *   command group
 */
public enum CommandGroup {

    /**
     * account cmd group
     */
    Account,
    Consensus,
    Ledger,
    Transaction,
    Block,
    Smart_Contract,
    Cross_Chain,
    System;

    final String title;

    CommandGroup(){
        this.title = this.name().toLowerCase();
    }

    public String getTitle(){
        return title;
    }

}
