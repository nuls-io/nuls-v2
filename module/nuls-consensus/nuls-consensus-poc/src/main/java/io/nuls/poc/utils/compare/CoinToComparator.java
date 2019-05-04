package io.nuls.poc.utils.compare;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinTo;

import java.util.Arrays;
import java.util.Comparator;

/**
 * CoinTo对比工具类
 * Node Contrast Tool Class
 *
 * @author tag
 * 2019/4/28
 */
public class CoinToComparator implements Comparator<CoinTo> {

    @Override
    public int compare(CoinTo o1, CoinTo o2) {
        if(o1.getLockTime() != o2.getLockTime()){
            if(o1.getLockTime()>o2.getLockTime()){
                return 1;
            }else {
                return -1;
            }
        }else{
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
}
