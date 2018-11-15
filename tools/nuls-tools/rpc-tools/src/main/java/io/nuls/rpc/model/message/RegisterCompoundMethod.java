package io.nuls.rpc.model.message;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class RegisterCompoundMethod {
    private String compoundMethodName;
    private String compoundMethodDescription;
    private List<Object> compoundMethods;

    public String getCompoundMethodName() {
        return compoundMethodName;
    }

    public void setCompoundMethodName(String compoundMethodName) {
        this.compoundMethodName = compoundMethodName;
    }

    public String getCompoundMethodDescription() {
        return compoundMethodDescription;
    }

    public void setCompoundMethodDescription(String compoundMethodDescription) {
        this.compoundMethodDescription = compoundMethodDescription;
    }

    public List<Object> getCompoundMethods() {
        return compoundMethods;
    }

    public void setCompoundMethods(List<Object> compoundMethods) {
        this.compoundMethods = compoundMethods;
    }
}
