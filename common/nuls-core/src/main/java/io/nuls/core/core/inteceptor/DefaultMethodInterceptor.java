/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.core.inteceptor;

import io.nuls.core.core.inteceptor.base.BeanMethodInterceptorManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * System default method interceptor, used foraopBottom level implementation
 * The system's default method interceptor is used for the aop underlying implementation.
 *
 * @author: Niels Wang
 */
public class DefaultMethodInterceptor implements MethodInterceptor {
    /**
     * interceptor method
     * Intercept method
     *
     * @param obj         Object to which the method belongs/Method owner
     * @param method      Method definition/Method definition
     * @param params      Method parameter list/Method parameter list
     * @param methodProxy Method proxy
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        Annotation[] clsAnns = obj.getClass().getSuperclass().getDeclaredAnnotations();
        Annotation[] methodAnns = method.getDeclaredAnnotations();
        if ((null == method.getDeclaredAnnotations() || method.getDeclaredAnnotations().length == 0) &&
                (obj.getClass().getSuperclass().getDeclaredAnnotations() == null || obj.getClass().getSuperclass().getDeclaredAnnotations().length == 0 )) {
            return methodProxy.invokeSuper(obj, params);
        }
        Annotation[] anns = Arrays.copyOf(methodAnns,methodAnns.length + clsAnns.length);
        System.arraycopy(clsAnns,0,anns,methodAnns.length,clsAnns.length);
        return BeanMethodInterceptorManager.doInterceptor(anns, obj, method, params, methodProxy);
    }
}
