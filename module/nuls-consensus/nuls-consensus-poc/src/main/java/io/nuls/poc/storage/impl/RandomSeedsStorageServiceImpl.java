package io.nuls.poc.storage.impl;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ArraysTool;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.po.RandomSeedPo;
import io.nuls.poc.model.po.RandomSeedStatusPo;
import io.nuls.poc.storage.RandomSeedsStorageService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
@Component
public class RandomSeedsStorageServiceImpl implements RandomSeedsStorageService {


    @Override
    public RandomSeedStatusPo getAddressStatus(int chainId, byte[] address) {
        byte[] bytes = RocksDBService.get(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, address);
        if (null == bytes) {
            return null;
        }
        RandomSeedStatusPo po = new RandomSeedStatusPo();
        try {
            po.parse(new NulsByteBuffer(bytes, 0));
            po.setAddress(address);
        } catch (NulsException e) {
            Log.error(e);
        }
        return po;
    }

    @Override
    public boolean saveAddressStatus(int chainId, byte[] address, long nowHeight, byte[] nextSeed, byte[] seedHash) {
        RandomSeedStatusPo po = new RandomSeedStatusPo();
        po.setAddress(address);
        po.setHeight(nowHeight);
        po.setNextSeed(nextSeed);
        po.setSeedHash(seedHash);
        try {
            RocksDBService.put(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, address, po.serialize());
            return true;
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public void deleteAddressStatus(int chainId, byte[] address) {
        try {
            RocksDBService.delete(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, address);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean saveRandomSeed(int chainId, long height, long preHeight, byte[] seed, byte[] nextSeedHash) {
        RandomSeedPo po = new RandomSeedPo();
        po.setPreHeight(preHeight);
        po.setSeed(seed);
        po.setNextSeedHash(nextSeedHash);
        try {
            RocksDBService.put(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, SerializeUtils.uint64ToByteArray(height), po.serialize());
            return true;
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean deleteRandomSeed(int chainId, long height) {
        try {
            RocksDBService.delete(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, SerializeUtils.uint64ToByteArray(height));
            return true;
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public RandomSeedPo getSeed(int chainId, long height) {
        byte[] bytes = RocksDBService.get(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId, SerializeUtils.uint64ToByteArray(height));
        if (null == bytes) {
            return null;
        }
        RandomSeedPo po = new RandomSeedPo();
        try {
            po.parse(new NulsByteBuffer(bytes, 0));
            po.setHeight(height);
        } catch (NulsException e) {
            Log.error(e);
        }
        return po;
    }

    @Override
    public List<byte[]> getSeeds(int chainId, long maxHeight, int seedCount) {
        List<byte[]> list = new ArrayList<>();
        long minHeight = maxHeight - 1000L;
        while (maxHeight > minHeight) {
            RandomSeedPo po = getSeed(chainId, maxHeight--);
            if (null != po && !ArraysTool.arrayEquals(po.getSeed(), ConsensusConstant.EMPTY_SEED)) {
                list.add(po.getSeed());
            }
            if (list.size() >= seedCount) {
                break;
            }
        }
        return list;
    }

    @Override
    public List<byte[]> getSeeds(int chainId, long startHeight, long endHeight) {
        List<byte[]> list = new ArrayList<>();
        long height = startHeight;
        while (height <= endHeight) {
            RandomSeedPo po = getSeed(chainId, height++);
            if (null != po && !ArraysTool.arrayEquals(po.getSeed(), ConsensusConstant.EMPTY_SEED)) {
                list.add(po.getSeed());
            }
        }
        return list;
    }

}
