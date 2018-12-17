package io.nuls.tools.data;

import ch.qos.logback.classic.Logger;
import io.nuls.tools.log.logback.LoggerBuilder;

public class LogUtilTest {
    public static void main(String[] args){
        //logger.debug("test logger");
        Logger log = LoggerBuilder.getLogger("blockLog");
        while(true){
            log.warn("test block log");
        }
        /*Logger log = LoggerBuilder.getLogger("blockLog",LogUtilTest.class);
        int index = 0;
        while (index<10000){
            index++;
            log.debug("test block log");
        }*/
    }
}
