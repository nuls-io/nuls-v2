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
package io.nuls.contract.tx.contractvm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.model.dto.*;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.tx.base.BaseQuery;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramMultyAssetValue;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.constant.ContractCmdConstant.CALL;
import static io.nuls.contract.constant.ContractCmdConstant.CREATE;
import static io.nuls.contract.util.ContractUtil.*;

/**
 * 测试场景:
 * <p>
 * 1. 双合约测试，调用者向A合约转入100，A调用B转入100，B保留30，转移70给调用者
 * 期望执行结果中
 * 有退回到调用者的70
 * A有0
 * B有30
 * <p>
 * <p>
 * 2. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移70给调用者
 * 期望执行结果中
 * 有退回到调用者的70
 * A有20
 * B有10
 * <p>
 * 3. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移60给调用者，转移10锁定给调用者
 * 期望执行结果中
 * 有退回到调用者的60可用，10锁定
 * A有20
 * B有10
 * <p>
 * 4. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移20给sender，A转移10锁定给sender
 * 期望执行结果中
 * 有退回到调用者的50可用，20锁定
 * A有0
 * B有30
 * <p>
 * 6. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移110给A
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 7. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A, B转移100给sender
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 8. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移30给sender，A转移10锁定给sender
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 9. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A，转移10锁定给A
 * 期望执行结果中
 * 执行失败，不允许转移锁定资产给合约地址
 * <p>
 * 11. 双合约测试，调用者向A合约转入100，A调用B转入100，B保留30，转移70给调用者
 * 期望执行结果中
 * 有退回到调用者的70
 * A有0
 * B有30
 * <p>
 * <p>
 * 12. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移70给调用者
 * 期望执行结果中
 * 有退回到调用者的70
 * A有20
 * B有10
 * <p>
 * 13. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，转移60给调用者，转移10锁定给调用者
 * 期望执行结果中
 * 有退回到调用者的60可用，10锁定
 * A有20
 * B有10
 * <p>
 * 14. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移20给sender，A转移10锁定给sender
 * 期望执行结果中
 * 有退回到调用者的50可用，20锁定
 * A有0
 * B有30
 * <p>
 * 16. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移110给A
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 17. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A, B转移100给sender
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 18. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移20给A，B转移30给sender，B转移10锁定给sender，B调用A转移25给A，A转移15给B，A转移30给sender，A转移10锁定给sender
 * 期望执行结果中
 * 执行失败，余额不足
 * <p>
 * 19. 双合约测试，调用者向A合约转入100，A调用B转入100，B转移10给A，转移10锁定给A
 * 期望执行结果中
 * 执行失败，不允许转移锁定资产给合约地址
 *
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmV8SendTxTest extends BaseQuery {

    protected long gasLimit = 200000L;
    protected long gasPrice = 25L;
    protected long minutes_3 = 60 * 3;
    protected long minutes_5 = 60 * 5;
    protected String contractA = "";
    protected String contractB = "";
    protected boolean createContract = true;
    int multyAssetId = 2;

    @Before
    public void createAndInit() throws Exception {
        // 加载协议升级的数据
        ContractContext.CHAIN_ID = chainId;

        if (!createContract) {
            // 注册链内资产
            //assetRegisterTest();
            // -------------------------------------------------------------------------------------//
            //InputStream inA = new FileInputStream(getClass().getResource("/contract-vm-v8-testA-1.0-SNAPSHOT.jar").getFile());
            //InputStream inB = new FileInputStream(getClass().getResource("/contract-vm-v8-testB-1.0-SNAPSHOT.jar").getFile());
            InputStream inA = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-v8-testA/target/contract-vm-v8-testA-1.0-SNAPSHOT.jar");
            InputStream inB = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-v8-testB/target/contract-vm-v8-testB-1.0-SNAPSHOT.jar");
            contractA = this.createContract(inA, "test_a");
            contractB = this.createContract(inB, "test_b");

            // ------------------------------initial----------------------------------------------------//
            ContractResultDto contractResult;
            contractResult = this.callByParams(contractA, toAddress, "setContractB", "0", new String[]{contractB});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());
            contractResult = this.callByParams(contractB, toAddress, "setContractA", "0", new String[]{contractA});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());

            contractResult = this.callByParams(contractA, toAddress, "setSender", "0", new String[]{sender});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());
            contractResult = this.callByParams(contractB, toAddress, "setSender", "0", new String[]{sender});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());
        } else {
            //contractA = "tNULSeBaMy6NZRUzvKSMpKjw87ABAgietZ2THh";
            //contractB = "tNULSeBaN8Ytuc6AuwD37gGozqrPSVi8qCLmqy";
            contractA = "tNULSeBaN2XQNq6Z83Ner1rf2u61ovyYACRKXW";
            contractB = "tNULSeBaMydn9HiYWf9KovR3Bmr1ePZGTsGPoa";
        }
        JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testContractResultDto() throws Exception {
        String hash = "db7a99bcd2c6356126a32e8e0f06a6ef7d16bdfea127fda2d852241b0acd35a1";
        Map map = this.waitGetContractTx(hash);
        Map map1 = (Map) map.get("contractResult");
        ContractResultDto dto = this.converterDto(map1);
        System.out.println(JSONUtils.obj2PrettyJson(dto));
    }

    @Test
    public void testSome() throws Exception {
        test3();
        test9();
        test13();
        test14();
    }

    @Test
    public void testFull() throws Exception {
        test1();
        test2();
        test3();
        test4();
        test6();
        test7();
        test8();
        test9();
        test11();
        test12();
        test13();
        test14();
        test16();
        test17();
        test18();
        test19();
    }

    @Test
    public void test() {
        System.out.println(ProtocolGroupManager.getCurrentVersion(chainId));
    }

    @Test
    public void test1() throws Exception {
        this.testAsset(contractA, sender, "test1", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, sender}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test2() throws Exception {
        this.testAsset(contractA, sender, "test2", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, sender}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test3() throws Exception {
        this.testAsset(contractA, sender, "test3", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, sender}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test4() throws Exception {
        this.testAsset(contractA, sender, "test4", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, new String[]{contractA, contractB, sender}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test6() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test6", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, errorMsgKey);
    }

    @Test
    public void test7() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test7", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, errorMsgKey);
    }

    @Test
    public void test8() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test8", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, errorMsgKey);
    }

    @Test
    public void test9() throws Exception {
        String errorMsgKey = "Cannot transfer the locked amount to the contract address";
        this.testFailed(contractA, sender, "test9", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, false, errorMsgKey);
    }

    /*@Test
    public void test1v2() throws Exception {
        this.testAsset(contractA, sender, "test1", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, sender}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test2v2() throws Exception {
        this.testAsset(contractA, sender, "test2", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, sender}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test3v2() throws Exception {
        this.testAsset(contractA, sender, "test3", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, sender}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test4v2() throws Exception {
        this.testAsset(contractA, sender, "test4", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, new String[]{contractA, contractB, sender}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test6v2() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test6", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, errorMsgKey);
    }

    @Test
    public void test7v2() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test7", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, errorMsgKey);
    }

    @Test
    public void test8v2() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test8", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, errorMsgKey);
    }

    @Test
    public void test9v2() throws Exception {
        String errorMsgKey = "Cannot transfer the locked amount to the contract address";
        this.testFailed(contractA, sender, "test9", new String[]{}, BigInteger.valueOf(100L), chainId, assetId, true, errorMsgKey);
    }*/

    @Test
    public void test11() throws Exception {
        this.testAsset(contractA, sender, "test11", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, new String[]{contractA, contractB, sender}, 0, 30, 70, 0, 0, 0);
    }

    @Test
    public void test12() throws Exception {
        this.testAsset(contractA, sender, "test12", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, new String[]{contractA, contractB, sender}, 20, 10, 70, 0, 0, 0);
    }

    @Test
    public void test13() throws Exception {
        this.testAsset(contractA, sender, "test13", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, new String[]{contractA, contractB, sender}, 20, 10, 60, 0, 0, 10);
    }

    @Test
    public void test14() throws Exception {
        this.testAsset(contractA, sender, "test14", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, new String[]{contractA, contractB, sender}, 0, 30, 50, 0, 0, 20);
    }

    @Test
    public void test16() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test16", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, errorMsgKey);
    }

    @Test
    public void test17() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test17", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, errorMsgKey);
    }

    @Test
    public void test18() throws Exception {
        String errorMsgKey = "not enough balance";
        this.testFailed(contractA, sender, "test18", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, errorMsgKey);
    }

    @Test
    public void test19() throws Exception {
        String errorMsgKey = "Cannot transfer the locked amount to the contract address";
        this.testFailed(contractA, sender, "test19", new String[]{}, BigInteger.valueOf(100L), chainId, multyAssetId, true, errorMsgKey);
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

    protected List<ProgramTransfer> converterTransfer(ContractMergedTransferDto dto) {
        List<ProgramTransfer> resultList = new ArrayList<>();
        byte[] from = AddressTool.getAddress(dto.getFrom());
        List<ContractOutputDto> outputs = dto.getOutputs();
        for (ContractOutputDto outputDto : outputs) {
            ProgramTransfer transfer = new ProgramTransfer();
            transfer.setFrom(from);
            transfer.setTo(AddressTool.getAddress(outputDto.getTo()));
            transfer.setValue(new BigInteger(outputDto.getValue()));
            transfer.setAssetChainId(chainId);
            transfer.setAssetId(assetId);
            transfer.setLockedTime(outputDto.getLockTime());
            resultList.add(transfer);
        }
        return resultList;
    }

    protected List<ProgramTransfer> converterTransfer(ContractMultyAssetMergedTransferDto dto) {
        List<ProgramTransfer> resultList = new ArrayList<>();
        byte[] from = AddressTool.getAddress(dto.getFrom());
        List<MultyAssetOutputDto> outputs = dto.getOutputs();
        for (MultyAssetOutputDto outputDto : outputs) {
            ProgramTransfer transfer = new ProgramTransfer();
            transfer.setFrom(from);
            transfer.setTo(AddressTool.getAddress(outputDto.getTo()));
            transfer.setValue(new BigInteger(outputDto.getValue()));
            transfer.setAssetChainId(outputDto.getAssetChainId());
            transfer.setAssetId(outputDto.getAssetId());
            transfer.setLockedTime(outputDto.getLockTime());
            resultList.add(transfer);
        }
        return resultList;
    }

    protected void testAsset(String contract, String sender, String method, String[] args, BigInteger value, int assetChainId, int assetId, boolean setAsset, String[] addresses, long... expectBalances) throws Exception {
        try {
            Assert.assertTrue("地址与期望余额参数不合法", addresses.length * 2 == expectBalances.length);
            BigInteger[][] prevBalances = new BigInteger[addresses.length][];
            int k = 0;
            for (String address : addresses) {
                prevBalances[k++] = this.getBalanceByAccount(address, assetChainId, assetId);
            }
            ContractResultDto programResult;
            if (setAsset) {
                programResult = this.callOfDesignatedAssetByParams(contract, sender, method, value.toString(), args, assetChainId, assetId);
            } else {
                programResult = this.callByParams(contract, sender, method, value.toString(), args);
            }
            Assert.assertTrue(String.format("测试方法[%s]expect success, errorMsg: %s, stackTrace: %s", method, programResult.getErrorMessage(), programResult.getStackTrace()), programResult.isSuccess());

            List<ProgramTransfer> transfers = new ArrayList<>();
            List<ContractMergedTransferDto> transferDtos = programResult.getTransfers();
            for (ContractMergedTransferDto dto : transferDtos) {
                transfers.addAll(converterTransfer(dto));
            }
            List<ContractMultyAssetMergedTransferDto> multyAssetTransferDtos = programResult.getMultyAssetTransfers();
            for (ContractMultyAssetMergedTransferDto dto : multyAssetTransferDtos) {
                transfers.addAll(converterTransfer(dto));
            }

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
            Assert.assertTrue(String.format("测试方法[%s]期望 A: %s, 实际: %s", method, expectBalances[0], toNuls(balanceA)), balanceA.longValue() == toNa(BigDecimal.valueOf(expectBalances[0])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 B: %s, 实际: %s", method, expectBalances[1], toNuls(balanceB)), balanceB.longValue() == toNa(BigDecimal.valueOf(expectBalances[1])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 sender: %s, 实际: %s", method, expectBalances[2], toNuls(balanceSender)), balanceSender.longValue() == toNa(BigDecimal.valueOf(expectBalances[2])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 A-锁定: %s, 实际: %s", method, expectBalances[3], toNuls(balanceALock)), balanceALock.longValue() == toNa(BigDecimal.valueOf(expectBalances[3])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 B-锁定: %s, 实际: %s", method, expectBalances[4], toNuls(balanceBLock)), balanceBLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[4])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 sender-锁定: %s, 实际: %s", method, expectBalances[5], toNuls(balanceSenderLock)), balanceSenderLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[5])).longValue());


            BigInteger[][] currentBalances = new BigInteger[addresses.length][];
            k = 0;
            for (String address : addresses) {
                currentBalances[k++] = this.getBalanceByAccount(address, assetChainId, assetId);
            }
            BigInteger fee = BigInteger.ZERO;
            boolean mainAsset = assetChainId == this.chainId && assetId == this.assetId;
            if (mainAsset) {
                fee = new BigInteger(programResult.getTxSizeFee()).add(new BigInteger(programResult.getActualContractFee()));
            }
            balanceA = currentBalances[0][0].subtract(prevBalances[0][0]);
            balanceB = currentBalances[1][0].subtract(prevBalances[1][0]);
            balanceSender = currentBalances[2][0].add(toNa(BigDecimal.valueOf(100L))).subtract(prevBalances[2][0]).add(fee);
            balanceALock = currentBalances[0][1].subtract(prevBalances[0][1]);
            balanceBLock = currentBalances[1][1].subtract(prevBalances[1][1]);
            balanceSenderLock = currentBalances[2][1].subtract(prevBalances[2][1]);
            Assert.assertTrue(String.format("测试方法[%s]期望 A: %s, 实际: %s", method, expectBalances[0], toNuls(balanceA)), balanceA.longValue() == toNa(BigDecimal.valueOf(expectBalances[0])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 B: %s, 实际: %s", method, expectBalances[1], toNuls(balanceB)), balanceB.longValue() == toNa(BigDecimal.valueOf(expectBalances[1])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 sender: %s, 实际: %s", method, expectBalances[2], toNuls(balanceSender)), balanceSender.longValue() == toNa(BigDecimal.valueOf(expectBalances[2])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 A-锁定: %s, 实际: %s", method, expectBalances[3], toNuls(balanceALock)), balanceALock.longValue() == toNa(BigDecimal.valueOf(expectBalances[3])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 B-锁定: %s, 实际: %s", method, expectBalances[4], toNuls(balanceBLock)), balanceBLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[4])).longValue());
            Assert.assertTrue(String.format("测试方法[%s]期望 sender-锁定: %s, 实际: %s", method, expectBalances[5], toNuls(balanceSenderLock)), balanceSenderLock.longValue() == toNa(BigDecimal.valueOf(expectBalances[5])).longValue());

            System.out.println(String.format("method [%s] 测试通过", method));
        } catch (Throwable e) {
            System.err.println(String.format("method [%s] 测试失败", method));
            e.printStackTrace();
        }

    }

    protected void testFailed(String contract, String sender, String method, String[] args, BigInteger value, int assetChainId, int assetId, boolean setAsset, String errorMsgKey) throws Exception {
        try {
            System.out.println("清空合约余额");
            ContractResultDto contractResult;
            contractResult = this.callByParams(contractA, toAddress, "clearBalance", "0", new String[]{});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());
            contractResult = this.callByParams(contractB, toAddress, "clearBalance", "0", new String[]{});
            Assert.assertTrue("expect success, " + contractResult.getErrorMessage() + ", " + contractResult.getStackTrace(), contractResult.isSuccess());

            ContractResultDto programResult;
            if (setAsset) {
                programResult = this.callOfDesignatedAssetByParams(contract, sender, method, value.toString(), args, assetChainId, assetId);
            } else {
                programResult = this.callByParams(contract, sender, method, value.toString(), args);
            }
            System.out.println("errorMsg: " + programResult.getErrorMessage());
            Assert.assertFalse(String.format("测试方法[%s]expect failed, errorMsg: %s, stackTrace: %s", method, programResult.getErrorMessage(), programResult.getStackTrace()), programResult.isSuccess());
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains(errorMsgKey)) {
                System.out.println(String.format("method [%s] 测试通过，期望: %s", method, errorMsgKey));
            } else {
                System.err.println(String.format("method [%s] 测试失败, error: %s", method, e.getMessage()));
            }
        }
    }

    protected BigInteger[] getBalanceByAccount(String account, int assetChainId, int assetId) throws Exception {
        /*
        {
            "available" : 0,
            "permanentLocked" : 0,
            "freeze" : 0,
            "nonce" : "0000000000000000",
            "timeHeightLocked" : 0,
            "nonceType" : 1
        }
        */
        Map<String, Object> balance = LedgerCall.getBalanceAndNonce(chain, assetChainId, assetId, account);
        BigInteger[] result = new BigInteger[2];
        result[0] = new BigInteger(balance.get("available").toString());
        result[1] = new BigInteger(balance.get("freeze").toString());
        return result;
    }

    protected String createContract(InputStream in, String alias) throws Exception {
        byte[] contractCode = IOUtils.toByteArray(in);
        String remark = "test multy asset " + alias;
        Map params = this.makeCreateParams(toAddress, contractCode, alias, remark);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CREATE, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CREATE));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        String contractAddress = (String) result.get("contractAddress");
        Map map = waitGetContractTx(hash);
        assertTrue(map);
        return contractAddress;
    }

    protected ContractResultDto converterDto(Map contractMap) {
        ContractResultDto dto = JSONUtils.map2pojo(contractMap, ContractResultDto.class);
        List<Map> transfers = (List<Map>) contractMap.get("transfers");
        List<ContractMergedTransferDto> mergedTransferDtoList = new ArrayList<>();
        for (Map transfer : transfers) {
            ContractMergedTransferDto transferDto = JSONUtils.map2pojo(transfer, ContractMergedTransferDto.class);
            List<Map> outputs = (List<Map>) transfer.get("outputs");
            List<ContractOutputDto> outputDtoList = new ArrayList<>();
            for (Map output : outputs) {
                outputDtoList.add(JSONUtils.map2pojo(output, ContractOutputDto.class));
            }
            transferDto.setOutputs(outputDtoList);
            mergedTransferDtoList.add(transferDto);
        }
        dto.setTransfers(mergedTransferDtoList);

        List<Map> multyAssetTransfers = (List<Map>) contractMap.get("multyAssetTransfers");
        List<ContractMultyAssetMergedTransferDto> multyAssetTransferDtoList = new ArrayList<>();
        for (Map transfer : multyAssetTransfers) {
            ContractMultyAssetMergedTransferDto transferDto = JSONUtils.map2pojo(transfer, ContractMultyAssetMergedTransferDto.class);
            List<Map> outputs = (List<Map>) transfer.get("outputs");
            List<MultyAssetOutputDto> outputDtoList = new ArrayList<>();
            for (Map output : outputs) {
                outputDtoList.add(JSONUtils.map2pojo(output, MultyAssetOutputDto.class));
            }
            transferDto.setOutputs(outputDtoList);
            multyAssetTransferDtoList.add(transferDto);
        }
        dto.setMultyAssetTransfers(multyAssetTransferDtoList);

        return dto;
    }

    protected ContractResultDto callByParams(String contract, String sender, String methodName, String valueStr, Object[] args) throws Exception {
        BigInteger value = toNa(new BigDecimal(valueStr));
        Map params = this.makeCallParams(sender, value, contract, methodName, null, "", args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);

        Map map1 = (Map) map.get("contractResult");
        ContractResultDto dto = this.converterDto(map1);
        return dto;
    }

    protected ContractResultDto callOfDesignatedAssetByParams(String contract, String sender, String methodName, String valueStr, Object[] args, int assetChainId, int assetId) throws Exception {
        BigInteger value = toNa(new BigDecimal(valueStr));
        Map params = this.makeCallParams(sender, null, gasLimit, gasPrice, contract, methodName, null, "", new ProgramMultyAssetValue[]{new ProgramMultyAssetValue(value, assetChainId, assetId)}, args);
        Response cmdResp2 = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, CALL, params);
        Map result = (HashMap) (((HashMap) cmdResp2.getResponseData()).get(CALL));
        assertTrue(cmdResp2, result);
        String hash = (String) result.get("txHash");
        Map map = waitGetContractTx(hash);

        Map map1 = (Map) map.get("contractResult");
        ContractResultDto dto = this.converterDto(map1);
        return dto;
    }

    protected void assetRegisterTest() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("assetSymbol", "MTA");
        params.put("assetName", "MTA");
        params.put("initNumber", 100000000);
        params.put("decimalPlace", 8);
        params.put("txCreatorAddress", sender);
        params.put("assetOwnerAddress", sender);
        params.put("password", "nuls123456");
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "chainAssetTxReg", params);
        Assert.assertTrue(JSONUtils.obj2PrettyJson(response), response.isSuccess());
    }
}
