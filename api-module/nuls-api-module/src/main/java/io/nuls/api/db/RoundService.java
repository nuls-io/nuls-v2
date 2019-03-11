package io.nuls.api.db;

import com.mongodb.client.model.Sorts;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.PocRound;
import io.nuls.api.model.po.db.PocRoundItem;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Component
public class RoundService {

    @Autowired
    private MongoDBService mongoDBService;

    public PocRound getRound(int chainId, long roundIndex) {
        Document document = this.mongoDBService.findOne(MongoTableConstant.ROUND_TABLE + chainId, eq("_id", roundIndex));
        if (null == document) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "index", PocRound.class);
    }

    public List<PocRoundItem> getRoundItemList(int chainId, long roundIndex) {
        List<Document> list = this.mongoDBService.query(MongoTableConstant.ROUND_ITEM_TABLE + chainId, eq("roundIndex", roundIndex), Sorts.ascending("order"));
        List<PocRoundItem> itemList = new ArrayList<>();
        for (Document document : list) {
            itemList.add(DocumentTransferTool.toInfo(document, "id", PocRoundItem.class));
        }
        return itemList;
    }

    public void saveRound(int chainId, PocRound round) {
        Document document = DocumentTransferTool.toDocument(round, "index");
        this.mongoDBService.insertOne(MongoTableConstant.ROUND_TABLE + chainId, document);
    }

    public long updateRound(int chainId, PocRound round) {
        Document document = DocumentTransferTool.toDocument(round, "index");
        return this.mongoDBService.updateOne(MongoTableConstant.ROUND_TABLE + chainId, eq("_id", round.getIndex()), document);
    }

    public long updateRoundItem(int chainId, PocRoundItem item) {
        Document document = DocumentTransferTool.toDocument(item, "id");
        return this.mongoDBService.updateOne(MongoTableConstant.ROUND_ITEM_TABLE + chainId, eq("_id", item.getId()), document);
    }

    public void saveRoundItemList(int chainId, List<PocRoundItem> itemList) {
        List<Document> docsList = new ArrayList<>();
        for (PocRoundItem item : itemList) {
            Document document = DocumentTransferTool.toDocument(item, "id");
            docsList.add(document);
        }
        try {
            this.mongoDBService.insertMany(MongoTableConstant.ROUND_ITEM_TABLE + chainId, docsList);
        } catch (Exception e) {
            Log.warn("", e);
        }
    }

}
