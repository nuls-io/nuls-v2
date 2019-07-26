package io.nuls.poc.model.dto.input;
/**
 * 多签账户创建节点参数
 * Multi-Sign Account Creation Node Parameters
 *
 * @author tag
 * 2019/7/25
 * */
public class CreateMultiAgentDTO extends CreateAgentDTO{
    private String signAddress;

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
