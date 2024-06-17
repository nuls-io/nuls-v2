package io.nuls.consensus.model.dto.input;
/**
 * Stop node parameters for multi account signing
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
