package io.nuls.test;

import io.nuls.api.provider.block.facade.BlockHeaderData;
import io.nuls.test.cases.block.GetLastBlockHeaderCase;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.tools.parse.MapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 17:41
 * @Description: 功能描述
 */
@Slf4j
public class Test {

    public static void main(String[] args) {
        RemoteCaseReq req = new RemoteCaseReq();
        req.setCaseClass(GetLastBlockHeaderCase.class);
        RestFulUtils.getInstance().setServerUri("http://192.168.1.66:9999/api");
        RemoteResult<BlockHeaderData> res = RestFulUtils.getInstance().post("/remote/call", MapUtils.beanToMap(req));
        log.info("res:{}",res);
    }

}
