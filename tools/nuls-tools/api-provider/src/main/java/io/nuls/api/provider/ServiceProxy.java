package io.nuls.api.provider;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 18:33
 * @Description: 功能描述
 */
@Slf4j
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
        Object res = methodProxy.invokeSuper(o, objects);
        return res;
    }

}
