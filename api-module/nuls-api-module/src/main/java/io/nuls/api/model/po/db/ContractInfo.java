package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import lombok.Data;
import org.bson.Document;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContractInfo extends TxDataInfo {

    private String contractAddress;

    private String creater;

    private String createTxHash;

    private long blockHeight;

    private boolean success;

    private BigInteger balance;

    private String errorMsg;

    private boolean isNrc20;//是否支持NRC20协议(0-否、1-是)

    private int status; // -1,执行失败，0未认证 1正在审核 2通过验证 3 已删除

    private long certificationTime;

    private long createTime;

    private String remark;

    private int txCount;

    private String deleteHash;

    private List<ContractMethod> methods;

    //以下字段，为NRC20合约特有
    private String tokenName;

    private String symbol;

    private int decimals;

    private String totalSupply;

    private int transferCount;

    private List<String> owners;

    private boolean isNew;

    private ContractResultInfo resultInfo;


    public Document toDocument() {
        Document document = DocumentTransferTool.toDocument(this, "contractAddress");
        List<Document> methodsList = new ArrayList<>();
        for (ContractMethod method : methods) {
            Document doc = DocumentTransferTool.toDocument(method);
            methodsList.add(doc);
        }

        document.put("methods", methodsList);
        //document.put("resultInfo", resultInfo.toDocument());
        document.remove("resultInfo");
        return document;
    }

    public static ContractInfo toInfo(Document document) {
        List<ContractMethod> methods = new ArrayList<>();
        List<Document> methodsList = (List<Document>) document.get("methods");
        for (Document doc : methodsList) {
            ContractMethod method = DocumentTransferTool.toInfo(doc, ContractMethod.class);
            methods.add(method);
        }
        document.remove("methods");
        ContractInfo info = DocumentTransferTool.toInfo(document, "contractAddress", ContractInfo.class);

        info.setMethods(methods);
        return info;

    }
}
