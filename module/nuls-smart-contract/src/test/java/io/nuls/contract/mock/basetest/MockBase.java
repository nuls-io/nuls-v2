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
package io.nuls.contract.mock.basetest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.base.Base;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.model.bo.Chain;
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
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class MockBase extends Base {

    protected static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    protected static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected ProgramExecutor programExecutor;
    private VMContext vmContext;
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
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        SpringLiteContext.putBean(CmdRegisterManager.class.getName(), cmdRegisterManager);
    }

    protected byte[] create(byte[] prevStateRoot, String sender, byte[] contractCode, String... args) {
        return this.create(prevStateRoot, null, sender, contractCode, args);
    }

    protected byte[] create(byte[] prevStateRoot, String contractAddress, String sender, byte[] contractCode, String... args) {
        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(contractAddress == null ? NativeAddress.toBytes(ADDRESS) : NativeAddress.toBytes(contractAddress));
        programCreate.setSender(NativeAddress.toBytes(sender));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
        programCreate.setArgs(args);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.create(programCreate);
        track.commit();

        Log.info(programResult.toString());
        Log.info("\n");
        return track.getRoot();
    }

    protected Object[] call(byte[] preStateRoot, String sender, String methodName, String[] args) throws JsonProcessingException {
        return call(preStateRoot, sender, methodName, null, args);
    }

    protected Object[] call(byte[] preStateRoot, String sender, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        Object[] objects = execute(preStateRoot, sender, methodName, methodDesc, args);
        ProgramExecutor track = (ProgramExecutor) objects[0];
        track.commit();
        ProgramResult programResult = (ProgramResult) objects[1];
        byte[] newRootBytes = track.getRoot();
        return new Object[]{newRootBytes, programResult};
    }

    protected String view(byte[] preStateRoot, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        ProgramResult programResult = (ProgramResult) execute(preStateRoot, null, methodName, methodDesc, args)[1];
        Log.info(String.format("view cost: %s", programResult.getGasUsed()));
        return programResult.getResult();
    }

    protected String view(byte[] preStateRoot, String methodName, String[] args) throws JsonProcessingException {
        return view(preStateRoot, methodName, null, args);
    }

    private Object[] execute(byte[] preStateRoot, String sender, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(sender));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);

        ProgramExecutor track = programExecutor.begin(preStateRoot);
        ProgramResult programResult = track.call(programCall);
        return new Object[]{track, programResult};
    }

}
