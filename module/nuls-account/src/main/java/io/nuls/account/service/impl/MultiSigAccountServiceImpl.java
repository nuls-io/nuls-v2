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

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.MultiSignTransactionResultDTO;
import io.nuls.account.model.po.MultiSigAccountPO;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.SerializeUtils;

import java.util.ArrayList;
import java.util.List;

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
    private AccountService accountService;
    @Autowired
    private MultiSignAccountService multiSignAccountService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private TransactionService transactionService;


    @Override
    public MultiSigAccount createMultiSigAccount(int chainId, List<String> pubKeys, int minSigns) {
        MultiSigAccount multiSigAccount = null;
        try {
            //Script redeemScript = ScriptBuilder.createNulsRedeemScript(m, pubKeys);
            Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(AccountTool.createMultiSigAccountOriginBytes(chainId, minSigns, pubKeys)));
            multiSigAccount = this.saveMultiSigAccount(chainId, address, pubKeys, minSigns);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public MultiSigAccount getMultiSigAccountByAddress(String address) {
        MultiSigAccountPO multiSigAccountPo;
        MultiSigAccount multiSigAccount = null;
        try {
            multiSigAccountPo = multiSigAccountStorageService.getAccount(AddressTool.getAddress(address));
            if (multiSigAccountPo != null) {
                multiSigAccount = multiSigAccountPo.toAccount();
            }
        } catch (Exception e) {
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public MultiSigAccount importMultiSigAccount(int chainId, String address, List<String> pubKeys, int minSigns) {
        //TODO 查询是否存在，如果存在则不能再次导入
        MultiSigAccount multiSigAccount;
        try {
            Address addressObj = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(AccountTool.createMultiSigAccountOriginBytes(chainId, minSigns, pubKeys)));
            if (!AddressTool.getStringAddressByBytes(addressObj.getAddressBytes()).equals(address)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            multiSigAccount = this.saveMultiSigAccount(chainId, addressObj, pubKeys, minSigns);
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public boolean removeMultiSigAccount(int chainId, String address) {
        boolean result;
        try {
            byte[] addressBytes = AddressTool.getAddress(address);
            MultiSigAccountPO multiSigAccountPo = this.multiSigAccountStorageService.getAccount(addressBytes);
            if (multiSigAccountPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            Address addressObj = new Address(address);
            result = multiSigAccountStorageService.removeAccount(addressObj);
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return result;
    }

    /**
     * @param address  多签账户地址
     * @param signAddr 签名地址
     **/
    @Override
    public MultiSignTransactionResultDTO setMultiAlias(Chain chain, String address, String password, String aliasName, String signAddr) {
        MultiSignTransactionResultDTO multiSignTransactionResultDto;
        try {
            Account account = accountService.getAccount(chain.getChainId(), signAddr);
            MultiSigAccount multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            if (null == multiSigAccount) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            if (account.isEncrypted() && account.isLocked()) {
                if (!account.validatePassword(password)) {
                    throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }
            multiSignTransactionResultDto = transactionService.createSetAliasMultiSignTransaction(chain, account, password, multiSigAccount,
                    AddressTool.getStringAddressByBytes(AddressTool.getAddress(NulsConfig.BLACK_HOLE_PUB_KEY, chain.getChainId())),aliasName , null);
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
        }
        return multiSignTransactionResultDto;
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
        boolean result = this.multiSigAccountStorageService.saveAccount(multiSigAccountPo);
        if (result) {
            multiSigAccount = multiSigAccountPo.toAccount();
        }
        return multiSigAccount;
    }

}
