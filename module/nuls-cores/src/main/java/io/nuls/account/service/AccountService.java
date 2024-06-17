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
 */

package io.nuls.account.service;

import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.bo.Chain;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * Account module provides external service interface definitions
 * account service definition
 *
 * @author: qinyifeng
 */
public interface AccountService {

    /**
     * Create a specified number of accounts（Include address）
     * Create a specified number of accounts,and encrypt the accounts,
     * all the accounts are encrypted by the same password
     * if the password is NULL or "", the accounts will be unencrypted.
     *
     * @param chain    chainID
     * @param count    Number of accounts you want to create
     * @param count    the number of account you want to create.
     * @param password the password of the accounts.
     * @return the account list created.
     */
    List<Account> createAccount(Chain chain, int count, String password);

    /**
     * Obtain complete account information based on the account address string
     * Query account by address.
     *
     * @param chainId chainID
     * @param address the address of the account you want to query.
     * @return the account.
     */
    Account getAccount(int chainId, String address);

    /**
     * Is the account on the contract whitelist
     * @param chainId
     * @param address
     * @return
     */
    boolean validationWhitelistForTransferOnContractCall(int chainId, String address);

    /**
     * Get all account collections,And put it in cache
     * Query all account collections and put them in cache.
     *
     * @return account list of all accounts.
     */
    List<Account> getAccountList();

    /**
     * Get a collection of all accounts in the specified chain
     * Gets all account sets in the specified chain
     *
     * @param chainId
     * @return
     */
    List<Account> getAccountListByChain(int chainId);

    /**
     * Set account password
     * set the password for exist account
     *
     * @param chainId
     * @param address
     * @param password
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    boolean setPassword(int chainId, String address, String password);

    /**
     * Change the account password based on the original password
     * Change the account password according to the current password
     *
     * @param chainId
     * @param address
     * @param oldPassword
     * @param newPassword
     * @return
     */
    boolean changePassword(int chainId, String address, String oldPassword, String newPassword);

    /**
     * Set offline account password
     * set the password for offline account
     *
     * @param chainId
     * @param address
     * @param priKey
     * @param password
     * @return encryptedPriKey
     * <p>
     */
    String setOfflineAccountPassword(int chainId, String address, String priKey, String password);

    /**
     * Modify the offline account password based on the original password
     * Change offline account password according to current password
     *
     * @param chainId
     * @param address
     * @param priKey
     * @param oldPassword
     * @param newPassword
     * @return
     */
    String changeOfflinePassword(int chainId, String address, String priKey, String oldPassword, String newPassword);

    /**
     * Verify if the account is encrypted
     * check if the account is encrypted
     *
     * @param chainId
     * @param address
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    boolean isEncrypted(int chainId, String address);

    /**
     * Remove specified account
     * Remove specified account
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    boolean removeAccount(int chainId, String address, String password);

    /**
     * Set notes for the account
     * Set remark for accounts
     *
     * @param chainId
     * @param address
     * @param remark
     * @return
     */
    boolean setRemark(int chainId, String address, String remark);

    /**
     * Obtain account private key, only return encrypted account private key, do not return unencrypted account
     * Get the account private key,Only returns the private key of the encrypted account, and the unencrypted account does not return.
     * HexUtil.encode(priKeyBytes)
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    String getPrivateKey(int chainId, String address, String password);

    /**
     * Obtain account private key, only return encrypted account private key, do not return unencrypted account
     * Get the account private key,Only returns the private key of the encrypted account, and the unencrypted account does not return.
     * HexUtil.encode(priKeyBytes)
     *
     * @param chainId
     * @param account
     * @param password
     * @return
     */
    String getPrivateKey(int chainId, Account account, String password);


    /**
     * Obtain account public key, only return encrypted account public key, do not return unencrypted account
     * Get the account public key,Only returns the public key of the encrypted account, and the unencrypted account does not return.
     * HexUtil.encode(pubKeyBytes)
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    String getPublicKey(int chainId, String address, String password);

    /**
     * To obtain all local account private keys, it is necessary to ensure that all account passwords are consistent,
     * If the passwords in the local account are inconsistent, an error message will be returned
     * Get the all local private keys
     * If the password in the local account is different, the error message will be returned.
     *
     * @param chainId
     * @param password
     * @return
     */
    List<String> getAllPrivateKey(int chainId, String password);

    /**
     * Import account based on private key and password
     * import an account from plant private key and encrypt the account.
     *
     * @param chain
     * @param prikey
     * @param password
     * @param overwrite
     * @return
     * @throws NulsException
     */
    Account importAccountByPrikey(Chain chain, String prikey, String password, boolean overwrite) throws NulsException;

    /**
     * fromkeyStoreImport account(Password used for verificationkeystore)
     * 1.fromkeyStoreObtain plaintext private key(If there is no plaintext private key,Then use a password to access thekeyStoreMiddleencryptedPrivateKeySolve it out)
     * 2.adoptkeyStoreCreate a new account,Encrypted account
     * 3.Search the alias of this account from the database,If not found, do not set(Aliases are not derived fromkeyStoreObtain from,Because it may be changed)
     * 4.Save Account
     * import an account form account key store.
     *
     * @param keyStore  the keyStore of the account.
     * @param chain
     * @param password  the password of account
     * @param overwrite
     * @return the result of the operation.
     * @throws NulsException
     */
    Account importAccountByKeyStore(AccountKeyStore keyStore, Chain chain, String password, boolean overwrite) throws NulsException;


    void importAccountListByKeystore(List<AccountKeyStore> keyStoreList, Chain chain) throws NulsException;

    /**
     * Data Summary Signature
     * sign digest data
     *
     * @param digest   data digest.
     * @param chainId
     * @param address  address of account.
     * @param password password of account.
     * @return the signData byte[].
     * @throws NulsException nulsException
     */
    P2PHKSignature signDigest(byte[] digest, int chainId, String address, String password) throws NulsException;

    /**
     * Block Data Summary Signature
     * block sign digest data
     *
     * @param digest   data digest.
     * @param chainId
     * @param address  address of account.
     * @param password password of account.
     * @return the signData byte[].
     * @throws NulsException nulsException
     */
    BlockSignature signBlockDigest(byte[] digest, int chainId, String address, String password) throws NulsException;
}
