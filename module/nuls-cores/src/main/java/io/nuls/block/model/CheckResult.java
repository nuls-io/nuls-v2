package io.nuls.block.model;

/**
 * @author Niels
 */
public class CheckResult {
    private boolean result;
    private boolean timeout;

    public CheckResult(boolean result, boolean timeout) {
        this.result = result;
        this.timeout = timeout;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }
}
