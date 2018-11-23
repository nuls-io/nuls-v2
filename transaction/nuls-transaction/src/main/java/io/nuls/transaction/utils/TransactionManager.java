package io.nuls.transaction.utils;

import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.TxRegister;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易管理类，存储管理交易注册的基本信息
 * @author: Charlie
 * @date: 2018/11/22
 */
public class TransactionManager {
    private static final Map<Integer, TxRegister> TX_REGISTER_MAP = new HashMap<>();

    public static void init() throws NulsException {
        //TODO 注册跨链交易
    }

    //注册交易
    public static void register(TxRegister txRegister){
        //TODO 判断重复 模块，交易，然后注册
        if(!TX_REGISTER_MAP.containsKey(txRegister.getTxType())){
            TX_REGISTER_MAP.put(txRegister.getTxType(), txRegister);
        }
    }

    public static TxRegister getTxRegister(int type){
        return TX_REGISTER_MAP.get(type);
    }




}
