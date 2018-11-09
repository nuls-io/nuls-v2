package io.nuls.poc.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;

/**
 * 交易管理
 * @author  tag
 * 2018/11/6
 * */
public interface TransactionStorageService {

    /**
     * 保存交易
     * @param tx 交易对象
     * */
    boolean save(Transaction tx,int chainID);

    /**
     * 查询交易
     * @param hash 交易hash
     * @return 交易对象
     * */
    Transaction get(NulsDigestData hash,int chainID);

    /**
     * 删除交易
     * @param hash 交易hash
     * */
    boolean delete(NulsDigestData hash,int chainID);

    /**
     * 获取轮询数据库时在下标
     * @return  下标
     * */
    //int getStartIndex();

    /**
     * 轮询数据库
     * @return 交易对象
     * */
    //Transaction poll();
}
