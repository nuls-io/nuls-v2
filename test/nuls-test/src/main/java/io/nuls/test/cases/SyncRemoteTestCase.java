package io.nuls.test.cases;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.network.NetworkProvider;
import io.nuls.core.core.config.ConfigSetting;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.MapUtils;
import io.nuls.test.Config;
import io.nuls.test.controller.RemoteCaseReq;
import io.nuls.test.controller.RemoteResult;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.test.utils.Utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:48
 * @Description: 功能描述
 */
public abstract class SyncRemoteTestCase<T> extends BaseTestCase<Boolean, RemoteTestParam<T>> {

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
        List<String> nodeList;
        if(StringUtils.isNotBlank(config.getTestNodeList())){
            nodeList = Arrays.asList(config.getTestNodeList().split(","));
        }else{
            nodeList = nodes.getList().stream().map(node->node.split(":")[0]).filter(node->config.getNodeExclude().indexOf(node)==-1).collect(Collectors.toList());

        }
        if(nodeList.isEmpty()){
            throw new TestFailException("remote fail ,network node is empty");
        }
        int testNodeCount = config.getTestNodeCount() > nodeList.size() ? nodeList.size() : config.getTestNodeCount();
        nodeList = nodeList.subList(0,testNodeCount);
        for (String node : nodeList) {
//            Map remoteRes = doRemoteTest(node,param.getCaseCls(),param.getParam());
            RemoteCaseReq req = new RemoteCaseReq();
            req.setCaseClass(param.getCaseCls());
            if(param.getParam() != null){
                req.setParam(Utils.toJson(param.getParam()));
            }
//        try {
//            req.setParam(JSONUtils.obj2json(param));
//        } catch (JsonProcessingException e) {
//            throw new TestFailException("序列化远程测试参数错误", e);
//        }
            RestFulUtils.getInstance().setServerUri("http://" + node.split(":")[0] + ":" + config.getHttpPort() + "/api");
            Log.debug("call {} remote case:{}",node,req);
            RemoteResult<T> result = RestFulUtils.getInstance().post("remote/call", MapUtils.beanToMap(req));
            Log.debug("call remote case returl :{}",result);

            checkResultStatus(new Result(result.getData()));
            Type type = this.getClass().getGenericSuperclass();
            Type[] params = ((ParameterizedType) type).getActualTypeArguments();
            Class<T> reponseClass = (Class) params[0];
            boolean sync;
            if(ConfigSetting.isPrimitive(reponseClass)){
                sync = this.equals(param.getSource(),result.getData());
            }else{
                T remoteData = JSONUtils.map2pojo((Map)result.getData(),reponseClass);
                sync = this.equals(param.getSource(),remoteData);
            }
//            JSONUtils.
//            T remoteData = JSONUtils.map2pojo(remoteRes,reponseClass);
            if(!sync){
                return false;
            }
            Utils.success(depthSpace(depth)+"节点【"+node+"】测试通过");
        }
        return true;
    }

}
