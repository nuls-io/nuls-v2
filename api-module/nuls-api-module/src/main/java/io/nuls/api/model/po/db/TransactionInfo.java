package io.nuls.api.model.po.db;

import io.nuls.api.constant.ApiConstant;
import lombok.Data;
import org.bson.Document;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TransactionInfo {

    private String hash;

    private int type;

    private long height;

    private int size;

    private BigInteger fee;

    private long createTime;

    private String remark;

    private String txDataHex;

    private TxDataInfo txData;

    private List<TxDataInfo> txDataList;

    private List<CoinFromInfo> coinFroms;

    private List<CoinToInfo> coinTos;

    private BigInteger value;


    public void calcValue() {
        BigInteger value = BigInteger.ZERO;
        if (type == ApiConstant.TX_TYPE_COINBASE ||
                type == ApiConstant.TX_TYPE_STOP_AGENT ||
                type == ApiConstant.TX_TYPE_CANCEL_DEPOSIT) {
            if (coinTos != null) {
                for (CoinToInfo output : coinTos) {
                    value.add(output.getAmount());
                }
            }
        } else if (type == ApiConstant.TX_TYPE_TRANSFER ||
                type == ApiConstant.TX_TYPE_ALIAS ||
                type == ApiConstant.TX_TYPE_CONTRACT_TRANSFER) {
            Set<String> addressSet = new HashSet<>();
            for (CoinFromInfo input : coinFroms) {
                addressSet.add(input.getAddress());
            }
            for (CoinToInfo output : coinTos) {
                if (!addressSet.contains(output.getAddress())) {
                    value.add(output.getAmount());
                }
            }
        } else if (type == ApiConstant.TX_TYPE_REGISTER_AGENT || type == ApiConstant.TX_TYPE_JOIN_CONSENSUS) {
            for (CoinToInfo output : coinTos) {
                if (output.getLockTime() == -1) {
                    value.add(output.getAmount());
                }
            }
        } else {
            value = this.fee;
        }
        this.value = value;
    }

    public Document toDocument() {
        Document document = new Document();
        document.append("_id", hash).append("height", height).append("createTime", createTime).append("type", type).append("value", value).append("fee", fee);
        return document;
    }

    public static TransactionInfo fromDocument(Document document) {
        TransactionInfo info = new TransactionInfo();
        info.setHash(document.getString("_id"));
        info.setHeight(document.getLong("height"));
        info.setCreateTime(document.getLong("createTime"));
        info.setType(document.getInteger("type"));
        info.setFee(new BigInteger(document.getString("fee")));

        info.setValue(new BigInteger(document.getString("value")));
        return info;
    }
}
