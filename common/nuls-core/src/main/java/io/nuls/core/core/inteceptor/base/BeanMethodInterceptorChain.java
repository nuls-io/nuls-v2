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
import java.util.List;

/**
 * Method Interceptor Chain：A method can be intercepted by multiple interceptors, and the sequence of these interceptors forms a chain of interceptors. Each interceptor can decide whether to continue executing subsequent interceptors
 * Method the interceptor chain: one method can be more interceptors to intercept,
 * between multiple interceptors sequence formed a chain of interceptors, behind each blocker can decide whether to continue the interceptor
 *
 * @author Niels Wang
 */
public class BeanMethodInterceptorChain {

    /**
     * List of interceptors in the chain
     * List of interceptors in the interceptors chain.
     */
    protected List<BeanMethodInterceptor> interceptorList = new ArrayList<>();

    /**
     * Thread safe execution cache, used to mark the current execution progress
     * Thread-safe execution cache to mark the current execution progress.
     */
    private ThreadLocal<Integer> index = new ThreadLocal<>();

    /**
     * Method proxy cache, thread safe
     * Method agent cache, thread safe.
     */
    private ThreadLocal<MethodProxy> methodProxyThreadLocal = new ThreadLocal<>();

    /**
     * Add a method interceptor to the chain
     * Add a method interceptor to the chain.
     *
     * @param interceptor Interceptor
     */
    protected void add(BeanMethodInterceptor interceptor) {
        interceptorList.add(interceptor);
    }

    /**
     * Put a method into the interceptor chain for execution, and obtain the return result
     * Puts a method in the interceptor chain to retrieve the returned result.
     *
     * @param annotation  Annotation instance of interception method/Annotation instances of the intercepting method.
     * @param object      Object to which the method belongs/Method owner
     * @param method      Method definition/Method definition
     * @param params      Method parameter list/Method parameter list
     * @param methodProxy Method proxy
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    public Object startInterceptor(Annotation annotation, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        methodProxyThreadLocal.set(methodProxy);
        index.set(-1);
        Object result = null;
        try {
            result = execute(annotation, object, method, params);
        } finally {
            index.remove();
            methodProxyThreadLocal.remove();
        }
        return result;
    }


    /**
     * Call a specific interceptor
     * Call a specific interceptor.
     */
    public Object execute(Annotation annotation, Object object, Method method, Object[] params) throws Throwable {
        index.set(1 + index.get());
        if (index.get() == interceptorList.size()) {
            return methodProxyThreadLocal.get().invokeSuper(object, params);
        }
        BeanMethodInterceptor interceptor = interceptorList.get(index.get());
        return interceptor.intercept(annotation, object, method, params, this);
    }

}
