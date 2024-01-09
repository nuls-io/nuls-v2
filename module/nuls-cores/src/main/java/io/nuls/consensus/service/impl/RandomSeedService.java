package io.nuls.consensus.service.impl;


import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ArraysTool;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.po.RandomSeedPo;
import io.nuls.consensus.model.po.RandomSeedStatusPo;
import io.nuls.consensus.storage.RandomSeedsStorageService;
import io.nuls.consensus.utils.RandomSeedUtils;

/**
 * @author Niels
 */
@Component
public class RandomSeedService {

    @Autowired
    private RandomSeedsStorageService randomSeedsStorageService;

    public void processBlock(int chainId, BlockHeader header, byte[] prePackingAddress) {
        byte[] headerPackingAddress = header.getPackingAddress(chainId);
        byte[] nextSeed = null;
        if (ArraysTool.arrayEquals(headerPackingAddress, RandomSeedUtils.CACHE_SEED.getAddress())) {
            nextSeed = RandomSeedUtils.CACHE_SEED.getNextSeed();
        }
        BlockExtendsData extendsData = header.getExtendsData();
        byte[] seed = extendsData.getSeed();
        RandomSeedStatusPo po = this.randomSeedsStorageService.getAddressStatus(chainId, headerPackingAddress);
        long preHeight = 0;

        // pierre test comment out
        if (null == po || ArraysTool.arrayEquals(prePackingAddress, headerPackingAddress) || !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
            seed = ConsensusConstant.EMPTY_SEED;
        }
        //if (null == po || !ArraysTool.arrayEquals(RandomSeedUtils.getLastDigestEightBytes(extendsData.getSeed()), po.getSeedHash())) {
        //    seed = ConsensusConstant.EMPTY_SEED;
        //}
        if (null != po) {
            preHeight = po.getHeight();
        }
        randomSeedsStorageService.saveAddressStatus(chainId, headerPackingAddress, header.getHeight(), nextSeed, extendsData.getNextSeedHash());
        randomSeedsStorageService.saveRandomSeed(chainId, header.getHeight(), preHeight, seed, extendsData.getNextSeedHash());
    }

    public void rollbackBlock(int chainId, BlockHeader header) {
        RandomSeedPo po = randomSeedsStorageService.getSeed(chainId, header.getHeight());
        randomSeedsStorageService.deleteRandomSeed(chainId, header.getHeight());
        byte[] headerPackingAddress = header.getPackingAddress(chainId);
        if (null == po || po.getPreHeight() == 0L) {
            randomSeedsStorageService.deleteAddressStatus(chainId, headerPackingAddress);
        } else {
            randomSeedsStorageService.saveAddressStatus(chainId, headerPackingAddress, po.getPreHeight(), po.getSeed(), RandomSeedUtils.getLastDigestEightBytes(po.getSeed()));
        }
    }
}
