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

package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.po.MultiSigAccountPO;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.SerializeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: EdwardChan
 * <p>
 * Dec.20th 2018
 */
@Component
public class MultiSigAccountServiceImpl implements MultiSignAccountService {
    @Autowired
    private MultiSigAccountStorageService multiSigAccountStorageService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MultiSignAccountService multiSignAccountService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionService transactionService;

    /**
     * 如果是地址则先获取账户信息得到原始公钥字符串
     * @param chainId
     * @param pubKeys
     * @return 返回的都必须是原始公钥字符串
     */
    private List<String> getOriginalPubKeys(int chainId, List<String> pubKeys){
        //for(String pubKey: pubKeys){
        for(int i=0; i < pubKeys.size();i++){
            String pubKey = pubKeys.get(i);
            if(AddressTool.validAddress(chainId, pubKey)) {
                if (AddressTool.isMultiSignAddress(pubKey)) {
                    //不能用多签地址创建多签账户
                    throw new NulsRuntimeException(AccountErrorCode.CONTRACT_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT);
                } else if (AddressTool.validContractAddress(AddressTool.getAddress(pubKey), chainId)) {
                    //不能用智能合约地址创建多签账户
                    throw new NulsRuntimeException(AccountErrorCode.MULTISIG_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT);
                } else if (AddressTool.validNormalAddress(AddressTool.getAddress(pubKey), chainId)) {
                    //合法地址
                    Account account = accountService.getAccount(chainId, pubKey);
                    if (account == null) {
                        //地址账户不存在
                        throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                    }
                    pubKeys.set(i, HexUtil.encode(account.getPubKey()));
                }
            }
        }
        return pubKeys;
    }

    @Override
    public MultiSigAccount createMultiSigAccount(Chain chain, List<String> pubKeys, int minSigns) throws NulsException {
        MultiSigAccount multiSigAccount = null;
        int chainId = chain.getChainId();
        //公钥参数允许传入原始公钥或者账户地址,如果公钥参数里面含有账户地址,则需要查询到该地址并获取原始公钥
        getOriginalPubKeys(chainId, pubKeys);
        //验证公钥是否重复
        Set<String> pubkeySet = new HashSet<>(pubKeys);
        if(pubkeySet.size() < pubKeys.size()){
           throw new NulsException(AccountErrorCode.PUBKEY_REPEAT);
        }
        Address address = null;
        try {
            address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(AddressTool.createMultiSigAccountOriginBytes(chainId, minSigns, pubKeys)));
        } catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(AccountErrorCode.CREATE_MULTISIG_ADDRESS_FAIL);
        }
        multiSigAccount = this.saveMultiSigAccount(chainId, address, pubKeys, minSigns);
        return multiSigAccount;
    }


    @Override
    public MultiSigAccount getMultiSigAccountByAddress(byte[] address) {
        MultiSigAccount multiSigAccount = null;
        try {
            MultiSigAccountPO multiSigAccountPo = multiSigAccountStorageService.getAccount(address);
            if (multiSigAccountPo != null) {
                multiSigAccount = multiSigAccountPo.toAccount();
            }
        } catch (Exception e) {
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public MultiSigAccount getMultiSigAccountByAddress(String address) {
        return getMultiSigAccountByAddress(AddressTool.getAddress(address));
    }

    @Override
    public boolean removeMultiSigAccount(int chainId, String address) {
        boolean result;
        try {
            byte[] addressBytes = AddressTool.getAddress(address);
            MultiSigAccountPO multiSigAccountPo = this.multiSigAccountStorageService.getAccount(addressBytes);
            if (multiSigAccountPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.MULTISIGN_ACCOUNT_NOT_EXIST);
            }
            Address addressObj = new Address(address);
            result = multiSigAccountStorageService.removeAccount(addressObj);
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return result;
    }

    private MultiSigAccount saveMultiSigAccount(int chainId, Address addressObj, List<String> pubKeys, int minSigns) {
        MultiSigAccount multiSigAccount = null;
        MultiSigAccountPO multiSigAccountPo = new MultiSigAccountPO();
        multiSigAccountPo.setChainId(chainId);
        multiSigAccountPo.setAddress(addressObj);
        List<byte[]> list = new ArrayList<>();
        for (String pubKey : pubKeys) {
            list.add(HexUtil.decode(pubKey));
        }
        multiSigAccountPo.setPubKeyList(list);
        multiSigAccountPo.setM((byte) minSigns);
        //加载别名数据(如果有)
        multiSigAccountPo.setAlias(aliasService.getAliasByAddress(chainId, addressObj.getBase58()));
        boolean result = this.multiSigAccountStorageService.saveAccount(multiSigAccountPo);
        if (result) {
            multiSigAccount = multiSigAccountPo.toAccount();
        }
        return multiSigAccount;
    }

}
