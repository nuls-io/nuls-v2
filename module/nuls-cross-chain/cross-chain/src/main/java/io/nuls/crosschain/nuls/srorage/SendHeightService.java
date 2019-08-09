package io.nuls.crosschain.nuls.srorage;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import java.util.Map;

/**
 * 待广播给其他链节点的区块高度和广播的跨链交易Hash列表数据库相关操作
 * Block Height Broadcast to Other Chain Nodes and Related Operation of Broadcast Cross-Chain Transaction Hash List Database
 *
 * @author  tag
 * 2019/4/16
 * */
public interface SendHeightService {
    /**
     * 保存
     * @param height            友链协议跨链交易Hash
     * @param po                po
     * @param chainID           链ID
     * @return                  保存成功与否
     * */
    boolean save(long height, SendCtxHashPO po, int chainID);

    /**
     * 查询
     * @param height    友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          高度对应的交易Hash列表
     * */
    SendCtxHashPO get(long height, int chainID);

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
    Map<Long , SendCtxHashPO> getList(int chainID);
}
