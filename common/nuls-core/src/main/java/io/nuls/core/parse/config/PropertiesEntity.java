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
package io.nuls.core.parse.config;

import java.util.Properties;

/**
 * @author Niels
 */
public class PropertiesEntity {

    private final Properties prop;

    public PropertiesEntity(Properties prop) {
        this.prop = prop;
    }

    /**
     * obtainPropertiesSpecify the value of the specified value type for the specified key
     *
     * @param name         key
     * @param defaultValue Value type
     * @return Value Object
     */
    public <T> T getPropValue(String name, T defaultValue) {
        if (prop.getProperty(name) == null) {
            return defaultValue;
        }
        String value = prop.getProperty(name);
        return IniEntity.getValueByType(value, defaultValue);
    }
}
