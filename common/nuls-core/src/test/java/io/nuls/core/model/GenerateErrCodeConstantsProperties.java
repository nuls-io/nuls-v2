package io.nuls.core.model;

import java.io.*;
import java.util.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-18 18:18
 * @Description: 生成语言包文件列表
 */
public class GenerateErrCodeConstantsProperties {

    static String[] LANGS = new String[]{"zh-CHS"};

    static Map<String, TreeMap<String, String>> data = new HashMap<>();

    public static void main(String[] args) {
        File out = new File(System.getProperty("user.dir") + File.separator + "/build/gen_languages");
        if (!out.exists()) {
            out.mkdir();
        }
        Arrays.stream(out.listFiles()).forEach(file -> {
            file.delete();
        });
        File file = new File(System.getProperty("user.dir") + File.separator + "module");
        readLanguages(file, out);
        data.entrySet().forEach(entry -> {
            System.out.println("创建语言包文件：" + out.getAbsolutePath() + File.separator + entry.getKey());
            File outFile = new File(out.getAbsolutePath() + File.separator + entry.getKey());
            if (outFile.exists()) {
                outFile.delete();
            }
            try {
                outFile.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true))) {
                for (Map.Entry<String, String> d : entry.getValue().entrySet()) {
                    System.out.println(d.getKey() + "=" + d.getValue());
                    writer.newLine();
                    writer.write(d.getKey() + "=" + d.getValue());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Arrays.stream(LANGS).forEach(lang->{
                File langFile = new File(out.getAbsolutePath() + File.separator + lang + ".properties");
                System.out.println("创建语言包文件：" + out.getAbsolutePath() + File.separator + lang + ".properties");
                if(langFile.exists()){
                    langFile.delete();
                }
                try {
                    langFile.createNewFile();
                    try(OutputStream outFile1 = new FileOutputStream(langFile);
                        InputStream inFile1 = new FileInputStream(outFile)){
                        inFile1.transferTo(outFile1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void readLanguages(File file, File out) {
        if (file.isFile()) return;
        if (file.getName().equals("target")) return;
        if (file.getAbsolutePath().equals(out.getAbsolutePath())) return;
//        System.out.println("in:" + file.getAbsolutePath());
//        System.out.println("out:" + out.getAbsolutePath());
        Arrays.stream(file.listFiles()).forEach(f -> {
//            System.out.println(f.getAbsolutePath());
            if (f.isDirectory() && f.getName().equals("languages")) {
                warite(f, out);
            } else {
                readLanguages(f, out);
            }
        });
    }

    private static void warite(File f, File outDir) {
        Arrays.stream(f.listFiles()).forEach(file -> {
            try {
                if(!file.getName().startsWith("en"))return ;
                File outFile = new File(outDir.getAbsolutePath() + File.separator + file.getName());
                if (file.getAbsolutePath().equals(outFile.getAbsolutePath())) return;
                System.out.println("找到语言文件：" + file.getAbsolutePath());
                if (!data.containsKey(file.getName())) {
                    data.put(file.getName(), new TreeMap<>((o1, o2) -> {
                        if (o1.length() > o2.length()) {
                            return 1;
                        } else if (o1.length() < o2.length()) {
                            return -1;
                        } else {
                            return o1.toLowerCase().compareTo(o2.toLowerCase());
                        }
                    }));
                }
                Map<String, String> map = data.get(file.getName());
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while (true) {
                        line = reader.readLine();
                        if (line == null) break;
                        if (StringUtils.isBlank(line)) continue;
                        if (line.indexOf("=") < 1) continue;
                        map.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1, line.length()));
                    }
                }
//                if (!outFile.exists()) {
//                    outFile.createNewFile();
////                    try(FileInputStream in = new FileInputStream(file);FileOutputStream out = new FileOutputStream(outFile)){
////                        in.transferTo(out);
////                    }
//                }
//
//                try (
//                        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true));
//                        BufferedReader reader = new BufferedReader(new FileReader(file))) {
//                    String line = reader.readLine();
//                    while (line != null) {
//                        writer.newLine();
//                        writer.write(line);
//                        line = reader.readLine();
//                    }
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        });
    }

}
