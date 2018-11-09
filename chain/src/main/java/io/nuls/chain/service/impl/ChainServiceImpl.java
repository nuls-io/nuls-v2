package io.nuls.chain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.chain.dao.ChainDao;
import io.nuls.chain.model.Chain;
import io.nuls.chain.service.ChainService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.parse.JSONUtils;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Service
public class ChainServiceImpl implements ChainService {

    @Autowired
    private ChainDao chainDao;

    /**
     * Save chain information when registering a new chain
     *
     * @param chain Chain information filled in when the user registers
     * @return Number of saves
     */
    @Override
    public int chainRegister(Chain chain) {
        try {
            System.out.println("service:" + JSONUtils.obj2json(chain));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return chainDao.save(chain);
    }

    /**
     * Destroy a chain
     *
     * @param id The id of the chain to be deleted
     * @return Number of deletions
     */
    @Override
    public int chainDestroy(int id) {
        return 0;
    }

    /**
     * Query a chain by id
     *
     * @param id The id of the chain to be queried
     * @return Chain
     */
    @Override
    public Chain chainInfo(int id) {
        return null;
    }

    /**
     * Query all the chains
     *
     * @return List of the chain
     */
    @Override
    public List<Chain> chainsInfo() {
        return null;
    }

    @Override
    public Chain chainInfo(String name) {
        return chainDao.selectByName(name);
    }
}
