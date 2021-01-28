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
package io.nuls.contract.mock.storagestructure;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractConfig;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.util.ContractDBUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.util.VMContextMock;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.vm.DataWord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: PierreLuo
 * @date: 2021-01-28
 */
public class LoadLargeContractTest {

    protected ProgramExecutor programExecutor;
    private VMContext vmContext;
    protected static String dataPath = "/Users/pierreluo/IdeaProjects/nuls_newer_2.0/data_beta_61802";
    protected static int chainId = 2;
    protected static int assetId = 1;

    @BeforeClass
    public static void initClass() throws Exception {
        Log.info("init log.");
        Set<String> skipTables = new HashSet<>();
        skipTables.add(ContractDBConstant.DB_NAME_CONTRACT + "_" + chainId);
        RocksDBManager.init(dataPath + File.separator + "smart-contract", null, skipTables);
    }

    @Before
    public void setUp() {
        // 加载协议升级的数据
        ContractContext.CHAIN_ID = chainId;
        ContractContext.ASSET_ID = assetId;

        Chain chain = new Chain();
        ConfigBean configBean = new ConfigBean();
        configBean.setChainId(chainId);
        configBean.setAssetId(assetId);
        configBean.setMaxViewGas(100000000L);
        chain.setConfig(configBean);

        ContractConfig contractConfig = new ContractConfig();
        contractConfig.setDataPath(dataPath);
        SpringLiteContext.putBean(ContractConfig.class.getName(), contractConfig);

        vmContext = new VMContextMock();
        programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);
        protocolUpdate();
    }

    protected void protocolUpdate() {
        short version = 9;
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, version);
        if (version >= 8) {
            VMFactory.reInitVM_v8();
        }
    }

    @Test
    public void test() throws Exception {
        ProgramExecutor executor = programExecutor.begin(HexUtil.decode("812d5558cdfeba754cf31d5bb833b65646bd8148b5e855cd064da603ef03051b"));
        Field field = ProgramExecutorImpl.class.getDeclaredField("repository");
        field.setAccessible(true);
        RepositoryRoot root = (RepositoryRoot) field.get(executor);

        String contract = "tNULSeBaMxDoqtuYTWrRdayUJD2Mqx9sw2aL6A";
        checkOne(root, contract);
    }

    @Test
    public void testAll() throws Exception {
        ProgramExecutor executor = programExecutor.begin(HexUtil.decode("812d5558cdfeba754cf31d5bb833b65646bd8148b5e855cd064da603ef03051b"));
        Field field = ProgramExecutorImpl.class.getDeclaredField("repository");
        field.setAccessible(true);
        RepositoryRoot root = (RepositoryRoot) field.get(executor);

        List<ContractAddressInfoPo> contracts = allContracts();
        List<String> correctList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        for (ContractAddressInfoPo contractInfo : contracts) {
            String contract = AddressTool.getStringAddressByBytes(contractInfo.getContractAddress());
            if (!checkOne(root, contract)) {
                errorList.add(contract);
            } else {
                correctList.add(contract);
            }
        }
        System.out.println("correct: " + Arrays.toString(correctList.toArray()));
        System.out.println("error: " + Arrays.toString(errorList.toArray()));
    }

    boolean checkOne(RepositoryRoot root, String contract) {
        try {
            System.out.println(String.format("contract address: %s", contract));
            byte[] contractBytes = AddressTool.getAddress(contract);
            String key = String.format("R_%s,0", contract);
            //key = "objectRefCount";

            DataWord dataWord = getDataWord(root, contractBytes, key);
            if (dataWord == null) {
                System.err.println("NULL DataWord");
                return false;
            }
            Map<String, Object> map = JSONUtils.json2map(dataWord.asString());
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (e.getValue() == null) {
                    continue;
                }
                String value = e.getValue().toString();
                if (value.endsWith(",m")) {
                    // Map
                    DataWord dw = getDataWord(root, contractBytes, value);
                    Map<String, Object> _map = JSONUtils.json2map(dw.asString());
                    int mapSize = Integer.parseInt(_map.get("size").toString().substring(2));
                    System.out.println(String.format("mapSize: %s", mapSize));
                } else if (value.endsWith(",g")) {
                    // list集合
                    DataWord dw = getDataWord(root, contractBytes, value);
                    Map<String, Object> _map = JSONUtils.json2map(dw.asString());
                    int listSize = Integer.parseInt(_map.get("size").toString().substring(2));
                    System.out.println(String.format("listSize: %s", listSize));
                } else if (value.endsWith(",Ljava/util/HashSet;")) {
                    // set集合
                    DataWord dw = getDataWord(root, contractBytes, value);
                    Map<String, Object> _map = JSONUtils.json2map(dw.asString());
                    dw = getDataWord(root, contractBytes, _map.get("map").toString());
                    _map = JSONUtils.json2map(dw.asString());
                    int size = Integer.parseInt(_map.get("size").toString().substring(2));
                    System.out.println(String.format("setSize: %s", size));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    DataWord getDataWord(RepositoryRoot root, byte[] contractBytes, String key) {
        if (key.endsWith(",0")) {
            String keySub = key.substring(0, key.length() - 1);
            DataWord dataWord = getDataWord(root, contractBytes, keySub, 0);
            return dataWord;
        } else {
            return getDataWord(root, contractBytes, key, null);
        }
    }

    DataWord getDataWord(RepositoryRoot root, byte[] contractBytes, String keySub, Integer index) {
        String key;
        if (index != null) {
            if (index > 20) return null;
            key = keySub + index;
        } else {
            key = keySub;
        }
        DataWord dataWord = root.getStorageValue(contractBytes, new DataWord(key));
        if (dataWord != null && index != null) {
            System.out.println(String.format("DB get - get key: %s, get value: %s", key.toString(), dataWord == null ? null : dataWord.asString()));
        }
        if (dataWord == null && index != null) {
            dataWord = getDataWord(root, contractBytes, keySub, index + 1);
        }
        return dataWord;
    }

    List<ContractAddressInfoPo> allContracts() {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + "_" + chainId);
        if (list == null || list.size() == 0) {
            return Collections.emptyList();
        }
        List<ContractAddressInfoPo> resultList = new ArrayList<>();
        ContractAddressInfoPo po;
        for (Entry<byte[], byte[]> entry : list) {
            po = ContractDBUtil.getModel(entry.getValue(), ContractAddressInfoPo.class);
            po.setContractAddress(entry.getKey());
            resultList.add(po);
        }
        return resultList;
    }
}
