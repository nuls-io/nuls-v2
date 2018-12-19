package io.nuls.tools.data;

import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

public class LogUtilTest {
    public static void main(String[] args){
        //logger.debug("test logger");
        NulsLogger log = LoggerBuilder.getLogger("blockLog","test");
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
