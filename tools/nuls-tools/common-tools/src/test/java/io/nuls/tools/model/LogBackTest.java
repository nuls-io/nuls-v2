package io.nuls.tools.model;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogBackTest {
    private static File _tmpDir;
    private Set<File> _filesExpected;

    @BeforeClass
    public static void setupForAll() throws Exception {
        String tmp = System.getProperty("java.io.tmpdir") + "/timedwindowlogbacktest/" + System.currentTimeMillis();
        _tmpDir = new File(tmp);
        _tmpDir.mkdirs();
        assertTrue(_tmpDir + " not created", _tmpDir.isDirectory());
        assertEquals(_tmpDir + " not empty", 0, _tmpDir.list().length);
        for(String p : new String[]{ "java.vendor", "java.version", "os.name", "os.version", "os.arch" } ) {
            System.out.println(p + "=" + System.getProperty(p));
        }
        System.out.println("using " + _tmpDir.getAbsolutePath() + "\n");
    }

    @Before
    public void setup() {
        _filesExpected = new HashSet<File>();
    }

    private void verifyFile(File file, long maxSize) {
        String name = file.getName();
        assertTrue(name + " duplicate", _filesExpected.add(file));
        assertTrue(name + " does not exist", file.canRead());
        assertTrue(name + " size is zero", file.length() > 0);
        float percent = 100f * file.length() / maxSize - 100f;
        String msg = String.format("%s size is %.1fMB which is %.1f%% more than %.1fMB requested",
                name, file.length()/1024f/1024, percent, maxSize/1024f/1024);
        assertTrue(msg, file.length() <= maxSize * 1.1); // give it 10% slack
    }

    private Appender<ILoggingEvent> createAppender(String basename_, String rotated_, long maxFileSize_, int maxFiles_, boolean append_) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        SizeAndTimeBasedFNATP triggeringPolicy = new SizeAndTimeBasedFNATP<ILoggingEvent>();
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(String.valueOf(maxFileSize_)));

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(triggeringPolicy);
        rollingPolicy.setMaxHistory(maxFiles_ - 1);
        rollingPolicy.setFileNamePattern(rotated_);

        triggeringPolicy.setContext(context);
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
        appender.setFile(basename_);
        appender.setAppend(append_);
        appender.setRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(triggeringPolicy);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        triggeringPolicy.start();

        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%t] %replace(%caller{1}){'\\t|Caller.{1}0|\\r\\n', ''} - %msg%n");
        layout.setContext(context);
        layout.start();

        appender.setEncoder(layout);
        appender.setContext(context);
        appender.start();
        return appender;
    }

    private void doTest(final String testname, String basenameS, String rotatedS, boolean compress, int maxFiles, long maxSize) throws Exception {
        File testDir = new File(_tmpDir, testname);
        testDir.mkdirs();
        assertEquals(testDir + " not empty", 0, testDir.listFiles().length);

        File basename = new File(testDir, basenameS);
        File rotated  = new File(testDir, rotatedS + (compress ? ".gz" : ""));
        boolean append = false;

        Appender<ILoggingEvent> appender = createAppender(basename.getPath(), rotated.getPath(), maxSize, maxFiles, append);
        org.slf4j.Logger slf4jLog = LoggerFactory.getLogger(testname);
        Logger logger = (Logger) slf4jLog;
        logger.addAppender(appender);

        // log some stuff, loads of it
        Random rnd = new Random();
        String msg = "{} During the application {} in the case long-lived application on {} when {} is another random";
        for(int i = 0; i < 200000; ++i) {
            logger.info(msg, System.nanoTime(), new Date(), rnd.nextGaussian());
        }

        for(int i = 0; i < maxFiles - 1; ++i) {
            String file = String.format(resolveFilename(rotated.getPath()), i);
            verifyFile(new File(file), maxSize);
        }

        verifyFile(basename, maxSize);

        Set<File> filesPresent = new HashSet<File>(Arrays.asList(testDir.listFiles()));
        filesPresent.removeAll(_filesExpected);
        assertTrue("unexpected " + filesPresent.size() + " files: " + filesPresent, filesPresent.isEmpty());
    }

    private String resolveFilename(String rotatedPattern_) {
        String dateFormat = null;
        final String DP = "%d{";
        int i = rotatedPattern_.indexOf(DP);
        int j;
        if (i > -1) {
            j = rotatedPattern_.indexOf('}');
            if (j > DP.length() + i) {
                dateFormat = rotatedPattern_.substring(i + DP.length(), j);
            }
        } else {
            dateFormat = "yyyy-MM-dd";
            i = rotatedPattern_.indexOf("%d");
            j = i + 1;
        }
        String s = rotatedPattern_.substring(0, i)
                + new SimpleDateFormat(dateFormat).format(new Date())
                + rotatedPattern_.substring(j + 1);
        return s.replace("%i", "%d");
    }

    //  @Test public void testDailyMax3_5MB() throws Exception { doTest("daily_max3_5MB_each", "test.log", "test_%d.log.%i", false, 3, 5 * 1024 * 1024); }
    @Test
    public void testDailyMax8_5MB() throws Exception { doTest("daily_max8_5MB_each", "test.log", "test_%d.log.%i", false, 8, 5 * 1024 * 1024); }

    @Test public void testDailyMax4_10MB() throws Exception { doTest("daily_max4_10MB_each", "test.log", "test_%d.log.%i", false, 4, 10 * 1024 * 1024); }
//  @Test public void testDailyMax2_10MB() throws Exception { doTest("daily_max2_10MB_each", "test.log", "test_%d.log.%i", false, 2, 10 * 1024 * 1024); }

    @Test public void testOnePerDay_max2_30MB() throws Exception { doTest("one_daily_max2_30MB_each", "test.log", "test_%d.log", false, 2, 30 * 1024 * 1024); }
    @Test public void testOnePerDay_max2_15MB() throws Exception { doTest("one_daily_max2_15MB_each", "test.log", "test_%d.log", false, 2, 15 * 1024 * 1024); }

    @Test public void testCustomPattern() throws Exception { doTest("custom_pattern", "test.log", "test_%d{yyyyMMdd}.log", false, 2, 20 * 1024 * 1024); }

    @Test public void testHourly10MB() throws Exception { doTest("hourly10", "test.log", "test_%d{yyyyMMdd_HH}.log", false, 2, 10 * 1024 * 1024); }
    @Test public void testHourly20MB() throws Exception { doTest("hourly20", "test.log", "test_%d{yyyyMMdd_HH}.log", false, 2, 20 * 1024 * 1024); }
}
