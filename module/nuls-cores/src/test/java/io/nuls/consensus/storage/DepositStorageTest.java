package io.nuls.consensus.storage;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.consensus.model.po.DepositPo;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class DepositStorageTest {
    private DepositStorageService depositStorageService;
    private NulsHash hash = NulsHash.calcHash(new byte[23]);

    //    @Before
//    public void init() {
//        try {
//            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
//            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
//            RocksDBService.init(path);
//            TestUtil.initTable(1);
//        } catch (Exception e) {
//            Log.error(e);
//        }
//        SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
//        depositStorageService = SpringLiteContext.getBean(DepositStorageService.class);
//    }
    @Test
    public void getAgentList() throws Exception {
        RocksDBService.init("/Users/niels/Downloads/nuls-data");
        List<byte[]> list = RocksDBService.valueList("consensus_deposit1");
        List<DepositPo> agentPoList = new ArrayList<>();
        for (byte[] val : list) {
            DepositPo po = new DepositPo();
            po.parse(val, 0);
            agentPoList.add(po);
        }
        List<String> agentList = new ArrayList<>();
        agentList.add("528a630b43f5d1eeea5b4567e87c7f7f3d4b86046b8a3d079ef0b9a1aea64360");
        agentList.add("a27170a4ad246758cc7fb45ded14b065f6a1919836a2bba34e6dcd9335a054da");
        agentList.add("ad82dc5237378a39abb3bbd8174ac0f77c882573a02c8fac01b4c7a058a96d90");
        agentList.add("d11d29e38b3db75aec0ebb69dc66eb4f6276d0a1d9c7faa6a4fa33b699637447");
        for (DepositPo po : agentPoList) {
            if (!agentList.contains(po.getAgentHash().toHex())) {
                continue;
            }
            System.out.println(po.getAgentHash().toHex() + " , " + AddressTool.getStringAddressByBytes(po.getAddress()) + " , " + po.getDeposit().toString() + " ,  " + po.getDelHeight());
        }
    }

    @Test
    public void saveDeposit() throws Exception {
        DepositPo po = new DepositPo();
        po.setAgentHash(hash);
        po.setTxHash(hash);
        po.setDelHeight(-1);
        po.setAddress(new byte[23]);
        po.setDeposit(BigInteger.valueOf(20000));
        po.setTime(NulsDateUtils.getCurrentTimeSeconds());
        po.setBlockHeight(100);
        System.out.println(depositStorageService.save(po, 1));
        getDepositList();
    }


    @Test
    public void getDeposit() {
        DepositPo po = depositStorageService.get(hash, 1);
        assertNotNull(po);
    }

    @Test
    public void deleteDeposit() {
        System.out.println(depositStorageService.delete(hash, 1));
        getDeposit();
    }

    @Test
    public void getDepositList() throws Exception {
        List<DepositPo> depositPos = depositStorageService.getList(1);
        System.out.println(depositPos.size());
    }
}
