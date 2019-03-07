/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.api;

import java.util.*;

/**
 * @author Niels
 */
public class ApiContext {

    public static boolean doSync = true;

    public static long bestHeight;

    public static Properties config;

    //种子节点地址
    public static Set<String> SEED_NODE_ADDRESS = new HashSet<>();
    //开发者节点地址
    public static Set<String> DEVELOPER_NODE_ADDRESS = new HashSet<>();
    //大使节点地址
    public static Set<String> AMBASSADOR_NODE_ADDRESS = new HashSet<>();
    //映射地址
    public static Set<String> MAPPING_ADDRESS = new HashSet<>();
    //商务地址
    public static String BUSINESS_ADDRESS;
    //团队地址
    public static String TEAM_ADDRESS;
    //社区地址
    public static String COMMUNITY_ADDRESS;
    //销毁地址
    public static String DESTROY_ADDRESS;

    //Nuls数量统计
    public static Map<String, Long> NULS_MAP = new HashMap<>();

    static {
        NULS_MAP.put("total", 0L);
        NULS_MAP.put("consensusTotal", 0L);
        NULS_MAP.put("circulation", 0L);
    }

}
