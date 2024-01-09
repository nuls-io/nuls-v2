package io.nuls.consensus.model.dto.input;
/**
 * Multiple account creation node parameters
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
