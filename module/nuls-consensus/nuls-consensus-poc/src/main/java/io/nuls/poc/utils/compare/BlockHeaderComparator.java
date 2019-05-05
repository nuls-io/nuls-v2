package io.nuls.poc.utils.compare;

import io.nuls.base.data.BlockHeader;

import java.util.Comparator;

/**
 * @author tag
 * 2018/11/20
 */
public class BlockHeaderComparator implements Comparator<BlockHeader>{
    @Override
    public int compare(BlockHeader o1, BlockHeader o2) {
        return (int) (o1.getHeight() - o2.getHeight());
    }
}
