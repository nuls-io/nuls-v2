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

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.model.po.MultiSigAccountPo;
import io.nuls.account.rpc.call.EventCmdCall;
import io.nuls.account.service.*;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.log.LogUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.script.Script;
import io.nuls.base.script.ScriptBuilder;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.AESEncrypt;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.CryptoException;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: EdwardChan
 *
 * Dec.20th 2018
 *
 */
@Service
public class MultiSigAccountServiceImpl implements MultiSignAccountService {

    @Autowired
    private MultiSigAccountStorageService multiSigAccountStorageService;

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AccountKeyStoreService keyStoreService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public MultiSigAccount createMultiSigAccount(int chainId, List<String> pubKeys, int m) {
        MultiSigAccount multiSigAccount = null;
        try {
            Script redeemScript = ScriptBuilder.createNulsRedeemScript(m, pubKeys);
            Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
            MultiSigAccountPo multiSigAccountPo = new MultiSigAccountPo();
            multiSigAccountPo.setChainId(chainId);
            multiSigAccountPo.setAddress(address);
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : pubKeys) {
                list.add(HexUtil.decode(pubKey));
            }
            multiSigAccountPo.setPubKeyList(list);
            multiSigAccountPo.setM((byte) m);
            boolean result = this.multiSigAccountStorageService.saveAccount(multiSigAccountPo);
            if (result) {
                multiSigAccount = multiSigAccountPo.toAccount();
            }
        } catch (Exception e) {
            LogUtil.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

}
