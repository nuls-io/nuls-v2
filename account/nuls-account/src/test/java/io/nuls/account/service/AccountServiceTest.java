package io.nuls.account.service;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountParam;
import io.nuls.account.AccountBootstrap;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.thread.TimeService;
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
    protected int chainId = 12345;
    protected String password = "nuls123456";

    @BeforeClass
    public static void beforeTest() {
        //初始化配置
        AccountBootstrap.initCfg();
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());
        //springLite容器初始化
        SpringLiteContext.init("io.nuls.account", new ModularServiceMethodInterceptor());
        //启动时间同步线程
        TimeService.getInstance().start();
        accountService = SpringLiteContext.getBean(AccountService.class);
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
        List<Account> result = accountService.createAccount(chainId, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(account.isEncrypted());
        // set password
        String password = "abc12345890987";
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
        List<Account> result = accountService.createAccount(chainId, 1, null);
        assertTrue(result != null && result.size() == 1);
        Account account = result.get(0);
        assertFalse(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
        // set password
        String password = "abc12345890987";
        accountService.setPassword(chainId, account.getAddress().getBase58(), password);
        assertTrue(accountService.isEncrypted(chainId, account.getAddress().getBase58()));
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
        List<Account> accountList = accountService.createAccount(chainId, count, null);
        //Checking the number of accounts returned
        assertEquals(accountList.size(), count);
        for(Account account : accountList){
            System.out.println(account.getAddress().getBase58());
        }
        try {
            //Test the largest number of generated accounts.
            accountList = accountService.createAccount(chainId, 101, password);
            assertNull(accountList);
        } catch (NulsRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Remove specified account test
     */
    @Test
    public void removeAccounTest() {
        //create account
        List<Account> accountList = accountService.createAccount(chainId, 2, password);
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
        List<Account> accountList = accountService.createAccount(chainId, 1, password);
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
        List<Account> accouts = accountService.createAccount(chainId, 1, password);
        //check whether the accounts are equal in number.
        accountList = accountService.getAccountList();
        int newSize = accountList.size();
        assertEquals(newSize, oldSize + accouts.size());
    }

    @Test
    public void getAllPrivateKeyTest() {
        try {
            List<Account> accountList = accountService.createAccount((short) 1, 1, password);
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
            List<Account> accountList = accountService.createAccount(chainId, 1, password);
            //Query specified account private key
            String unencryptedPrivateKey = accountService.getPrivateKey(chainId, accountList.get(0).getAddress().getBase58(), password);
            assertNotNull(unencryptedPrivateKey);

            //Create account without password
            List<Account> accountNoPwdList = accountService.createAccount(chainId, 1, null);
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
            List<Account> accountList = accountService.createAccount(chainId, 1, password);
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
            List<Account> accountList = accountService.createAccount(chainId, 1, password);
            String address = accountList.get(0).getAddress().getBase58();
            byte[] addressBytes = accountList.get(0).getAddress().getAddressBytes();

            //创建一笔设置别名的交易
            AliasTransaction tx = new AliasTransaction();
            tx.setTime(TimeService.currentTimeMillis());
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
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));

            //测试密码正确
            P2PHKSignature signature=accountService.signDigest(tx.getHash().getDigestBytes(), chainId, address, password);
            assertNotNull(signature);

            //测试密码不正确
            try {
                accountService.signDigest(tx.getHash().getDigestBytes(), chainId, address, password + "error");
            } catch (NulsException ex) {
                assertEquals(AccountErrorCode.PASSWORD_IS_WRONG.getCode(), ex.getErrorCode().getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
