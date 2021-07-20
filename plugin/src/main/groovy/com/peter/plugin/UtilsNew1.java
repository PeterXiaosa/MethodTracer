package com.peter.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipFile;

/**
 * @author Peter Fu
 * @date 2021/7/14
 */
public class UtilsNew1 {
    private static int BUFFER_SIZE = 16384;

    public static String readFileAsString(String filePath) {
        StringBuffer fileData = new StringBuffer();
        InputStreamReader fileReader = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            fileReader = new InputStreamReader(inputStream, "UTF-8");
            char[] buf = new char[BUFFER_SIZE];
            int numRead = fileReader.read(buf);
            while (numRead != -1) {
                String readData = new String(buf, 0, numRead);
                fileData.append(readData);
                numRead = fileReader.read(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(fileReader);
            closeQuietly(inputStream);
        }
        return fileData.toString();
    }

    private static void closeQuietly(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Closeable) {
            try {
                ((Closeable) obj).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (obj instanceof AutoCloseable) {
            try {
                ((AutoCloseable) obj).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("obj $obj is not closeable");
        }
    }
}
