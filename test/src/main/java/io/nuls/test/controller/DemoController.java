package io.nuls.test.controller;

import io.nuls.tools.core.annotation.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 19:48
 * @Description: 功能描述
 */
@Path("/demo")
@Component
public class DemoController {

    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public String hello(@QueryParam("name") String name){
        return "hello " + name;
    }

}
