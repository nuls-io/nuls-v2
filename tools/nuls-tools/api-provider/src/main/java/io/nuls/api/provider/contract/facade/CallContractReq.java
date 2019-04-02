package io.nuls.api.provider.contract.facade;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 15:27
 * @Description: 功能描述
 */
@Data
public class CallContractReq extends Contract {

    private String contractAddress;

    private long value;

    private String methodName;

    private String methodDesc;

    private Object[] args;

}
