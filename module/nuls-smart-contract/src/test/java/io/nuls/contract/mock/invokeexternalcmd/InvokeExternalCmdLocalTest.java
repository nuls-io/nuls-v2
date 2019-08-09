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
package io.nuls.contract.mock.invokeexternalcmd;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.basic.AddressTool;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.helper.ContractNewTxFromOtherModuleHandler;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.manager.interfaces.RequestAndResponseInterface;
import io.nuls.contract.mock.helper.ContractHelperMock;
import io.nuls.contract.mock.helper.ContractNewTxFromOtherModuleHandlerMock;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.util.*;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.*;

public class InvokeExternalCmdLocalTest {

    private VMContext vmContext;
    private ProgramExecutor programExecutor;
    private CmdRegisterManager cmdRegisterManager;

    private static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    private static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    private static final String BUYER = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";

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
        vmContext = new VMContextMock();
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);

        cmdRegisterManager = new CmdRegisterManager();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chain.getChainId(), chain);
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        cmdRegisterMap.put("returnStringNewTx", new CmdRegister("sc", "returnStringNewTx", CmdRegisterMode.NEW_TX, List.of("name", "age"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("returnString", new CmdRegister("sc", "returnString", CmdRegisterMode.QUERY_DATA, List.of("name", "age"), CmdRegisterReturnType.STRING));
        cmdRegisterMap.put("returnStringArray", new CmdRegister("sc", "returnStringArray", CmdRegisterMode.QUERY_DATA, List.of("name", "age"), CmdRegisterReturnType.STRING_ARRAY));
        cmdRegisterMap.put("returnString2Array", new CmdRegister("sc", "returnString2Array", CmdRegisterMode.QUERY_DATA, List.of("name", "age"), CmdRegisterReturnType.STRING_TWO_DIMENSIONAL_ARRAY));
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        // 默认的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                return InvokeExternalCmdLocalTest.this.defaultRequestAndResponse(moduleCode, cmdName, args);
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
        Response response = MessageUtil.newSuccessResponse("888888");
        return response;
    }

    @Test
    public void testValidAddress() {
        Assert.assertTrue(AddressTool.validAddress(2, "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG"));
    }

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/contract-invoke-external-cmd-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCreate.setSender(NativeAddress.toBytes(SENDER));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
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
    public void testInvokeTwoDimensionalMethod() throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName(BALANCE_TRIGGER_METHOD_NAME);
        programCall.setMethodDesc(BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC);
        String[][] args = new String[2][];
        args[0] = new String[]{"a", "100"};
        args[1] = new String[]{"c", "200"};
        programCall.setArgs(args);

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        byte[] newRootBytes = track.getRoot();
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(newRootBytes));
        System.out.println();

        programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("result");
        track = programExecutor.begin(newRootBytes);
        programResult = track.call(programCall);
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println();

        sleep();
    }

    @Test
    public void testContractMethod() throws IOException {

        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/contract-invoke-external-cmd-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);

        System.out.println();
        System.out.println(JSONUtils.obj2PrettyJson(programMethods));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnString() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                Map cmd = new HashMap(2);
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, "this is the return value.");
                cmd.put(cmdName, map);
                response.setResponseData(cmd);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnString";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();


        //System.out.println(JSON.toJSONString(programResult, true));
        //System.out.println(JSONObject.toJSONString(programResult, true));
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnStringArray() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                String[] stringArray = {"a", "b", "c", "d", "e"};
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, stringArray);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringArrayView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnStringArray";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnStringArrayList() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                List stringArrayList = List.of("a", "b", "c", "d", "e");
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, stringArrayList);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringArrayView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnStringArray";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnStringTwoDimensionalArray() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                String[] stringArray0 = {
                        "a0",
                        "b0",
                        "c0",
                        "d0",
                        "e0"};
                String[] stringArray1 = {
                        "a1",
                        "b1",
                        "c1",
                        "d1",
                        "e1"};
                String[] stringArray2 = {
                        "a2",
                        "b2",
                        "c2",
                        "d2",
                        "e2"};
                String[][] string2Array = {stringArray0, stringArray1, stringArray2};
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, string2Array);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringTwoDimensionalArrayView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnString2Array";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnStringTwoDimensionalArrayObject() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                String[] stringArray0 = {
                        "a0",
                        "b0",
                        "c0",
                        "d0",
                        "e0"};
                String[] stringArray1 = {
                        "a1",
                        "b1",
                        "c1",
                        "d1",
                        "e1"};
                String[] stringArray2 = {
                        "a2",
                        "b2",
                        "c2",
                        "d2",
                        "e2"};
                String[][] string2Array = {stringArray0, stringArray1, stringArray2};
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, string2Array);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(10000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringTwoDimensionalArrayObjectView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnString2Array";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testCallReturnStringTwoDimensionalArrayList() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                List stringArrayList0 = List.of(
                        "a0",
                        "b0",
                        "c0",
                        "d0",
                        "e0");
                List stringArrayList1 = List.of(
                        "a1",
                        "b1",
                        "c1",
                        "d1",
                        "e1");
                List stringArrayList2 = List.of(
                        "a2",
                        "b2",
                        "c2",
                        "d2",
                        "e2");
                String[] stringArray0 = {
                        "a0",
                        "b0",
                        "c0",
                        "d0",
                        "e0"};
                String[] stringArray1 = {
                        "a1",
                        "b1",
                        "c1",
                        "d1",
                        "e1"};
                String[] stringArray2 = {
                        "a2",
                        "b2",
                        "c2",
                        "d2",
                        "e2"};

                List string2ArrayList = List.of(stringArrayList0, stringArrayList1, stringArrayList2, stringArray0, stringArray1, stringArray2);
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, string2ArrayList);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringTwoDimensionalArrayView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnString2Array";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(track.getRoot()));
        System.out.println();

        sleep();
    }

    @Test
    public void testReturnStringNewTx() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                String[] result = {"this is the return new TxHash.", "this is the return new TxString."};
                Map map = new HashMap(2);
                map.put(RPC_RESULT_KEY, result);
                response.setResponseData(map);
                return response;
            }
        });

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("invokeReturnStringNewTxView");
        programCall.setMethodDesc("");
        Object[] args = new Object[2];
        String[] arg1 = {"a", "b"};
        args[0] = "returnStringNewTx";
        args[1] = arg1;
        programCall.setArgs(ContractUtil.twoDimensionalArray(args));

        System.out.println(programCall);
        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("0139455b50ac8c37446793583774d25bceeaa792bd6f3b83b1c5d6859c27b42b");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(JSONUtils.obj2PrettyJson(programResult));
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
