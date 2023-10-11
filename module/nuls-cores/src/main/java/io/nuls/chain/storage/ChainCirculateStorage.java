package io.nuls.chain.storage;


import io.nuls.chain.model.po.ChainAsset;

import java.math.BigInteger;
import java.util.Map;

public interface ChainCirculateStorage {


    BigInteger load(String key) throws Exception;


    void save(String key, BigInteger amount) throws Exception;



}
