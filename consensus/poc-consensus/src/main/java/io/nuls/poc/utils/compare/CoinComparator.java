package io.nuls.poc.utils.compare;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Coin;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Coin对比工具类
 * Node Contrast Tool Class
 *
 * @author tag
 * 2019/4/28
 */
public class CoinComparator implements Comparator<Coin> {
    @Override
    public int compare(Coin o1, Coin o2) {
        if(!Arrays.equals(o1.getAddress(), o2.getAddress())){
            return AddressTool.getStringAddressByBytes(o1.getAddress()).compareTo(AddressTool.getStringAddressByBytes(o2.getAddress()));
        }else{
            if(o1.getAssetsChainId() != o2.getAssetsChainId()){
                return o1.getAssetsChainId() - o2.getAssetsChainId();
            }else{
                if(o1.getAssetsId() != o2.getAssetsId()){
                    return o1.getAssetsId() - o2.getAssetsId();
                }else{
                    return o1.getAmount().compareTo(o2.getAmount());
                }
            }
        }
    }
}
