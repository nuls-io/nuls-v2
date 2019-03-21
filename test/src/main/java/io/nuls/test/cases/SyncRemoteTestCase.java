package io.nuls.test.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.network.NetworkProvider;
import io.nuls.test.Config;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.test.utils.Utils;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.MapUtils;
import lombok.Setter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:48
 * @Description: 功能描述
 */
public abstract class SyncRemoteTestCase<T> implements TestCaseIntf<Boolean, RemoteTestParam<T>> {

    NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    public boolean equals(T source,T remote){
        return source.equals(remote);
    }

    @Override
    public Boolean doTest(RemoteTestParam<T> param,int depth) throws TestFailException {
        Result<String> nodes = networkProvider.getNodes();
        Config config = SpringLiteContext.getBean(Config.class);
        if(!config.isMaster()){
            throw new RuntimeException("非master节点不允许进行远程调用");
        }
        List<String> nodeList = nodes.getList().stream().map(node->node.split(":")[0]).filter(node->config.getNodeExclude().indexOf(node)==-1).collect(Collectors.toList());
        if(nodeList.isEmpty()){
            throw new TestFailException("remote fail ,network node is empty");
        }
        for (String node : nodeList) {
            Map remoteRes = doRemoteTest(node,param.getCaseCls(),param.getParam());
            Type type = this.getClass().getGenericSuperclass();
            Type[] params = ((ParameterizedType) type).getActualTypeArguments();
            Class<T> reponseClass = (Class) params[0];
            T remoteData = JSONUtils.map2pojo(remoteRes,reponseClass);
            boolean sync = this.equals(param.getSource(),remoteData);
            if(!sync){
                return false;
            }
            Utils.success(depthSpace(depth)+"节点【"+node+"】测试通过");
        }
        return true;
    }

    public Map doRemoteTest(String node, Class<? extends TestCaseIntf> caseCls, Object param) throws TestFailException {
        RemoteCaseReq req = new RemoteCaseReq();
        req.setCaseClass(caseCls);
        if(param != null){
            req.setParam(Utils.toJson(param));
        }
//        try {
//            req.setParam(JSONUtils.obj2json(param));
//        } catch (JsonProcessingException e) {
//            throw new TestFailException("序列化远程测试参数错误", e);
//        }
        RestFulUtils.getInstance().setServerUri("http://" + node.split(":")[0] + ":9999/api");
        Log.debug("call {} remote case:{}",node,req);
        RemoteResult<Map> result = RestFulUtils.getInstance().post("remote/call", MapUtils.beanToMap(req));
        Log.debug("call remote case returl :{}",result);
        checkResultStatus(new Result(result.getData()));
        return result.getData();
    }

}
