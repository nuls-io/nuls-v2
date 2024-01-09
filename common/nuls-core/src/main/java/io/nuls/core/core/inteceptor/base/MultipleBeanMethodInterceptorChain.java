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

import io.nuls.core.log.Log;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Multiple Interceptor Chain:Only used when there are multiple connector chains for a method, which are initialized before each method execution、assemble
 * Multiple interceptors chain.Only when one method has multiple connector chains,
 * The chain is initialized and assembled every time a method is executed.
 *
 * @author Niels Wang
 */
public class MultipleBeanMethodInterceptorChain extends BeanMethodInterceptorChain {

    /**
     * Annotation List
     */
    protected List<Annotation> annotationList = new ArrayList<>();

    /**
     * Execution progress markers
     * Progress mark
     */
    protected Integer index = -1;

    /**
     * Method proxy
     * Method proxy object
     */
    protected MethodProxy methodProxy;


    /**
     * Initialize multiple interceptor chains
     * Initialize multiple interceptor chains.
     *
     * @param annotations Annotation List
     * @param chainList   Blocker Chain List
     */
    public MultipleBeanMethodInterceptorChain(List<Annotation> annotations, List<BeanMethodInterceptorChain> chainList) {
        if (null == annotations || annotations.isEmpty()) {
            return;
        }
        for (int i = 0; i < annotations.size(); i++) {
            fillInterceptorList(annotations.get(i), chainList.get(i));
        }
    }

    /**
     * Add an interceptor chain to multiple interceptor chains
     * Add an interceptor chain to the multiple interceptor chain,
     *
     * @param annotation                 The annotation corresponding to this interceptor chain、The comment for the interceptor chain.
     * @param beanMethodInterceptorChain Interceptor chain
     */
    private void fillInterceptorList(Annotation annotation, BeanMethodInterceptorChain beanMethodInterceptorChain) {
        for (BeanMethodInterceptor interceptor : beanMethodInterceptorChain.interceptorList) {
            annotationList.add(annotation);
            interceptorList.add(interceptor);
        }
    }


    /**
     * Start executing interceptor chain
     * Start executing the interceptor chain.
     *
     * @param annotation  Annotation instance of interception method/Annotation instances of the intercepting method.
     * @param object      Object to which the method belongs/Method owner
     * @param method      Method definition/Method definition
     * @param params      Method parameter list/Method parameter list
     * @param methodProxy Method proxy
     */
    @Override
    public Object startInterceptor(Annotation annotation, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        this.methodProxy = methodProxy;
        index = -1;
        Object result = null;
        try {
            result = execute(null, object, method, params);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw e;
        } finally {
            index = -1;
            this.methodProxy = null;
        }
        return result;
    }

    /**
     * Call a specific interceptor
     * Call a specific interceptor.
     *
     * @param annotation Annotation instance of interception method/Annotation instances of the intercepting method.
     * @param object     Object to which the method belongs/Method owner
     * @param method     Method definition/Method definition
     * @param params     Method parameter list/Method parameter list
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    @Override
    public Object execute(Annotation annotation, Object object, Method method, Object[] params) throws Throwable {
        index += 1;
        if (index == interceptorList.size()) {
            return methodProxy.invokeSuper(object, params);
        }
        annotation = annotationList.get(index);
        BeanMethodInterceptor interceptor = interceptorList.get(index);
        return interceptor.intercept(annotation, object, method, params, this);
    }
}
