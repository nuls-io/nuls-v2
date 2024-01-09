package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface ChainService {
    /**
     * Consensus module transaction submission
     * @param params
     * @return Result
     * */
    Result commitCmd(Map<String,Object> params);

    /**
     * Consensus module transaction rollback
     * @param params
     * @return Result
     * */
    Result rollbackCmd(Map<String,Object> params);

    /**
     * Query Yellow Card List
     * @param params
     * @return Result
     * */
    Result getPublishList(Map<String,Object> params);

    /**
     * Query consensus information across the entire network
     * @param params
     * @return Result
     * */
    Result getWholeInfo(Map<String,Object> params);

    /**
     * Query consensus information for specified accounts
     * @param params
     * @return Result
     * */
    Result getInfo(Map<String,Object> params);

    /**
     * Batch Verify Consensus Module Transactions
     * @param params
     * @return Result
     * */
    Result batchValid(Map<String,Object> params);

    /**
     * Obtain current round information
     * @param params
     * @return Result
     * */
    Result getCurrentRoundInfo(Map<String,Object> params);

    /**
     * Obtain specified block rounds
     * @param params
     * @return Result
     * */
    Result getRoundMemberList(Map<String,Object> params);


    /**
     * Stop a sub chain
     * @param params
     * @return Result
     * */
    Result stopChain(Map<String,Object> params);

    /**
     * Run a sub chain
     * @param params
     * @return Result
     * */
    Result runChain(Map<String,Object> params);

    /**
     * Running the main chain
     * @param params
     * @return Result
     * */
    Result runMainChain(Map<String,Object> params);

    /**
     * Cache the latest block
     * @param params
     * @return Result
     * */
    Result addEvidenceRecord(Map<String,Object> params);

    /**
     * Shuanghua transaction records
     * @param params
     * @return Result
     * */
    Result doubleSpendRecord(Map<String,Object> params);

    /**
     * Obtain common module recognition configuration information
     * @param params
     * @return Result
     * */
    Result getConsensusConfig(Map<String,Object> params);

    Result getSeedNodeList(Map<String,Object> params);

    /**
     * Obtain node change information between two rounds of consensus
     * @param params
     * @return Result
     * */
    Result getAgentChangeInfo(Map<String,Object> params);
}
