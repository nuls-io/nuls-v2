package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.PunishLogInfo;
import io.nuls.api.model.po.db.TxDataInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static io.nuls.api.constant.MongoTableConstant.PUNISH_TABLE;

@Component
public class PunishService {

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
        mongoDBService.insertMany(PUNISH_TABLE + chainId, documentList);
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

}
