package io.nuls.transaction.utils;

import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.TxRegister;

import java.util.*;

/**
 * 交易管理类，存储管理交易注册的基本信息
 * @author: Charlie
 * @date: 2018/11/22
 */
public class TransactionManager {

    public static final TransactionManager INSTANCE = new TransactionManager();
    /**
     * 交易注册信息
     */
    private static final Map<Integer, TxRegister> TX_REGISTER_MAP = new HashMap<>();



    private TransactionManager() {
        //TODO 注册跨链交易
        TxRegister txRegister = new TxRegister();
        txRegister.setModuleCode(TxConstant.MODULE_CODE);
        txRegister.setModuleValidator(TxConstant.TX_MODULE_VALIDATOR);
        txRegister.setTxType(TxConstant.CROSS_TRANSFER_TYPE);
        txRegister.setValidator(TxConstant.CROSS_TRANSFER_VALIDATOR);
        txRegister.setCommit(TxConstant.CROSS_TRANSFER_COMMIT);
        txRegister.setRollback(TxConstant.CROSS_TRANSFER_ROLLBACK);
        txRegister.setSystemTx(true);
        txRegister.setUnlockTx(false);
        txRegister.setVerifySignature(true);
        register(txRegister);

    }


    //注册交易
    public boolean register(TxRegister txRegister){
        boolean rs = false;
        if(!TX_REGISTER_MAP.containsKey(txRegister.getTxType())){
            TX_REGISTER_MAP.put(txRegister.getTxType(), txRegister);
            rs = true;
        }
        return rs;
    }

    /**
     * 获取交易的注册对象
     * @param type
     * @return
     */
    public TxRegister getTxRegister(int type){
        return TX_REGISTER_MAP.get(type);
    }

    /**
     * 根据交易类型返回交易类型是否存在
     * @param type
     * @return
     */
    public boolean contain(int type){
        return TX_REGISTER_MAP.containsKey(type);
    }

    /**
     * 返回系统交易类型
      */
    public List<Integer> getSysTypes(){
        List<Integer> list = new ArrayList<>();
        for(Map.Entry<Integer, TxRegister> map : TX_REGISTER_MAP.entrySet()){
            if(map.getValue().getSystemTx()){
                list.add(map.getKey());
            }
        }
        return list;
    }


}
