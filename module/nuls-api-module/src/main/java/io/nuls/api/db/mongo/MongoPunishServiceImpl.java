package io.nuls.api.db.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.Sorts;
import io.nuls.api.db.PunishService;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.PunishLogInfo;
import io.nuls.api.model.po.db.TxDataInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static io.nuls.api.constant.DBTableConstant.PUNISH_TABLE;

@Component
public class MongoPunishServiceImpl implements PunishService {

    @Autowired
    private MongoDBService mongoDBService;

    public void savePunishList(int chainId, List<PunishLogInfo> punishLogList) {
        if (punishLogList.isEmpty()) {
            return;
        }

        List<Document> documentList = new ArrayList<>();
        for (PunishLogInfo punishLog : punishLogList) {
            documentList.add(DocumentTransferTool.toDocument(punishLog));
        }
        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);
        mongoDBService.insertMany(PUNISH_TABLE + chainId, documentList, options);
    }

    public List<TxDataInfo> getYellowPunishLog(int chainId, String txHash) {
        List<Document> documentList = mongoDBService.query(PUNISH_TABLE + chainId, Filters.eq("txHash", txHash));
        List<TxDataInfo> punishLogs = new ArrayList<>();
        for (Document document : documentList) {
            PunishLogInfo punishLog = DocumentTransferTool.toInfo(document, PunishLogInfo.class);
            punishLogs.add(punishLog);
        }
        return punishLogs;
    }


    public PunishLogInfo getRedPunishLog(int chainId, String txHash) {
        Document document = mongoDBService.findOne(PUNISH_TABLE + chainId, Filters.eq("txHash", txHash));
        if (document == null) {
            return null;
        }
        PunishLogInfo punishLog = DocumentTransferTool.toInfo(document, PunishLogInfo.class);
        return punishLog;
    }

    public long getYellowCount(int chainId, String agentAddress) {
        Bson filter = and(eq("type", 1), eq("address", agentAddress));
        long count = mongoDBService.getCount(PUNISH_TABLE + chainId, filter);
        return count;
    }

    public PageInfo<PunishLogInfo> getPunishLogList(int chainId, int type, String address, int pageIndex, int pageSize) {
        Bson filter;
        if (type == 0) {
            filter = Filters.eq("address", address);
        } else {
            filter = Filters.and(eq("type", type), eq("address", address));
        }

        long totalCount = mongoDBService.getCount(PUNISH_TABLE + chainId, filter);
        List<Document> documentList = mongoDBService.pageQuery(PUNISH_TABLE + chainId, filter, Sorts.descending("time"), pageIndex, pageSize);
        List<PunishLogInfo> punishLogList = new ArrayList<>();
        for (Document document : documentList) {
            punishLogList.add(DocumentTransferTool.toInfo(document, PunishLogInfo.class));
        }
        PageInfo<PunishLogInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, totalCount, punishLogList);
        return pageInfo;
    }


    public void rollbackPunishLog(int chainID,List<String> txHashs, long height) {
        if (txHashs.isEmpty()) {
            return;
        }
        mongoDBService.delete(PUNISH_TABLE + chainID, Filters.eq("blockHeight", height));
    }
}
