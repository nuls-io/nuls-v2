package io.nuls.crosschain.nuls.model.bo;

public class BroadFailFlag {
    private boolean verifierInitFlag = false;
    private boolean verifierChangeFlag = false;
    private boolean crossChainTransferFlag =false;

    public boolean isVerifierInitFlag() {
        return verifierInitFlag;
    }

    public void setVerifierInitFlag(boolean verifierInitFlag) {
        this.verifierInitFlag = verifierInitFlag;
    }

    public boolean isVerifierChangeFlag() {
        return verifierChangeFlag;
    }

    public void setVerifierChangeFlag(boolean verifierChangeFlag) {
        this.verifierChangeFlag = verifierChangeFlag;
    }

    public boolean isCrossChainTransferFlag() {
        return crossChainTransferFlag;
    }

    public void setCrossChainTransferFlag(boolean crossChainTransferFlag) {
        this.crossChainTransferFlag = crossChainTransferFlag;
    }
}
