package io.nuls.api.db;

import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.PunishLogInfo;
import io.nuls.api.model.po.db.TxDataInfo;

import java.util.List;

public interface PunishService {

    void savePunishList(int chainId, List<PunishLogInfo> punishLogList);

    List<TxDataInfo> getYellowPunishLog(int chainId, String txHash);

    PunishLogInfo getRedPunishLog(int chainId, String txHash);

    long getYellowCount(int chainId, String agentAddress);

    PageInfo<PunishLogInfo> getPunishLogList(int chainId, int type, String address, int pageIndex, int pageSize);

    void rollbackPunishLog(int chainID,List<String> txHashs, long height);
}
