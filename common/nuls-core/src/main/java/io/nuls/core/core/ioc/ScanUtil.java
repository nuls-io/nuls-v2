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
package io.nuls.core.core.ioc;

import io.nuls.core.model.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 该工具类用于扫描classpath中所有的类，包含jar文件中的class和文件夹下的class
 * The utility class is used to scan all classes in the classpath,
 * including the class in the jar file and the class under the folder.
 *
 * @author Niels Wang
 */
public class ScanUtil {
    private static final ClassLoader CLASS_LOADER = ScanUtil.class.getClassLoader();

    private static final String FILE_TYPE = "file";
    private static final String JAR_TYPE = "jar";
    private static final String CLASS_TYPE = ".class";

    /**
     * 扫描执行包名称下的所有类型
     * Scans all types under the package name.
     *
     * @param packageName 要扫描的包路径，如果传入的路径为空，默认为“io.nuls”/The package path to be scanned.If the incoming path is empty, the default is "IO. Nuls"
     * @return 所有扫描到的类型的列表/List of all scanned types.
     */
    public static List<Class> scan(String packageName) {
        if (StringUtils.isBlank(packageName)) {
            packageName = "io.nuls";
        }
        List<Class> list = new ArrayList<>();
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (null == dirs) {
            return list;
        }
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            if (FILE_TYPE.equals(protocol)) {
                findClassLocal(packageName, url.getPath(), list);
            } else if (JAR_TYPE.equals(protocol)) {
                findClassJar(packageName, url.getPath(), list);
            }
        }
        return list;
    }

    /**
     * 扫描所有本地类型，将扫描到的结果添加到类型列表中
     * Scan all local types and add the scanned results to the list of types.
     *
     * @param packageName 要扫描的包路径/The package path to be scanned.
     * @param filePath    文件路径
     * @param list        结果列表
     */
    public static void findClassLocal(final String packageName, String filePath, List<Class> list) {
        File file = new File(filePath);
        file.listFiles(new LocalFileFilter(packageName, list));
    }

    /**
     * 扫描某个jar包中符合条件的所有类型，将结果添加到类型列表中
     *
     * @param packageName 要扫描的包路径/The package path to be scanned.
     * @param pathName    jar包文件路径/
     * @param list        结果列表
     */
    private static void findClassJar(String packageName, String pathName, List<Class> list) {
        if (StringUtils.isBlank(pathName) || list == null) {
            return;
        }
        JarFile jarFile;
        try {
            int index = pathName.indexOf("!");
            if (index > 0) {
                pathName = pathName.substring(0, index);
            }
            URL url = new URL(pathName);
            jarFile = new JarFile(url.getFile());
        } catch (IOException e) {
            throw new RuntimeException("could not be parsed as a URI reference");
        }
        packageName = packageName.replace(".", "/");
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (!jarEntryName.contains(packageName) || jarEntryName.equals(packageName + "/")) {
                continue;
            }
            if (jarEntryName.endsWith(CLASS_TYPE)) {
                Class<?> clazz;
                try {
                    String className = jarEntry.getName().replace("/", ".").replace(CLASS_TYPE, "");
                    clazz = CLASS_LOADER.loadClass(className);
                } catch (ClassNotFoundException|NoClassDefFoundError e) {
                    continue;
                }
                list.add(clazz);
            }

        }

    }

    /**
     * 文件过滤器
     * File filter
     */
    static class LocalFileFilter implements FileFilter {

        private List<Class> list;
        private String packageName;

        LocalFileFilter(String packageName, List<Class> list) {
            this.list = list;
            this.packageName = packageName;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                findClassLocal(packageName + "." + file.getName(), file.getPath(), list);
                return true;
            }
            if (file.getName().endsWith(CLASS_TYPE)) {
                Class<?> clazz;
                try {
                    clazz = CLASS_LOADER.loadClass(packageName + "." + file.getName().replace(CLASS_TYPE, ""));
                } catch (ClassNotFoundException e) {
                    return false;
                }
                list.add(clazz);
                return true;
            }
            return false;
        }
    }
}
