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
 * When calling annotations {@link io.nuls.core.core.annotation.Configuration}The class ofsetterWhen using the method, make a judgmentsetterMethod modificationfieldIs there any {@link io.nuls.core.core.annotation.Persist}Annotations, if available, will save the latest values todiskFor injection on the next startup
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
