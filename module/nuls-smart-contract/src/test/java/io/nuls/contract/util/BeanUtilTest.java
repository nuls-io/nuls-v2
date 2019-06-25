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

import com.alibaba.fastjson.JSONObject;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.parse.JSONUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

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
            Field field = getField(src, beanName);
            field.setAccessible(true);
            field.set(src, bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field getField(Object src, String beanName) {
        Field field = null;
        Class<?> srcClass = src.getClass();
        try {
            field = src.getClass().getDeclaredField(beanName);
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superclass;
            while((superclass = srcClass.getSuperclass()) != null) {
                try {
                    field = superclass.getDeclaredField(beanName);
                    return field;
                } catch (NoSuchFieldException e1) {}
                srcClass = superclass;
            }
        }
        return field;
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

    @Test
    public void test() throws IOException {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":2103470749,\"result\":{\"address\":\"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD\",\"alias\":null,\"type\":1,\"txCount\":27,\"totalOut\":70800000,\"totalIn\":1000000060219650,\"consensusLock\":0,\"timeLock\":0,\"balance\":999999989419650,\"totalBalance\":999999989419650,\"totalReward\":1000000060219650,\"tokens\":[\"tNULSeBaNCHAhqG84z2kdeHx6AuFH6Zk6TmDDG,POCMTEST\"]}}";
        RpcResult<Map> rpcResult = JSONObject.parseObject(json, RpcResult.class);
        System.out.println(rpcResult);
        RpcResult rpcResult1 = JSONUtils.json2pojo(json, RpcResult.class);
        System.out.println(rpcResult1);
    }

    class RpcResult<T> {

        private String jsonrpc = "2.0";

        private long id;

        private T result;

        private RpcResultError error;

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public T getResult() {
            return result;
        }

        public RpcResult setResult(T result) {
            this.result = result;
            return this;
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"jsonrpc\":")
                    .append('\"').append(jsonrpc).append('\"');
            sb.append(",\"id\":")
                    .append(id);
            sb.append(",\"result\":")
                    .append('\"').append(result).append('\"');
            sb.append(",\"error\":")
                    .append(error);
            sb.append('}');
            return sb.toString();
        }
    }

    class RpcResultError {

        private String code;

        private String message;

        private Object data;

        public RpcResultError() {

        }

        public RpcResultError(String code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public RpcResultError(ErrorCode errorCode) {
            this.code = errorCode.getCode();
            this.message = errorCode.getMsg();
        }

        public String getCode() {
            return code;
        }

        public RpcResultError setCode(String code) {
            this.code = code;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public RpcResultError setMessage(String message) {
            this.message = message;
            return this;
        }

        public Object getData() {
            return data;
        }

        public RpcResultError setData(Object data) {
            this.data = data;
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"code\":")
                    .append(code);
            sb.append(",\"message\":")
                    .append('\"').append(message).append('\"');
            sb.append(",\"entity\":")
                    .append('\"').append(data).append('\"');
            sb.append('}');
            return sb.toString();
        }
    }
}
