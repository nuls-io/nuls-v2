/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.mock.pocm;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.base.Base;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractNewTxFromOtherModuleHandler;
import io.nuls.contract.mock.helper.ContractHelperMock;
import io.nuls.contract.mock.helper.ContractNewTxFromOtherModuleHandlerMock;
import io.nuls.contract.mock.invokeexternalcmd.InvokeExternalCmdLocalTest;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.manager.interfaces.RequestAndResponseInterface;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.model.dto.BlockHeaderDto;
import io.nuls.contract.util.*;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramCreate;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.RPC_RESULT_KEY;

/**
 * @author: PierreLuo
 * @date: 2019-05-08
 */
public class ContractPOCMLocalTest extends Base {

    private VMContext vmContext;
    private ProgramExecutor programExecutor;
    private CmdRegisterManager cmdRegisterManager;
    private BigInteger contractBalance = BigInteger.ZERO;
    private long blockHeight = chainId;

    private static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    private static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";

    class VMContextMockBalanceAndBlock extends VMContextMock {
        @Override
        public ContractBalance getBalance(int chainId, byte[] address) {
            ContractBalance balance = ContractBalance.newInstance();
            balance.setBalance(contractBalance);
            return balance;
        }

        @Override
        protected BlockHeaderDto newDto(int chainId) {
            BlockHeaderDto dto = super.newDto(chainId);
            dto.setHeight(blockHeight);
            return dto;
        }
    }
    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        RocksDBService.init("./data");
    }

    @Before
    public void setUp() {
        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(2);
        configBean.setAssetsId(1);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);
        vmContext = new VMContextMockBalanceAndBlock();
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);

        cmdRegisterManager = new CmdRegisterManager();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chain.getChainId(), chain);
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        cmdRegisterMap.put("cs_createContractAgent", new CmdRegister("cs", "cs_createContractAgent", CmdRegisterMode.NEW_TX, List.of("packingAddress", "deposit", "commissionRate"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("cs_contractDeposit", new CmdRegister("cs", "cs_contractDeposit", CmdRegisterMode.NEW_TX, List.of("agentHash", "deposit"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("cs_stopContractAgent", new CmdRegister("cs", "cs_stopContractAgent", CmdRegisterMode.NEW_TX, List.of(), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("cs_contractWithdraw", new CmdRegister("cs", "cs_contractWithdraw", CmdRegisterMode.NEW_TX, List.of("joinAgentHash"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("cs_getContractAgentInfo", new CmdRegister("cs", "cs_getContractAgentInfo", CmdRegisterMode.QUERY_DATA, List.of("agentHash"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("cs_getContractDepositInfo", new CmdRegister("cs", "cs_getContractDepositInfo", CmdRegisterMode.QUERY_DATA, List.of("joinAgentHash"), CmdRegisterReturnType.STRING_ARRAY));
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        // 默认的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                return ContractPOCMLocalTest.this.defaultRequestAndResponse(moduleCode, cmdName, args);
            }
        });
        SpringLiteContext.putBean(CmdRegisterManager.class.getName(), cmdRegisterManager);

        ContractHelper contractHelper = new ContractHelperMock();
        SpringLiteContext.putBean(ContractHelper.class.getName(), contractHelper);

        ContractNewTxFromOtherModuleHandler handler = new ContractNewTxFromOtherModuleHandlerMock();
        SpringLiteContext.putBean(ContractNewTxFromOtherModuleHandler.class.getName(), handler);
    }

    private Response defaultRequestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
        Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
        String[] result = null;
        String[] resultNewTX = new String[]{"e3f57c0c08d9ac5f73523f8f69c340ac80c24588c4968bf4056e97319c14af5f", "1400c06397956a010d7472616e736665722074657374008c0117020001f7ec6473df12e751d64cf20a8baa7edd50810f810200010000ab90410000000000000000000000000000000000000000000000000000000008ffffffffffffffff0001170200017fe9a685e43b3124e00fd9c8e4e59158baea63450200010000ca9a3b0000000000000000000000000000000000000000000000000000000000000000000000006a2103958b790c331954ed367d37bac901de5c2f06ac8368b37d7bd6cd5ae143c1d7e34730450221008c72cc7caadd344e09b92845dd2e074427d67ffcf828e9e2e3b683533cdef0ac02203094c8137b78d0349a22e93c333ed0d9c24b0523570200c6ef3fa5ae409b3073"};
        String[] resultGetAgent = new String[9];
        String[] resultGetDeposit = new String[8];
        switch (cmdName) {
            case "cs_createContractAgent" :
            case "cs_contractDeposit" :
            case "cs_stopContractAgent" :
            case "cs_contractWithdraw" : result = resultNewTX;break;
            case "cs_getContractAgentInfo" :
                resultGetAgent[7] = "-1";
                result = resultGetAgent;
                break;
            case "cs_getContractDepositInfo" : result = resultGetDeposit;break;
            default:break;
        }

        Response response = MessageUtil.newSuccessResponse("888888");
        Map map = new HashMap(2);
        map.put(RPC_RESULT_KEY, result);
        Map map1 = new HashMap(2);
        map1.put(cmdName, map);
        response.setResponseData(map1);
        return response;
    }

    private String prevStateRootString = "bbf73528cf3f0c39381b87fabf0b8bbce045cef6e53f358d5fa12d198bf44ec5";

    @Test
    public void createContract() throws IOException {
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/pocmContract-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCreate.setSender(NativeAddress.toBytes(SENDER));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
        Object[] args = new Object[]{"pocManager", "POCM", 100000000, 8, 5000, 5, 200, 20, true, "tNULSeBaMtEPLXxUgyfnBt9bpb5Xv84dyJV98p",
                                    null, null, null, null};
        programCreate.setArgs(ContractUtil.twoDimensionalArray(args));

        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.create(programCreate);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void createAgent() throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setValue(BigInteger.valueOf(20001_00000000L));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("createAgentByOwner");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        byte[] newRootBytes = track.getRoot();
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(newRootBytes));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Before
    public void updateBalance() {
        contractBalance = BigInteger.valueOf(0000_0000_0000L);
    }

    @Test
    public void depositForOwn() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(toAddress1));
        programCall.setValue(BigInteger.valueOf(800_00000000L));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("depositForOwn");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void quit() throws IOException {
        blockHeight += 21;
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(toAddress0));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("quit");
        programCall.setMethodDesc("");
        programCall.args("0");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void takeBackUnLockDeposit() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(toAddress6));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("takeBackUnLockDeposit");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void transferConsensusReward() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("transferConsensusRewardByOwner");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void takeBackConsensusCreateAgentDepositByOwner() throws IOException {
        contractBalance = BigInteger.valueOf(50000_00000000L);
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("takeBackConsensusCreateAgentDepositByOwner");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }

    @Test
    public void stopAgentManuallyByOwner() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("stopAgentManuallyByOwner");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();
        Assert.assertTrue(programResult.isSuccess());

        sleep();
    }


    @Test
    public void getContractWholeInfo() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("wholeConsensusInfoForTest");
        programCall.setMethodDesc("");

        byte[] prevStateRoot = HexUtil.decode(prevStateRootString);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        Assert.assertTrue(programResult.isSuccess());
        System.out.println(String.format("invoke view contract method cost: %s gas", programResult.getGasUsed()));
        String result = programResult.getResult();
        if(StringUtils.isNotBlank(result)) {
            System.out.println(JSONUtils.obj2PrettyJson(JSONUtils.json2map(result)));
        }
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    public static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
