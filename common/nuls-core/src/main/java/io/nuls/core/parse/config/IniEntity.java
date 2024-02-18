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

import io.nuls.core.model.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Niels
 */
public class IniEntity {

    private final Ini ini;

    public IniEntity(Ini ini) {
        this.ini = ini;
    }

    /**
     * Get the value of the specified key in the specified section
     *
     * @param section Belonging part
     * @param key     attribute（key）
     * @return String The value corresponding to the key
     */
    public String getCfgValue(String section, String key) throws Exception {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            throw new Exception("CONFIGURATION_ITEM_DOES_NOT_EXIST");
        }
        String value = ps.get(key);
        if (StringUtils.isBlank(value)) {
            throw new Exception("CONFIGURATION_ITEM_DOES_NOT_EXIST");
        }
        return value;
    }

    /**
     * Get the value of the specified value type for the specified key in the specified section
     *
     * @param section      Belonging part
     * @param key          attribute（key）
     * @param defaultValue The type of value
     * @return T The value corresponding to the key
     */
    public <T> T getCfgValue(String section, String key, T defaultValue) {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            return defaultValue;
        }
        String value = ps.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return getValueByType(value, defaultValue);
    }

    /**
     * takeStringConvert to specified type
     *
     * @param value        String
     * @param defaultValue Value type
     * @return T Value type
     */
    protected static <T> T getValueByType(String value, T defaultValue) {
        if (defaultValue instanceof Integer) {
            return (T) ((Integer) Integer.parseInt(value));
        } else if (defaultValue instanceof Long) {
            return (T) ((Long) Long.parseLong(value));
        } else if (defaultValue instanceof Float) {
            return (T) ((Float) Float.parseFloat(value));
        } else if (defaultValue instanceof Double) {
            return (T) ((Double) Double.parseDouble(value));
        } else if (defaultValue instanceof Boolean) {
            return (T) ((Boolean) Boolean.parseBoolean(value));
        }
        return (T) value;
    }

    /**
     * obtainSectionobject
     *
     * @param section Section key
     * @return Section
     */
    public Profile.Section getSection(String section) throws Exception {
        Profile.Section ps = ini.get(section);
        if (null == ps) {
            throw new Exception("CONFIGURATION_ITEM_DOES_NOT_EXIST");
        }
        return ps;
    }


    /**
     * Get allSectionkey
     *
     * @return List<String>
     */
    public List<String> getSectionList() {
        Set<Map.Entry<String, Profile.Section>> entrySet = ini.entrySet();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Profile.Section> entry : entrySet) {
            list.add(entry.getKey());
        }
        return list;
    }

    /**
     * Current object conversionString
     *
     * @return String
     */
    @Override
    public String toString() {
        return ini.toString();
    }
}
