package io.nuls.test.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.test.TestUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * 数据库相关操作测试
 *
 * @author tag
 * 2018/12/1
 * */
public class AgentStorageTest {
    private AgentStorageService agentStorageService;

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
        agentStorageService = SpringLiteContext.getBean(AgentStorageService.class);
    }
    
    @Test
    public void saveAgent(){
        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[23]);
        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(new byte[23]);
        agentPo.setRewardAddress(new byte[23]);
        agentPo.setPackingAddress(new byte[23]);
        agentPo.setDeposit(BigInteger.valueOf(20000));
        agentPo.setHash(hash);
        System.out.println(agentStorageService.save(agentPo,1));
    }

    @Test
    public void deleteAgent(){
        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[23]);

        boolean success = agentStorageService.delete(hash,1);

        assert(success);

        AgentPo agentPo = agentStorageService.get(hash,1);

        assertNull(agentPo);
    }

    @Test
    public void getAgent(){
        NulsDigestData hash = NulsDigestData.calcDigestData(new byte[23]);

        AgentPo agentPo = agentStorageService.get(hash,1);

        System.out.println(agentPo.getDeposit());

        assertNotNull(agentPo);

        assert(Arrays.equals(agentPo.getAgentAddress(), new byte[23]));
    }

    @Test
    public void getAgentList()throws Exception{
        List<AgentPo> agentPoList = agentStorageService.getList(1);
        for (AgentPo agentPo:agentPoList) {
            System.out.println(agentPo.getHash());
            System.out.println(agentPo.getDeposit());
        }
    }


    @Test
    public void repeatSaveAgent()throws Exception{
        int index = 0;
        while (true){
            saveAgent();
            index++;
            if(index>=5){
                break;
            }
        }
        getAgentList();
    }
}
