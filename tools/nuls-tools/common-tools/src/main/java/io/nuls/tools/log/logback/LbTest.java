package io.nuls.tools.log.logback;

import io.nuls.tools.log.Log;

public class LbTest {
    public static void main(String[] args){
        /*Logger builder = LoggerBuilder.getLogger("test","consensus");
        while (true){
            builder.debug("test log debug");
            builder.info("test log info");
            builder.warn("test log warn");
            builder.error("test log error");
        }*/
        while (true){
            Log.debug("basic log test");
        }
    }
}
