package io.nuls.api.db;

import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.PunishLogInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

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
        mongoDBService.insertMany(MongoTableConstant.PUNISH_TABLE + chainId, documentList);
    }
}
