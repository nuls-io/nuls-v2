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
 *
 */
package io.nuls.contract.mock.basetest;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.crypto.HexUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ContractTest {

    private VMContext vmContext;
    private ProgramExecutor programExecutor;

    private static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    private static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    private static final String BUYER = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";

    static String[] senderSeeds = {
            "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"};

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
    }

    @Before
    public void setUp() {
        RocksDBService.init("./data");
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(2);
        configBean.setAssetsId(1);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);
        //ContractTokenBalanceManager tokenBalanceManager = ContractTokenBalanceManager.newInstance(chain.getChainId());
        //chain.setContractTokenBalanceManager(tokenBalanceManager);
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);
    }

    @Test
    public void testValidAddress() {
        Assert.assertTrue(AddressTool.validAddress(2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/token_contract").getFile());
        //InputStream in = new FileInputStream(ContractTest.class.getResource("/").getFile() + "../simple_chinese");
        byte[] contractCode = IOUtils.toByteArray(in);

        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCreate.setSender(NativeAddress.toBytes(SENDER));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
        //programCreate.args();
        System.out.println(programCreate);

        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.create(programCreate);
        track.commit();

        System.out.println(programResult);
        System.out.println("stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testCall() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("mint");
        programCall.setMethodDesc("");
        programCall.args(BUYER, "1000");
        System.out.println(programCall);

        byte[] prevStateRoot = HexUtil.decode("740bfe8f98ca7bbf67b852304956b999364dc2882e92910144ce6e18d389db07");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(BUYER);
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testAddBalance() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("_payable");
        programCall.setMethodDesc("()V");
        programCall.setValue(new BigInteger("100"));
        System.out.println(programCall);

        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testStop() throws IOException {
        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.stop(1, address, sender);
        track.commit();

        System.out.println(programResult);
        System.out.println("stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testStatus() throws IOException {
        byte[] prevStateRoot = HexUtil.decode("f4c8f955a79de88ceb9de02ecb2dc6fb997e21a0f1b9ff4be063d5bc282e5b2b");
        //byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramStatus programStatus = track.status(address);

        System.out.println(programStatus);
        System.out.println("stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testTransactions() {
        List<ProgramCall> transactions = new ArrayList<>();

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(ADDRESS);
        System.out.println(programCall);
        transactions.add(programCall);

        ProgramCall programCall1 = new ProgramCall();
        programCall1.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall1.setSender(NativeAddress.toBytes(SENDER));
        programCall1.setPrice(1);
        programCall1.setGasLimit(1000000);
        programCall1.setNumber(1);
        programCall1.setMethodName("balanceOf");
        programCall1.setMethodDesc("");
        programCall1.args(BUYER);
        System.out.println(programCall1);
        transactions.add(programCall1);

        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");

        for (ProgramCall transaction : transactions) {
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.call(transaction);
            track.commit();

            prevStateRoot = track.getRoot();

            System.out.println("programResult: " + programResult);
            System.out.println("stateRoot: " + HexUtil.encode(track.getRoot()));
            System.out.println();
        }
        sleep();
    }

    @Test
    public void testMethod() throws IOException {
        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        List<ProgramMethod> methods = track.method(address);
        //track.commit();

        for (ProgramMethod method : methods) {
            System.out.println(method);
        }
        sleep();
    }

    @Test
    public void testJarMethod() throws IOException {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/token_contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        List<ProgramMethod> methods = programExecutor.jarMethod(contractCode);

        for (ProgramMethod method : methods) {
            System.out.println(method);
        }
        sleep();
    }

    @Test
    public void testTransfer() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("transfer");
        programCall.setMethodDesc("");
        programCall.args(BUYER, "-1000");
        System.out.println(programCall);

        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(BUYER);
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testGetter() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        //programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("getName");
        programCall.setMethodDesc("");
        programCall.setValue(new BigInteger("0"));
        System.out.println(programCall);

        byte[] prevStateRoot = HexUtil.decode("5649c2c5e4899b1565db76c029b39fcbec36d6e878ea8f4d9c3955386ef008a4");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        sleep();
    }

    @Test
    public void testThread() {
        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        //ProgramExecutor track = programExecutor.begin(prevStateRoot);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                track.commit();
            }
        }).start();
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
