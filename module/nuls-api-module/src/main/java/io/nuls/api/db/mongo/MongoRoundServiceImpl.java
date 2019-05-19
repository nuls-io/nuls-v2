package io.nuls.api.db.mongo;

import com.mongodb.client.model.Sorts;
import io.nuls.api.db.RoundService;
import io.nuls.api.model.po.db.PocRound;
import io.nuls.api.model.po.db.PocRoundItem;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static io.nuls.api.constant.DBTableConstant.ROUND_ITEM_TABLE;
import static io.nuls.api.constant.DBTableConstant.ROUND_TABLE;

@Component
public class MongoRoundServiceImpl implements RoundService {

    @Autowired
    private MongoDBService mongoDBService;

    public PocRound getRound(int chainId, long roundIndex) {
        Document document = this.mongoDBService.findOne(ROUND_TABLE + chainId, eq("_id", roundIndex));
        if (null == document) {
            return null;
        }
        return DocumentTransferTool.toInfo(document, "index", PocRound.class);
    }

    public List<PocRoundItem> getRoundItemList(int chainId, long roundIndex) {
        List<Document> list = this.mongoDBService.query(ROUND_ITEM_TABLE + chainId, eq("roundIndex", roundIndex), Sorts.ascending("order"));
        List<PocRoundItem> itemList = new ArrayList<>();
        for (Document document : list) {
            itemList.add(DocumentTransferTool.toInfo(document, "id", PocRoundItem.class));
        }
        return itemList;
    }

    public void saveRound(int chainId, PocRound round) {
        Document document = DocumentTransferTool.toDocument(round, "index");
        this.mongoDBService.insertOne(ROUND_TABLE + chainId, document);
    }

    public long updateRound(int chainId, PocRound round) {
        Document document = DocumentTransferTool.toDocument(round, "index");
        return this.mongoDBService.updateOne(ROUND_TABLE + chainId, eq("_id", round.getIndex()), document);
    }

    public long updateRoundItem(int chainId, PocRoundItem item) {
        Document document = DocumentTransferTool.toDocument(item, "id");
        return this.mongoDBService.updateOne(ROUND_ITEM_TABLE + chainId, eq("_id", item.getId()), document);
    }

    public void saveRoundItemList(int chainId, List<PocRoundItem> itemList) {
        List<Document> docsList = new ArrayList<>();
        for (PocRoundItem item : itemList) {
            Document document = DocumentTransferTool.toDocument(item, "id");
            docsList.add(document);
        }
        try {
            this.mongoDBService.insertMany(ROUND_ITEM_TABLE + chainId, docsList);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }

    public void removeRound(int chainId, long roundIndex) {
        this.mongoDBService.delete(ROUND_TABLE + chainId, eq("_id", roundIndex));
        this.mongoDBService.delete(ROUND_ITEM_TABLE + chainId, eq("roundIndex", roundIndex));
    }

    public long getTotalCount(int chainId) {
        return this.mongoDBService.getCount(ROUND_TABLE + chainId);
    }

    public List<PocRound> getRoundList(int chainId, int pageIndex, int pageSize) {
        List<Document> list = this.mongoDBService.pageQuery(ROUND_TABLE + chainId, Sorts.descending("_id"), pageIndex, pageSize);
        List<PocRound> roundList = new ArrayList<>();
        for (Document document : list) {
            roundList.add(DocumentTransferTool.toInfo(document, "index", PocRound.class));
        }
        return roundList;
    }

}
