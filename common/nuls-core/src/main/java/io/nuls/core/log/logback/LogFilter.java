package io.nuls.core.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;

/**
 * Log printing level filter
 * Log Printing Level Filter
 *
 * @author tag
 * 2018/12/17
 * */
public class LogFilter {
    /**
     * adoptlevelcatalog filter,Less than thislevelAll log files are printed
     * @param level
     * @return
     * */
    public ThresholdFilter getThresholdFilter(Level level){
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(level.levelStr);
        return thresholdFilter;
    }
}
