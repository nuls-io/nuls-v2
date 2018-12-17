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

package io.nuls.account.storage.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.base.basic.AddressTool;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author EdwardChan
 * @data Nov.9th 2018
 *
 * <p>
 * the operation about alias
 */
@Service
public class AliasStorageServiceImpl implements AliasStorageService, InitializingBean {

    /**
     * Initialize the db when the application boot up
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        //If tables do not exist, create tables.
        /*
        if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS)) {
            try {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS);
            } catch (Exception e) {
                if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                    Log.error(e.getMessage());
                    throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
                }
            }
        }
        */
    }

    /**
     * get the list of aliaspo
     *
     * @param chainId
     *
     * @return the aliaspo list
     */
    @Override
    public List<AliasPo> getAliasList(int chainId) {
        List<AliasPo> aliasPoList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId);
            if (list != null) {
                for (byte[] value : list) {
                    AliasPo aliasPo = new AliasPo();
                    //将byte数组反序列化为AccountPo返回
                    aliasPo.parse(value, 0);
                    aliasPoList.add(aliasPo);
                }
            }
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return aliasPoList;
    }

    /**
     * get AliasPo by chainId and alias
     * @param chainId
     * @param alias the alias
     * @return AliasPo
     */
    @Override
    public AliasPo getAlias(int chainId,String alias) {
        if (alias == null || "".equals(alias.trim())) {
            return null;
        }
        byte[] aliasBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId, StringUtils.bytes(alias));
        if (null == aliasBytes) {
            return null;
        }
        AliasPo aliasPo = new AliasPo();
        try {
            //将byte数组反序列化为AliasPo返回
            aliasPo.parse(aliasBytes, 0);
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return aliasPo;
    }

    @Override
    public AliasPo getAliasByAddress(int chainId, String address) {
        AliasPo aliasPo;
        if (!AddressTool.validAddress(chainId, address)) {
            Log.debug("the address is illegal,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        try {
            byte[] aliasBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADRESS + chainId, AddressTool.getAddress(address));
            if (null == aliasBytes) {
                return null;
            }
            aliasPo = new AliasPo();
            //将byte数组反序列化为AliasPo返回
            aliasPo.parse(aliasBytes, 0);
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR, e);
        }
        return aliasPo;
    }

    /**
     * save the alias to db
     * @param chainId
     * @param alias
     * @return
     */
    @Override
    public boolean saveAlias(int chainId, Alias alias) {
        AliasPo aliasPo = new AliasPo();
        aliasPo.setAlias(alias.getAlias());
        aliasPo.setAddress(alias.getAddress());
        aliasPo.setChainId(chainId);
        String tableNameKeyIsAlias = AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId;
        String tableNameKeyIsAddress = AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADRESS + chainId;
        boolean result = false;
        try {
            //check if the table is exist
            if (!RocksDBService.existTable(tableNameKeyIsAlias)) {
                result = RocksDBService.createTable(tableNameKeyIsAlias);
                if (!result) {
                    return false;
                }
            }
            if (!RocksDBService.existTable(tableNameKeyIsAddress)) {
                result = RocksDBService.createTable(tableNameKeyIsAddress);
                if (!result) {
                    return false;
                }
            }
            result = RocksDBService.put(tableNameKeyIsAlias, StringUtils.bytes(aliasPo.getAlias()), aliasPo.serialize());
            if (!result) {
                return false;
            }
            result = RocksDBService.put(tableNameKeyIsAddress, aliasPo.getAddress(), aliasPo.serialize());
            return result;
        } catch (Exception e) {
            Log.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_ERROR);
        }
    }

    /**
     * remove the alias from db
     * <p>
     * if the alias isn't exist in db,it wil return true
     *
     * @param alias
     * @return
     */

    @Override
    public boolean removeAlias(int chainId,String alias) {
        try {
            return RocksDBService.delete(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId, StringUtils.bytes(alias));
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.DB_DELETE_ERROR,e);
        }
    }



}
