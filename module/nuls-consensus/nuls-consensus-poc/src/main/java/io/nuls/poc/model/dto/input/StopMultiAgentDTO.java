package io.nuls.poc.model.dto.input;
/**
 * 多签账户停止节点参数
 * Multi-Sign Account Stop Node Parameters
 *
 * @author tag
 * 2019/7/25
 * */
public class StopMultiAgentDTO extends StopAgentDTO{
    private String signAddress;

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
