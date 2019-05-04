package io.nuls.contract.model.bo;

import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.manager.ContractTxCreateUnconfirmedManager;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.vm.program.ProgramExecutor;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链信息类
 * Chain information class
 *
 * @author: PierreLuo
 * @date: 2019-02-26
 */
public class Chain {

    /**
     * 链基础配置信息
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * 智能合约执行器
     */
    private ProgramExecutor programExecutor;

    /**
     * 智能合约执行器一些配置信息
     */
    private CommonConfig commonConfig;
    private DefaultConfig defaultConfig;

    /**
     * 智能合约token余额管理
     */
    private ContractTokenBalanceManager contractTokenBalanceManager;

    /**
     * 智能合约创建合约未确认交易管理器
     */
    private ContractTxCreateUnconfirmedManager contractTxCreateUnconfirmedManager;

    /**
     * 批量执行信息
     */
    private BatchInfo batchInfo = new BatchInfo();

    /**
     * 向合约模块注册接口提供给合约来调用
     */
    private Map<String, CmdRegister> cmdRegisterMap = new ConcurrentHashMap<>();

    public int getChainId() {
        return config.getChainId();
    }

    public ConfigBean getConfig() {
        return config;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }

    public ProgramExecutor getProgramExecutor() {
        return programExecutor;
    }

    public void setProgramExecutor(ProgramExecutor programExecutor) {
        this.programExecutor = programExecutor;
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public ContractTokenBalanceManager getContractTokenBalanceManager() {
        return contractTokenBalanceManager;
    }

    public void setContractTokenBalanceManager(ContractTokenBalanceManager contractTokenBalanceManager) {
        this.contractTokenBalanceManager = contractTokenBalanceManager;
    }

    public ContractTxCreateUnconfirmedManager getContractTxCreateUnconfirmedManager() {
        return contractTxCreateUnconfirmedManager;
    }

    public void setContractTxCreateUnconfirmedManager(ContractTxCreateUnconfirmedManager contractTxCreateUnconfirmedManager) {
        this.contractTxCreateUnconfirmedManager = contractTxCreateUnconfirmedManager;
    }

    public BatchInfo getBatchInfo() {
        return batchInfo;
    }

    public void setBatchInfo(BatchInfo batchInfo) {
        this.batchInfo = batchInfo;
    }

    public Map<String, CmdRegister> getCmdRegisterMap() {
        return cmdRegisterMap;
    }

    public void setCmdRegisterMap(Map<String, CmdRegister> cmdRegisterMap) {
        this.cmdRegisterMap = cmdRegisterMap;
    }
}
