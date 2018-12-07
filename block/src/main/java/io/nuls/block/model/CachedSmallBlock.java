package io.nuls.block.model;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.SmallBlock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class CachedSmallBlock {

    @Getter @Setter
    private List<NulsDigestData> missingTransactions;

    @Getter @Setter
    private SmallBlock smallBlock;

}
