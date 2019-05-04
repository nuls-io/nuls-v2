package io.nuls.poc.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 委托信息存储管理
 * Delegated Information Storage Management
 *
 *
 * @author tag
 * 2018/11/6
 * */
public interface DepositStorageService {
    /**
     * 存储委托信息
     * Storage delegate information
     *
     * @param depositPo 委托对象/deposit bean
     * @param chainID   链ID/chain id
     * @return  boolean
     * */
    boolean save(DepositPo depositPo,int chainID);

    /**
     * 获取委托信息
     * Get delegate information
     *
     * @param hash      委托交易HASH/deposit hash
     * @param chainID   链ID/chain id
     * @return DepositPo
     * */
    DepositPo get(NulsDigestData hash,int chainID);

    /**
     * 删除委托信息
     * Delete delegation information
     *
     * @param hash     委托交易HASH/deposit transaction hash
     * @param chainID  链ID/chain id
     * @return   boolean
     * */
    boolean delete(NulsDigestData hash,int chainID);

    /**
     * 获取委托信息列表
     * Get a list of delegation information
     *
     * @param chainID  链ID/chain id
     * @return List<DepositPo>
     * @exception
     * */
    List<DepositPo> getList(int chainID) throws NulsException;

    /**
     * 获取委托信息长度
     * Get the length of delegation information
     *
     * @param chainID  链ID/chain id
     * @return int
     **/
    int size(int chainID);
}
