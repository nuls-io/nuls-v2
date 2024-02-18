package io.nuls.consensus.storage;

import io.nuls.base.data.NulsHash;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * Entrust information storage management
 * Delegated Information Storage Management
 *
 *
 * @author tag
 * 2018/11/6
 * */
public interface DepositStorageService {
    /**
     * Storage delegation information
     * Storage delegate information
     *
     * @param depositPo Entrusted object/deposit bean
     * @param chainID   chainID/chain id
     * @return  boolean
     * */
    boolean save(DepositPo depositPo,int chainID);

    /**
     * Obtain commission information
     * Get delegate information
     *
     * @param hash      Entrusted transactionHASH/deposit hash
     * @param chainID   chainID/chain id
     * @return DepositPo
     * */
    DepositPo get(NulsHash hash,int chainID);

    /**
     * Delete delegation information
     * Delete delegation information
     *
     * @param hash     Entrusted transactionHASH/deposit transaction hash
     * @param chainID  chainID/chain id
     * @return   boolean
     * */
    boolean delete(NulsHash hash,int chainID);

    /**
     * Obtain a list of delegation information
     * Get a list of delegation information
     *
     * @param chainID  chainID/chain id
     * @return List<DepositPo>
     * @exception
     * */
    List<DepositPo> getList(int chainID) throws NulsException;

    /**
     * Obtain the length of delegation information
     * Get the length of delegation information
     *
     * @param chainID  chainID/chain id
     * @return int
     **/
    int size(int chainID);
}
