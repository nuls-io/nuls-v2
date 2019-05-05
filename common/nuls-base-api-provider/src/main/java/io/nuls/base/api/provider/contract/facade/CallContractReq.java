package io.nuls.base.api.provider.contract.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 15:27
 * @Description: 功能描述
 */
public class CallContractReq extends Contract {

    private String contractAddress;

    private long value;

    private String methodName;

    private String methodDesc;

    private Object[] args;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
