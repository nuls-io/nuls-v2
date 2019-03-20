package io.nuls.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.GetAccountByAddressCase;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.config.ConfigSetting;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.JSONUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:08
 * @Description: 功能描述
 */
@Path("/remote/")
@Component
public class RemoteTestController {


    @Path("/call")
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public <T> RemoteResult doTest(RemoteCaseReq req) {
        Class<? extends TestCaseIntf> caseClass = req.getCaseClass();
        Type type = caseClass.getGenericSuperclass();
        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        Class<T> reponseClass = (Class) params[1];
        TestCaseIntf tc = SpringLiteContext.getBean(caseClass);
        try {
            return new RemoteResult(true, tc.check(JSONUtils.json2pojo(req.getParam(), reponseClass), 0));
        } catch (TestFailException e) {
            return new RemoteResult(false,null);
        } catch (IOException e) {
            e.printStackTrace();
            return new RemoteResult(false,null);
        }
    }

}
