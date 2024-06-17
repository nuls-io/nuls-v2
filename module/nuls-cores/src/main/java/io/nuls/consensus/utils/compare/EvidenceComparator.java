package io.nuls.consensus.utils.compare;

import io.nuls.consensus.model.bo.consensus.Evidence;

import java.util.Comparator;
/**
 * Evidence information comparator
 * Evidence Information Comparator
 *
 * @author  tag
 * 2018/11/28
 **/
public class EvidenceComparator implements Comparator<Evidence> {
    @Override
    public int compare(Evidence o1, Evidence o2) {
        return (int) (o1.getRoundIndex()-o2.getRoundIndex());
    }
}
