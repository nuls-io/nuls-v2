package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import lombok.Data;
import org.bson.Document;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ChainInfo {

    private int chainId;

    private AssetInfo defaultAsset;

    private Set<AssetInfo> assets;

    private Set<String> seeds;

    private BigInteger inflationCoins;

    public ChainInfo() {
        assets = new HashSet<>();
        seeds = new HashSet<>();
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("chainId", chainId);

        Document defaultAssetDoc = DocumentTransferTool.toDocument(defaultAsset);
        document.put("defaultAsset", defaultAssetDoc);
        document.put("assets", assets);
        document.put("seeds", seeds);
        document.put("inflationCoins", inflationCoins.toString());
        return document;
    }

    public static ChainInfo toInfo(Document document) {
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(document.getInteger("chainId"));

        AssetInfo defaultAsset = DocumentTransferTool.toInfo((Document) document.get("defaultAsset"), AssetInfo.class);
        chainInfo.setDefaultAsset(defaultAsset);

        List<AssetInfo> list = (List<AssetInfo>) document.get("assets");
        chainInfo.getAssets().addAll(list);

        List<String> seeds = (List<String>) document.get("seeds");
        chainInfo.getSeeds().addAll(seeds);

        String inflationCoins = document.getString("inflationCoins");
        chainInfo.setInflationCoins(new BigInteger(inflationCoins));
        return chainInfo;
    }
}
