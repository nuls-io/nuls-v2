package io.nuls.cmd.client.test.account;

import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.cmd.client.CmdClientBootstrap;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.account.CreateProcessor;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:23
 * @Description: 功能描述
 */
public class TestCreateProcessor {

    AccountService accountService;

    @Before
    public void before(){
        ServiceManager.init(1, Provider.ProviderType.RPC);
        accountService = ServiceManager.get(AccountService.class);
        CmdClientBootstrap.main(new String[]{});
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreate(){
        CreateAccountReq req = new CreateAccountReq(1,"nuls123456");
        Result<String> res = accountService.createAccount(req);
        Assert.assertTrue(res.isSuccess());
        Assert.assertTrue(res.getList().size() == 1);
        res.getList().forEach(System.out::println);
    }

    @Test public void testCreateForCmd(){
        CreateProcessor cp = new CreateProcessor();
        CommandResult res = cp.execute(new String[0]);
        Assert.assertTrue(res.isSuccess());
        System.out.println(res.getMessage());
    }

    @Test
    public void testMain() throws IOException {
        int chainId = 1;
        int assetId = 1;
        String password = "nuls123456";
        BigInteger amount = BigInteger.valueOf(
                23000000000000L);
        String formAddress = "NULSd6HgZaT3KuXGuMomS1yGPTzMgedAMwjCF";
        String prikey = "f2559da1414960b6413938979109262272f9dbae81a704891f0db69edcbdbb30";
        AccountService accountService = ServiceManager.get(AccountService.class);
        accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(password,prikey,true));
        Map<String,String> ip = new HashMap<>();
        ip.put("nuls130","192.168.1.130");
        ip.put("nuls131","192.168.1.131");
        ip.put("nuls132","192.168.1.132");
        ip.put("nuls133","192.168.1.133");
        ip.put("nuls134","192.168.1.134");
        ip.put("nuls135","192.168.1.135");
        ip.put("nuls136","192.168.1.136");
        ip.put("nuls137","192.168.1.137");
        ip.put("nuls138","192.168.1.138");
        ip.put("nuls139","192.168.1.139");
        ip.put("nuls140","192.168.1.140");
        String file_path = System.getProperty("user.dir") + "/.temp/";
        File file = new File(file_path);
        if(!file.exists()){
            file.mkdir();
        }
        String addressPrefix = "NULS";
//        BufferedReader ca = new BufferedReader(new FileReader(new File(file_path + "cp")));
//        String line = ca.readLine();
        Map<String,String> cprikey = new HashMap<>();
//        while(line != null){
//            String[] ary = line.split("=");
//            cprikey.put(ary[0],ary[1]);
//            line = ca.readLine();
//        }
//        ca.close();
        BufferedWriter cp = new BufferedWriter(new FileWriter(new File(file_path + "cp")));
        BufferedWriter pp = new BufferedWriter(new FileWriter(new File(file_path + "pp")));
        BufferedWriter pa = new BufferedWriter(new FileWriter(new File(file_path + "pa")));
        BufferedWriter pascript = new BufferedWriter(new FileWriter(new File(file_path + "pascript")));
        BufferedWriter ccscript = new BufferedWriter(new FileWriter(new File(file_path + "ccscript")));
        BufferedWriter calias = new BufferedWriter(new FileWriter(new File(file_path + "calias")));

        pascript.write("#!/bin/bash");
        pascript.newLine();
        try{
            int count = ip.size();
            for (int i = 0; i < count; i++) {
                String id = String.format("nuls%02d",i+1);
                String nk = ip.get(id);
//                String agentPrikey = cprikey.get(nk);
                ECKey key = new ECKey();
                Address address = new Address(chainId, addressPrefix, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
                cp.write(nk + "=" + key.getPrivateKeyAsHex());
                cp.newLine();
                String caddress = AddressTool.getStringAddressByBytes(address.getAddressBytes(), address.getPrefix());
                TransferService transferService = ServiceManager.get(TransferService.class);
                TransferReq.TransferReqBuilder builder =
                        new TransferReq.TransferReqBuilder(chainId, assetId)
                                .addForm(chainId, assetId, formAddress, password, amount)
                                .addTo(chainId, assetId, caddress, amount);
                Result<String> result = transferService.transfer(builder.build(new TransferReq()));
                if(result.isFailed()){
                    Log.error("失败:{}",result.getMessage());
                }else{
                    Log.info("{}",result);
                }
                ImportAccountByPrivateKeyReq req = new ImportAccountByPrivateKeyReq(password,key.getPrivateKeyAsHex(),true);
                req.setChainId(chainId);
                accountService.importAccountByPrivateKey(req);
                key = new ECKey();
                address = new Address(chainId, addressPrefix, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(key.getPubKey()));
                pp.write(nk + "=" +  key.getPrivateKeyAsHex());
                pp.newLine();
                String paddress = AddressTool.getStringAddressByBytes(address.getAddressBytes(), address.getPrefix());
                pa.write(id + "=" + nk + "=" + paddress);
                pa.newLine();
                pascript.write("ssh root@"+nk+" 'bash -s ' < ./remote-import-address " + key.getPrivateKeyAsHex());
                pascript.newLine();
                ccscript.write("createagent " + caddress + " " + paddress + " 200000 " + caddress + " " + password);
                ccscript.newLine();
                calias.write("setalias " + caddress + " " + String.format("nuls%02d",i+1) + " " + password);
                calias.newLine();
            }
            cp.flush();
            pp.flush();
            pa.flush();
            calias.flush();
            pascript.flush();
            ccscript.flush();
        }finally {
            cp.close();
//            ca.close();
            pp.close();
            pa.close();
            calias.close();
            pascript.close();
            ccscript.close();
        }
    }

}
