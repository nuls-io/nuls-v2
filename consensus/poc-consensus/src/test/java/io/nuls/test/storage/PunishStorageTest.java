package io.nuls.test.storage;

import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.PunishStorageService;
import io.nuls.poc.utils.enumeration.PunishReasonEnum;
import io.nuls.poc.utils.enumeration.PunishType;
import io.nuls.test.TestUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.thread.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class PunishStorageTest {
    private PunishStorageService punishStorageService;
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
        punishStorageService = SpringLiteContext.getBean(PunishStorageService.class);
    }

    @Test
    public void savePunish()throws Exception{
        PunishLogPo po = new PunishLogPo();
        po.setAddress(new byte[23]);
        po.setEvidence(new byte[30]);
        po.setHeight(88);
        po.setIndex(10);
        po.setReasonCode(PunishReasonEnum.BIFURCATION.getCode());
        po.setRoundIndex(102);
        po.setTime(TimeService.currentTimeMillis());
        po.setType(PunishType.RED.getCode());
        System.out.println(punishStorageService.save(po,1));
        getPunishList();
    }

    @Test
    public void getPunishList()throws Exception{
        List<PunishLogPo> poList = punishStorageService.getPunishList(1);
        assertNotNull(poList);
        System.out.println(poList.size());
    }

    @Test
    public void deletePunish()throws Exception{
        getPunishList();
        List<PunishLogPo> poList = punishStorageService.getPunishList(1);
        System.out.println(punishStorageService.delete(poList.get(0).getKey(),1));
        getPunishList();
    }
}
