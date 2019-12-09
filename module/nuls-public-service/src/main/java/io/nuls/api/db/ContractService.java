package io.nuls.api.db;

import io.nuls.api.model.po.ContractInfo;
import io.nuls.api.model.po.ContractResultInfo;
import io.nuls.api.model.po.ContractTxInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.mini.MiniContractInfo;

import java.util.List;
import java.util.Map;

public interface ContractService {

    void initCache();

    ContractInfo getContractInfo(int chainId, String contractAddress);

    ContractInfo getContractInfoByHash(int chainId, String txHash);

    void saveContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap);

    void rollbackContractInfos(int chainId, Map<String, ContractInfo> contractInfoMap);

    void saveContractTxInfos(int chainId, List<ContractTxInfo> contractTxInfos);

    void rollbackContractTxInfos(int chainId, List<String> contractTxHashList);

    void saveContractResults(int chainId, List<ContractResultInfo> contractResultInfos);

    void rollbackContractResults(int chainId, List<String> contractTxHashList);

    PageInfo<ContractTxInfo> getContractTxList(int chainId, String contractAddress, int type, int pageNumber, int pageSize);

    PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, int tokenType, boolean isHidden);

    PageInfo<MiniContractInfo> getContractList(int chainId, int pageNumber, int pageSize, String address, int tokenType, boolean isHidden);

    List<MiniContractInfo> getContractList(int chainId, List<String> addressList);

    ContractResultInfo getContractResultInfo(int chainId, String txHash);

}
