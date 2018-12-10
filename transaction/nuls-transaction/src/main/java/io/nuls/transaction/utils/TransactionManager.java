/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.transaction.utils;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.TxRegister;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易管理类，存储管理交易注册的基本信息
 * @author: Charlie
 * @date: 2018/11/22
 */
public class TransactionManager {
    /**
     * 交易注册信息
     */
    private static final Map<Integer, TxRegister> TX_REGISTER_MAP = new HashMap<>();

    private static final TransactionManager INSTANCE = new TransactionManager();

    public static TransactionManager getInstance(){
        return INSTANCE;
    }


    private TransactionManager() {
        //TODO 注册跨链交易
        TxRegister txRegister = new TxRegister();
        txRegister.setModuleCode(TxConstant.MODULE_CODE);
        txRegister.setModuleValidator(TxConstant.TX_MODULE_VALIDATOR);
        txRegister.setTxType(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
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

    /**
     * 判断交易是系统交易
     * @param tx
     * @return
     */
    public boolean isSystemTx(Transaction tx){
        TxRegister txRegister =TX_REGISTER_MAP.get(tx.getType());
        return txRegister.getSystemTx();
    }

    /**
     * 验证交易
     * @param tx
     * @return
     */
    public Result verify(Transaction tx){
        //todo 调验证器
        BaseTxValidate(tx);
        TxRegister txRegister = this.getTxRegister(tx.getType());
        txRegister.getValidator();

        return null;
    }

    /**
     *  交易基础字段
     *  最大交易size
     *  交易类型
     *  交易签名
     *  交易手续费
     *
     */
    public Result BaseTxValidate(Transaction tx){

        if (null == tx) {
            return Result.getFailed(TxErrorCode.TX_NOT_EXIST);
        }
        if (tx.getHash() == null || tx.getHash().size() == 0 || tx.getHash().size() > TxConstant.TX_HASH_DIGEST_BYTE_MAX_LEN) {
            return Result.getFailed(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if(!contain(tx.getType())) {
            return Result.getFailed(TxErrorCode.TX_NOT_EFFECTIVE);
        }
        if (tx.getTime() == 0L) {
            return Result.getFailed(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if(tx.size() > TxConstant.TX_MAX_SIZE){
            return Result.getFailed(TxErrorCode.TX_SIZE_TOO_LARGE);
        }
        try {
            if(!SignatureUtil.validateTransactionSignture(tx)){
                return Result.getFailed(TxErrorCode.SIGNATURE_ERROR);
            }
        } catch (NulsException e) {
            e.printStackTrace();
            return Result.getFailed(e.getErrorCode());
        }
        return Result.getSuccess(TxErrorCode.SUCCESS);
    }


}
