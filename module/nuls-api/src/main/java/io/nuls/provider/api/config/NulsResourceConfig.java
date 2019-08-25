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
package io.nuls.provider.api.config;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.provider.api.filter.RpcServerFilter;
import io.nuls.provider.api.jsonrpc.JsonRpcContext;
import io.nuls.provider.api.jsonrpc.RpcMethodInvoker;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.provider.utils.Log;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Niels
 */
public class NulsResourceConfig extends ResourceConfig {

    public NulsResourceConfig() {
        register(MultiPartFeature.class);
        register(RpcServerFilter.class);
        register(JacksonJsonProvider.class);

        Collection<Object> list = SpringLiteContext.getAllBeanList();
        for (Object object : list) {
            if (object.getClass().getAnnotation(Path.class) != null) {
                Log.debug("register restFul resource:{}", object.getClass());
                register(object);
            }
            initJsonRpcMethodHandlers(object);
        }
    }

    private void initJsonRpcMethodHandlers(Object bean) {
        Annotation anno = SpringLiteContext.getFromArray(bean.getClass().getDeclaredAnnotations(), Controller.class);
        if (null == anno) {
            return;
        }
        Method[] methods = bean.getClass().getDeclaredMethods();
        if (null == methods || methods.length == 0) {
            return;
        }
        for (Method method : methods) {
            RpcMethod rpc = (RpcMethod) SpringLiteContext.getFromArray(method.getDeclaredAnnotations(), RpcMethod.class);
            if (null == rpc) {
                continue;
            }
            String methodCmd = rpc.value();
            if (methodCmd == null || methodCmd.trim().length() == 0) {
                Log.warn("null method:" + bean.getClass() + ":" + method.getName());
                continue;
            }
            JsonRpcContext.RPC_METHOD_INVOKER_MAP.put(methodCmd, new RpcMethodInvoker(bean, method));
        }
    }

}
