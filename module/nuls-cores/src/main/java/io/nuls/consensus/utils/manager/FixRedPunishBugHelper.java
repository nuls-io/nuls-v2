package io.nuls.consensus.utils.manager;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.storage.AgentStorageService;

@Component
public class FixRedPunishBugHelper {

    @Autowired
    private AgentStorageService agentStorageService;

    /**
     * This method is used to solve the problem of inconsistent node list caused by the massive node fork event that occurred on 2022-04-10. This method forces four nodes to stop at different heights, and then modifs the affected data through other transaction types to maintain the security and consistency of data on the main network.
     * @param chainId
     * @param agent
     * @param startBlockHeight
     */
    public void v13Filter(int chainId, Agent agent, long startBlockHeight) {
        execute(chainId, "15673a9ab94fd5737ec541e3d6b289ea119268b361f0bfbe86b69a95f83c196f", startBlockHeight, 3718800L, agent);
        execute(chainId, "a27170a4ad246758cc7fb45ded14b065f6a1919836a2bba34e6dcd9335a054da", startBlockHeight, 8083986L, agent);
        execute(chainId, "ad82dc5237378a39abb3bbd8174ac0f77c882573a02c8fac01b4c7a058a96d90", startBlockHeight, 8083892L, agent);
        execute(chainId, "d11d29e38b3db75aec0ebb69dc66eb4f6276d0a1d9c7faa6a4fa33b699637447", startBlockHeight, 8084009L, agent);
    }

    private void execute(int chainId, String hash, long startBlockHeight, long deleteHeight, Agent agent) {
        if (deleteHeight > startBlockHeight) {
            return;
        }
        if (agent.getDelHeight() == deleteHeight) {
            return;
        }
        if (!hash.equals(agent.getTxHash().toHex())) {
            return;
        }
        agent.setDelHeight(deleteHeight);
        agent.setStatus(0);
        AgentPo po = this.agentStorageService.get(agent.getTxHash(), chainId);
        if (null == po) {
            return;
        }
        po.setDelHeight(deleteHeight);
        this.agentStorageService.save(po, chainId);
        Log.warn("update special agent:" + hash);
    }
}
