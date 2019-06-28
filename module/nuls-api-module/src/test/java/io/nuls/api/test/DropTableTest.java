package io.nuls.api.test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import io.nuls.api.constant.DBTableConstant;
import io.nuls.api.db.mongo.MongoDBService;
import org.junit.Test;

import java.math.BigInteger;

import static io.nuls.api.constant.DBTableConstant.TX_RELATION_SHARDING_COUNT;

public class DropTableTest {

    private static int chainId = 100;

    @Test
    public void dropTable() {
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        MongoClient mongoClient = new MongoClient(serverAddress);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(DBTableConstant.DATABASE_NAME);
        MongoDBService mongoDBService = new MongoDBService(mongoClient, mongoDatabase);

        mongoDBService.dropTable(DBTableConstant.TEST_TABLE);
        mongoDBService.dropTable(DBTableConstant.CHAIN_INFO_TABLE);
        mongoDBService.dropTable(DBTableConstant.SYNC_INFO_TABLE);
        mongoDBService.dropTable(DBTableConstant.BLOCK_HEADER_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ACCOUNT_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.AGENT_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ALIAS_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.DEPOSIT_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.TX_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.TX_UNCONFIRM_RELATION_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.TX_UNCONFIRM_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.COINDATA_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.PUNISH_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ROUND_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ROUND_ITEM_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.ACCOUNT_TOKEN_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.CONTRACT_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.CONTRACT_TX_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.TOKEN_TRANSFER_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.CONTRACT_RESULT_TABLE + chainId);
        mongoDBService.dropTable(DBTableConstant.STATISTICAL_TABLE + chainId);

        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            mongoDBService.dropTable(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i);
        }
    }


    @Test
    public void testBigInteger() {
        BigInteger b1 = new BigInteger("12345678987654321");
        System.out.println(b1);
        b1 = b1.multiply(new BigInteger("40")).divide(new BigInteger("100"));
        System.out.println(b1);
    }
}
