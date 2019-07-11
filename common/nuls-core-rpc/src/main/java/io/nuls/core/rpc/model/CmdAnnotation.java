/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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


package io.nuls.core.rpc.model;

import io.nuls.core.rpc.info.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拥有该注解的方法会被认为：对外提供的接口
 * The methods which contain this annotation would be considered: the interface provided to the outside world
 *
 * @author tangyi
 * @date 2018/12/5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmdAnnotation {
    /**
     * 调用接口的字符串
     * The string used to invoke method
     *
     * @return String
     */
    String cmd();

    /**
     * 接口的版本号
     * The version of the method
     *
     * @return double
     */
    double version();

    /**
     * 接口权限级别，参考Constants.PUBLIC的注释，默认为PRIVATE
     * Interface permission level, refer to Constants.PUBLIC's annotation, default value is PRIVATE
     *
     * @return String
     */
    String scope() default Constants.PUBLIC;

    /**
     * 返回结果的改变次数
     * Number of changes of return value
     *
     * @return int
     */
    int minEvent() default 0;

    /**
     * 调用最小间隔，单位是秒
     * Minimum interval of call, unit is seconds
     *
     * @return int
     */
    int minPeriod() default 0;

    /**
     * 接口优先级
     * interface priority
     * */
    CmdPriority priority() default CmdPriority.DEFAULT;

    /**
     * 方法描述信息
     * Description information of method
     *
     * @return String
     */
    String description();
}
