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
package io.nuls.core.core.inteceptor.base;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interceptor Manager
 * Interceptor manager.
 *
 * @author Niels Wang
 */
public class BeanMethodInterceptorManager {

    /**
     * Interceptor pool
     * The interceptor pool
     */
    private static final Map<Class, BeanMethodInterceptorChain> INTERCEPTOR_MAP = new HashMap<>();

    /**
     * Add method interceptors to the manager
     * Add a method interceptor to the manager.
     *
     * @param annotationType annotation type
     * @param interceptor    Interceptor
     */
    public static void addBeanMethodInterceptor(Class annotationType, BeanMethodInterceptor interceptor) {
        BeanMethodInterceptorChain interceptorChain = INTERCEPTOR_MAP.get(annotationType);
        if (null == interceptorChain) {
            interceptorChain = new BeanMethodInterceptorChain();
            INTERCEPTOR_MAP.put(annotationType, interceptorChain);
        }
        interceptorChain.add(interceptor);
    }

    /**
     * Execute a method, assemble an interceptor chain based on the method's annotations, and place it into the interceptor chain for execution
     * Implement a method that assembles the interceptor chain according to the method's annotations and puts it into the interceptor chain.
     *
     * @param annotations List of annotations annotated on the method/Method annotated list of annotations.
     * @param object      Object to which the method belongs/Method owner
     * @param method      Method definition/Method definition
     * @param params      Method parameter list/Method parameter list
     * @param methodProxy Method proxy
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    public static Object doInterceptor(Annotation[] annotations, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        List<Annotation> annotationList = new ArrayList<>();
        List<BeanMethodInterceptorChain> chainList = new ArrayList<>();
        for (Annotation ann : annotations) {
            BeanMethodInterceptorChain chain = INTERCEPTOR_MAP.get(ann.annotationType());
            if (null != chain) {
                chainList.add(chain);
                annotationList.add(ann);
            }
        }
        if (annotationList.isEmpty()) {
            return methodProxy.invokeSuper(object, params);
        }
        MultipleBeanMethodInterceptorChain chain = new MultipleBeanMethodInterceptorChain(annotationList, chainList);
        return chain.startInterceptor(null, object, method, params, methodProxy);
    }
}
