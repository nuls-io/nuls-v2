package io.nuls.economic.nuls.model.bo;
import java.math.BigInteger;

/**
 * 节点委托信息
 * deposit info
 *
 * @author tag
 * 2019/7/23
 * */
public class DepositInfo {
    private BigInteger deposit;
    private byte[] address;

    public DepositInfo(){}

    public DepositInfo(BigInteger deposit,byte[] address){
        this.deposit = deposit;
        this.address = address;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }
}
