package io.nuls.api.model.po.db;

import lombok.Data;

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

    private String balance;

    private List<NulsTransfer> nulsTransfers;

    private List<TokenTransfer> tokenTransfers;

    private String nulsTransferStr;

    private String tokenTransferStr;

    private String tokenName;

    private String symbol;

    private Long decimals;

    private String remark;

    private Long confirmCount;

    private Long createTime;
}
