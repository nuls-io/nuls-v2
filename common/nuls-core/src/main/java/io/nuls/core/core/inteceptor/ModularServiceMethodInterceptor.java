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
import io.nuls.core.exception.BeanStatusException;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * System default service interceptor
 * System default service interceptor.
 *
 * @author Niels
 */
public class ModularServiceMethodInterceptor implements MethodInterceptor {
    /**
     * Thread safe interceptor execution progress identification
     * Thread-safe interceptors perform progress identification.
     */
    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

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
        threadLocal.set(0);
        Throwable throwable = null;
        while (threadLocal.get() < 100) {
            try {
                return this.doIntercept(obj, method, params, methodProxy);
            } catch (BeanStatusException e) {
                threadLocal.set(threadLocal.get() + 1);
                throwable = e;
                Thread.sleep(200L);
            }
        }
        throw throwable;
    }

    /**
     * Actual interception methods
     * The actual intercept method
     *
     * @param obj         Object to which the method belongs/Method owner
     * @param method      Method definition/Method definition
     * @param params      Method parameter list/Method parameter list
     * @param methodProxy Method proxy
     * @return The return value of the intercepted method can be processed and replaced/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable This method may throw exceptions, please handle with caution/This method may throw an exception, handle with care.
     */
    private Object doIntercept(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        List<Annotation> annotationList = new ArrayList<>();
        if (!method.getDeclaringClass().equals(Object.class)) {
            String className = obj.getClass().getCanonicalName();
            className = className.substring(0, className.indexOf("$$"));
            Class clazz = Class.forName(className);
            fillAnnotationList(annotationList, clazz, method);
        }
        if (annotationList.isEmpty()) {
            return methodProxy.invokeSuper(obj, params);
        }
        return BeanMethodInterceptorManager.doInterceptor(annotationList.toArray(new Annotation[0]), obj, method, params, methodProxy);

    }

    /**
     * List of annotation instances required for assembling interceptors
     * A list of annotated instances needed to assemble the interceptor.
     *
     * @param annotationList List of all annotated instances/Full annotation instance list.
     * @param clazz          The type of object to which the method belongs/The type of the object that the method belongs to.
     * @param method         Method definition/Method definition
     */
    private void fillAnnotationList(List<Annotation> annotationList, Class clazz, Method method) {
        Set<Class> classSet = new HashSet<>();
        for (Annotation ann : method.getDeclaredAnnotations()) {
            annotationList.add(ann);
            classSet.add(ann.annotationType());
        }
        for (Annotation ann : clazz.getDeclaredAnnotations()) {
            if (classSet.add(ann.annotationType())) {
                annotationList.add(0, ann);
            }
        }
        for (Annotation ann : clazz.getAnnotations()) {
            if (classSet.add(ann.annotationType())) {
                annotationList.add(0, ann);
            }
        }
    }
}
