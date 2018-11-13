package io.nuls.transaction.model.bo;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public class ModuleRegister {

    /**
     * module code
     */
    private String moduleCode;

    /**
     * Module unified validator cmd name
     */
    private String moduleValidator;

    /**
     * Module transactions register information
     */
    private List<TxRegister> txRegisterList;

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleValidator() {
        return moduleValidator;
    }

    public void setModuleValidator(String moduleValidator) {
        this.moduleValidator = moduleValidator;
    }

    public List<TxRegister> getTxRegisterList() {
        return txRegisterList;
    }

    public void setTxRegisterList(List<TxRegister> txRegisterList) {
        this.txRegisterList = txRegisterList;
    }
}
