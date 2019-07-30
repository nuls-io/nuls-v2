package io.nuls.base.api.provider.account.facade;

import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-23 17:05
 * @Description: 功能描述
 */
public class MultiSignAccountInfo {

        private int chainId;
        private Address address;
        private int minSigns;
        private List<String> pubKeyList;
        private String alias;

}
