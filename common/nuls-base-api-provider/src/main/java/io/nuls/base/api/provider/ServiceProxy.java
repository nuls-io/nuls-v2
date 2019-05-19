package io.nuls.base.api.provider;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.log.Log;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 18:33
 * @Description: 在请求参数中注入默认chainId
 */
public class ServiceProxy implements MethodInterceptor {

    int chainId;

    public ServiceProxy(int chainId){
        this.chainId = chainId;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Arrays.stream(objects).forEach(d->{
            if(d instanceof BaseReq){
                BaseReq req = (BaseReq) d;
                if(req.getChainId() == null){
                    req.setChainId(this.chainId);
                }
            }
        });
        try{
            return methodProxy.invokeSuper(o, objects);
        }catch(Exception e){
            Log.error("Calling provider interface failed. service:{} - method:{} ,message :{}",o.getClass(),method.getName(),e.getMessage());
            return BaseService.fail(CommonCodeConstanst.FAILED);
        }
    }

}
