package io.nuls.poc.storage;


import io.nuls.poc.model.po.RandomSeedPo;
import io.nuls.poc.model.po.RandomSeedStatusPo;

import java.util.List;

/**
 * @author Niels
 */
public interface RandomSeedsStorageService {

    RandomSeedStatusPo getAddressStatus(int chainId, byte[] address);

    boolean saveAddressStatus(int chainId, byte[] address, long nowHeight, byte[] nextSeed, byte[] seedHash);

    boolean saveRandomSeed(int chainId, long height, long preHeight, byte[] seed, byte[] nextSeedHash);

    boolean deleteRandomSeed(int chainId, long height);

    List<byte[]> getSeeds(int chainId, long maxHeight, int seedCount);

    List<byte[]> getSeeds(int chainId, long startHeight, long endHeight);

    void deleteAddressStatus(int chainId, byte[] address);

    RandomSeedPo getSeed(int chainId, long height);
}
