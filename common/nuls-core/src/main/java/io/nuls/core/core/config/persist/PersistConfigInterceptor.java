package io.nuls.core.core.config.persist;

import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Interceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptorChain;

import java.lang.reflect.Method;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-15 18:07
 * @Description:
 * 在调用注解 {@link io.nuls.core.core.annotation.Configuration}了的类的setter方法时，判断setter方法修改的field是否有 {@link io.nuls.core.core.annotation.Persist}注解，如果有这将保存最新的值到disk，供下次启动时注入
 */
@Interceptor(Configuration.class)
public class PersistConfigInterceptor implements BeanMethodInterceptor<Configuration> {

    @Override
    public Object intercept(Configuration annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        if(method.getName().startsWith("set")){
            Object res = interceptorChain.execute(annotation, object, method, params);
            PersistManager.saveConfigItem((Configuration)annotation,object,method,params);
            return res;
        }else{
            return interceptorChain.execute(annotation, object, method, params);
        }
    }


}
