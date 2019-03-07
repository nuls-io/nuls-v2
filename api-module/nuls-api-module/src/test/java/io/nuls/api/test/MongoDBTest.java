package io.nuls.api.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.MongoDBService;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static io.nuls.api.constant.Constant.DEFAULT_SCAN_PACKAGE;

public class MongoDBTest {

    @Before
    public void before() {
        String dbName = "nuls-api";
        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
        MongoDBService mongoDBService = new MongoDBService(mongoClient, mongoDatabase);
        SpringLiteContext.putBean("dbService", mongoDBService);
        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
    }

    @Test
    public void testDBSave() {
        BlockHeaderInfo headerInfo = new BlockHeaderInfo();
        headerInfo.setHeight(0L);
        headerInfo.setTotalFee(new BigInteger("1000000000000000000000000000000000000001"));

        BlockService blockService = SpringLiteContext.getBean(BlockService.class);
        blockService.saveBLockHeaderInfo(12345, headerInfo);

    }

    @Test
    public void testDBGet() {
        BlockService blockService = SpringLiteContext.getBean(BlockService.class);
        BlockHeaderInfo headerInfo = blockService.getBlockHeader(12345, 0L);
        System.out.println();
    }


    @Test
    public void testTransferUseTime() {
        BlockHeaderInfo blockHeaderInfo = new BlockHeaderInfo();
        blockHeaderInfo.setHash("abcdefg");
        blockHeaderInfo.setHeight(1L);
        blockHeaderInfo.setAgentAlias("alias");
        blockHeaderInfo.setAgentHash("bbbbbb");
        blockHeaderInfo.setAgentId("dfsaf");
        blockHeaderInfo.setAgentVersion(2);
        blockHeaderInfo.setCreateTime(131313L);
        blockHeaderInfo.setMerkleHash("dfsdfsdfsd");
        blockHeaderInfo.setPackingAddress("dfasdfdasf");
        blockHeaderInfo.setPackingIndexOfRound(1);
        blockHeaderInfo.setPreHash("fdsfsdf");
        blockHeaderInfo.setReward(new BigInteger("123456"));
        blockHeaderInfo.setRoundIndex(1L);
        blockHeaderInfo.setRoundStartTime(131L);
        blockHeaderInfo.setScriptSign("fdsfsdf");
        blockHeaderInfo.setTotalFee(new BigInteger("123456"));
        blockHeaderInfo.setTxCount(1);
        blockHeaderInfo.setTxHashList(List.of("abcd", "aset", "sdfsd", "eeee"));

        long time1 = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            Document document = new Document();
            document.put("hash", blockHeaderInfo.getHash());
            document.put("height", blockHeaderInfo.getHeight());
            document.put("preHash", blockHeaderInfo.getPreHash());
            document.put("merkleHash", blockHeaderInfo.getMerkleHash());
            document.put("createTime", blockHeaderInfo.getCreateTime());
            document.put("agentHash", blockHeaderInfo.getAgentHash());
            document.put("agentId", blockHeaderInfo.getAgentId());
            document.put("packingAddress", blockHeaderInfo.getPackingAddress());
            document.put("agentAlias", blockHeaderInfo.getAgentAlias());
            document.put("txCount", blockHeaderInfo.getTxCount());
            document.put("roundIndex", blockHeaderInfo.getRoundIndex());
            document.put("totalFee", blockHeaderInfo.getTotalFee());
            document.put("reward", blockHeaderInfo.getReward());
            document.put("size", blockHeaderInfo.getSize());
            document.put("packingIndexOfRound", blockHeaderInfo.getPackingIndexOfRound());
            document.put("scriptSign", blockHeaderInfo.getScriptSign());
            document.put("txHashList", blockHeaderInfo.getTxHashList());
            document.put("isSeedPacked", blockHeaderInfo.isSeedPacked());
            document.put("roundStartTime", blockHeaderInfo.getRoundStartTime());
            document.put("agentVersion", blockHeaderInfo.getAgentVersion());
        }

        System.out.println("-------------time;" + (System.currentTimeMillis() - time1));

        time1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Document document = DocumentTransferTool.toDocument(blockHeaderInfo);
        }
        System.out.println("-------------time;" + (System.currentTimeMillis() - time1));
    }
}
