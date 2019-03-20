package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinDataInfo {

    private String txHash;

    private List<CoinFromInfo> fromList;

    private List<CoinToInfo> toList;

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
}
