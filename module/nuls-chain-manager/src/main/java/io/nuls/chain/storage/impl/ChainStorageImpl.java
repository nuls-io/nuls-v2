package io.nuls.chain.storage.impl;

import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.chain.storage.InitDB;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 关于链的所有操作：增删改查 key =chainId, value = BlockChain
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
@Component
public class ChainStorageImpl extends BaseStorage implements ChainStorage, InitDB, InitializingBean {

    private final String TBL = "block_chain";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }

    /**
     * 保存链到数据库
     * Save BlockChain to database
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void save(int key, BlockChain blockChain) throws Exception {
        RocksDBService.put(TBL, ByteUtils.intToBytes(key), blockChain.serialize());
    }

    /**
     * 更新链信息
     * Update BlockChain
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void update(int key, BlockChain blockChain) throws Exception {
        RocksDBService.put(TBL, ByteUtils.intToBytes(key), blockChain.serialize());
    }

    /**
     * @param kvs
     * @throws Exception
     */
    @Override
    public void batchUpdate(Map<byte[], byte[]> kvs) throws Exception {
        RocksDBService.batchPut(TBL, kvs);
    }

    /**
     * 从数据库彻底删除链
     * Delete BlockChain in database
     *
     * @param key Chain ID
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void delete(int key) throws Exception {
        RocksDBService.delete(TBL, ByteUtils.intToBytes(key));
    }

    /**
     * 根据序号获取链
     * Get the chain according to the ID
     *
     * @param key Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain load(int key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, ByteUtils.intToBytes(key));
        if (bytes == null) {
            return null;
        }

        BlockChain blockChain = new BlockChain();
        blockChain.parse(bytes, 0);
        return blockChain;
    }

    @Override
    public List<BlockChain> loadAllRegChains() throws Exception {
        List<BlockChain> blockChains = new ArrayList<>();
        List<byte[]> list = RocksDBService.valueList(TBL);
        if (list == null) {
            return blockChains;
        }
        for (byte[] blockChainByte : list) {
            BlockChain blockChain = new BlockChain();
            blockChain.parse(blockChainByte, 0);
            blockChains.add(blockChain);
        }
        return blockChains;
    }

    @Override
    public void initTableName() throws NulsException {
        super.initTableName(TBL);
    }
}
