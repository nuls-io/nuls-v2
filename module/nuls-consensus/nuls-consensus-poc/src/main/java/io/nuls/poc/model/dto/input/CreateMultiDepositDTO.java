package io.nuls.poc.model.dto.input;
/**
 * 多签账户委托共识参数
 * Multi-Signed Account Delegation Consensus Parameters
 *
 * @author tag
 * 2019/7/25
 * */
public class CreateMultiDepositDTO extends CreateDepositDTO{
    private String signAddress;

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
