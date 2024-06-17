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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.*;

/**
 * Test scenario:
 * <p>
 * 1. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Bhold30Transfer70To the caller
 * Expected execution results
 * Returned to the caller70
 * Ahave0
 * Bhave30
 * <p>
 * <p>
 * 2. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toATransfer70To the caller
 * Expected execution results
 * Returned to the caller70
 * Ahave20
 * Bhave10
 * <p>
 * 3. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toATransfer60To the caller, transfer10Lock to caller
 * Expected execution results
 * Returned to the caller60Available,10locking
 * Ahave20
 * Bhave10
 * <p>
 * 4. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toA,Btransfer30tosender,Btransfer10Locked tosender,BcallAtransfer25toA,Atransfer15toB,Atransfer20tosender,Atransfer10Locked tosender
 * Expected execution results
 * Returned to the caller50Available,20locking
 * Ahave0
 * Bhave30
 * <p>
 * 6. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer110toA
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 7. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer10toA, Btransfer100tosender
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 8. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toA,Btransfer30tosender,Btransfer10Locked tosender,BcallAtransfer25toA,Atransfer15toB,Atransfer30tosender,Atransfer10Locked tosender
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 9. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer10toATransfer10Locked toA
 * Expected execution results
 * Execution failed, transfer of locked assets to contract address not allowed
 * <p>
 * 11. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Bhold30Transfer70To the caller
 * Expected execution results
 * Returned to the caller70
 * Ahave0
 * Bhave30
 * <p>
 * <p>
 * 12. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toATransfer70To the caller
 * Expected execution results
 * Returned to the caller70
 * Ahave20
 * Bhave10
 * <p>
 * 13. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toATransfer60To the caller, transfer10Lock to caller
 * Expected execution results
 * Returned to the caller60Available,10locking
 * Ahave20
 * Bhave10
 * <p>
 * 14. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toA,Btransfer30tosender,Btransfer10Locked tosender,BcallAtransfer25toA,Atransfer15toB,Atransfer20tosender,Atransfer10Locked tosender
 * Expected execution results
 * Returned to the caller50Available,20locking
 * Ahave0
 * Bhave30
 * <p>
 * 16. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer110toA
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 17. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer10toA, Btransfer100tosender
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 18. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer20toA,Btransfer30tosender,Btransfer10Locked tosender,BcallAtransfer25toA,Atransfer15toB,Atransfer30tosender,Atransfer10Locked tosender
 * Expected execution results
 * Execution failed due to insufficient balance
 * <p>
 * 19. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Btransfer10toATransfer10Locked toA
 * Expected execution results
 * Execution failed, transfer of locked assets to contract address not allowed
 *
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmV8Test extends MockBase {

    String contractA = "tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx";
    String contractB = "tNULSeBaN1gZJobF3bxuLwXxvvAosdwQTVxWFn";
    byte[] prevStateRoot;

    @Override
    protected void protocolUpdate() {
        short version = 8;
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, version);
        if (version >= 8) {
            VMFactory.reInitVM_v8();
        }
    }

    @Before
    public void createAndInit() throws Exception {
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
    public void test1() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test1", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test2() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test2", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test3() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test3", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test4() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test4", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test6() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test6", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
    }

    @Test
    public void test7() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test7", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
    }

    @Test
    public void test8() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test8", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
    }

    @Test
    public void test9() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test9", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
    }

    /*@Test
    public void test1v2() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test1", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, SENDER}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test2v2() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test2", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, SENDER}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test3v2() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test3", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, SENDER}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test4v2() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test4", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, SENDER}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test6v2() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test6", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true);
    }

    @Test
    public void test7v2() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test7", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true);
    }

    @Test
    public void test8v2() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test8", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true);
    }

    @Test
    public void test9v2() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test9", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, true);
    }*/

    @Test
    public void test11() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test11", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test12() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test12", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test13() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test13", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test14() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test14", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test16() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test16", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test17() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test17", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test18() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test18", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test19() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test19", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test21() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test1", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 0, 30, 70, 0, 0, 0);
        this.testAsset(contractA, prevStateRoot, SENDER, "test11", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test22() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test2", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 20, 10, 70, 0, 0, 0);
        this.testAsset(contractA, prevStateRoot, SENDER, "test12", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test23() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test3", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 20, 10, 60, 0, 0, 10);
        this.testAsset(contractA, prevStateRoot, SENDER, "test13", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test24() throws Exception {
        this.testAsset(contractA, prevStateRoot, SENDER, "test4", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, SENDER}, 0, 30, 50, 0, 0, 20);
        this.testAsset(contractA, prevStateRoot, SENDER, "test14", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true, new String[]{contractA, contractB, SENDER}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test26() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test6", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
        this.testFailed(contractA, prevStateRoot, SENDER, "test16", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test27() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test7", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
        this.testFailed(contractA, prevStateRoot, SENDER, "test17", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test28() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test8", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
        this.testFailed(contractA, prevStateRoot, SENDER, "test18", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
    }

    @Test
    public void test29() throws Exception {
        this.testFailed(contractA, prevStateRoot, SENDER, "test9", new String[]{}, BigDecimal.valueOf(100L), chainId, assetId, false);
        this.testFailed(contractA, prevStateRoot, SENDER, "test19", new String[]{}, BigDecimal.valueOf(100L), chainId, 2, true);
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

    protected LinkedHashMap<String, BigInteger>[] filterContractValue(List<ProgramTransfer> transfers) {
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

    protected void testAsset(String contract, byte[] stateRoot, String sender, String method, String[] args, BigDecimal value, int assetChainId, int assetId, boolean setAsset, String[] addresses, long... expectBalances) throws JsonProcessingException {
        Assert.assertTrue("The address and expected balance parameters are illegal", addresses.length * 2 == expectBalances.length);
        Object[] objects;
        ProgramResult programResult;
        BigInteger _value = toNa(value);
        if (setAsset) {
            objects = super.call(contract, stateRoot, sender, method, args, _value, assetChainId, assetId);
        } else {
            objects = super.call(contract, stateRoot, sender, method, args, _value);
        }
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue(String.format("test method[%s]expect success, errorMsg: %s, stackTrace: %s", method, programResult.getErrorMessage(), Arrays.deepToString(programResult.getStackTraces().toArray())), programResult.isSuccess());

        Log.info(JSONUtils.obj2PrettyJson(programResult));
        List<ProgramTransfer> transfers = programResult.getTransfers();
        LinkedHashMap<String, BigInteger>[] contracts = this.filterContractValue(transfers);
        BigInteger[][] balanceList = this.balanceList(assetChainId, assetId, contracts, addresses);
        BigInteger balanceA = balanceList[0][0].add(toNa(BigDecimal.valueOf(100L)));
        BigInteger balanceB = balanceList[0][1];
        BigInteger balanceSender = balanceList[0][2];
        BigInteger balanceALock = balanceList[1][0];
        BigInteger balanceBLock = balanceList[1][1];
        BigInteger balanceSenderLock = balanceList[1][2];

        for (ProgramTransfer transfer : transfers) {
            Log.info("transfer: {}", transfer.toString());
        }
        Assert.assertTrue(String.format("test method[%s]expect A: %s, actual: %s", method, expectBalances[0], toNuls(balanceA)), balanceA.longValue() == toNa(BigDecimal.valueOf(expectBalances[0])).longValue());
        Assert.assertTrue(String.format("test method[%s]expect B: %s, actual: %s", method, expectBalances[1], toNuls(balanceB)), balanceB.longValue() == toNa(BigDecimal.valueOf(expectBalances[1])).longValue());
        Assert.assertTrue(String.format("test method[%s]expect sender: %s, actual: %s", method, expectBalances[2], toNuls(balanceSender)), balanceSender.longValue() == toNa(BigDecimal.valueOf(expectBalances[2])).longValue());
        Assert.assertTrue(String.format("test method[%s]expect A-locking: %s, actual: %s", method, expectBalances[3], toNuls(balanceALock)), balanceALock.longValue() == toNa(BigDecimal.valueOf(expectBalances[3])).longValue());
        Assert.assertTrue(String.format("test method[%s]expect B-locking: %s, actual: %s", method, expectBalances[4], toNuls(balanceBLock)), balanceBLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[4])).longValue());
        Assert.assertTrue(String.format("test method[%s]expect sender-locking: %s, actual: %s", method, expectBalances[5], toNuls(balanceSenderLock)), balanceSenderLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[5])).longValue());
    }

    protected void testFailed(String contract, byte[] stateRoot, String sender, String method, String[] args, BigDecimal value, int assetChainId, int assetId, boolean setAsset) throws JsonProcessingException {
        Object[] objects;
        ProgramResult programResult;
        BigInteger _value = toNa(value);
        if (setAsset) {
            objects = super.call(contract, stateRoot, sender, method, args, _value, assetChainId, assetId);
        } else {
            objects = super.call(contract, stateRoot, sender, method, args, _value);
        }
        programResult = (ProgramResult) objects[1];
        System.out.println("errorMsg: " + programResult.getErrorMessage());
        Assert.assertFalse(String.format("test method[%s]expect failed, errorMsg: %s, stackTrace: %s", method, programResult.getErrorMessage(), Arrays.deepToString(programResult.getStackTraces().toArray())), programResult.isSuccess());
    }

}
