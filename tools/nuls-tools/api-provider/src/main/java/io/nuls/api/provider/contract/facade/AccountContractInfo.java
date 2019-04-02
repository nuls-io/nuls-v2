package io.nuls.api.provider.contract.facade;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:57
 * @Description: 功能描述
 */
@Data
public class AccountContractInfo {

    private String contractAddress;
    private boolean isCreate;
    private String createTime;
    private long height;
    private long confirmCount;
    private String remarkName;
    private int status;
    private String msg;

}
