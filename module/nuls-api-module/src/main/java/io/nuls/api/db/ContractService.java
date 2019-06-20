package io.nuls.api.db;

import io.nuls.api.model.po.db.ContractInfo;
import io.nuls.api.model.po.db.ContractResultInfo;
import io.nuls.api.model.po.db.ContractTxInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.mini.MiniContractInfo;

import java.util.List;
import java.util.Map;

public interface ContractService {

    ContractInfo getContractInfo(int chainId, String contractAddress);

    ContractInfo getContractInfoByHash(int chainId, String txHash);

    void saveContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap);

    void rollbackContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap);

    void saveContractTxInfos(int chainId, List<ContractTxInfo> contractTxInfos);

    void rollbackContractTxInfos(int chainId, List<String> contractTxHashList);

    void saveContractResults(int chainId, List<ContractResultInfo> contractResultInfos);

    void rollbackContractResults(int chainId, List<String> contractTxHashList);

    PageInfo<ContractTxInfo> getContractTxList(int chainId, String contractAddress, int type, int pageNumber, int pageSize);

    PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, boolean onlyNrc20, boolean isHidden);

    PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, String address, boolean onlyNrc20, boolean isHidden);

    List<MiniContractInfo> getContractList(int chainId, List<String> addressList);

    ContractResultInfo getContractResultInfo(int chainId, String txHash);
}
