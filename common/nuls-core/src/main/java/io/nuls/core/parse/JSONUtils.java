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
package io.nuls.core.parse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JsonSerialization and Deserialization Tools,UsedjacksonAs a toolkit,When optimizing, mainly refer to official documents
 * @see <a href="https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance">Jackson-Performance</a>
 * <br>
 * The importance of the optimization points listed below decreases
 * <br>
 * 1.Reuse heavyweight objects: ObjectMapper (data-binding)       Implemented
 * <br>
 * 2.Close things that need to be closed: JsonParser, JsonGenerator     Not used
 * <br>
 * 3.Try to choose byte streams for input and output as much as possible,Avoid secondary conversion             Implemented
 *
 * The document also states that further optimization is needed,You can pay attention to the following four points
 * 1.Compatible, not so easy: Use the Streaming API
 * 2.Non-compatible, easy: Smile binary "JSON"
 * 3.Non-compatible, easy: POJOs as JSON Arrays (Jackson 2.1)
 * 4.Compatible, easy: Afterburner
 * @author captain
 * @version 1.0
 * @date 2019/7/31 afternoon6:05
 */
public final class JSONUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new AfterburnerModule());
    }

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    public static byte[] obj2ByteArray(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    /**
     * javaBean,list,array convert to json string/Object conversionJSONcharacter string
     *
     * @param obj Objects to be converted
     * @return ConvertedJSONcharacter string
     */
    public static String obj2json(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    public static String obj2PrettyJson(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * json string convert to javaBean/JSONConvert strings to regular objects
     *
     * @param jsonStr JSONcharacter string
     * @param clazz   Target Object Type
     * @return The converted object
     */
    public static <T> T json2pojo(String jsonStr, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, clazz);
    }

    public static <T> T byteArray2pojo(byte[] array, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(array, clazz);
    }

    /**
     * JSONConvert strings to complex objects（list,mapetc.）
     *
     * @param json        JSONcharacter string
     * @param entityClass Target object
     * @param itemClass   List/MapThe type of data saved
     * @return The converted object
     */
    public static <T> T json2pojo(String json, Class<T> entityClass, Class... itemClass) throws IOException {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(entityClass, itemClass);
        return OBJECT_MAPPER.readValue(json, javaType);
    }

    /**
     * json string convert to map/JSONString conversionMAP
     *
     * @param jsonStr JSONcharacter string
     * @return ConvertedMAP
     */
    public static <T> Map<String, Object> json2map(String jsonStr)
            throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map/JSONString conversionMAP
     *
     * @param jsonStr JSONcharacter string
     * @return ConvertedMAP
     */
    public static <T> Map<String, T> jsonToMap(String jsonStr)
            throws IOException {
        return OBJECT_MAPPER.readValue(jsonStr, Map.class);
    }

    /**
     * json string convert to map with javaBean/JSONturnMAP
     *
     * @param jsonStr JSONcharacter string
     * @param clazz   MAPThe type of median
     * @return ConvertedMAP
     */
    public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) throws IOException {
        Map<String, Map<String, Object>> map = OBJECT_MAPPER.readValue(jsonStr, new TypeReference<Map<String, Map<String, Object>>>() {
        });
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * json array string convert to list with javaBean/JSONString conversionList
     *
     * @param jsonArrayStr JSONcharacter string
     * @param clazz        ListTypes of stored objects
     * @return Obtained after conversionList
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) throws IOException {
        List<Map<String, Object>> list = OBJECT_MAPPER.readValue(jsonArrayStr, new TypeReference<List<Map<String, Object>>>() {
        });
        List<T> result = new ArrayList<T>();
        for (Map<String, Object> map : list) {
            result.add(map2pojo(map, clazz));
        }
        return result;
    }

    /**
     * map convert to javaBean/mapturnjavabean
     *
     * @param map   To be convertedMAP
     * @param clazz Target type
     * @return Target object obtained
     */
    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

}

