package io.nuls.test;

import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.core.parse.MapUtils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 17:41
 * @Description: 功能描述
 */
public class Test {

    public static void main(String[] args) {
        RemoteCaseReq req = new RemoteCaseReq();
        req.setCaseClass(SleepAdapter.$15SEC.class);
        req.setParam("5MR_2CaLdKkCgdLAg9NYnppSRU9o5Lkx9wT");
        RestFulUtils.getInstance().setServerUri("http://192.168.1.115:9999/api");
        RemoteResult<AccountInfo> res = RestFulUtils.getInstance().post("/remote/call", MapUtils.beanToMap(req));
        System.out.println("res:{}" + res);
    }

}
