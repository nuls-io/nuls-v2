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


import io.nuls.network.locker.Lockers;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.manager.handler.message.OtherModuleMessageHandler;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.model.po.ProtocolHandlerPo;
import io.nuls.network.model.po.RoleProtocolPo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 映射 message 与 message handler 的关系
 * message handler factory
 * Map the relationship between message and message handler
 *
 * @author lan
 * @date 2018/10/15
 */
public class MessageHandlerFactory {
    private StorageManager storageManager = StorageManager.getInstance();
    private static Map<String, BaseMeesageHandlerInf> handlerMap = new HashMap<>();
    /**
     * key : protocol cmd, value : Map<role,ProtocolRoleHandler>
     */
    private static Map<String, Map<String, ProtocolRoleHandler>> protocolRoleHandlerMap = new ConcurrentHashMap<>();

    private static MessageHandlerFactory INSTANCE = new MessageHandlerFactory();

    public static MessageHandlerFactory getInstance() {
        return INSTANCE;
    }

    public Map<String, Map<String, ProtocolRoleHandler>> getProtocolRoleHandlerMap() {
        return protocolRoleHandlerMap;
    }

    public static void addHandler(String messageCmd, BaseMeesageHandlerInf handler) {
        handlerMap.put(messageCmd, handler);
    }

    private MessageHandlerFactory() {

    }

    public BaseMeesageHandlerInf getHandler(String messageCmd) {
        return handlerMap.get(messageCmd);
    }

    public OtherModuleMessageHandler getOtherModuleHandler() {
        return OtherModuleMessageHandler.getInstance();
    }

    /**
     * add handler Map entity
     *
     * @param protocolCmd protocolCmd
     * @param handler     handler
     */
    public void addProtocolRoleHandlerMap(String protocolCmd, ProtocolRoleHandler handler) {
        Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.lock();
        try {
            Map<String, ProtocolRoleHandler> roleMap = protocolRoleHandlerMap.get(protocolCmd);
            if (null == roleMap) {
                roleMap = new HashMap<>();
                roleMap.put(handler.getRole(), handler);
                protocolRoleHandlerMap.put(protocolCmd, roleMap);
            } else {
                //replace
                roleMap.put(handler.getRole(), handler);

            }
        } finally {
            Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.unlock();
        }
    }

    public void clearCacheProtocolRoleHandlerMap(String role) {
        Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.lock();
        try {
            Collection<Map<String, ProtocolRoleHandler>> values = protocolRoleHandlerMap.values();
            for (Map<String, ProtocolRoleHandler> value : values) {
                value.remove(role);
            }
        } finally {
            Lockers.PROTOCOL_HANDLERS_REGISTER_LOCK.unlock();
        }

    }

    /**
     * get handler entity
     *
     * @param protocolCmd protocolCmd
     * @return Collection
     */
    public Collection<ProtocolRoleHandler> getProtocolRoleHandlerMap(String protocolCmd) {
        if (null != protocolRoleHandlerMap.get(protocolCmd)) {
            return protocolRoleHandlerMap.get(protocolCmd).values();
        }
        return null;
    }

    public void init() {
        /*
         * 加载协议注册信息
         * load protocolRegister info
         */
        List<RoleProtocolPo> list = storageManager.getProtocolRegisterInfos();
        for (RoleProtocolPo roleProtocolPo : list) {
            roleProtocolPo.getRole();
            List<ProtocolHandlerPo> protocolHandlerPos = roleProtocolPo.getProtocolHandlerPos();
            for (ProtocolHandlerPo protocolHandlerPo : protocolHandlerPos) {
                ProtocolRoleHandler protocolRoleHandler = new ProtocolRoleHandler(roleProtocolPo.getRole(), protocolHandlerPo.getHandler());
                addProtocolRoleHandlerMap(protocolHandlerPo.getProtocolCmd(), protocolRoleHandler);
            }
        }
    }
}
