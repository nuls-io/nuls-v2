package io.nuls.tools.io;

import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.log.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author  tag
 * */
public class IoUtils {
    private static final int SIZE = 1024 * 8;
    /**
     * 读取远程文件字节流
     * @param  urlStr 远程文件地址
     * @return 读取回的字节流
     * */
    public static byte[] download(String urlStr) throws IOException {
        Log.info("Get the version info file from " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(60 * 1000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);
        return getData;
    }


    /**
     * 从输入流中获取字节数组
     * @param  inputStream 输入流
     * @return 读取的字节数组
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[inputStream.available()];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 读取本地指定路径的文件信息（不换行）
     * @param path 文件路径
     * @return 文件内容
     * */
    public static String read(String path) throws Exception {
        ObjectUtils.canNotEmpty(path, "null parameter");
        String filePath = IoUtils.class.getClassLoader().getResource(path).getPath();
        return readRealPath(filePath, false);
    }

    /**
     * 读取本地指定绝对路径的文件信息
     * @param realPath 文件的绝对路径
     * @param format   读取时是否换行
     * @return 文件内容
     * */
    public static String readRealPath(String realPath, boolean format) throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(realPath));
        } catch (FileNotFoundException e) {
            Log.error(e);
            throw new Exception(e);
        }
        StringBuilder str = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {

                if (format) {
                    str.append(line);
                    str.append("\n");
                } else {
                    str.append(line.trim());
                }
            }
        } catch (IOException e) {
            Log.error(e);
            throw new Exception(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return str.toString();
    }

    /**
     * 以字节流的方式读取到字符串。
     *
     * @param is 输入流
     * @return 字符串
     */
    public static String readBytesToString(InputStream is) {
        return new String(readBytes(is));
    }

    /**
     * 以字节流的方式读取到字符串。
     *
     * @param is 输入流
     * @param charsetName 字符集
     * @return 字符串
     */
    public static String readBytesToString(InputStream is, String charsetName) {
        try {
            return new String(readBytes(is), charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以字节流的方式从文件中读取字符串
     *
     * @param file 文件
     * @param charsetName 字符集
     * @return 字符串
     */
    public static String readBytesToString(File file, String charsetName) {
        try {
            return new String(readBytes(file), charsetName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 以字节流的方式从文件中读取字符串。
     *
     * @param file 文件
     * @return 字符串
     */
    public static String readBytesToString(File file) {
        return new String(readBytes(file));
    }

    /**
     * 以字节流的方式读取到字符串。
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] readBytes(InputStream is) {
        byte[] bytes = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] cbuf = new byte[SIZE];
            int len;
            ByteArrayOutputStream outWriter = new ByteArrayOutputStream();
            while ((len = bis.read(cbuf))!= -1) {
                outWriter.write(cbuf, 0, len);
            }
            outWriter.flush();

            bis.close();
            is.close();

            bytes = outWriter.toByteArray();
            outWriter.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 以字符流的方式读取到字符串。
     * @param is 输入流
     * @param charsetName 编码
     * @return 字符数组
     */
    public static char[] readChars(InputStream is,String charsetName) {
        char[] chars = null;
        try {
            InputStreamReader isr = null;
            if (charsetName == null) {
                isr = new InputStreamReader(is);
            }else {
                isr = new InputStreamReader(is, charsetName);
            }
            BufferedReader br = new BufferedReader(isr);
            char[] cbuf = new char[SIZE];
            int len;
            CharArrayWriter outWriter = new CharArrayWriter();
            while ((len = br.read(cbuf))!= -1) {
                outWriter.write(cbuf, 0, len);
            }
            outWriter.flush();

            br.close();
            isr.close();
            is.close();

            chars = outWriter.toCharArray();
            outWriter.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chars;
    }

    /**
     * 以字符流的方式读取到字符串。
     * @param file 文件
     * @return 字符串
     */
    public static String readCharsToString(File file) {
        try {
            return readCharsToString(new FileInputStream(file), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以字节流的方式读取到字符串。
     *
     * @param file 文件
     * @param charset 字符集
     * @return 字符串
     */
    public static String readCharsToString(File file, String charset) {
        try {
            return readCharsToString(new FileInputStream(file), charset);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以字符流的方式读取到字符串。默认编码
     *
     * @param is 输入流
     * @return 字符串
     */
    public static String readCharsToString(InputStream is) {
        return new String(readChars(is, null));
    }

    /**
     * 以字符流的方式读取到字符串。
     *
     * @param is 输入流
     * @param charsetName 编码
     * @return 字符串
     */
    public static String readCharsToString(InputStream is, String charsetName) {
        return new String(readChars(is, charsetName));
    }

    // ---------------readCharsToString 完成。分割线-----------------------

    /**
     * 以字节流的方式读取到字符串。
     *
     * @param file 文件
     * @return 字节数组
     */
    public static byte[] readBytes(File file) {
        try {
            return readBytes(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 以字符流的方式读取到字符串。
     *
     * @param file 文件
     * @param charsetName 编码
     * @return 字符数组
     */
    public static char[] readChars(File file, String charsetName) {
        try {
            return readChars(new FileInputStream(file), charsetName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 通过字节输出流输出bytes
     *
     * @param os 输出流
     * @param text 字节数组
     */
    public static void writeBytes(OutputStream os, byte[] text) {
        writeBytes(os, text, 0, text.length);
    }

    /**
     * 通过字节输出流输出bytes
     *
     * @param os 输出流
     * @param text 字节数组
     * @param off 数组起始下标
     * @param lenght 长度
     */
    public static void writeBytes(OutputStream os, byte[] text, int off, int lenght) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write(text, off, lenght);
            bos.flush();
            bos.close();
            os.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过字符输出流输出chars
     *
     * @param os 输出流
     * @param text 字节数组
     * @param charsetName 编码方式
     */
    public static void writeChars(OutputStream os, char[] text, String charsetName) {
        writeChars(os, text, 0, text.length, charsetName);
    }

    /**
     * 通过字符输出流输出chars
     *
     * @param os 输出流
     * @param text 字节数组
     * @param off 数组起始下标
     * @param lenght 长度
     * @param charsetName 编码方式
     */
    public static void writeChars(OutputStream os, char[] text, int off, int lenght, String charsetName) {
        try {
            OutputStreamWriter osw = null;

            if (charsetName == null) {
                osw = new OutputStreamWriter(os);
            } else {
                osw = new OutputStreamWriter(os, charsetName);
            }
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(text, off, lenght);

            bw.flush();
            bw.close();
            osw.close();
            os.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将字符串以默认编码写入文件
     *
     * @param file 文件
     * @param text 字符串
     */
    public static void writeString(File file, boolean append, String text) {
        writeString(file, append, text, 0, text.length(), null);
    }

    /**
     * 将字符串写入文件
     *
     * @param file 文件
     * @param append 是否追加
     * @param text 字符串
     * @param off 起始下标
     * @param lenght 长度
     * @param charsetName 编码名称
     */
    public static void writeString(File file, boolean append, String text, int off, int lenght, String charsetName) {
        try {
            writeString(new FileOutputStream(file, append), text, off, lenght, charsetName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串以默认编码写入文件
     *
     * @param file 文件
     * @param text 字符串
     */
    public static void writeString(File file, String text) {
        writeString(file, false, text, 0, text.length(), null);
    }

    /**
     * 将字符串写入文件（默认覆盖）
     *
     * @param file 文件
     * @param append 是否追加
     * @param text 字符串
     * @param charsetName 编码名称
     */
    public static void writeString(File file, boolean append, String text, String charsetName) {
        writeString(file, append, text, 0, text.length(), charsetName);
    }

    /**
     * 将字符串写入文件（默认覆盖）
     *
     * @param file 文件
     * @param text 字符串
     * @param charsetName 编码名称
     */
    public static void writeString(File file, String text, String charsetName) {
        writeString(file, false, text, 0, text.length(), charsetName);
    }

    /**
     * 字符输出流输出字符串
     *
     * @param os 输出流
     * @param text 字符串
     * @param charsetName 编码
     */
    public static void writeString(OutputStream os, String text, String charsetName) {
        writeString(os, text, 0, text.length(), charsetName);
    }

    /**
     * 字符输出流输出字符串
     *
     * @param os 输出流
     * @param text 字符串
     * @param off 起始下标
     * @param lenght 长度
     * @param charsetName 编码
     */
    public static void writeString(OutputStream os, String text, int off, int lenght, String charsetName) {
        try {
            OutputStreamWriter osw = null;

            if (charsetName == null) {
                osw = new OutputStreamWriter(os);
            } else {
                osw = new OutputStreamWriter(os, charsetName);
            }
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(text, off, lenght);

            bw.flush();
            bw.close();
            osw.close();
            os.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
