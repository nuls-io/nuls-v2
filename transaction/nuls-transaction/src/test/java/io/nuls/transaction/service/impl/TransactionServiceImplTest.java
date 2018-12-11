package io.nuls.transaction.service.impl;

import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.rocksdb.storage.LanguageStorageService;
import io.nuls.transaction.db.rocksdb.storage.impl.LanguageStorageServiceImpl;
import io.nuls.transaction.init.TransactionBootStrap;
import io.nuls.transaction.model.dto.CoinDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TransactionServiceImplTest {

    /**
     * RsPSmnARhBFh3ueSnv3wJDisbvxC83930
     * Qar8HtENUvhf2DmbZtYJcFxeVmNsC3930
     * LUfvVN5DFay5TJ3UPZrGQ85xdGVtm3930
     * HcQ97xm5gdJz5PcDfN47ddfYZxeiR3930
     * TY5UtmrN17tMgFVHoFbeM1zoDnNxF3930
     * VCTNxnWdRtnodJvsuiauWaSfdc1BN3930
     * MQCbukaeK22w8rAy355NcVU4yz9hE3930
     * KXUoxve8142puKYLUMq2p4suUJ7zy3930
     * NJLo16NgDoPEjnzU36uxpbu2huqYq3930
     * PGkq3ZJiLbUhhm4g6YdvoTQAfvCnd3930
     * pwd:nuls123456 chainId:12345
     */

    static List<CoinDTO> froms = new ArrayList<>();
    static List<CoinDTO> tos = new ArrayList<>();

    static List<CoinFrom> coinFroms = new ArrayList<>();
    static List<CoinTo> coinTos = new ArrayList<>();

    @Autowired
    TransactionServiceImpl transactionServiceImpl = new TransactionServiceImpl();
    @Before
    public void setUp() throws Exception {
        //初始化上下文
        //SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        /*try {
            LanguageStorageServiceImpl languageService = new LanguageStorageServiceImpl();
            String languageDB = (String) languageService.getLanguage();
            I18nUtils.loadLanguage("languages","");
            String language = null == languageDB ? I18nUtils.getLanguage() : languageDB;
            I18nUtils.setLanguage(language);
            if (null == languageDB) {
                languageService.saveLanguage(language);
            }
        }catch (Exception e){
            Log.error(e);
        }*/



        CoinDTO coin1 = new CoinDTO();
        coin1.setAddress("Q9TwCWfw3uhW11mHLUExT48e8pb9s3930");
        coin1.setAmount(new BigInteger("1"));
        coin1.setAssetsChainId(12345);
        coin1.setAssetsId(1);
        coin1.setPassword("nuls123456");

        CoinDTO coin2 = (CoinDTO)coin1.clone();
        coin2.setAddress("Qar8HtENUvhf2DmbZtYJcFxeVmNsC3930");
        coin2.setAmount(new BigInteger("200"));
        coin2.setAssetsChainId(22222);
        coin2.setAssetsId(2);

        CoinDTO coin3 = (CoinDTO)coin2.clone();
        coin3.setAddress("LUfvVN5DFay5TJ3UPZrGQ85xdGVtm3930");
        coin3.setAmount(new BigInteger("100"));
        coin3.setAssetsChainId(33333);
        coin3.setAssetsId(3);

        CoinDTO coin4 = new CoinDTO();
        coin4.setAddress("Q9TwCWfw3uhW11mHLUExT48e8pb9s3930");
        coin4.setAmount(new BigInteger("100"));
        coin4.setAssetsChainId(22222);
        coin4.setAssetsId(2);
        coin4.setPassword("nuls123456");

        froms.add(coin4);
        froms.add(coin2);
        froms.add(coin3);
        froms.add(coin1);

        CoinDTO coin5 = (CoinDTO)coin4.clone();
        coin5.setAddress("Qar8HtENUvhf2DmbZtYJcFxeVmNsC3930");
        coin5.setAmount(new BigInteger("300"));
        coin5.setAssetsChainId(22222);
        coin5.setAssetsId(2);

        CoinDTO coin6 = (CoinDTO)coin4.clone();
        coin6.setAddress("Qar8HtENUvhf2DmbZtYJcFxeVmNsC3930");
        coin6.setAmount(new BigInteger("100"));
        coin6.setAssetsChainId(33333);
        coin6.setAssetsId(2);

        tos.add(coin5);
        tos.add(coin6);
        //transactionService = SpringLiteContext.getBean(TransactionService.class);
    }


   /* @Test
    public void assemblyCoinFrom() throws NulsException {
        coinFroms = transactionServiceImpl.assemblyCoinFrom(TxConstant.CURRENT_CHAINID,froms);
        Assert.assertTrue(coinFroms.size() > 0);
    }

    @Test
    public void assemblyCoinTo() throws NulsException  {
        coinTos = transactionServiceImpl.assemblyCoinTo(tos);
        Assert.assertTrue(coinTos.size() > 0);
    }

    @Test
    public void validateCoinFrom() throws NulsException {
        assemblyCoinFrom();
        Result result = transactionServiceImpl.validateCoinFrom(TxConstant.CURRENT_CHAINID, coinFroms);
        System.out.println(result.getErrorCode().getCode() + " : " + result.getErrorCode().getMsg());
        Assert.assertTrue(result.isSuccess());
    }*/

    @Test
    public void newTx() {
    }

    @Test
    public void register() {
    }

    @Test
    public void getTransaction() {
    }

    @Test
    public void createCrossTransaction() {
    }

    @Test
    public void crossTransactionValidator() {
    }

    @Test
    public void crossTransactionCommit() {
    }

    @Test
    public void crossTransactionRollback() {
    }


}