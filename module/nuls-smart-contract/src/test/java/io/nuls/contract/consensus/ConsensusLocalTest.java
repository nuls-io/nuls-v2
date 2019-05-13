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
package io.nuls.contract.consensus;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.base.Base;
import io.nuls.contract.invokeexternalcmd.InvokeExternalCmdLocalTest;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.util.BeanUtilTest;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.util.VMContextMock;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramCreate;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

import static io.nuls.contract.constant.ContractConstant.BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC;
import static io.nuls.contract.constant.ContractConstant.BALANCE_TRIGGER_METHOD_NAME;

/**
 * @author: PierreLuo
 * @date: 2019-05-08
 */
public class ConsensusLocalTest extends Base {

    private static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    private static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    private VMContext vmContext;
    private ProgramExecutor programExecutor;
    private CmdRegisterManager cmdRegisterManager;

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
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        SpringLiteContext.putBean(CmdRegisterManager.class.getName(), cmdRegisterManager);


    }

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/contract-consensus-test.jar").getFile());
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
    }

    @Test
    public void testInvokePayableMethod() throws Exception {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName(BALANCE_TRIGGER_METHOD_NAME);
        programCall.setMethodDesc(BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC);
        String[][] args = new String[35][];
        programCall.setArgs(args);
        Random random = new Random();
        random.nextLong();
        for (int i = 0; i < 35; i++) {
            args[i] = new String[]{address("getToAddress", i), String.valueOf(Math.abs(random.nextInt()))};
        }

        System.out.println(JSONUtils.obj2PrettyJson(programCall));

        byte[] prevStateRoot = HexUtil.decode("6f16fb71a93efb41fc89d45099cfcc026f3786ffc827eda388e702a0c25a55b6");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        byte[] newRootBytes = track.getRoot();
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("pierre - stateRoot: " + HexUtil.encode(newRootBytes));
        System.out.println();

        // view
        //programCall = new ProgramCall();
        //programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        //programCall.setSender(NativeAddress.toBytes(SENDER));
        //programCall.setPrice(1);
        //programCall.setGasLimit(1000000);
        //programCall.setNumber(1);
        //programCall.setMethodName("getMiners");
        //
        //track = programExecutor.begin(newRootBytes);
        //programResult = track.call(programCall);
        //System.out.println(JSONUtils.obj2PrettyJson(programResult));
        //System.out.println();

    }

    @Test
    public void testGetMinerInfo() throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("getMinerInfo");
        String[] args = new String[]{toAddress0};
        programCall.setArgs(args);

        byte[] preStateRoot = HexUtil.decode("20cb2fc6e70174f171caac28f8d4f4f32f35b93a361a9d296b35e83af122dac4");

        ProgramExecutor track = programExecutor.begin(preStateRoot);
        ProgramResult programResult = track.call(programCall);
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println();
    }

    @Test
    public void testGetMiners() throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("getMiners");

        byte[] preStateRoot = HexUtil.decode("20cb2fc6e70174f171caac28f8d4f4f32f35b93a361a9d296b35e83af122dac4");

        ProgramExecutor track = programExecutor.begin(preStateRoot);
        ProgramResult programResult = track.call(programCall);
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println();
    }

}
