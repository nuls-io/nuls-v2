package io.nuls.api.model.po.db;

import lombok.Data;

import java.util.List;

@Data
public class ContractInfo extends TxDataInfo{

    private String contractAddress;

    private String creater;

    private String createTxHash;

    private long blockHeight;

    private boolean success;

    private long balance;

    private String errorMsg;

    private boolean isNrc20;//是否支持NRC20协议(0-否、1-是)

    private int status; // -1,执行失败，0未认证 1正在审核 2通过验证 3 已删除

    private long certificationTime;

    private long createTime;

    private String remark;

    private int txCount;

    private String deleteHash;

    private List<ContractMethod> methods;

    private String methodStr;

    //以下字段，为NRC20合约特有
    private String tokenName;

    private String symbol;

    private int decimals;

    private String totalSupply;

    private int transferCount;

    private List<String> owners;

    private boolean isNew;

    private ContractResultInfo resultInfo;
}
