package io.nuls.crosschain.nuls.srorage;

import java.util.List;

/**
 * 跨链交易处理结果数据库相关操作类
 * Cross-Chain Transaction Processing Result Database Related Operating Classes
 *
 * @author  tag
 * 2019/4/16
 * */
public interface CtxStateService {
    /**
     * 保存
     * @param atxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          保存成功与否
     * */
    boolean save(byte[] atxHash,int chainID);

    /**
     * 查询
     * @param atxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          Hash对应的交易
     * */
    boolean get(byte[] atxHash, int chainID);

    /**
     * 删除
     * @param atxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(byte[] atxHash,int chainID);

    /**
     * 查询所有
     * @param chainID   链ID
     * @return          该表所有数据
     * */
    List<byte[]> getList(int chainID);
}
