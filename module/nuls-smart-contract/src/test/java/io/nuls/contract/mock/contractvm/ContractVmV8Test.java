/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.mock.contractvm;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.addressKey;
import static io.nuls.contract.util.ContractUtil.mapAddBigInteger;

/**
 * 测试场景:
 *
 *  1. 双合约测试，调用者向A合约转入100，A调用B转入100，B保留30，转移70给调用者
 *     期望执行结果中
 *      有退回到调用者的70
 *      A有0
 *      B有30
 *
 *
 *  2. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移70给调用者
 *     期望执行结果中
 *      有退回到调用者的70
 *      A有20
 *      B有10
 *
 *  3. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移60给调用者，转移10锁定给调用者
 *     期望执行结果中
 *      有退回到调用者的60可用，10锁定
 *      A有20
 *      B有10
 *
 *  4. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移20给sender，A转移10锁定给sender
 *     期望执行结果中
 *      有退回到调用者的50可用，20锁定
 *      A有0
 *      B有30
 *
 *  6. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移110给A
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  7. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A, B转移100给sender
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  8. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移30给sender，A转移10锁定给sender
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  9. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A，转移10锁定给A
 *     期望执行结果中
 *      执行失败，不允许转移锁定资产给合约地址
 *
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmV8Test extends MockBase {

    String contractA = "tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx";
    String contractB = "tNULSeBaN1gZJobF3bxuLwXxvvAosdwQTVxWFn";
    byte[] prevStateRoot;

    @Before
    public void createAndInit() throws Exception {
        // 加载协议升级的数据
        ContractContext.CHAIN_ID = 2;
        short version = 8;
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, version);
        if (version >= 8) {
            VMFactory.reInitVM_v8();
        }

        // -------------------------------------------------------------------------------------//
        //InputStream inA = new FileInputStream(getClass().getResource("/contract-vm-v8-testA-1.0-SNAPSHOT.jar").getFile());
        //InputStream inB = new FileInputStream(getClass().getResource("/contract-vm-v8-testB-1.0-SNAPSHOT.jar").getFile());
        InputStream inA = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-v8-testA/target/contract-vm-v8-testA-1.0-SNAPSHOT.jar");
        InputStream inB = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-v8-testB/target/contract-vm-v8-testB-1.0-SNAPSHOT.jar");
        byte[] contractCodeA = IOUtils.toByteArray(inA);
        byte[] contractCodeB = IOUtils.toByteArray(inB);

        byte[] initialStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        prevStateRoot = super.create(initialStateRoot, contractA, SENDER, contractCodeA);
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));

        prevStateRoot = super.create(prevStateRoot, contractB, SENDER, contractCodeB);
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));

        // ------------------------------initial----------------------------------------------------//
        Object[] objects = super.call(contractA, prevStateRoot, SENDER, "setContractB", new String[]{contractB});
        prevStateRoot = (byte[]) objects[0];
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));
        ProgramResult programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());

        objects = super.call(contractB, prevStateRoot, SENDER, "setContractA", new String[]{contractA});
        prevStateRoot = (byte[]) objects[0];
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
    }

    @Test
    public void test() {
        System.out.println(ProtocolGroupManager.getCurrentVersion(chainId));
    }

    @Test
    public void test1() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test1", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test1]expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
        BigInteger[][] balanceList = this.balanceList(chainId, assetId, contracts, contractA, contractB, SENDER);
        BigInteger balanceA = balanceList[0][0].add(BigInteger.valueOf(100L));
        BigInteger balanceB = balanceList[0][1];
        BigInteger balanceSender = balanceList[0][2];

        for(ProgramTransfer transfer : transfers) {
            Log.info("transfer: {}", transfer.toString());
        }
        Assert.assertTrue(String.format("测试方法[test1]期望 A: 0, 实际: %s", balanceA.longValue()), balanceA.longValue() == 0);
        Assert.assertTrue(String.format("测试方法[test1]期望 B: 30, 实际: %s", balanceB.longValue()), balanceB.longValue() == 30);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender: 70, 实际: %s", balanceSender.longValue()), balanceSender.longValue() == 70);
    }

    @Test
    public void test2() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test2", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test2]expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
        BigInteger[][] balanceList = this.balanceList(chainId, assetId, contracts, contractA, contractB, SENDER);
        BigInteger balanceA = balanceList[0][0].add(BigInteger.valueOf(100L));
        BigInteger balanceB = balanceList[0][1];
        BigInteger balanceSender = balanceList[0][2];

        for(ProgramTransfer transfer : transfers) {
            Log.info("transfer: {}", transfer.toString());
        }
        Assert.assertTrue(String.format("测试方法[test1]期望 A: 20, 实际: %s", balanceA.longValue()), balanceA.longValue() == 20);
        Assert.assertTrue(String.format("测试方法[test1]期望 B: 10, 实际: %s", balanceB.longValue()), balanceB.longValue() == 10);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender: 70, 实际: %s", balanceSender.longValue()), balanceSender.longValue() == 70);
    }

    @Test
    public void test3() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test3", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test3]expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
        BigInteger[][] balanceList = this.balanceList(chainId, assetId, contracts, contractA, contractB, SENDER);
        BigInteger balanceA = balanceList[0][0].add(BigInteger.valueOf(100L));
        BigInteger balanceB = balanceList[0][1];
        BigInteger balanceSender = balanceList[0][2];
        BigInteger balanceSenderLock = balanceList[1][2];

        for(ProgramTransfer transfer : transfers) {
            Log.info("transfer: {}", transfer.toString());
        }
        Assert.assertTrue(String.format("测试方法[test1]期望 A: 20, 实际: %s", balanceA.longValue()), balanceA.longValue() == 20);
        Assert.assertTrue(String.format("测试方法[test1]期望 B: 10, 实际: %s", balanceB.longValue()), balanceB.longValue() == 10);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender: 60, 实际: %s", balanceSender.longValue()), balanceSender.longValue() == 60);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender-锁定: 10, 实际: %s", balanceSenderLock.longValue()), balanceSenderLock.longValue() == 10);
    }

    @Test
    public void test4() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test4", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test4]expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(chainId, transfers);
        BigInteger[][] balanceList = this.balanceList(chainId, assetId, contracts, contractA, contractB, SENDER);
        BigInteger balanceA = balanceList[0][0].add(BigInteger.valueOf(100L));
        BigInteger balanceB = balanceList[0][1];
        BigInteger balanceSender = balanceList[0][2];
        BigInteger balanceSenderLock = balanceList[1][2];

        for(ProgramTransfer transfer : transfers) {
            Log.info("transfer: {}", transfer.toString());
        }
        Assert.assertTrue(String.format("测试方法[test1]期望 A: 0, 实际: %s", balanceA.longValue()), balanceA.longValue() == 0);
        Assert.assertTrue(String.format("测试方法[test1]期望 B: 30, 实际: %s", balanceB.longValue()), balanceB.longValue() == 30);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender: 50, 实际: %s", balanceSender.longValue()), balanceSender.longValue() == 50);
        Assert.assertTrue(String.format("测试方法[test1]期望 sender-锁定: 20, 实际: %s", balanceSenderLock.longValue()), balanceSenderLock.longValue() == 20);
    }


    @Test
    public void test6() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test6", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        System.out.println("errorMsg: " + programResult.getErrorMessage());
        Assert.assertFalse("测试方法[test6]expect failed, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
    }

    @Test
    public void test7() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test7", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        System.out.println("errorMsg: " + programResult.getErrorMessage());
        Assert.assertFalse("测试方法[test7]expect failed, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
    }

    @Test
    public void test8() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test8", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        System.out.println("errorMsg: " + programResult.getErrorMessage());
        Assert.assertFalse("测试方法[test8]expect failed, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
    }

    @Test
    public void test9() throws Exception{
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test9", new String[]{}, BigInteger.valueOf(100L));
        programResult = (ProgramResult) objects[1];
        System.out.println("errorMsg: " + programResult.getErrorMessage());
        Assert.assertFalse("测试方法[test9]expect failed, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
    }

    protected BigInteger[][] balanceList(int assetChainId, int assetId, LinkedHashMap<String, BigInteger>[] contracts, String... addresses) {
        BigInteger[][] result = new BigInteger[2][];
        BigInteger[] resultAvailable = new BigInteger[addresses.length];
        BigInteger[] resultLock = new BigInteger[addresses.length];
        result[0] = resultAvailable;
        result[1] = resultLock;

        LinkedHashMap<String, BigInteger> fromValue = contracts[0];
        LinkedHashMap<String, BigInteger> toValue = contracts[1];
        LinkedHashMap<String, BigInteger> toLockValue = contracts[2];
        int i = 0;
        for (String address : addresses) {
            byte[] addressBytes = AddressTool.getAddress(address);
            String key = addressKey(addressBytes, assetChainId, assetId);
            BigInteger balance = mapValueOf(fromValue, key).negate().add(mapValueOf(toValue, key));
            resultAvailable[i] = balance;
            resultLock[i++] = mapValueOf(toLockValue, key);
        }
        return result;
    }

    protected BigInteger mapValueOf(LinkedHashMap<String, BigInteger> map, String key) {
        BigInteger value = map.get(key);
        if (value == null) {
            return BigInteger.ZERO;
        }
        return value;
    }

    protected LinkedHashMap<String, BigInteger>[] filterContractValue(int chainId, List<ProgramTransfer> transfers) {
        LinkedHashMap<String, BigInteger> contractFromValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger> contractToLockValue = MapUtil.createLinkedHashMap(4);
        LinkedHashMap<String, BigInteger>[] contracts = new LinkedHashMap[3];
        contracts[0] = contractFromValue;
        contracts[1] = contractToValue;
        contracts[2] = contractToLockValue;

        byte[] from, to;
        int assetChainId, assetId;
        BigInteger transferValue;
        boolean lock;
        for (ProgramTransfer transfer : transfers) {
            from = transfer.getFrom();
            to = transfer.getTo();
            transferValue = transfer.getValue();
            assetChainId = transfer.getAssetChainId();
            assetId = transfer.getAssetId();
            lock = transfer.getLockedTime() > 0;
            mapAddBigInteger(contractFromValue, from, assetChainId, assetId, transferValue);
            if (lock) {
                mapAddBigInteger(contractToLockValue, to, assetChainId, assetId, transferValue);
            } else {
                mapAddBigInteger(contractToValue, to, assetChainId, assetId, transferValue);
            }
        }
        return contracts;
    }

    protected byte[] callVmTest(byte[] prevStateRoot, String method, String expect, String viewMethod) throws Exception {
        Object[] objects;
        ProgramResult programResult;

        objects = super.call(contractA, prevStateRoot, SENDER, method, new String[]{});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法["+method+"]expect success, " + programResult.getErrorMessage() + ", " + Arrays.deepToString(programResult.getStackTraces().toArray()), programResult.isSuccess());
        Assert.assertTrue(String.format("测试方法[%s]返回值期望a=%s, 实际a=%s", method, expect, programResult.getResult()), expect.equals(programResult.getResult()));

        if(StringUtils.isNotBlank(viewMethod)) {
            String a = super.view(contractA, prevStateRoot, viewMethod, new String[]{});
            Assert.assertTrue(String.format("测试方法[%s]View期望a=%s, 实际a=%s", method, expect, a), expect.equals(a));
        }
        return prevStateRoot;
    }

    protected byte[] callVmTest(byte[] stateRoot, String method, String expect) throws Exception {
        return callVmTest(stateRoot, method, expect, "viewA");
    }

    protected byte[] callVmTest(byte[] stateRoot, String method, String expect, boolean containViewExpect) throws Exception {
        String viewMethod = null;
        if(containViewExpect) {
            viewMethod = "viewA";
        }
        return callVmTest(stateRoot, method, expect, viewMethod);
    }

}
