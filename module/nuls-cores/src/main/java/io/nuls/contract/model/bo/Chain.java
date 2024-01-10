package io.nuls.contract.model.bo;

import io.nuls.common.ConfigBean;
import io.nuls.contract.enums.BlockType;
import io.nuls.contract.manager.ContractTxCreateUnconfirmedManager;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramExecutor;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain information class
 * Chain information class
 *
 * @author: PierreLuo
 * @date: 2019-02-26
 */
public class Chain {

    /**
     * Packaging blocks - 0, Verify Block - 1
     */
    private static ThreadLocal<Integer> currentThreadBlockType = new ThreadLocal<>();
    /**
     * Chain basic configuration information
     * Chain Foundation Configuration Information
     */
    private ConfigBean config;

    /**
     * Smart contract executor
     */
    private ProgramExecutor programExecutor;

    /**
     * Some configuration information for smart contract executors
     */
    private CommonConfig commonConfig;
    private DefaultConfig defaultConfig;

    /**
     * Smart contract creation contract unconfirmed transaction manager
     */
    private ContractTxCreateUnconfirmedManager contractTxCreateUnconfirmedManager;

    /**
     * Batch execution information when packaging blocks
     */
    private BatchInfo batchInfo;

    /**
     * Batch execution information during block validation
     */
    private BatchInfo verifyBatchInfo;

    /**
     * Batch execution information when packaging blocks(version8And above)
     */
    private BatchInfoV8 batchInfoV8;
    /**
     * Batch execution information during block validation(version8And above)
     */
    private BatchInfoV8 verifyBatchInfoV8;

    /**
     * Register an interface with the contract module to provide it to the contract for calling
     */
    private Map<String, CmdRegister> cmdRegisterMap = new ConcurrentHashMap<>();

    /**
     * Contract assetsIDCache Graph
     */
    private Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = new HashMap<>();
    /**
     * Contract assetsID - Contract Address Cache Map
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

    public void clearOldBatchInfo() {
        Log.info("clear Old BatchInfo.");
        this.batchInfo = null;
        this.verifyBatchInfo = null;
    }

    public void clearBatchInfo() {
        Log.info("clear BatchInfo.");
        this.batchInfoV8 = null;
        this.verifyBatchInfoV8 = null;
    }

    public BatchInfoV8 getBatchInfoV8() {
        Integer blockType = currentThreadBlockType.get();
        if(blockType == null) {
            return null;
        }
        if(blockType == BlockType.PACKAGE_BLOCK.type()) {
            return batchInfoV8;
        }
        if(blockType == BlockType.VERIFY_BLOCK.type()) {
            return verifyBatchInfoV8;
        }
        Log.error("Unkown blockType! - [{}]", blockType);
        return null;
    }

    public void setBatchInfoV8(BatchInfoV8 batchInfo) {
        Integer blockType = currentThreadBlockType.get();
        if(blockType == null) {
            return;
        }
        if(blockType == BlockType.PACKAGE_BLOCK.type()) {
            this.batchInfoV8 = batchInfo;
            return;
        }
        if(blockType == BlockType.VERIFY_BLOCK.type()) {
            this.verifyBatchInfoV8 = batchInfo;
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
