package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Unsubscribe {
    private String[] unsubscribeMethods;

    public String[] getUnsubscribeMethods() {
        return unsubscribeMethods;
    }

    public void setUnsubscribeMethods(String[] unsubscribeMethods) {
        this.unsubscribeMethods = unsubscribeMethods;
    }
}
