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
package io.nuls.core.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Json序列化与反序列化工具,使用了jackson作为工具包,优化时主要参考官方文档
 * @see <a href="https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance">Jackson-Performance</a>
 * <br>
 * 以下列举的优化点重要程度递减
 * <br>
 * 1.重用重量级对象: ObjectMapper (data-binding)       已实现
 * <br>
 * 2.Close things that need to be closed: JsonParser, JsonGenerator     未用到
 * <br>
 * 3.输入输出尽量选择字节流,避免二次转换             已实现
 *
 * 文档中还说要进一步优化,可以关注下面四点
 * 1.Compatible, not so easy: Use the Streaming API
 * 2.Non-compatible, easy: Smile binary "JSON"
 * 3.Non-compatible, easy: POJOs as JSON Arrays (Jackson 2.1)
 * 4.Compatible, easy: Afterburner
 * @author captain
 * @version 1.0
 * @date 2019/7/31 下午6:05
 */
public final class JSONUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    public static byte[] obj2ByteArray(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    /**
     * javaBean,list,array convert to json string/对象转JSON字符串
     *
     * @param obj 需转换的对象
     * @return 转换得到的JSON字符串
     */
    public static String obj2json(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static String obj2PrettyJson(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * json string convert to javaBean/JSON字符串转普通对象
     *
     * @param jsonStr JSON字符串
     * @param clazz   目标对象类型
     * @return 转换得到的对象
     */
    public static <T> T json2pojo(String jsonStr, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    public static <T> T byteArray2pojo(byte[] array, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(array, clazz);
    }

    /**
     * JSON字符串转为复杂对象（list,map等）
     *
     * @param json        JSON字符串
     * @param entityClass 目标对象
     * @param itemClass   List/Map保存的数据的类型
     * @return 转换得到的对象
     */
    public static <T> T json2pojo(String json, Class<T> entityClass, Class... itemClass) throws IOException {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(entityClass, itemClass);
        return OBJECT_MAPPER.readValue(json, javaType);
    }

    /**
     * json string convert to map/JSON字符串转MAP
     *
     * @param jsonStr JSON字符串
     * @return 转换得到的MAP
     */
    public static <T> Map<String, Object> json2map(String jsonStr)
            throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map/JSON字符串转MAP
     *
     * @param jsonStr JSON字符串
     * @return 转换得到的MAP
     */
    public static <T> Map<String, T> jsonToMap(String jsonStr)
            throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map with javaBean/JSON转MAP
     *
     * @param jsonStr JSON字符串
     * @param clazz   MAP中值的类型
     * @return 转换得到的MAP
     */
    public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) throws IOException {
        Map<String, Map<String, Object>> map = OBJECT_MAPPER.readValue(jsonStr, new TypeReference<Map<String, T>>() {
        });
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * json array string convert to list with javaBean/JSON字符串转List
     *
     * @param jsonArrayStr JSON字符串
     * @param clazz        List存储的对象类型
     * @return 转换后得到的List
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) throws IOException {
        List<Map<String, Object>> list = OBJECT_MAPPER.readValue(jsonArrayStr, new TypeReference<List<T>>() {
        });
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : list) {
            result.add(map2pojo(map, clazz));
        }
        return result;
    }

    /**
     * map convert to javaBean/map转javabean
     *
     * @param map   需转化的MAP
     * @param clazz 目标类型
     * @return 得到的目标对象
     */
    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

}

