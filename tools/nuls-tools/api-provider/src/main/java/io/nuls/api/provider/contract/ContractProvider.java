package io.nuls.api.provider.contract;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.CreateContractReq;
import io.nuls.api.provider.contract.facade.GetContractConstructorArgsReq;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:35
 * @Description: 功能描述
 */
public interface ContractProvider  {

    public Result<Map> createContract(CreateContractReq req);

    public Result<Map> getContractConstructorArgs(GetContractConstructorArgsReq req);

}
