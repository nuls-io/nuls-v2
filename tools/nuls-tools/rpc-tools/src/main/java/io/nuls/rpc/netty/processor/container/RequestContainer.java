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
package io.nuls.rpc.netty.processor.container;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求的数据存放容器
 * Requested data storage container
 * @author ln
 * 2019/2/27
 * */
public final class RequestContainer {

    private static Map<String, ResponseContainer> REQUEST_MESSAGE_MAP = new ConcurrentHashMap<>();

    public static ResponseContainer putRequest(String messageId) {
        ResponseContainer responseContainer = new ResponseContainer(messageId, new CompletableFuture<>());
        REQUEST_MESSAGE_MAP.put(messageId, responseContainer);
        return responseContainer;
    }

    public static ResponseContainer getResponseContainer(String messageId) {
        return REQUEST_MESSAGE_MAP.get(messageId);
    }

    public static boolean removeResponseContainer(String messageId) {
        return REQUEST_MESSAGE_MAP.remove(messageId) != null;
    }
}
