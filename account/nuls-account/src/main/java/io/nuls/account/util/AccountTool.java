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

package io.nuls.account.util;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.rpc.call.NetworkCall;
import io.nuls.tools.log.Log;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author: qinyifeng
 */
public class AccountTool {

    public static final int CREATE_MAX_SIZE = 100;

    public static Address newAddress(int chainId, String prikey) {
        ECKey key;
        try {
            key = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(prikey)));
        } catch (Exception e) {
            throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
        }
        return newAddress(chainId, key.getPubKey());
    }

    public static Address newAddress(int chainId, ECKey key) {
        return newAddress(chainId, key.getPubKey());
    }

    public static Address newAddress(int chainId, byte[] publicKey) {
        return new Address(chainId, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(publicKey));
    }

    public static Account createAccount(int chainId, String prikey) throws NulsException {
        ECKey key = null;
        if (StringUtils.isBlank(prikey)) {
            key = new ECKey();
        } else {
            try {
                key = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(prikey)));
            } catch (Exception e) {
                throw new NulsException(AccountErrorCode.PRIVATE_KEY_WRONG, e);
            }
        }
        Address address = new Address(chainId, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
        Account account = new Account();
        account.setChainId(chainId);
        account.setAddress(address);
        account.setPubKey(key.getPubKey());
        account.setPriKey(key.getPrivKeyBytes());
        account.setEncryptedPriKey(new byte[0]);
        account.setCreateTime(NetworkCall.getCurrentTimeMillis());
        account.setEcKey(key);
        return account;
    }

    public static Account createAccount(int chainId) throws NulsException {
        return createAccount(chainId, null);
    }

    /**
     * 创建智能合约地址
     * Create smart contract address
     *
     * @param chainId
     * @return
     */
    public static Address createContractAddress(int chainId) {
        ECKey key = new ECKey();
        return new Address(chainId, BaseConstant.CONTRACT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
    }

    //    /**
//     * Generate the corresponding account management private key or transaction private key according to the seed private key and password
//     */
    public static BigInteger genPrivKey(byte[] encryptedPriKey, byte[] pw) {
        byte[] privSeedSha256 = Sha256Hash.hash(encryptedPriKey);
        //get sha256 of encryptedPriKey and  sha256 of pw，
        byte[] pwSha256 = Sha256Hash.hash(pw);
        //privSeedSha256 + pwPwSha256
        byte[] pwPriBytes = new byte[privSeedSha256.length + pwSha256.length];
        for (int i = 0; i < pwPriBytes.length; i += 2) {
            int index = i / 2;
            pwPriBytes[index] = privSeedSha256[index];
            pwPriBytes[index + 1] = pwSha256[index];
        }
        //get prikey
        return new BigInteger(1, Sha256Hash.hash(pwPriBytes));
    }

    public static byte[] createMultiSigAccountOriginBytes(int chainId, int n, List<String> pubKeys) throws NulsException {
        byte[] result = null;
        if (n < 2 || (pubKeys == null ? 0 : pubKeys.size()) < n) {
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        HashSet<String> hashSet = new HashSet(pubKeys);
        List<String> list = new ArrayList<>();
        list.addAll(hashSet);
        if (pubKeys.size() < n) {
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        Collections.sort(list, AccountConstant.PUBKEY_COMPARATOR);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(chainId);
            byteArrayOutputStream.write(n);
            for (String pubKey : pubKeys) {
                byteArrayOutputStream.write(HexUtil.decode(pubKey));
            }
            result = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (Exception e) {
            }
        }
        return  result;
    }






}
