package io.nuls.api.model.po.db;

import java.util.List;

public class ContractMethod {

    private String name;

    private String returnType;

    private List<ContractMethodArg> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<ContractMethodArg> getParams() {
        return params;
    }

    public void setParams(List<ContractMethodArg> params) {
        this.params = params;
    }
}
