/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.util;

import java.lang.reflect.Field;

/**
 * @author: PierreLuo
 * @date: 2019-04-26
 */
public class BeanUtilTest {
    public static void setBean(Object src, Object bean) {
        try {
            String beanName;
            Class<?>[] interfaces = bean.getClass().getInterfaces();
            if(interfaces != null && interfaces.length > 0) {
                beanName = interfaces[0].getSimpleName();
            } else {
                beanName = bean.getClass().getSimpleName();
            }
            beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
            Field field = src.getClass().getDeclaredField(beanName);
            field.setAccessible(true);
            field.set(src, bean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setBean(Object src, String beanName, Object bean) {
        try {
            Field field = src.getClass().getDeclaredField(beanName);
            field.setAccessible(true);
            field.set(src, bean);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
