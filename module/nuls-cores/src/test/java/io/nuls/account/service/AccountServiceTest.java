package io.nuls.account.service;

import io.nuls.account.AccountBootstrap;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.ByteUtils;
import io.nuls.sdk.core.utils.TimeService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/8
 */
public class AccountServiceTest {

    protected static AccountService accountService;
    protected static int chainId = 2;
    protected static int assetId = 1;
    protected String password = "nuls123456";

    private static Chain chain = new Chain();


    @BeforeClass
    public static void beforeTest() {
        //初始化配置
        SpringLiteContext.init("io.nuls", new ModularServiceMethodInterceptor());
        AccountBootstrap accountBootstrap = SpringLiteContext.getBean(AccountBootstrap.class);
        //初始化配置
        accountBootstrap.init();
//        启动时间同步线程
        TimeService.getInstance().start();
        accountService = SpringLiteContext.getBean(AccountService.class);
        chain.setConfig(new ConfigBean(chainId, assetId));
    }


    /**
     * Create a specified number of accounts
     */
    @Test
    public void createAccountTest() throws Exception {
        int count = 10;
        //Test to create an account that is not empty.
//        List<Account> accountList = accountService.createAccount(chainId, count, password);
//        //Checking the number of accounts returned
//        assertEquals(accountList.size(), count);
//        for(Account account : accountList){
//            System.out.println(account.getAddress().getBase58());
//        }
        //Test to create an empty password account
//        List<Account> accountList = accountService.createAccount(chain, count, null);
//        //Checking the number of accounts returned
//        assertEquals(accountList.size(), count);
//        for(Account account : accountList){
//            System.out.println(account.getAddress().getBase58());
//        }
        try {
            //Test the largest number of generated accounts.
            chain.setConfig(new ConfigBean(assetId, 5));
            List<Account> accountList = accountService.createAccount(chain, 6, password);
            for(Account acc : accountList){
                System.out.println(acc.getAddress().getBase58());
            }
        } catch (NulsRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * set password test
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void setPasswordTest() {
        // create account
        Chain chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId));
        List<Account> result = accountService.createAccount(chain, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(account.isEncrypted());
        // set password
        String password = "abc2890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        //get account
        Account accountAfterSetPassword = accountService.getAccount(chainId, account.getAddress().getBase58());
        //check if the account is encrypted
        assertNotNull(accountAfterSetPassword);
        assertTrue(accountAfterSetPassword.isEncrypted());
        assertTrue(account.validatePassword(password));
    }

    /**
     * the account is encrypted test
     * <p>
     * Nov.10th 2018
     *
     * @auther EdwardChan
     */
    @Test
    public void isEncryptedTest() {
        // create account
        List<Account> result = accountService.createAccount(chain, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
        // set password
        String password = "abc2890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        assertTrue(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
    }


    /**
     * Remove specified account test
     */
    @Test
    public void removeAccounTest() {
        //create account
        List<Account> accountList = accountService.createAccount(chain, 2, password);
        //query account
        Account account = accountService.getAccount(chainId, accountList.get(0).getAddress().getBase58());
        assertNotNull(account);
        //remove specified account
        boolean result = accountService.removeAccount(chainId, accountList.get(0).getAddress().getBase58(), password);
        assertTrue(result);
        //once again verify that accounts exist.
        account = accountService.getAccount(chainId, accountList.get(0).getAddress().getBase58());
        assertNull(account);
    }

    @Test
    public void getAccountTest() {
        //create account
        List<Account> accountList = accountService.createAccount(chain, 1, password);
        //query account
        Account account = accountService.getAccount(chainId, accountList.get(0).getAddress().getBase58());
        assertNotNull(account);
        assertEquals(accountList.get(0).getAddress().getBase58(), account.getAddress().getBase58());
    }

    @Test
    public void getAccountListTest() {
        //query all accounts
        List<Account> accountList = accountService.getAccountList();
        int oldSize = accountList.size();
        //create account
        List<Account> accouts = accountService.createAccount(chain, 1, password);
        //check whether the accounts are equal in number.
        accountList = accountService.getAccountList();
        int newSize = accountList.size();
        assertEquals(newSize, oldSize + accouts.size());
    }

    @Test
    public void getAllPrivateKeyTest() {
        try {
            List<Account> accountList = accountService.createAccount(chain, 1, password);
            //query all accounts privateKey
            List<String> privateKeyAllList = accountService.getAllPrivateKey((short) 0, password);
            accountService.getAllPrivateKey((short) 0, null);
            //query all accounts privateKey the specified chain
            List<String> privateKeyList = accountService.getAllPrivateKey((short) 1, password);
            assertTrue(privateKeyList.size() >= accountList.size());
            assertTrue(privateKeyAllList.size() >= privateKeyList.size());
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPrivateKeyTest() {
        try {
            //Create password accounts
            List<Account> accountList = accountService.createAccount(chain, 1, password);
            //Query specified account private key
            String unencryptedPrivateKey = accountService.getPrivateKey(chainId, accountList.get(0).getAddress().getBase58(), password);
            assertNotNull(unencryptedPrivateKey);

            //Create account without password
            List<Account> accountNoPwdList = accountService.createAccount(chain, 1, null);
            accountService.getPrivateKey(chainId, accountNoPwdList.get(0).getAddress().getBase58(), null);
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setRemarkTest() {
        try {
            String remark = "test remark";
            String errorRemark = "test error remark test error remark test error remark test error remark测试";
            //Create password accounts
            List<Account> accountList = accountService.createAccount(chain, 1, password);
            //Set the correct remarks for the account
            boolean result = accountService.setRemark(chainId, accountList.get(0).getAddress().getBase58(), remark);
            Account account = accountService.getAccount(chainId, accountList.get(0).getAddress().getBase58());
            assertTrue(result);
            assertEquals(remark, account.getRemark());
            //Set the correct remarks for the account
            result = accountService.setRemark(chainId, accountList.get(0).getAddress().getBase58(), "");
            assertTrue(result);
            //Set incorrect remarks for the account >60 bytes
            result = accountService.setRemark(chainId, accountList.get(0).getAddress().getBase58(), errorRemark);
            assertFalse(result);
        } catch (NulsRuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void signDigestTest() {
        try {
            //创建加密账户 create encrypted account
            List<Account> accountList = accountService.createAccount(chain, 1, password);
            String address = accountList.get(0).getAddress().getBase58();
            byte[] addressBytes = accountList.get(0).getAddress().getAddressBytes();

            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(System.currentTimeMillis()/1000);
            Alias alias = new Alias(addressBytes, "别名");
            tx.setTxData(alias.serialize());

//            CoinDataResult coinDataResult = accountLedgerService.getCoinData(addressBytes, AccountConstant.ALIAS_NA, tx.size() , TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
//            if (!coinDataResult.isEnough()) {
//                return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
//            }
            CoinData coinData = new CoinData();
            //coinData.setFrom(coinDataResult.getCoinList());
            CoinTo coin = new CoinTo();
            coin.setAddress(AddressTool.getAddress("Nse5FeeiYk1opxdc5RqYpEWkiUDGNuLs" + HexUtil.encode(ByteUtils.shortToBytes((short) chainId))));
            coin.setAmount(new BigInteger("1"));
            coin.setAssetsChainId(chainId);
            coin.setAssetsId(1);
            coinData.addTo(coin);

            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            //测试密码正确
            P2PHKSignature signature=accountService.signDigest(tx.getHash().getBytes(), chainId, address, password);
            assertNotNull(signature);

            //测试密码不正确
            try {
                accountService.signDigest(tx.getHash().getBytes(), chainId, address, password + "error");
            } catch (NulsException ex) {
                assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), ex.getErrorCode().getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
