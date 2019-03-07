package io.nuls.api.cache;

import io.nuls.api.model.po.db.*;
import io.nuls.base.data.BlockHeader;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ApiCache {

    private ChainInfo chainInfo;

    private BlockHeader bestHeader;

    private Map<String, AccountInfo> accountMap = new ConcurrentHashMap<>();

    private Map<String, AccountLedgerInfo> ledgerMap = new ConcurrentHashMap<>();

    private Map<String, AgentInfo> agentMap = new ConcurrentHashMap<>();

    private Map<String, AliasInfo> aliasMap = new ConcurrentHashMap<>();

    public void addAccountInfo(AccountInfo accountInfo) {
        accountMap.put(accountInfo.getAddress(), accountInfo);
    }

    public AccountInfo getAccountInfo(String address) {
        return accountMap.get(address);
    }

    public AccountLedgerInfo getAccountLedgerInfo(String key) {
        return ledgerMap.get(key);
    }

    public void addAccountLedgerInfo(AccountLedgerInfo ledgerInfo) {
        ledgerMap.put(ledgerInfo.getKey(), ledgerInfo);
    }

    public void addAgentInfo(AgentInfo agentInfo) {
        agentMap.put(agentInfo.getTxHash(), agentInfo);
    }

    public AgentInfo getAgentInfo(String agentHash) {
        return agentMap.get(agentHash);
    }

    public void addAlias(AliasInfo aliasInfo) {
        aliasMap.put(aliasInfo.getAddress(), aliasInfo);
        aliasMap.put(aliasInfo.getAlias(), aliasInfo);
    }

    public AliasInfo getAlias(String key) {
        return aliasMap.get(key);
    }

}
