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
package io.nuls.contract.mock.basetest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.base.Base;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.CmdRegisterManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.util.BeanUtilTest;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.util.VMContextMock;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rockdb.service.RocksDBService;
import org.junit.Before;
import org.junit.BeforeClass;

import java.math.BigInteger;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public abstract class MockBase extends Base {

    protected static final String ADDRESS = "tNULSeBaN7vAqBANTtVxsiFsam4NcRUbqrCpzK";
    protected static final String SENDER = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected ProgramExecutor programExecutor;
    private VMContext vmContext;
    private CmdRegisterManager cmdRegisterManager;
    protected static String dataPath = "./data";

    @BeforeClass
    public static void initClass() {
        Log.info("init log.");
        RocksDBService.init(dataPath);
    }

    protected abstract void protocolUpdate();

    @Before
    public void setUp() {
        // 加载协议升级的数据
        ContractContext.CHAIN_ID = chainId;
        ContractContext.ASSET_ID = assetId;

        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(2);
        configBean.setAssetId(1);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);

        NulsCoresConfig contractConfig = new NulsCoresConfig();
        contractConfig.setDataPath(dataPath);
        SpringLiteContext.putBean(NulsCoresConfig.class.getName(), contractConfig);

        vmContext = new VMContextMock();
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);

        cmdRegisterManager = new CmdRegisterManager();
        ChainManager chainManager = new ChainManager();
        chainManager.getChainMap().put(chain.getChainId(), chain);
        BeanUtilTest.setBean(cmdRegisterManager, chainManager);
        SpringLiteContext.putBean(CmdRegisterManager.class.getName(), cmdRegisterManager);

        protocolUpdate();
    }

    protected byte[] create(byte[] prevStateRoot, String sender, byte[] contractCode, String... args) {
        return this.create(prevStateRoot, null, sender, contractCode, args);
    }

    protected byte[] create(byte[] prevStateRoot, String contractAddress, String sender, byte[] contractCode, String... args) {
        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(contractAddress == null ? NativeAddress.toBytes(ADDRESS) : NativeAddress.toBytes(contractAddress));
        programCreate.setSender(NativeAddress.toBytes(sender));
        programCreate.setPrice(1);
        programCreate.setGasLimit(10000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
        programCreate.setArgs(args);

        ProgramExecutor begin = programExecutor.begin(prevStateRoot);
        ProgramExecutor track = begin.startTracking();
        long s = System.currentTimeMillis();
        ProgramResult programResult = track.create(programCreate);
        track.commit();
        begin.commit();

        long e = System.currentTimeMillis();

        Log.info("create cost: " + (e - s));
        Log.info(programResult.toString());
        return begin.getRoot();
    }

    protected Object[] call(byte[] preStateRoot, String sender, String methodName, String[] args, BigInteger value) throws JsonProcessingException {
        return call(null, preStateRoot, sender, methodName, null, args, value);
    }

    protected Object[] call(byte[] preStateRoot, String sender, String methodName, String[] args) throws JsonProcessingException {
        return call(null, preStateRoot, sender, methodName, null, args, null);
    }

    protected Object[] call(byte[] preStateRoot, String sender, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        return call(null, preStateRoot, sender, methodName, methodDesc, args, null);
    }

    protected Object[] call(String contractAddress, byte[] preStateRoot, String sender, String methodName, String[] args) throws JsonProcessingException {
        return call(contractAddress, preStateRoot, sender, methodName, null, args, null);
    }

    protected Object[] call(String contractAddress, byte[] preStateRoot, String sender, String methodName, String[] args, BigInteger value) throws JsonProcessingException {
        return call(contractAddress, preStateRoot, sender, methodName, null, args, value);
    }

    protected Object[] call(String contractAddress, byte[] preStateRoot,  String sender, String methodName, String methodDesc, String[] args, BigInteger value) throws JsonProcessingException {
        Object[] objects = execute(contractAddress, preStateRoot, sender, methodName, methodDesc, args, value, null, null);
        ProgramExecutor track = (ProgramExecutor) objects[0];
        track.commit();
        ProgramResult programResult = (ProgramResult) objects[1];
        byte[] newRootBytes = track.getRoot();
        return new Object[]{newRootBytes, programResult};
    }

    protected Object[] call(String contractAddress, byte[] preStateRoot,  String sender, String methodName, String[] args, BigInteger value, Integer assetChainId, Integer assetId) throws JsonProcessingException {
        return call(contractAddress, preStateRoot, sender, methodName, null, args, value, assetChainId, assetId);
    }

    protected Object[] call(String contractAddress, byte[] preStateRoot,  String sender, String methodName, String methodDesc, String[] args, BigInteger value, Integer assetChainId, Integer assetId) throws JsonProcessingException {
        Object[] objects = execute(contractAddress, preStateRoot, sender, methodName, methodDesc, args, value, assetChainId, assetId);
        ProgramExecutor track = (ProgramExecutor) objects[0];
        track.commit();
        ProgramResult programResult = (ProgramResult) objects[1];
        byte[] newRootBytes = track.getRoot();
        return new Object[]{newRootBytes, programResult};
    }

    protected String view(String contractAddress, byte[] preStateRoot, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        ProgramResult programResult = (ProgramResult) executeView(contractAddress, preStateRoot, null, methodName, methodDesc, args)[1];
        Log.info(String.format("view cost: %s", programResult.getGasUsed()));
        return programResult.getResult();
    }

    protected String view(byte[] preStateRoot, String methodName, String[] args) throws JsonProcessingException {
        return view(null, preStateRoot, methodName, null, args);
    }

    protected String view(String contractAddress, byte[] preStateRoot, String methodName, String[] args) throws JsonProcessingException {
        return view(contractAddress, preStateRoot, methodName, null, args);
    }

    private Object[] execute(String contractAddress, byte[] preStateRoot, String sender, String methodName, String methodDesc, String[] args, BigInteger value, Integer assetChainId, Integer assetId) throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddress == null ? NativeAddress.toBytes(ADDRESS) : NativeAddress.toBytes(contractAddress));
        programCall.setSender(NativeAddress.toBytes(sender));
        programCall.setPrice(1);
        programCall.setGasLimit(10000000);
        programCall.setNumber(1);
        programCall.setValue(value == null ? BigInteger.ZERO : value);
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        if (assetChainId != null && assetId != null) {
            programCall.setValue(BigInteger.ZERO);
            programCall.setMultyAssetValues(List.of(new ProgramMultyAssetValue(value == null ? BigInteger.ZERO : value, assetChainId, assetId)));
        }
        ProgramExecutor begin = programExecutor.begin(preStateRoot);
        ProgramExecutor tracking = begin.startTracking();
        ProgramResult programResult = tracking.call(programCall);
        tracking.commit();
        return new Object[]{begin, programResult};
    }

    private Object[] executeView(String contractAddress, byte[] preStateRoot, String sender, String methodName, String methodDesc, String[] args) throws JsonProcessingException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(contractAddress == null ? NativeAddress.toBytes(ADDRESS) : NativeAddress.toBytes(contractAddress));
        programCall.setSender(NativeAddress.toBytes(sender));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(2);
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);

        ProgramExecutor begin = programExecutor.begin(preStateRoot);
        ProgramExecutor tracking = begin.startTracking();
        ProgramResult programResult = tracking.call(programCall);
        return new Object[]{begin, programResult};
    }

}
