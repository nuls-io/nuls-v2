package io.nuls.tools.log.logback;
import ch.qos.logback.classic.Logger;

public class LbTest {
    public static void main(String[] args){
        Logger builder = LoggerBuilder.getLogger("test");
        while (true){
            builder.debug("test log debug");
            builder.info("test log info");
            builder.warn("test log warn");
            builder.error("test log error");
        }
    }
}
