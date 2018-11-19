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
import io.nuls.account.constant.AccountParam;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

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
        //读取配置文件，数据存储根目录，初始化打开该目录下包含的表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());
        try {
            RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            }
        }
    }

    /**
     * get the list of aliaspo
     *
     * @return the aliaspo list
     */
    @Override
    public List<AliasPo> getAliasList() {
        List<AliasPo> aliasPoList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS);
            if (list != null) {
                for (byte[] value : list) {
                    AliasPo aliasPo = new AliasPo();
                    //将byte数组反序列化为AccountPo返回
                    aliasPo.parse(value, 0);
                    aliasPoList.add(aliasPo);
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return aliasPoList;
    }

    /**
     * get AliasPo by alias
     *
     * @param alias the alias
     * @return AliasPo
     */
    @Override
    public AliasPo getAlias(String alias) {
        if (alias == null || "".equals(alias.trim())) {
            return null;
        }
        byte[] aliasBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, StringUtils.bytes(alias));
        if (null == aliasBytes) {
            return null;
        }
        AliasPo aliasPo = new AliasPo();
        try {
            //将byte数组反序列化为AliasPo返回
            aliasPo.parse(aliasBytes, 0);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return aliasPo;
    }


    /**
     * save the alias to db
     *
     * @param aliasPo
     * @return
     */
    @Override
    public boolean saveAlias(AliasPo aliasPo) {
        try {
            return RocksDBService.put(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, StringUtils.bytes(aliasPo.getAlias()), aliasPo.serialize());
        } catch (Exception e) {
            Log.error("",e);
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
    public boolean removeAlias(String alias) {
        try {
            return RocksDBService.delete(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS, StringUtils.bytes(alias));
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(AccountErrorCode.DB_DELETE_ERROR);
        }
    }

}
