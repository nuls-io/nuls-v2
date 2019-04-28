package io.nuls.poc.utils.compare;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;

import java.util.Arrays;
import java.util.Comparator;

/**
 * CoinFrom对比工具类
 * Node Contrast Tool Class
 *
 * @author tag
 * 2019/4/28
 */
public class CoinFromComparator implements Comparator<CoinFrom> {
    @Override
    public int compare(CoinFrom o1, CoinFrom o2) {
        if(!Arrays.equals(o1.getAddress(), o2.getAddress())){
            return AddressTool.getStringAddressByBytes(o1.getAddress()).compareTo(AddressTool.getStringAddressByBytes(o2.getAddress()));
        }else{
            if(o1.getAssetsChainId() != o2.getAssetsChainId()){
                return o1.getAssetsChainId() - o2.getAssetsChainId();
            }else{
                if(o1.getAssetsId() != o2.getAssetsId()){
                    return o1.getAssetsId() - o2.getAssetsId();
                }else{
                    if(!o1.getAmount().equals(o2.getAmount())){
                        return o1.getAmount().compareTo(o2.getAmount());
                    }else{
                        return Arrays.compare(o1.getNonce(), o2.getNonce());
                    }
                }
            }
        }
    }
}
