package io.nuls.api.model.po.db;

import lombok.Data;

import java.util.List;

@Data
public class ContractMethod {

    private String name;

    private String returnType;

    private List<String> params;
}
