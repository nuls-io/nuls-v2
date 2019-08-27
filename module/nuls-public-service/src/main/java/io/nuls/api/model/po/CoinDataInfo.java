package io.nuls.api.model.po;

import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

import java.util.List;

public class CoinDataInfo {

    private String txHash;

    private List<CoinFromInfo> fromList;

    private List<CoinToInfo> toList;

    public CoinDataInfo() {
    }

    public CoinDataInfo(String txHash, List<CoinFromInfo> fromList, List<CoinToInfo> toList) {
        this.txHash = txHash;
        this.fromList = fromList;
        this.toList = toList;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("_id", txHash);
        document.put("fromList", DocumentTransferTool.toDocumentList(fromList));
        document.put("toList", DocumentTransferTool.toDocumentList(toList));
        return document;
    }

    public static CoinDataInfo toInfo(Document document) {
        CoinDataInfo info = new CoinDataInfo();
        info.setTxHash(document.getString("_id"));
        info.setFromList(DocumentTransferTool.toInfoList((List<Document>) document.get("fromList"), CoinFromInfo.class));
        info.setToList(DocumentTransferTool.toInfoList((List<Document>) document.get("toList"), CoinToInfo.class));
        return info;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public List<CoinFromInfo> getFromList() {
        return fromList;
    }

    public void setFromList(List<CoinFromInfo> fromList) {
        this.fromList = fromList;
    }

    public List<CoinToInfo> getToList() {
        return toList;
    }

    public void setToList(List<CoinToInfo> toList) {
        this.toList = toList;
    }
}
