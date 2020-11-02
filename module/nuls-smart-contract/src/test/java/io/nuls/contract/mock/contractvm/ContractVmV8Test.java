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
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

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
 *      A有10
 *      B有20
 *
 *  3. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移60给调用者，转移10锁定给调用者
 *     期望执行结果中
 *      有退回到调用者的60可用，10锁定
 *      A有10
 *      B有20
 *
 *  4. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移20给sender，A转移10锁定给sender
 *     期望执行结果中
 *      有退回到调用者的50可用，20锁定
 *      A有0
 *      B有30
 *
 *  5. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移110给A
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  6. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A, B转移100给sender
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  7. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移30给sender，A转移10锁定给sender
 *     期望执行结果中
 *      执行失败，余额不足
 *
 *  8. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A，转移10锁定给A
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
        InputStream inA = new FileInputStream(getClass().getResource("/contract-vm-testA-testA.jar").getFile());
        InputStream inB = new FileInputStream(getClass().getResource("/contract-vm-testB-testB.jar").getFile());
        //InputStream inA = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testA/target/contract-vm-testA-testA.jar");
        //InputStream inB = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testB/target/contract-vm-testB-testB.jar");
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        objects = super.call(contractB, prevStateRoot, SENDER, "setContractA", new String[]{contractA});
        prevStateRoot = (byte[]) objects[0];
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
    }

    @Test
    public void test() {
        System.out.println(ProtocolGroupManager.getCurrentVersion(chainId));
    }

    @Test
    public void test1() throws Exception{
        byte[] currentStateRoot;
        String a;
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test15", new String[]{}, BigInteger.valueOf(100L));
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test15]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        boolean success = false;
        for(ProgramTransfer transfer : transfers) {
            String from = AddressTool.getStringAddressByBytes(transfer.getFrom());
            String to = AddressTool.getStringAddressByBytes(transfer.getTo());
            Log.info("transfer from: {}, to: {}, value: {}", from, to, transfer.getValue().toString());
            if(from.equals(contractB) && to.equals(SENDER) && transfer.getValue().longValue() == 70L) {
                success = true;
                break;
            }
        }
        Assert.assertTrue("测试方法[test15]期望 退回70", success);
    }


    private byte[] callVmTest(byte[] prevStateRoot, String method, String expect, String viewMethod) throws Exception {
        Object[] objects;
        ProgramResult programResult;

        objects = super.call(contractA, prevStateRoot, SENDER, method, new String[]{});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法["+method+"]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("测试方法[%s]返回值期望a=%s, 实际a=%s", method, expect, programResult.getResult()), expect.equals(programResult.getResult()));

        if(StringUtils.isNotBlank(viewMethod)) {
            String a = super.view(contractA, prevStateRoot, viewMethod, new String[]{});
            Assert.assertTrue(String.format("测试方法[%s]View期望a=%s, 实际a=%s", method, expect, a), expect.equals(a));
        }
        return prevStateRoot;
    }

    private byte[] callVmTest(byte[] stateRoot, String method, String expect) throws Exception {
        return callVmTest(stateRoot, method, expect, "viewA");
    }

    private byte[] callVmTest(byte[] stateRoot, String method, String expect, boolean containViewExpect) throws Exception {
        String viewMethod = null;
        if(containViewExpect) {
            viewMethod = "viewA";
        }
        return callVmTest(stateRoot, method, expect, viewMethod);
    }

}
