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

import java.lang.reflect.Method;

/**
 * The interceptor interface used in the System Object Manager requires defining one's own interceptor and implementing it when intercepting certain methods
 * The interceptor interface used in the system object manager, when you want to intercept some methods,
 * you need to define your own interceptor to implement the interface.
 *
 * @author: Niels Wang
 */
public interface BeanMethodInterceptor<T> {
    /**
     * When an interceptor intercepts a method, it uses the method to logically determine whether to continue calling the intercepted method. It can perform some business operations before and after the call
     * When an interceptor intercepts a method, the method is used to logically determine whether the intercepting method is called in the method,
     * and it can do some business operations before and after the call.
     *
     * @param annotation       Annotation instance of interception method/Annotation instances of the intercepting method.
     * @param object           Object to which the method belongs/Method owner
     * @param method           Method definition/Method definition
     * @param params           Method parameter list/Method parameter list
     * @param interceptorChain Interceptor chain
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    Object intercept(T annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable;
}
