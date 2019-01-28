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
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.model.po.MultiSigAccountPo;
import io.nuls.account.rpc.call.EventCmdCall;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.service.*;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.log.LogUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.*;
import io.nuls.base.script.Script;
import io.nuls.base.script.ScriptBuilder;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.AESEncrypt;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.CryptoException;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: EdwardChan
 * <p>
 * Dec.20th 2018
 */
@Service
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
            LogUtil.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public MultiSigAccount getMultiSigAccountByAddress(int chainId, String address) {
        MultiSigAccountPo multiSigAccountPo;
        MultiSigAccount multiSigAccount = null;
        try {
            multiSigAccountPo = multiSigAccountStorageService.getAccount(AddressTool.getAddress(address));
            if (multiSigAccountPo != null) {
                multiSigAccount = multiSigAccountPo.toAccount();
            }
        } catch (Exception e) {
            LogUtil.error("", e);
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
            LogUtil.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public boolean removeMultiSigAccount(int chainId, String address) {
        boolean result;
        try {
            byte[] addressBytes = AddressTool.getAddress(address);
            MultiSigAccountPo multiSigAccountPo = this.multiSigAccountStorageService.getAccount(addressBytes);
            if (multiSigAccountPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            Address addressObj = new Address(address);
            result = multiSigAccountStorageService.removeAccount(addressObj);
        } catch (Exception e) {
            LogUtil.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return result;
    }

    /**
     * @param address  多签账户地址
     * @param signAddr 签名地址
     **/
    @Override
    public MultiSignTransactionResultDto setMultiAlias(int chainId, String address, String password, String aliasName, String signAddr) {
        MultiSignTransactionResultDto multiSignTransactionResultDto;
        try {
            Account account = accountService.getAccount(chainId, signAddr);
            MultiSigAccount multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, address);
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
            multiSignTransactionResultDto = transactionService.createSetAliasMultiSignTransaction(chainId, account, password, multiSigAccount, AddressTool.getStringAddressByBytes(AccountConstant.BLACK_HOLE_ADDRESS),aliasName , null);
        } catch (Exception e) {
            LogUtil.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
        }
        return multiSignTransactionResultDto;
    }

    private MultiSigAccount saveMultiSigAccount(int chainId, Address addressObj, List<String> pubKeys, int minSigns) {
        MultiSigAccount multiSigAccount = null;
        MultiSigAccountPo multiSigAccountPo = new MultiSigAccountPo();
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


    /**
     * 多签交易处理
     **/
    public void txMutilProcessing(MultiSigAccount multiSigAccount, Transaction tx, TransactionSignature transactionSignature) throws NulsException, IOException {
        //当已签名数等于M则自动广播该交易
        if (multiSigAccount.getM() == transactionSignature.getP2PHKSignatures().size()) {
            TransactionCmdCall.newTx(multiSigAccount.getChainId(), HexUtil.encode(tx.serialize()));
            // Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
//            if (saveResult.isFailed()) {
//                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
//                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
//                    Result rs = accountLedgerService.getMaxAmountOfOnce(fromAddr, tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
//                    if (rs.isSuccess()) {
//                        Na maxAmount = (Na) rs.getData();
//                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
//                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
//                    }
//                    return rs;
//                }
//                return saveResult;
//            }
//            transactionService.newTx(tx);
//            Result sendResult = transactionService.broadcastTx(tx);
//            if (sendResult.isFailed()) {
//                accountLedgerService.deleteTransaction(tx);
//                return sendResult;
//            }
//            return Result.getSuccess().setData(tx.getHash().getDigestHex());
        }
    }


}
