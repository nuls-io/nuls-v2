package io.nuls.api.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AgentInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AgentService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountService accountService;

    public AgentInfo getAgentByAgentHash(int chainID, String agentHash) {
        AgentInfo agentInfo = CacheManager.getCache(chainID).getAgentInfo(agentHash);
//        if (agentInfo == null) {
//            Document document = mongoDBService.findOne(MongoTableConstant.AGENT_TABLE + chainID, Filters.eq("_id", agentHash));
//            agentInfo = DocumentTransferTool.toInfo(document, "txHash", AgentInfo.class);
//            CacheManager.getCache(chainID).addAgentInfo(agentInfo);
//        }
        return agentInfo;
    }


    public AgentInfo getAgentByPackingAddress(int chainID, String packingAddress) {
        Collection<AgentInfo> agentInfos = CacheManager.getCache(chainID).getAgentMap().values();
        AgentInfo info = null;
        for (AgentInfo agent : agentInfos) {
            if (!agent.getPackingAddress().equals(packingAddress)) {
                continue;
            }
            if (null == info || agent.getCreateTime() > info.getCreateTime()) {
                info = agent;
            }
        }
        return info;
    }

    public AgentInfo getAgentByAgentAddress(int chainID, String agentAddress) {
        Collection<AgentInfo> agentInfos = CacheManager.getCache(chainID).getAgentMap().values();
        AgentInfo info = null;
        for (AgentInfo agent : agentInfos) {
            if (!agentAddress.equals(agent.getAgentAddress())) {
                continue;
            }
            if (null == info || agent.getCreateTime() > info.getCreateTime()) {
                info = agent;
            }
        }
        return info;
    }

    public void saveAgentList(int chainID, List<AgentInfo> agentInfoList) {
        if (agentInfoList.isEmpty()) {
            return;
        }
        List<WriteModel<Document>> modelList = new ArrayList<>();
        for (AgentInfo agentInfo : agentInfoList) {
            Document document = DocumentTransferTool.toDocument(agentInfo, "txHash");

            if (agentInfo.isNew()) {
                modelList.add(new InsertOneModel(document));
                agentInfo.setNew(false);
                ApiCache cache = CacheManager.getCache(chainID);
                cache.addAgentInfo(agentInfo);
            } else {
                modelList.add(new ReplaceOneModel<>(Filters.eq("_id", agentInfo.getTxHash()), document));
            }
        }
        mongoDBService.bulkWrite(MongoTableConstant.AGENT_TABLE + chainID, modelList);
    }

}
