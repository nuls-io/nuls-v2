package io.nuls.api.provider.contract.facade;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:33
 * @Description: 功能描述
 */
public class CreateContractReq extends Contract {

    private String contractCode;

    private Object[] args;

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
