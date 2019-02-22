package io.nuls.chain.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CacheDatas extends BaseNulsData {
    private long preBlockHeight = 0;
    /**
     * bak BlockChain
     */
    private List<BlockChain> blockChains = new ArrayList<>();
    /**
     * bak assets
     */
    private List<Asset> assets = new ArrayList<>();

    /**
     * bak ChainAsset
     */
    private List<ChainAsset> chainAssets = new ArrayList<>();

    public void addBlockChain(BlockChain blockChain) {
        if (null != blockChain) {
            blockChains.add(blockChain);
        }
    }

    public void addAsset(Asset asset) {
        if (null != asset) {
            assets.add(asset);
        }
    }

    public void addChainAsset(ChainAsset chainAsset) {
        if (null != chainAsset) {
            chainAssets.add(chainAsset);
        }
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(preBlockHeight);
        stream.writeUint16(blockChains.size());
        for (BlockChain blockChain : blockChains) {
            stream.writeNulsData(blockChain);
        }
        stream.writeUint16(assets.size());
        for (Asset asset : assets) {
            stream.writeNulsData(asset);
        }
        stream.writeUint16(chainAssets.size());
        for (ChainAsset chainAsset : chainAssets) {
            stream.writeNulsData(chainAsset);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        preBlockHeight = byteBuffer.readUint32();
        int blockChainsSize = byteBuffer.readUint16();
        for (int i = 0; i < blockChainsSize; i++) {
            BlockChain blockChain = new BlockChain();
            blockChain.parse(byteBuffer);
            blockChains.add(blockChain);
        }
        int assetsSize = byteBuffer.readUint16();
        for (int i = 0; i < assetsSize; i++) {
            Asset asset = new Asset();
            asset.parse(byteBuffer);
            assets.add(asset);
        }
        int chainAssetsSize = byteBuffer.readUint16();
        for (int i = 0; i < chainAssetsSize; i++) {
            ChainAsset chainAsset = new ChainAsset();
            chainAsset.parse(byteBuffer);
            chainAssets.add(chainAsset);
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint16();
        for (BlockChain blockChain : blockChains) {
            size += blockChain.size();
        }

        size += SerializeUtils.sizeOfUint16();
        for (Asset asset : assets) {
            size += asset.size();
        }

        size += SerializeUtils.sizeOfUint16();
        for (ChainAsset chainAsset : chainAssets) {
            size += chainAsset.size();
        }
        return size;
    }

    public CacheDatas() {
        super();
    }

}
