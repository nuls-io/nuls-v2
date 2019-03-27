package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import lombok.Data;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContractResultInfo {

    private String txHash;

    private String contractAddress;

    private boolean success;

    private String errorMessage;

    private String result;

    private long gasLimit;

    private long gasUsed;

    private long price;

    private String totalFee;

    private String txSizeFee;

    private String actualContractFee;

    private String refundFee;

    private String value;

    //private String balance;

    private List<NulsTransfer> nulsTransfers;

    private List<TokenTransfer> tokenTransfers;

    private String tokenName;

    private String symbol;

    private Long decimals;

    private String remark;

    private Long confirmCount;

    private Long createTime;

    public Document toDocument() {
        Document document = DocumentTransferTool.toDocument(this, "txHash");
        List<Document> nulsTransferList = new ArrayList<>();
        for (NulsTransfer transfer : nulsTransfers) {
            Document doc = DocumentTransferTool.toDocument(transfer);
            nulsTransferList.add(doc);
        }

        List<Document> tokenTransferList = new ArrayList<>();
        for (TokenTransfer transfer : tokenTransfers) {
            Document doc = DocumentTransferTool.toDocument(transfer);
            tokenTransferList.add(doc);
        }

        document.put("nulsTransfers", nulsTransferList);
        document.put("tokenTransfers", tokenTransferList);
        return document;
    }

    public static ContractResultInfo toInfo(Document document) {
        List<Document> documentList = (List<Document>) document.get("nulsTransfers");
        List<NulsTransfer> nulsTransferList = new ArrayList<>();
        for (Document doc : documentList) {
            NulsTransfer nulsTransfer = DocumentTransferTool.toInfo(doc, NulsTransfer.class);
            nulsTransferList.add(nulsTransfer);
        }

        documentList = (List<Document>) document.get("tokenTransfers");
        List<TokenTransfer> tokenTransferList = new ArrayList<>();
        for (Document doc : documentList) {
            TokenTransfer tokenTransfer = DocumentTransferTool.toInfo(doc, TokenTransfer.class);
            tokenTransferList.add(tokenTransfer);
        }

        document.remove("nulsTransfers");
        document.remove("tokenTransfers");

        ContractResultInfo resultInfo = DocumentTransferTool.toInfo(document, "txHash", ContractResultInfo.class);
        resultInfo.setNulsTransfers(nulsTransferList);
        resultInfo.setTokenTransfers(tokenTransferList);
        return resultInfo;
    }
}
