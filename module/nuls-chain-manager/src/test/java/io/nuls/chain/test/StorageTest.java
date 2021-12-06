package io.nuls.chain.test;

import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import org.junit.Test;

import java.util.List;

public class StorageTest {

    static String DB_PATH1 = "/Users/niels/workspace/nuls-v2/data/chain-manager";
    static String DB_PATH2 = "/Users/niels/workspace/nuls-v2/data-beta/chain-manager";


    static final String TABLE1 = "chain_asset";
    static final String TABLE2 = "block_chain";
    static final String TABLE3 = "chain_circulate";


    @Test
    public void readChainAsset() throws NulsException {
        RocksDBService.init(DB_PATH1);
        List<byte[]> blist = RocksDBService.valueList(TABLE1);
        for (byte[] b : blist) {
            ChainAsset registeredChainMessage = new ChainAsset();
            registeredChainMessage.parse(b, 0);
            Log.info("{}", registeredChainMessage);
        }
        System.out.println(blist.size());
    }

    @Test
    public void readChainAsset2() throws NulsException {
        RocksDBService.init(DB_PATH2);
        List<byte[]> blist = RocksDBService.valueList(TABLE1);
        for (byte[] b : blist) {
            ChainAsset registeredChainMessage = new ChainAsset();
            registeredChainMessage.parse(b, 0);
            Log.info("{}", registeredChainMessage);
        }
        System.out.println(blist.size());
    }

    @Test
    public void readChain() throws NulsException {
        RocksDBService.init(DB_PATH1);
        List<byte[]> blist = RocksDBService.valueList(TABLE2);
        for (byte[] b : blist) {
            BlockChain registeredChainMessage = new BlockChain();
            registeredChainMessage.parse(b, 0);
            Log.info("{}", registeredChainMessage);
        }
        System.out.println(blist.size());
    }

    @Test
    public void readChain2() throws NulsException {
        RocksDBService.init(DB_PATH2);
        List<byte[]> blist = RocksDBService.valueList(TABLE2);
        for (byte[] b : blist) {
            BlockChain registeredChainMessage = new BlockChain();
            registeredChainMessage.parse(b, 0);
            Log.info("{}", registeredChainMessage);
        }
        System.out.println(blist.size());
    }

    @Test
    public void readChainCirculate() throws NulsException {
        RocksDBService.init(DB_PATH1);
        List<byte[]> blist = RocksDBService.keyList(TABLE3);
        for (byte[] b : blist) {
            byte[] val = RocksDBService.get(TABLE3, b);
            Log.info("{}:  {}", new String(b), ByteUtils.bytesToBigInteger(val));
        }
        System.out.println(blist.size());
    }

    @Test
    public void readChainCirculate2() throws NulsException {
        RocksDBService.init(DB_PATH2);
        List<byte[]> blist = RocksDBService.keyList(TABLE3);
        for (byte[] b : blist) {
            byte[] val = RocksDBService.get(TABLE3, b);
            Log.info("{}:  {}", new String(b), ByteUtils.bytesToBigInteger(val));
        }
        System.out.println(blist.size());
    }
}
