package io.nuls.core.io;

import io.nuls.core.model.ObjectUtils;
import io.nuls.core.log.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author tag
 */
public class IoUtils {
    private static final int SIZE = 1024 * 8;

    /**
     * Read remote file byte stream
     *
     * @param urlStr Remote file address
     * @return Bytestream read and retrieved
     */
    public static byte[] download(String urlStr) throws IOException {
        Log.info("Get the version info file from " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(60 * 1000);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        InputStream inputStream = conn.getInputStream();
        return readInputStream(inputStream);
    }


    /**
     * Get byte array from input stream
     *
     * @param inputStream Input stream
     * @return Byte array read
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
     * Read file information for the local specified path（nowrap）
     *
     * @param path File path
     * @return File content
     */
    public static String read(String path) throws Exception {
        ObjectUtils.canNotEmpty(path, "null parameter");
        path = path.replaceAll("\\\\","/");
        try (InputStream stream = IoUtils.class.getClassLoader().getResourceAsStream(path)) {
            return readRealPath(stream);
        }
    }

    /**
     * Read file information for locally specified absolute path
     *
     * @param stream The absolute path of the file
     * @return File content
     */
    public static String readRealPath(InputStream stream) throws Exception {
        InputStreamReader inReader = null;
        BufferedReader br = null;
        try {
            inReader = new InputStreamReader(stream);
            br = new BufferedReader(inReader);
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                str.append(line.trim());
            }
            return str.toString();
        } catch (IOException e) {
            Log.error(e.getMessage());
            throw e;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (inReader != null) {
                    inReader.close();
                }
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        }
    }

    /**
     * Read a string as a byte stream.
     *
     * @param is Input stream
     * @return character string
     */
    public static String readBytesToString(InputStream is) {
        return new String(readBytes(is));
    }

    /**
     * Read a string as a byte stream.
     *
     * @param is          Input stream
     * @param charsetName character set
     * @return character string
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
     * Read strings from files in a byte stream format
     *
     * @param file        file
     * @param charsetName character set
     * @return character string
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
     * Read strings from files in a byte stream format.
     *
     * @param file file
     * @return character string
     */
    public static String readBytesToString(File file) {
        return new String(readBytes(file));
    }

    /**
     * Read a string as a byte stream.
     *
     * @param is Input stream
     * @return Byte array
     */
    public static byte[] readBytes(InputStream is) {
        byte[] bytes = null;
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] cbuf = new byte[SIZE];
            int len;
            ByteArrayOutputStream outWriter = new ByteArrayOutputStream();
            while ((len = bis.read(cbuf)) != -1) {
                outWriter.write(cbuf, 0, len);
            }
            outWriter.flush();

            bis.close();
            is.close();

            bytes = outWriter.toByteArray();
            outWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Read a string as a character stream.
     *
     * @param is          Input stream
     * @param charsetName coding
     * @return Character array
     */
    public static char[] readChars(InputStream is, String charsetName) {
        char[] chars = null;
        try {
            InputStreamReader isr = null;
            if (charsetName == null) {
                isr = new InputStreamReader(is);
            } else {
                isr = new InputStreamReader(is, charsetName);
            }
            BufferedReader br = new BufferedReader(isr);
            char[] cbuf = new char[SIZE];
            int len;
            CharArrayWriter outWriter = new CharArrayWriter();
            while ((len = br.read(cbuf)) != -1) {
                outWriter.write(cbuf, 0, len);
            }
            outWriter.flush();

            br.close();
            isr.close();
            is.close();

            chars = outWriter.toCharArray();
            outWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chars;
    }

    /**
     * Read a string as a character stream.
     *
     * @param file file
     * @return character string
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
     * Read a string as a byte stream.
     *
     * @param file    file
     * @param charset character set
     * @return character string
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
     * Read a string as a character stream. Default encoding
     *
     * @param is Input stream
     * @return character string
     */
    public static String readCharsToString(InputStream is) {
        return new String(readChars(is, null));
    }

    /**
     * Read a string as a character stream.
     *
     * @param is          Input stream
     * @param charsetName coding
     * @return character string
     */
    public static String readCharsToString(InputStream is, String charsetName) {
        return new String(readChars(is, charsetName));
    }

    // ---------------readCharsToString Complete. Division line-----------------------

    /**
     * Read a string as a byte stream.
     *
     * @param file file
     * @return Byte array
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
     * Read a string as a character stream.
     *
     * @param file        file
     * @param charsetName coding
     * @return Character array
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
     * Output through byte output streambytes
     *
     * @param os   Output stream
     * @param text Byte array
     */
    public static void writeBytes(OutputStream os, byte[] text) {
        writeBytes(os, text, 0, text.length);
    }

    /**
     * Output through byte output streambytes
     *
     * @param os     Output stream
     * @param text   Byte array
     * @param off    Starting index of array
     * @param lenght length
     */
    public static void writeBytes(OutputStream os, byte[] text, int off, int lenght) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write(text, off, lenght);
            bos.flush();
            bos.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Output through character output streamchars
     *
     * @param os          Output stream
     * @param text        Byte array
     * @param charsetName Encoding method
     */
    public static void writeChars(OutputStream os, char[] text, String charsetName) {
        writeChars(os, text, 0, text.length, charsetName);
    }

    /**
     * Output through character output streamchars
     *
     * @param os          Output stream
     * @param text        Byte array
     * @param off         Starting index of array
     * @param lenght      length
     * @param charsetName Encoding method
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Write a string to a file with default encoding
     *
     * @param file file
     * @param text character string
     */
    public static void writeString(File file, boolean append, String text) {
        writeString(file, append, text, 0, text.length(), null);
    }

    /**
     * Write a string to a file
     *
     * @param file        file
     * @param append      Whether to add
     * @param text        character string
     * @param off         Starting index
     * @param lenght      length
     * @param charsetName Encoding name
     */
    public static void writeString(File file, boolean append, String text, int off, int lenght, String charsetName) {
        try {
            writeString(new FileOutputStream(file, append), text, off, lenght, charsetName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a string to a file with default encoding
     *
     * @param file file
     * @param text character string
     */
    public static void writeString(File file, String text) {
        writeString(file, false, text, 0, text.length(), null);
    }

    /**
     * Write a string to a file（Default overlay）
     *
     * @param file        file
     * @param append      Whether to add
     * @param text        character string
     * @param charsetName Encoding name
     */
    public static void writeString(File file, boolean append, String text, String charsetName) {
        writeString(file, append, text, 0, text.length(), charsetName);
    }

    /**
     * Write a string to a file（Default overlay）
     *
     * @param file        file
     * @param text        character string
     * @param charsetName Encoding name
     */
    public static void writeString(File file, String text, String charsetName) {
        writeString(file, false, text, 0, text.length(), charsetName);
    }

    /**
     * Character output stream output string
     *
     * @param os          Output stream
     * @param text        character string
     * @param charsetName coding
     */
    public static void writeString(OutputStream os, String text, String charsetName) {
        writeString(os, text, 0, text.length(), charsetName);
    }

    /**
     * Character output stream output string
     *
     * @param os          Output stream
     * @param text        character string
     * @param off         Starting index
     * @param lenght      length
     * @param charsetName coding
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
