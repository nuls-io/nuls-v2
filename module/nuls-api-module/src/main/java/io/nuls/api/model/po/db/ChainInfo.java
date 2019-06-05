package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ChainInfo extends TxDataInfo {

    private int chainId;

    private String chainName;

    private AssetInfo defaultAsset;

    private List<AssetInfo> assets;

    private List<String> seeds;

    private BigInteger inflationCoins;

    private int status;

    @JsonIgnore
    private boolean isNew;

    public ChainInfo() {
        assets = new ArrayList<>();
        seeds = new ArrayList<>();
        status = ApiConstant.ENABLE;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", chainId);
        document.put("chainName", chainName);
        Document defaultAssetDoc = DocumentTransferTool.toDocument(defaultAsset);
        document.put("defaultAsset", defaultAssetDoc);

        document.put("assets", DocumentTransferTool.toDocumentList(assets));
        document.put("seeds", seeds);
        document.put("inflationCoins", inflationCoins.toString());
        return document;
    }

    public static ChainInfo toInfo(Document document) {
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(document.getInteger("_id"));
        chainInfo.setChainName(document.getString("chainName"));
        AssetInfo defaultAsset = DocumentTransferTool.toInfo((Document) document.get("defaultAsset"), AssetInfo.class);
        chainInfo.setDefaultAsset(defaultAsset);

        List<AssetInfo> list = DocumentTransferTool.toInfoList((List<Document>) document.get("assets"), AssetInfo.class);
        chainInfo.getAssets().addAll(list);

        List<String> seeds = (List<String>) document.get("seeds");
        chainInfo.getSeeds().addAll(seeds);

        String inflationCoins = document.getString("inflationCoins");
        chainInfo.setInflationCoins(new BigInteger(inflationCoins));
        return chainInfo;
    }

    public AssetInfo getAsset(int assetId) {
        for (AssetInfo assetInfo : assets) {
            if (assetInfo.getAssetId() == assetId) {
                return assetInfo;
            }
        }
        return null;
    }

    public AssetInfo removeAsset(int assetId) {
        for (int i = 0; i < assets.size(); i++) {
            AssetInfo assetInfo = assets.get(i);
            if (assetInfo.getAssetId() == assetId) {
                return assets.remove(i);
            }
        }
        return null;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public AssetInfo getDefaultAsset() {
        return defaultAsset;
    }

    public void setDefaultAsset(AssetInfo defaultAsset) {
        this.defaultAsset = defaultAsset;
    }

    public List<AssetInfo> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetInfo> assets) {
        this.assets = assets;
    }

    public List<String> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds;
    }

    public BigInteger getInflationCoins() {
        return inflationCoins;
    }

    public void setInflationCoins(BigInteger inflationCoins) {
        this.inflationCoins = inflationCoins;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }
}
