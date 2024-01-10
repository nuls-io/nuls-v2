/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.parse;

import io.nuls.core.constant.ToolsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Internationalization tools that can encode information into readable strings in different languages based on the language set by the system
 * Internationalized tools can convert information encoding into
 * a readable string of different languages, depending on the language set by the system.
 *
 * @author tag
 */
public class I18nUtils {
    /**
     * Language Pack Pool、Language pack data containing all configurations
     * The language pool contains all the configured language package entity.
     */
    private static final Map<String, Properties> ALL_MAPPING = new HashMap<>();

    /**
     * The system has currently selected a language pack
     * The system currently selects the language package.
     */
    private static Properties nowMapping = null;

    /**
     * The default language is set to English
     * default language is English
     */
    private static String key = "en";

    /**
     * Default language pack storage folder name
     * default properties file folder
     */
    private static final String FOLDER = "common-languages";

    public static boolean isSystemWin() {
        String pathSeparator = System.getProperty("path.separator");
        String unixPathSeparator = ":";
        if (unixPathSeparator.equals(pathSeparator)) {
            return false;
        } else {
            return true;
        }

    }

    public static void loadCommonLanguage(String defaultLanguage) {
        ALL_MAPPING.clear();
        load(I18nUtils.class, "common-languages", defaultLanguage);
    }

    public static void loadLanguage(Class c,String folder, String defaultLanguage) {
        ALL_MAPPING.clear();
        load(I18nUtils.class, "common-languages", defaultLanguage);
        load(c, folder, defaultLanguage);
    }

    /**
     * @param c
     * @param folder
     * @param defaultLanguage
     */
    private static void load(Class c, String folder, String defaultLanguage) {
        InputStream in = null;
        try {
            if (StringUtils.isBlank(folder)) {
                folder = FOLDER;
            }
            if (StringUtils.isNotBlank(defaultLanguage)) {
                key = defaultLanguage;
            }
            URL furl = I18nUtils.class.getClassLoader().getResource(folder);
            if (furl == null){
                Log.warn("language folder not exists");
                return ;
            }
            if (null != furl) {
                File folderFile = new File(furl.getPath());
                Log.info("furl.getPath()=" + furl.getPath());
                if (null != folderFile && null != folderFile.listFiles()) {
                    for (File file : folderFile.listFiles()) {
                        InputStream is = new FileInputStream(file);
                        Properties prop = new Properties();
                        prop.load(new InputStreamReader(is, ToolsConstant.DEFAULT_ENCODING));
                        String key = file.getName().replace(".properties", "");
                        Log.info("key[0]={}", key);
                        if (ALL_MAPPING.containsKey(key)) {
                            ALL_MAPPING.get(key).putAll(prop);
                        } else {
                            ALL_MAPPING.put(key, prop);
                        }
                    }
                } else {
                    URL url = c.getProtectionDomain().getCodeSource().getLocation();
                    if (url.getPath().endsWith(".jar")) {
                        try {
                            JarFile jarFile = new JarFile(url.getFile());
                            Enumeration<JarEntry> entrys = jarFile.entries();
                            while (entrys.hasMoreElements()) {
                                JarEntry jar = entrys.nextElement();
                                if (jar.getName().indexOf(folder + "/") == 0 && jar.getName().length() > (folder + "/").length()) {
                                    Log.info(jar.getName());
                                    in = I18nUtils.class.getClassLoader().getResourceAsStream(jar.getName());
                                    Properties prop = new Properties();
                                    prop.load(in);
                                    String key = jar.getName().replace(".properties", "");
                                    key = key.replace(folder + "/", "");
                                    Log.info("key[1]={}", key);
                                    if (ALL_MAPPING.containsKey(key)) {
                                        ALL_MAPPING.get(key).putAll(prop);
                                    } else {
                                        ALL_MAPPING.put(key, prop);
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        boolean isSuccess = false;
                        String jarPath = url.getPath();
                        if (jarPath.indexOf("!") > 0) {
                            try {
                                String filePath = folder + File.separator + defaultLanguage + ".properties";
                                Log.info("filePath[2]={}", filePath);
                                in = I18nUtils.class.getClassLoader().getResourceAsStream(filePath);
                                Properties prop = new Properties();
                                prop.load(in);
                                String key = defaultLanguage;
                                Log.info("key[2]={}, load properties size={}", key, prop.size());
                                if (ALL_MAPPING.containsKey(key)) {
                                    ALL_MAPPING.get(key).putAll(prop);
                                } else {
                                    ALL_MAPPING.put(key, prop);
                                }
                                isSuccess = true;
                            } catch (Exception e) {
                                Log.error(e.getMessage());
                            }
                        }
                        if(!isSuccess) {
                        Log.error("unSupport loadLanguage!");
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Set system language and switch language packs
     * Set up the system language and switch the language package.
     *
     * @param lang Language identification/Language identification
     */
    public static void setLanguage(String lang) throws NulsException {
        if (StringUtils.isBlank(lang)) {
            throw new NulsException(new Error());
        }
        key = lang;
        nowMapping = ALL_MAPPING.get(lang);
        if(nowMapping == null){
            throw new IllegalArgumentException("config error, can't found language package : " + lang);
        }
    }

    /**
     * Obtain a translated message body based on information encoding
     * Obtain a translated message body based on the information encoding.
     *
     * @param id Information encoding
     * @return String Translated string/The translated string.
     */
    public static String get(String id) {
        if (nowMapping == null) {
            nowMapping = ALL_MAPPING.get(key);
            return id;
        }
        return nowMapping.getProperty(id + "");
    }

    /**
     * Determine if a language pack has been loaded
     * Determines whether a language package has been loaded.
     *
     * @param lang Language identification/Language identification
     * @return Judging the result/Determine the results
     */
    public static boolean hasLanguage(String lang) {
        Objects.requireNonNull(lang,"must be enter language");
        return ALL_MAPPING.containsKey(lang);
    }

    /**
     * Obtain the language identifier set by the current system
     * Gets the language id that the current system has set.
     *
     * @return String Language identification/Language identification
     */
    public static String getLanguage() {
        return key;
    }

    public static Map<String, Properties> getAll() {
        return ALL_MAPPING;
    }

}
