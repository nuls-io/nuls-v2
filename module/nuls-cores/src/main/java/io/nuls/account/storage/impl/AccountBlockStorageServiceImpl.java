/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.account.model.bo.tx.AccountBlockInfo;
import io.nuls.account.model.dto.AccountBlockDTO;
import io.nuls.account.model.po.AccountBlockExtendPO;
import io.nuls.account.model.po.AccountBlockPO;
import io.nuls.account.storage.AccountBlockStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rockdb.service.RocksDBService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: qinyifeng
 */
@Component
public class AccountBlockStorageServiceImpl implements AccountBlockStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() {
    }

    private Set<Integer> intArray2set(int[] _array) {
        Set<Integer> result = new HashSet<>();
        for (int a : _array) {
            result.add(Integer.valueOf(a));
        }
        return result;
    }

    private int[] set2intArray(Set<Integer> set) {
        int[] result = new int[set.size()];
        int i = 0;
        for (Integer s : set) {
            result[i++] = s.intValue();
        }
        return result;
    }

    @Override
    public boolean operateAccountList(List<AccountBlockDTO> accountList) throws Exception {
        if (null == accountList || accountList.size() == 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        List<AccountBlockPO> poList = new ArrayList<>();
        for (AccountBlockDTO dto : accountList) {
            byte[] address = dto.getAddress();
            AccountBlockInfo info = dto.getInfo();
            if (info == null) {
                // 账户完全锁定
                poList.add(new AccountBlockPO(address));
            } else {
                AccountBlockPO po = this.getAccount(address);
                if (po == null) {
                    // 首次添加白名单
                    poList.add(new AccountBlockPO(address, info));
                } else if (po.getExtend() == null) {
                    // 首次添加白名单
                    po.setExtend(new AccountBlockExtendPO(address, info).serialize());
                    poList.add(po);
                } else {
                    // 更新白名单
                    AccountBlockExtendPO extendPO = new AccountBlockExtendPO();
                    extendPO.parse(po.getExtend(), 0);
                    int[] types = extendPO.getTypes();
                    String[] contracts = extendPO.getContracts();
                    int operationType = info.getOperationType();
                    if (operationType == 1) {
                        // 添加白名单
                        int[] _types = info.getTypes();
                        if (_types != null) {
                            Set<Integer> typeSet = types == null ? new HashSet<>() : this.intArray2set(types);
                            typeSet.addAll(this.intArray2set(_types));
                            extendPO.setTypes(this.set2intArray(typeSet));
                        }
                        String[] _contracts = info.getContracts();
                        if (_contracts != null) {
                            Set<String> contractSet = contracts == null ? new HashSet<>() : new HashSet<>(Arrays.asList(contracts));
                            contractSet.addAll(Arrays.asList(_contracts));
                            extendPO.setContracts(contractSet.toArray(new String[contractSet.size()]));
                        }
                        po.setExtend(extendPO.serialize());
                        poList.add(po);
                    } else if (operationType == 2) {
                        // 移除白名单
                        int[] _types = info.getTypes();
                        if (_types != null) {
                            Set<Integer> typeSet = types == null ? new HashSet<>() : this.intArray2set(types);
                            typeSet.removeAll(this.intArray2set(_types));
                            extendPO.setTypes(this.set2intArray(typeSet));
                        }
                        String[] _contracts = info.getContracts();
                        if (_contracts != null) {
                            Set<String> contractSet = contracts == null ? new HashSet<>() : new HashSet<>(Arrays.asList(contracts));
                            contractSet.removeAll(Arrays.asList(_contracts));
                            extendPO.setContracts(contractSet.toArray(new String[contractSet.size()]));
                        }
                        po.setExtend(extendPO.serialize());
                        poList.add(po);
                    }
                }
            }
        }
        return this.saveAccountList(poList);
    }

    @Override
    public boolean cancelOperateAccountList(List<AccountBlockDTO> accountList) throws Exception {
        if (null == accountList || accountList.size() == 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        List<byte[]> accountRemoveList = new ArrayList<>();
        List<AccountBlockPO> poList = new ArrayList<>();
        for (AccountBlockDTO dto : accountList) {
            byte[] address = dto.getAddress();
            AccountBlockInfo info = dto.getInfo();
            if (info == null) {
                // 取消账户完全锁定
                accountRemoveList.add(address);
            } else {
                AccountBlockPO po = this.getAccount(address);
                if (po == null) {
                    continue;
                }
                // 取消更新白名单
                AccountBlockExtendPO extendPO = new AccountBlockExtendPO();
                extendPO.parse(po.getExtend(), 0);
                int[] types = extendPO.getTypes();
                String[] contracts = extendPO.getContracts();
                int operationType = info.getOperationType();
                if (operationType == 1) {
                    // 取消添加白名单
                    int[] _types = info.getTypes();
                    if (_types != null) {
                        Set<Integer> typeSet = types == null ? new HashSet<>() : this.intArray2set(types);
                        typeSet.removeAll(this.intArray2set(_types));
                        extendPO.setTypes(this.set2intArray(typeSet));
                    }
                    String[] _contracts = info.getContracts();
                    if (_contracts != null) {
                        Set<String> contractSet = contracts == null ? new HashSet<>() : new HashSet<>(Arrays.asList(contracts));
                        contractSet.removeAll(Arrays.asList(_contracts));
                        extendPO.setContracts(contractSet.toArray(new String[contractSet.size()]));
                    }
                    po.setExtend(extendPO.serialize());
                    poList.add(po);
                } else if (operationType == 2) {
                    // 取消移除白名单
                    int[] _types = info.getTypes();
                    if (_types != null) {
                        Set<Integer> typeSet = types == null ? new HashSet<>() : this.intArray2set(types);
                        typeSet.addAll(this.intArray2set(_types));
                        extendPO.setTypes(this.set2intArray(typeSet));
                    }
                    String[] _contracts = info.getContracts();
                    if (_contracts != null) {
                        Set<String> contractSet = contracts == null ? new HashSet<>() : new HashSet<>(Arrays.asList(contracts));
                        contractSet.addAll(Arrays.asList(_contracts));
                        extendPO.setContracts(contractSet.toArray(new String[contractSet.size()]));
                    }
                    po.setExtend(extendPO.serialize());
                    poList.add(po);
                }
            }
        }
        if (!accountRemoveList.isEmpty()) {
            RocksDBService.deleteKeys(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, accountRemoveList);
        }
        return this.saveAccountList(poList);
    }

    @Override
    public boolean saveAccountList(List<AccountBlockPO> accountPOList) {
        if (null == accountPOList || accountPOList.size() == 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> accountPoMap = new HashMap<>();
        try {
            for (AccountBlockPO po : accountPOList) {
                //序列化对象为byte数组存储
                accountPoMap.put(po.getAddress(), po.serialize());
            }
            return RocksDBService.batchPut(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, accountPoMap);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }
    @Override
    public boolean removeAccountList(List<String> accountList) {
        if (null == accountList || accountList.isEmpty()) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            List<byte[]> addressBytesList = accountList.stream().map(a -> AddressTool.getAddress(a)).collect(Collectors.toList());
            return RocksDBService.deleteKeys(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, addressBytesList);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_DELETE_ERROR);
        }
    }
    @Override
    public List<AccountBlockPO> getAccountList() {
        List<AccountBlockPO> accountPOList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK);
            if (list != null) {
                for (byte[] value : list) {
                    AccountBlockPO accountPo = new AccountBlockPO();
                    //将byte数组反序列化为AccountPo返回
                    accountPo.parse(value, 0);
                    accountPOList.add(accountPo);
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return accountPOList;
    }
    @Override
    public AccountBlockPO getAccount(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, address);
        if (null == accountBytes) {
            return null;
        }
        AccountBlockPO accountPo = new AccountBlockPO();
        try {
            //将byte数组反序列化为AccountPo返回
            accountPo.parse(accountBytes, 0);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return accountPo;
    }
    @Override
    public byte[] getAccountBytes(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, address);
        if (null == accountBytes) {
            return null;
        }
        return accountBytes;
    }

    @Override
    public boolean existAccount(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_BLOCK, address);
        if (null == accountBytes) {
            return false;
        }
        return true;
    }

}
