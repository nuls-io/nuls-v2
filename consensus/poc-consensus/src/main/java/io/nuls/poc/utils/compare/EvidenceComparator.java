package io.nuls.poc.utils.compare;

import io.nuls.poc.model.bo.consensus.Evidence;

import java.util.Comparator;

public class EvidenceComparator implements Comparator<Evidence> {
    @Override
    public int compare(Evidence o1, Evidence o2) {
        return (int) (o1.getRoundIndex()-o2.getRoundIndex());
    }
}
