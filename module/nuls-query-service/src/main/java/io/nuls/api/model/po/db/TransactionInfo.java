package io.nuls.api.model.po.db;

import io.nuls.api.ApiContext;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.utils.DBUtil;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.TxType;
import org.bson.Document;

import java.math.BigInteger;
import java.util.List;

public class TransactionInfo {

    private String hash;

    private int type;

    private long height;

    private int size;

    private FeeInfo fee;

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

    public FeeInfo calcFee(int chainId) {
        AssetInfo assetInfo = CacheManager.getCacheChain(chainId).getDefaultAsset();
        ChainConfigInfo configInfo = CacheManager.getCache(chainId).getConfigInfo();
        FeeInfo feeInfo;
        if (type == TxType.COIN_BASE || type == TxType.YELLOW_PUNISH || type == TxType.RED_PUNISH ||
                type == TxType.CONTRACT_RETURN_GAS || type == TxType.CONTRACT_STOP_AGENT || type == TxType.CONTRACT_CANCEL_DEPOSIT ||
                type == TxType.CONTRACT_CREATE_AGENT || type == TxType.CONTRACT_DEPOSIT) {
            //系统交易没有手续费
            feeInfo = new FeeInfo(assetInfo.getChainId(), assetInfo.getAssetId(), assetInfo.getSymbol());
        } else if (type == TxType.CROSS_CHAIN) {
            //取出转出链和接收链的id
            int fromChainId = AddressTool.getChainIdByAddress(coinFroms.get(0).getAddress());
            int toChainId = AddressTool.getChainIdByAddress(coinTos.get(0).getAddress());

            //如果当前链是NULS主链，手续费是收取主网主资产NULS
            if (chainId == ApiContext.mainChainId) {
                feeInfo = new FeeInfo(ApiContext.mainChainId, ApiContext.mainAssetId, ApiContext.mainSymbol);
                if (toChainId == ApiContext.mainChainId) {
                    //如果接收地址是主链,则收取NULS的100%作为手续费
                    BigInteger feeValue = calcFeeValue(ApiContext.mainChainId, ApiContext.mainAssetId);
                    feeInfo.setValue(feeValue);
                } else {
                    //其他情况，主链收取NULS的60%作为手续费
                    BigInteger feeValue = calcFeeValue(ApiContext.mainChainId, ApiContext.mainAssetId);
                    feeValue = feeValue.multiply(new BigInteger("60")).divide(new BigInteger("100"));
                    feeInfo.setValue(feeValue);
                }
            } else {                        //如果当前链不是NULS主链
                //如果资产是从本链发起的，则收取本链的默认资产作为手续费
                if (fromChainId == chainId) {
                    feeInfo = new FeeInfo(assetInfo.getChainId(), assetInfo.getAssetId(), assetInfo.getSymbol());
                    feeInfo.setValue(calcFeeValue(assetInfo.getChainId(), assetInfo.getAssetId()));
                } else {
                    //如果本链是接收转账交易的目标链，则收取主网NULS资产的40%作为手续费
                    feeInfo = new FeeInfo(ApiContext.mainChainId, ApiContext.mainAssetId, ApiContext.mainSymbol);
                    BigInteger feeValue = calcFeeValue(ApiContext.mainChainId, ApiContext.mainAssetId);
                    feeValue = feeValue.multiply(new BigInteger("40")).divide(new BigInteger("100"));
                    feeInfo.setValue(feeValue);
                }
            }
        } else if (type == TxType.REGISTER_AGENT || type == TxType.DEPOSIT || type == TxType.CANCEL_DEPOSIT || type == TxType.STOP_AGENT) {
            //如果是共识相关的交易，收取共识配置的手续费
            assetInfo = CacheManager.getRegisteredAsset(DBUtil.getAssetKey(configInfo.getChainId(), configInfo.getAwardAssetId()));
            feeInfo = new FeeInfo(assetInfo.getChainId(), assetInfo.getAssetId(), assetInfo.getSymbol());
            BigInteger feeValue = calcFeeValue(assetInfo.getChainId(),assetInfo.getAssetId());
            feeInfo.setValue(feeValue);
        } else {
            //其他类型的交易,去本链默认资产手续费
            feeInfo = new FeeInfo(assetInfo.getChainId(), assetInfo.getAssetId(), assetInfo.getSymbol());
            feeInfo.setValue(calcFeeValue(assetInfo.getChainId(), assetInfo.getAssetId()));
        }
        this.fee = feeInfo;
        return feeInfo;
    }

    private BigInteger calcFeeValue(int chainId, int assetId) {
        BigInteger feeValue = BigInteger.ZERO;
        if (coinFroms != null && !coinFroms.isEmpty()) {
            for (CoinFromInfo fromInfo : coinFroms) {
                if (fromInfo.getChainId() == chainId && fromInfo.getAssetsId() == assetId) {
                    feeValue = feeValue.add(fromInfo.getAmount());
                }
            }
        }
        if (coinTos != null && !coinTos.isEmpty()) {
            for (CoinToInfo toInfo : coinTos) {
                if (toInfo.getChainId() == chainId && toInfo.getAssetsId() == assetId) {
                    feeValue = feeValue.subtract(toInfo.getAmount());
                }
            }
        }
        return feeValue;
    }

    public Document toDocument() {
        Document document = new Document();
        document.append("_id", hash).append("height", height).append("createTime", createTime).append("type", type)
                .append("value", value.toString()).append("fee", DocumentTransferTool.toDocument(fee)).append("status", status);
        return document;
    }

    public static TransactionInfo fromDocument(Document document) {
        TransactionInfo info = new TransactionInfo();
        info.setHash(document.getString("_id"));
        info.setHeight(document.getLong("height"));
        info.setCreateTime(document.getLong("createTime"));
        info.setType(document.getInteger("type"));
        info.setFee(DocumentTransferTool.toInfo((Document) document.get("fee"), FeeInfo.class));
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

    public FeeInfo getFee() {
        return fee;
    }

    public void setFee(FeeInfo fee) {
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
