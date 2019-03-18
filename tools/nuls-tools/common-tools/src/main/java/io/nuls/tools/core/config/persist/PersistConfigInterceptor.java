package io.nuls.tools.core.config.persist;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Interceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptorChain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-15 18:07
 * @Description:
 *
 */
@Interceptor(Configuration.class)
public class PersistConfigInterceptor implements BeanMethodInterceptor {

    @Override
    public Object intercept(Annotation annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        if(method.getName().startsWith("set")){
            Object res = interceptorChain.execute(annotation, object, method, params);
            PersistManager.saveConfigItem(annotation,object,method,params);
            return res;
        }else{
            return interceptorChain.execute(annotation, object, method, params);
        }
    }


}
