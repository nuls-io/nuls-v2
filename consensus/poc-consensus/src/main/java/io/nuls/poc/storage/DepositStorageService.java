package io.nuls.poc.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.po.DepositPo;
import java.util.List;

/**
 * 委托信息管理
 * @author tag
 * 2018/11/6
 * */
public interface DepositStorageService {
    /**
     * 存储委托信息
     * @param depositPo 委托对象
     * */
    boolean save(DepositPo depositPo,int chainID);

    /**
     * 获取委托信息
     * @param hash  委托交易HASH
     * */
    DepositPo get(NulsDigestData hash,int chainID);

    /**
     * 删除委托信息
     * @param hash  委托交易HASH
     * */
    boolean delete(NulsDigestData hash,int chainID);

    /**
     * 获取委托信息列表
     * */
    List<DepositPo> getList(int chainID) throws  Exception;

    /**
     * 获取委托信息长度
     **/
    int size(int chainID);
}
