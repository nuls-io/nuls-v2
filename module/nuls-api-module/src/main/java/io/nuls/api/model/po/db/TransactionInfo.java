package io.nuls.api.model.po.db;

import org.bson.Document;

import java.math.BigInteger;
import java.util.List;

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

    private int status;

    public void calcValue() {
        BigInteger value = BigInteger.ZERO;
        if (coinTos != null) {
            for (CoinToInfo output : coinTos) {
                value = value.add(output.getAmount());
            }
        }
        this.value = value;
//        if (type == TxType.COIN_BASE ||
//                type == TxType.STOP_AGENT ||
//                type == TxType.CANCEL_DEPOSIT ||
//                type == TxType.CONTRACT_RETURN_GAS ||
//                type == TxType.CONTRACT_STOP_AGENT ||
//                type == TxType.CONTRACT_CANCEL_DEPOSIT) {
//            if (coinTos != null) {
//                for (CoinToInfo output : coinTos) {
//                    value = value.add(output.getAmount());
//                }
//            }
//        } else if (type == TxType.TRANSFER ||
//                type == TxType.CALL_CONTRACT ||
//                type == TxType.CONTRACT_TRANSFER
//            //        type == TxType.TX_TYPE_DATA
//        ) {
//            Set<String> addressSet = new HashSet<>();
//            for (CoinFromInfo input : coinFroms) {
//                addressSet.add(input.getAddress());
//            }
//            for (CoinToInfo output : coinTos) {
//                if (!addressSet.contains(output.getAddress())) {
//                    value = value.add(output.getAmount());
//                }
//            }
//        } else if (type == TxType.REGISTER_AGENT ||
//                type == TxType.DEPOSIT ||
//                type == TxType.CONTRACT_CREATE_AGENT ||
//                type == TxType.CONTRACT_DEPOSIT) {
//            for (CoinToInfo output : coinTos) {
//                if (output.getLockTime() == -1) {
//                    value = value.add(output.getAmount());
//                }
//            }
//        } else if (type == TxType.ACCOUNT_ALIAS) {
//            value = ApiConstant.ALIAS_AMOUNT;
//        } else {
//            value = this.fee;
//        }
//        this.value = value.abs();
    }

    public Document toDocument() {
        Document document = new Document();
        document.append("_id", hash).append("height", height).append("createTime", createTime).append("type", type)
                .append("value", value.toString()).append("fee", fee.toString()).append("status", status);
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
        info.setStatus(document.getInteger("status"));
        return info;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTxDataHex() {
        return txDataHex;
    }

    public void setTxDataHex(String txDataHex) {
        this.txDataHex = txDataHex;
    }

    public TxDataInfo getTxData() {
        return txData;
    }

    public void setTxData(TxDataInfo txData) {
        this.txData = txData;
    }

    public List<TxDataInfo> getTxDataList() {
        return txDataList;
    }

    public void setTxDataList(List<TxDataInfo> txDataList) {
        this.txDataList = txDataList;
    }

    public List<CoinFromInfo> getCoinFroms() {
        return coinFroms;
    }

    public void setCoinFroms(List<CoinFromInfo> coinFroms) {
        this.coinFroms = coinFroms;
    }

    public List<CoinToInfo> getCoinTos() {
        return coinTos;
    }

    public void setCoinTos(List<CoinToInfo> coinTos) {
        this.coinTos = coinTos;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
