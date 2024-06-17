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
package io.nuls.core.core.annotation;

import java.lang.annotation.*;

import io.nuls.core.core.inteceptor.base.BeanMethodInterceptor;

/**
 * Interceptor annotation, annotated with the object of the annotation, needs to be implemented{@link BeanMethodInterceptor}Interface that can intercept all methods or objects annotated with specified annotations
 * The interceptor annotation, annotated with the object of the annotation, needs to implement the {@link BeanMethodInterceptor} interface,
 * which intercepts all methods or objects that annotate the specified annotation.
 *
 * @author: Niels Wang
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptor {
    /**
     * The annotation types that this interceptor is concerned about,This annotation can be annotated on a type or method. When on a type, all methods of that class will be intercepted, and when on a method, only the marked methods will be intercepted,Cannot be empty
     * The interceptor CARES about the type of annotation, the annotations can be marked on the type or method, on the type,
     * can intercept all the methods of the class, on the way, only intercept marked method, cannot be empty
     *
     * @return Class
     */
    Class value();
}
