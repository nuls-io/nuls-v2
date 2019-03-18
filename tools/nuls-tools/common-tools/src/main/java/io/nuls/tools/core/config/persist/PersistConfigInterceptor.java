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
 * 在调用注解 {@link io.nuls.tools.core.annotation.Configuration}了的类的setter方法时，判断setter方法修改的field是否有 {@link io.nuls.tools.core.annotation.Persist}注解，如果有这将保存最新的值到disk，供下次启动时注入
 */
@Interceptor(Configuration.class)
public class PersistConfigInterceptor implements BeanMethodInterceptor {

    @Override
    public Object intercept(Annotation annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        if(method.getName().startsWith("set")){
            Object res = interceptorChain.execute(annotation, object, method, params);
            if(Configuration.class.equals(annotation.annotationType())){
                PersistManager.saveConfigItem((Configuration)annotation,object,method,params);
            }
            return res;
        }else{
            return interceptorChain.execute(annotation, object, method, params);
        }
    }


}
