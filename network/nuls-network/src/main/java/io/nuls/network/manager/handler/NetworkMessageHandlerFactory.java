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

package io.nuls.network.manager.handler;


import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.*;
import io.nuls.network.model.message.*;
import io.nuls.network.model.message.base.BaseMessage;

import java.util.HashMap;
import java.util.Map;
/**
 *  message handler factory
 * @author lan
 * @date 2018/10/15
 *
 */
public class NetworkMessageHandlerFactory {

    private Map<String, BaseMeesageHandlerInf> handlerMap = new HashMap<>();

    private static NetworkMessageHandlerFactory INSTANCE = new NetworkMessageHandlerFactory();

    public static NetworkMessageHandlerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * add handlerMap in Constructor
     */
    private NetworkMessageHandlerFactory() {
        handlerMap.put(VersionMessage.class.getName(),VersionMessageHandler.getInstance());
        handlerMap.put(VerackMessage.class.getName(),VerackMessageHandler.getInstance());
        handlerMap.put(GetAddrMessage.class.getName(),GetAddrMessageHandler.getInstance());
        handlerMap.put(AddrMessage.class.getName(),AddrMessageHandler.getInstance());
        handlerMap.put(ByeMessage.class.getName(),ByeMessageHandler.getInstance());
        handlerMap.put(GetTimeMessage.class.getName(),GetTimeMessageHandler.getInstance());
        handlerMap.put(TimeMessage.class.getName(),TimeMessageHandler.getInstance());
    }

    public BaseMeesageHandlerInf getHandler(BaseMessage message) {
        return handlerMap.get(message.getClass().getName());
    }

    public BaseMeesageHandlerInf getHandler(String handlerKey) {
        return handlerMap.get(handlerKey);
    }
}
