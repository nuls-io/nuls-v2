package io.nuls.block.model;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.SmallBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 缓存的区块对象，用于区块广播、转发
 *
 * @author captain
 * @version 1.0
 * @date 18-12-13 下午3:01
 */
@NoArgsConstructor
@AllArgsConstructor
public class CachedSmallBlock {

    @Getter @Setter
    private List<NulsDigestData> missingTransactions;

    @Getter @Setter
    private SmallBlock smallBlock;

}
