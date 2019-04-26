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
package io.nuls.contract.invokeexternalcmd;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.manager.interfaces.RequestAndResponseInterface;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.util.*;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramCreate;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.parse.JSONUtils;
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

public class InvokeExternalCmdTest {

    private VMContext vmContext;
    private ProgramExecutor programExecutor;
    private CmdRegisterManager cmdRegisterManager;

    private static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    private static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    private static final String BUYER = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";

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
        vmContext = new VMContextTest();
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);

        cmdRegisterManager = new CmdRegisterManager();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chain.getChainId(), chain);
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        cmdRegisterMap.put("returnStringNewTx", new CmdRegister("sc", "returnStringNewTx", CmdRegisterMode.NEW_TX, List.of("name", "age"), CmdRegisterReturnType.STRING));
        cmdRegisterMap.put("returnString", new CmdRegister("sc", "returnString", CmdRegisterMode.QUERY_DATA, List.of("name", "age"), CmdRegisterReturnType.STRING));
        cmdRegisterMap.put("returnStringArray", new CmdRegister("sc", "returnStringArray", CmdRegisterMode.QUERY_DATA, List.of("name", "age"), CmdRegisterReturnType.STRING_ARRAY));
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        // 默认的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                return InvokeExternalCmdTest.this.defaultRequestAndResponse(moduleCode, cmdName, args);
            }
        });
        SpringLiteContext.putBean(CmdRegisterManager.class.getName(), cmdRegisterManager);
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
        InputStream in = new FileInputStream(InvokeExternalCmdTest.class.getResource("/contract-invoke-external-cmd-test.jar").getFile());
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
    public void testCallReturnString() throws IOException {
        // 自定义的requestAndResponseInterface
        BeanUtilTest.setBean(cmdRegisterManager, "requestAndResponseInterface", new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
                Log.info("moduleCode: [{}], cmdName: [{}], args: [{}]", moduleCode, cmdName, JSONUtils.obj2PrettyJson(args));
                Response response = MessageUtil.newSuccessResponse("888888");
                Map resultData = new HashMap(2);
                resultData.put("result", "this is the return value.");
                response.setResponseData(resultData);
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

        byte[] prevStateRoot = HexUtil.decode("47f3d661db7d9f98901973965f6d3ccbef433decee9ff5079b23e49c595233b0");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

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
                Map resultData = new HashMap(2);
                String[] stringArray = {"a", "b", "c", "d", "e"};
                resultData.put("result", stringArray);
                response.setResponseData(resultData);
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

        byte[] prevStateRoot = HexUtil.decode("47f3d661db7d9f98901973965f6d3ccbef433decee9ff5079b23e49c595233b0");

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
                Map resultData = new HashMap(2);
                List stringArrayList = List.of("a", "b", "c", "d", "e");
                resultData.put("result", stringArrayList);
                response.setResponseData(resultData);
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

        byte[] prevStateRoot = HexUtil.decode("47f3d661db7d9f98901973965f6d3ccbef433decee9ff5079b23e49c595233b0");

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
                Map resultData = new HashMap(4);
                resultData.put("result", "this is the return new Tx.");
                response.setResponseData(resultData);
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

        byte[] prevStateRoot = HexUtil.decode("47f3d661db7d9f98901973965f6d3ccbef433decee9ff5079b23e49c595233b0");

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
