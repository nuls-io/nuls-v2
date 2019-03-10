package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import lombok.Data;
import org.bson.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ChainInfo {

    private int chainId;

    private AssetInfo defaultAsset;

    private Set<AssetInfo> assets;

    public ChainInfo() {
        assets = new HashSet<>();
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("chainId", chainId);

        Document defaultAssetDoc = DocumentTransferTool.toDocument(defaultAsset);
        document.put("defaultAsset", defaultAssetDoc);
        document.put("assets", assets);
        return document;
    }

    public static ChainInfo toInfo(Document document) {
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(document.getInteger("chainId"));

        AssetInfo defaultAsset = DocumentTransferTool.toInfo((Document) document.get("defaultAsset"), AssetInfo.class);
        chainInfo.setDefaultAsset(defaultAsset);

        List<AssetInfo> list = (List<AssetInfo>) document.get("assets");
        chainInfo.getAssets().addAll(list);
        return chainInfo;
    }
}
