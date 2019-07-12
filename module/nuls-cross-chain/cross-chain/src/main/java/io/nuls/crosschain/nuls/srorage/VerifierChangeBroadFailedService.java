package io.nuls.crosschain.nuls.srorage;
import io.nuls.crosschain.nuls.model.po.VerifierChangeSendFailPO;

import java.util.Map;

/**
 * 主网验证人变更消息广播失败消息数据库相关接口
 * Main Network Verifier Change Message Broadcasting Failure Message Database Related Interface
 *
 * @author  tag
 * 2019/6/28
 * */
public interface VerifierChangeBroadFailedService {
    /**
     * 保存
     * @param height            友链协议跨链交易Hash
     * @param po                po
     * @param chainID           链ID
     * @return                  保存成功与否
     * */
    boolean save(long height, VerifierChangeSendFailPO po, int chainID);

    /**
     * 查询
     * @param height    友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          高度对应的交易Hash列表
     * */
    VerifierChangeSendFailPO get(long height, int chainID);

    /**
     * 删除
     * @param height    删除的键
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(long height,int chainID);

    /**
     * 查询所有
     * @param chainID   链ID
     * @return          该表所有数据
     * */
    Map<Long , VerifierChangeSendFailPO> getList(int chainID);
}
