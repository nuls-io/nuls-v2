package io.nuls.test.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.network.NetworkProvider;
import io.nuls.test.Config;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.MapUtils;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:48
 * @Description: 功能描述
 */
public abstract class AbstractRemoteTestCase implements TestCaseIntf<Boolean, RemoteTestParam> {


    NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    @Override
    public Boolean doTest(RemoteTestParam param,int depth) throws TestFailException {
        Result<String> nodes = networkProvider.getNodes();
        Config config = SpringLiteContext.getBean(Config.class);
        List<String> nodeList = nodes.getList().stream().map(node->node.split(":")[0]).filter(node->config.getNodeExclude().indexOf(node)==-1).collect(Collectors.toList());
        if(nodeList.isEmpty()){
            throw new TestFailException("remote fail ,network node is empty");
        }
        for (String node : nodeList) {
            RestFulUtils.getInstance().setServerUri("http://" + node.split(":")[0] + ":9999/api");
            Object remoteRes = doRemoteTest(param.getCaseCls(),param.getParam());
            boolean sync = remoteRes.equals(param.getSource());
            if(!sync){
                return false;
            }
        }
        return true;
    }

    public Object doRemoteTest(Class<? extends TestCaseIntf> caseCls,Object param) throws TestFailException {
        RemoteCaseReq req = new RemoteCaseReq();
        req.setCaseClass(caseCls);
        try {
            req.setParam(JSONUtils.obj2json(param));
        } catch (JsonProcessingException e) {
            throw new TestFailException("序列化远程测试参数错误", e);
        }
        RemoteResult<Object> result = RestFulUtils.getInstance().post("remote/call", MapUtils.beanToMap(req));
        return result.getData();
    }

}
