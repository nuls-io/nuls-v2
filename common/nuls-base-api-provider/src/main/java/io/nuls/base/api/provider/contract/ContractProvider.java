package io.nuls.base.api.provider.contract;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.contract.facade.*;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:35
 * @Description: 功能描述
 */
public interface ContractProvider  {

    Result<Map> createContract(CreateContractReq req);

    Result<Map> getContractConstructorArgs(GetContractConstructorArgsReq req);

    Result<String> callContract(CallContractReq req);

    Result<Map> viewContract(ViewContractReq req);

    Result<String> deleteContract(DeleteContractReq req);

    Result<Map> getContractTx(GetContractTxReq req);

    Result<Map> getContractResult(GetContractResultReq req);

    Result<Map> getContractInfo(GetContractInfoReq req);

    Result<String> transferToContract(TransferToContractReq req);

    Result<String> tokenTransfer(TokenTransferReq req);

    Result<AccountContractInfo> getAccountContractList(GetAccountContractListReq req);


}
