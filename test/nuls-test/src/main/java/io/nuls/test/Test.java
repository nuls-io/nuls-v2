package io.nuls.test;

import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.core.parse.MapUtils;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 17:41
 * @Description: Function Description
 */
public class Test {

    public static void main(String[] args) {
//        RemoteCaseReq req = new RemoteCaseReq();
//        req.setCaseClass(SleepAdapter.$15SEC.class);
//        req.setParam("5MR_2CaLdKkCgdLAg9NYnppSRU9o5Lkx9wT");
//        RestFulUtils.getInstance().setServerUri("http://192.168.1.115:9999/api");
//        RemoteResult<AccountInfo> res = RestFulUtils.getInstance().post("/remote/call", MapUtils.beanToMap(req));
//        System.out.println("res:{}" + res);

        String str = "ABCd";
        str = HexUtil.encode(Sha256Hash.hash(str.getBytes()));
        System.out.println(str);
        System.out.println("0cda48b9b56125648510ec9cd3ad8d3858ba73db9fe3c84e706585c6005d7bdc".length());
    }

}
