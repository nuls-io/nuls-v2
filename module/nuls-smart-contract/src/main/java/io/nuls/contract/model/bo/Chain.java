package io.nuls.contract.model.bo;

import io.nuls.contract.enums.BlockType;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.manager.ContractTxCreateUnconfirmedManager;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;

import java.util.HashMap;
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
     * 打包区块 - 0, 验证区块 - 1
     */
    private static ThreadLocal<Integer> currentThreadBlockType = new ThreadLocal<>();
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
     * 打包区块时批量执行信息
     */
    private BatchInfo batchInfo;

    /**
     * 验证区块时批量执行信息
     */
    private BatchInfo verifyBatchInfo;

    /**
     * 向合约模块注册接口提供给合约来调用
     */
    private Map<String, CmdRegister> cmdRegisterMap = new ConcurrentHashMap<>();

    /**
     * 合约资产ID缓存图
     */
    private Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = new HashMap<>();
    /**
     * 合约资产ID - 合约地址缓存图
     * key - chainId + "-" + assetId
     * value - contractAddress
     */
    private Map<String, String> tokenAssetsContractAddressInfoMap = new HashMap<>();

    public static void putCurrentThreadBlockType(Integer blockType) {
        currentThreadBlockType.set(blockType);
    }

    public static Integer currentThreadBlockType() {
        return currentThreadBlockType.get();
    }

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
        Integer blockType = currentThreadBlockType.get();
        if(blockType == null) {
            //Log.info("Empty blockType!");
            //throw new RuntimeException("Empty blockType!");
            return null;
        }
        if(blockType == BlockType.PACKAGE_BLOCK.type()) {
            return batchInfo;
        }
        if(blockType == BlockType.VERIFY_BLOCK.type()) {
            return verifyBatchInfo;
        }
        Log.error("Unkown blockType! - [{}]", blockType);
        //throw new RuntimeException(String.format("Empty blockType! - [%s]", blockType));
        return null;
    }

    public void setBatchInfo(BatchInfo batchInfo) {
        Integer blockType = currentThreadBlockType.get();
        if(blockType == null) {
            //Log.info("Setting value error. Empty blockType!");
            return;
        }
        if(blockType == BlockType.PACKAGE_BLOCK.type()) {
            this.batchInfo = batchInfo;
            return;
        }
        if(blockType == BlockType.VERIFY_BLOCK.type()) {
            this.verifyBatchInfo = batchInfo;
            return;
        }
        Log.error("Setting value error. Unkown blockType! - [{}]", blockType);
    }

    public Map<String, CmdRegister> getCmdRegisterMap() {
        return cmdRegisterMap;
    }

    public void setCmdRegisterMap(Map<String, CmdRegister> cmdRegisterMap) {
        this.cmdRegisterMap = cmdRegisterMap;
    }

    public Map<String, ContractTokenAssetsInfo> getTokenAssetsInfoMap() {
        return tokenAssetsInfoMap;
    }

    public void setTokenAssetsInfoMap(Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap) {
        this.tokenAssetsInfoMap = tokenAssetsInfoMap;
    }

    public Map<String, String> getTokenAssetsContractAddressInfoMap() {
        return tokenAssetsContractAddressInfoMap;
    }

    public void setTokenAssetsContractAddressInfoMap(Map<String, String> tokenAssetsContractAddressInfoMap) {
        this.tokenAssetsContractAddressInfoMap = tokenAssetsContractAddressInfoMap;
    }
}
