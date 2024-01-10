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


package io.nuls.core.rpc.model;

import io.nuls.core.rpc.info.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method with this annotation will be considered：External provided interfaces
 * The methods which contain this annotation would be considered: the interface provided to the outside world
 *
 * @author tangyi
 * @date 2018/12/5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmdAnnotation {
    /**
     * Calling the string of the interface
     * The string used to invoke method
     *
     * @return String
     */
    String cmd();

    /**
     * The version number of the interface
     * The version of the method
     *
     * @return double
     */
    double version();

    /**
     * Interface permission level, referenceConstants.PUBLICThe annotation for, defaults toPRIVATE
     * Interface permission level, refer to Constants.PUBLIC's annotation, default value is PRIVATE
     *
     * @return String
     */
    String scope() default Constants.PUBLIC;

    /**
     * Return the number of changes in the result
     * Number of changes of return value
     *
     * @return int
     */
    int minEvent() default 0;

    /**
     * Call the minimum interval in seconds
     * Minimum interval of call, unit is seconds
     *
     * @return int
     */
    int minPeriod() default 0;

    /**
     * interface priority
     * interface priority
     * */
    CmdPriority priority() default CmdPriority.DEFAULT;

    /**
     * Method description information
     * Description information of method
     *
     * @return String
     */
    String description();
}
