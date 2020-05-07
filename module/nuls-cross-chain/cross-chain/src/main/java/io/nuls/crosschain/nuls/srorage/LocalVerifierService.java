package io.nuls.crosschain.nuls.srorage;

import io.nuls.crosschain.nuls.model.po.LocalVerifierPO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;

import java.util.Map;

/**
 * 本地验证人列表相关操作
 * Local verifier list related operations
 *
 * @author  tag
 * 2020/4/26
 * */
public interface LocalVerifierService {
    /**
     * 保存
     * @param po                po
     * @param chainID           链ID
     * @return                  保存成功与否
     * */
    boolean save(LocalVerifierPO po, int chainID);

    /**
     * 查询
     * @param chainID   链ID
     * @return          高度对应的交易Hash列表
     * */
    LocalVerifierPO get(int chainID);

    /**
     * 删除
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(int chainID);
}
