package io.nuls.tools.model;

import ch.qos.logback.classic.Level;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

public class LogUtilTest {
    public static void main(String[] args){
        //logger.debug("test logger");
        NulsLogger log = LoggerBuilder.getLogger("blockLog","test", Level.ALL, Level.ALL);

        log.debug("test block log debug");

        log.info("test block log info");

        log.warn("test block log warn");

        log.error("test block log error");

        /*Logger log = LoggerBuilder.getLogger("blockLog",LogUtilTest.class);
        int index = 0;
        while (index<10000){
            index++;
            log.debug("test block log");
        }*/
    }
}
