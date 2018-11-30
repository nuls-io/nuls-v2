package io.nuls.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.tools.parse.JSONUtils;

/**
 * 这个类仅用作自动回调的测试类
 * This class is only used as a test class for automatic callbacks
 *
 * @author tangyi
 * @date 2018/11/26
 * @description For testing only
 */
public class InvokeMethod {
    public void invokeGetHeight(Object object) throws JsonProcessingException {
        System.out.println("invokeGetHeight:" + JSONUtils.obj2json(object));
    }

    public void invokeGetHeight2(Object object) throws JsonProcessingException {
        System.out.println("invokeGetHeight2222:" + JSONUtils.obj2json(object));
    }
}
