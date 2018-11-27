package io.nuls.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.tools.parse.JSONUtils;

/**
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
