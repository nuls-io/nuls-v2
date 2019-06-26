package io.nuls.api.cache;

import io.nuls.api.model.po.db.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiCache {

    private ChainInfo chainInfo;

    private ChainConfigInfo configInfo;

    private CoinContextInfo coinContextInfo;

    private BlockHeaderInfo bestHeader;

    private CurrentRound currentRound;

    private Map<String, AccountInfo> accountMap = new ConcurrentHashMap<>();

    private Map<String, AccountLedgerInfo> ledgerMap = new ConcurrentHashMap<>();

    private Map<String, AgentInfo> agentMap = new ConcurrentHashMap<>();

    private Map<String, AliasInfo> aliasMap = new ConcurrentHashMap<>();

    public ApiCache() {
        currentRound = new CurrentRound();
    }

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


    public ChainInfo getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
    }


    public BlockHeaderInfo getBestHeader() {
        return bestHeader;
    }

    public void setBestHeader(BlockHeaderInfo bestHeader) {
        this.bestHeader = bestHeader;
    }

    public CurrentRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(CurrentRound currentRound) {
        this.currentRound = currentRound;
    }

    public Map<String, AccountInfo> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<String, AccountInfo> accountMap) {
        this.accountMap = accountMap;
    }

    public Map<String, AccountLedgerInfo> getLedgerMap() {
        return ledgerMap;
    }

    public void setLedgerMap(Map<String, AccountLedgerInfo> ledgerMap) {
        this.ledgerMap = ledgerMap;
    }

    public Map<String, AgentInfo> getAgentMap() {
        return agentMap;
    }

    public void setAgentMap(Map<String, AgentInfo> agentMap) {
        this.agentMap = agentMap;
    }

    public Map<String, AliasInfo> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, AliasInfo> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public CoinContextInfo getCoinContextInfo() {
        return coinContextInfo;
    }

    public void setCoinContextInfo(CoinContextInfo coinContextInfo) {
        this.coinContextInfo = coinContextInfo;
    }

    public ChainConfigInfo getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(ChainConfigInfo configInfo) {
        this.configInfo = configInfo;
    }
}
