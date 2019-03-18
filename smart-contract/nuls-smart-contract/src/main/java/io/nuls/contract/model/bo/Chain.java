package io.nuls.contract.model.bo;

import io.nuls.base.data.BlockHeader;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.manager.TempBalanceManager;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.model.dto.ContractPackageDto;
import io.nuls.contract.vm.program.ProgramExecutor;
import lombok.Getter;
import lombok.Setter;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;

/**
 * 链信息类
 * Chain information class
 *
 * @author: PierreLuo
 * @date: 2019-02-26
 */
@Getter
@Setter
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
     * 批量执行信息
     */
    private BatchInfo batchInfo = new BatchInfo();

    public TempBalanceManager getTempBalanceManager() {
        return batchInfo.getTempBalanceManager();
    }

    public void setTempBalanceManager(TempBalanceManager tempBalanceManager) {
        batchInfo.setTempBalanceManager(tempBalanceManager);
    }

    public BlockHeader getCurrentBlockHeader() {
        return batchInfo.getCurrentBlockHeader();
    }

    public void setCurrentBlockHeader(BlockHeader currentBlockHeader) {
        batchInfo.setCurrentBlockHeader(currentBlockHeader);
    }

    public ContractPackageDto getContractPackageDto() {
        return batchInfo.getContractPackageDto();
    }

    public void setContractPackageDto(ContractPackageDto contractPackageDto) {
        batchInfo.setContractPackageDto(contractPackageDto);
    }


    public int getChainId() {
        return config.getChainId();
    }

}
