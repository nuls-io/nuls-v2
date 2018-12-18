package io.nuls.test.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.test.TestUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.thread.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class DepositStorageTest {
    private DepositStorageService depositStorageService;
    private NulsDigestData hash = NulsDigestData.calcDigestData(new byte[23]);

    @Before
    public void init(){
        try {
            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
            TestUtil.initTable(1);
        }catch (Exception e){
            Log.error(e);
        }
        SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
        depositStorageService = SpringLiteContext.getBean(DepositStorageService.class);
    }

    @Test
    public void saveDeposit()throws Exception{
        DepositPo po = new DepositPo();
        po.setAgentHash(hash);
        po.setTxHash(hash);
        po.setDelHeight(-1);
        po.setAddress(new byte[23]);
        po.setDeposit(BigInteger.valueOf(20000));
        po.setTime(TimeService.currentTimeMillis());
        po.setBlockHeight(100);
        System.out.println(depositStorageService.save(po,1));
        getDepositList();
    }

    @Test
    public void getDeposit(){
        DepositPo po = depositStorageService.get(hash,1);
        assertNotNull(po);
    }

    @Test
    public void deleteDeposit(){
        System.out.println(depositStorageService.delete(hash,1));
        getDeposit();
    }

    @Test
    public void getDepositList()throws  Exception{
        List<DepositPo> depositPos = depositStorageService.getList(1);
        System.out.println(depositPos.size());
    }
}
