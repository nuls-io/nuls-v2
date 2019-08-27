package io.nuls.api.db;

import io.nuls.api.model.po.DepositInfo;
import io.nuls.api.model.po.PageInfo;

import java.math.BigInteger;
import java.util.List;

public interface DepositService {

    DepositInfo getDepositInfoByKey(int chainId, String key);

    DepositInfo getDepositInfoByHash(int chainId, String hash);

    List<DepositInfo> getDepositListByAgentHash(int chainId, String hash);

    PageInfo<DepositInfo> getDepositListByAgentHash(int chainID, String hash, int pageIndex, int pageSize);

    List<DepositInfo> getDepositListByHash(int chainID, String hash);

    void rollbackDeposit(int chainId, List<DepositInfo> depositInfoList);

    void saveDepositList(int chainId, List<DepositInfo> depositInfoList);

    List<DepositInfo> getDepositList(int chainId, long startHeight);

    BigInteger getDepositAmount(int chainId, String address, String agentHash);

    PageInfo<DepositInfo> getCancelDepositListByAgentHash(int chainId, String hash, int type, int pageIndex, int pageSize);

    List<String> getAgentHashList(int chainId, String address);

    PageInfo<DepositInfo> getDepositListByAddress(int chainId,String agentHash, String address, int pageIndex, int pageSize);


}
