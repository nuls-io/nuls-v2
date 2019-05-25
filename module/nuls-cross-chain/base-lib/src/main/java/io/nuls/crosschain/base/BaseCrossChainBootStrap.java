package io.nuls.crosschain.base;
import io.nuls.crosschain.base.model.bo.TxRegisterDetail;
import io.nuls.crosschain.base.model.dto.ModuleTxRegisterDTO;
import io.nuls.crosschain.base.rpc.call.TransactionCall;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModule;

import java.util.*;

import static io.nuls.crosschain.base.constant.CrossChainConstant.*;

/**
 * 跨链模块启动类
 * Cross Chain Module Startup and Initialization Management
 * @author tag
 * 2019/4/10
 */
public abstract class BaseCrossChainBootStrap extends RpcModule {
    private Set<String> rpcPaths = new HashSet<>(){{add(RPC_PATH);}};

    /**
     * 跨链模块默认需要注册的交易（跨链交易）
     * Transactions that cross-link modules need to register by default
     * */
    private TxRegisterDetail baseTxRegisterDetail = new TxRegisterDetail(TX_TYPE_CROSS_CHAIN, CROSS_TX_VALIDATOR);

    /**
     * 跨链模块向跨链交易注册的完整信息
     * Complete information of cross-chain module registering for cross-chain transactions
     * */
    private ModuleTxRegisterDTO moduleTxRegisterDTO = new ModuleTxRegisterDTO(ModuleE.CC.name,VALIDATOR,COMMIT,ROLLBACK);

    /**
     * 新增需要加入RPC的CMD所在目录
     * Add the directory where the CMD needs to be added to RPC
     * */
    protected void registerRpcPath(String rpcPath){
        rpcPaths.add(rpcPath);
    }

    /**
     * 注册本链跨链交易类型
     * Registered Chain Cross-Chain Transaction Types
     * */
    protected void registerCrossTxType(int crossTxType){
        baseTxRegisterDetail.setTxType(crossTxType);
    }

    /**
     * 向交易模块注册跨链模块交易
     * Register cross-chain module transactions with the transaction module
     * */
    protected void registerTx(List<TxRegisterDetail> txRegisterDetailList,int chainId){
        if(txRegisterDetailList != null && txRegisterDetailList.size()>0){
            moduleTxRegisterDTO.getList().addAll(txRegisterDetailList);
        }
        moduleTxRegisterDTO.getList().add(baseTxRegisterDetail);
        moduleTxRegisterDTO.setChainId(chainId);
        TransactionCall.registerTx(moduleTxRegisterDTO);
        moduleTxRegisterDTO.getList().clear();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CC.name,ROLE);
    }

    /**
     * 指定RpcCmd的包名
     * 可以不实现此方法，若不实现将使用spring init扫描的包
     * @return
     */
    @Override
    public Set<String> getRpcCmdPackage(){
        return rpcPaths;
    }

    protected Set<String> getRpcPaths() {
        return rpcPaths;
    }

    public void setRpcPaths(Set<String> rpcPaths) {
        this.rpcPaths = rpcPaths;
    }

    public ModuleTxRegisterDTO getModuleTxRegisterDTO() {
        return moduleTxRegisterDTO;
    }

    public void setModuleTxRegisterDTO(ModuleTxRegisterDTO moduleTxRegisterDTO) {
        this.moduleTxRegisterDTO = moduleTxRegisterDTO;
    }
}
