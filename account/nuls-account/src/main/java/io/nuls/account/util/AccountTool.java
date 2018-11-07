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

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.base.data.Address;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.thread.TimeService;

import java.math.BigInteger;

/**
 * @author: qinyifeng
 */
public class AccountTool {

    public static final int CREATE_MAX_SIZE = 100;

    public static Address newAddress(ECKey key) throws NulsException {
        return newAddress(key.getPubKey());
    }

    public static Address newAddress(byte[] publicKey) throws NulsException {
        return null;
        //return new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(publicKey));
    }

    public static Account createAccount(String prikey) throws NulsException {
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
        //TODO
        //Address address = new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
        Address address = new Address((short)0,(byte)1,null);
        Account account = new Account();
        account.setEncryptedPriKey(new byte[0]);
        account.setAddress(address);
        account.setPubKey(key.getPubKey());
        account.setEcKey(key);
        account.setPriKey(key.getPrivKeyBytes());
        account.setCreateTime(TimeService.currentTimeMillis());
        return account;
    }

    public static Account createAccount() throws NulsException {
        return createAccount(null);
    }

    public static Address createContractAddress() throws NulsException {
        ECKey key = new ECKey();
        return null;
        //TODO
        //return new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.CONTRACT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
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

    /**
     *  Check the difficulty of the password
     *  length between 8 and 20, the combination of characters and numbers
     *
     * @return boolean
     */
    public static boolean validPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        if (password.matches("(.*)[a-zA-z](.*)")
                && password.matches("(.*)\\d+(.*)")
                && !password.matches("(.*)\\s+(.*)")
                && !password.matches("(.*)[\u4e00-\u9fa5\u3000]+(.*)")) {
            return true;
        } else {
            return false;
        }
    }
}
