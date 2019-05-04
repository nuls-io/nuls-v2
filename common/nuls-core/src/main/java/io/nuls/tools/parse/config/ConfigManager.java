/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.tools.parse.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器，维护所有本节点上运行的链的配置信息
 * @author captain
 * @date 18-11-8 下午1:37
 * @version 1.0
 */
public class ConfigManager {

    /**
     * 配置信息
     */
    private static  Map<String, ConfigItem> configItemsMap = new HashMap<>();

    /**
     * 获取的参数
     * @param name
     * @return String
     */
    public static String getValue(String name) {
        ConfigItem item = configItemsMap.get(name);
        if (item == null) {
            return null;
        }
        return item.getValue();
    }

    /**
     * 设置的参数
     * @param name
     * @param value
     * @return
     */
    public static boolean setValue(String name, String value) {
        ConfigItem item = configItemsMap.get(name);
        if (item.isReadOnly()) {
            return false;
        }
        item.setValue(value);
        return true;
    }

    /**
     * 新增设置参数
     * @param map
     */
    public static void add(Map<String, ConfigItem> map) {
       configItemsMap.putAll(map);
    }

}
