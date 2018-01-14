package com.geccocrawler.gecco.local;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nobody on 2017/12/24.
 * 文件操作的常用
 * http://blog.csdn.net/brushli/article/details/12356695
 */
public class FileUtil {

    /***
     * FileReader 去读文件,为字符串
     */

    public static String readByFileReader(String filePath)

    {

        StringBuffer str = new StringBuffer("");

        File file = new File(filePath);

        try {

            FileReader fr = new FileReader(file);

            int ch = 0;

            while ((ch = fr.read()) != -1)

            {
                str.append((char) ch);
            }

            fr.close();

        } catch (IOException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

            System.out.println("File reader出错");

        }

        return str.toString();

    }

    /***
     * 创建文件
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                result = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /***
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件
     * @param filePath
     * @return
     */
    public static String readFileByBytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        StringBuffer content = new StringBuffer();

        try {
            byte[] temp = new byte[1024];
            FileInputStream fileInputStream = new FileInputStream(file);
            while (fileInputStream.read(temp) != -1) {
                content.append(new String(temp));
                temp = new byte[1024];
            }

            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }


    /***
     * 以字符为单位读取文件，常用于读文本，数字等类型的文件，支持读取中文
     * @param filePath
     * @return
     */
    public static String readFileByChars(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        StringBuffer content = new StringBuffer();
        try {
            char[] temp = new char[1024];
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
            while (inputStreamReader.read(temp) != -1) {
                content.append(new String(temp));
                temp = new char[1024];
            }

            fileInputStream.close();
            inputStreamReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    /***
     * 以行为单位读取文件，常用于读面向行的格式化文件
     * @param args
     */
    public static List<String> readFileByLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        List<String> content = new ArrayList<String>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String lineContent = "";
            while ((lineContent = reader.readLine()) != null) {
                if(StringUtils.isNotEmpty(lineContent)){
                    content.add(lineContent);
                }
            }

            fileInputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /***
     * 字符串写入文件的几个类中，FileWriter效率最高，BufferedOutputStream次之，FileOutputStream最差。
     （1）通过FileOutputStream写入文件
     覆盖.

     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeFileByFileOutputStream(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(content.getBytes("GBK"));
            fos.close();
        }
    }

    /***
     * 通过BufferedOutputStream写入文件
     * 覆盖.
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeFileByBufferedOutputStream(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(filePath));
            fos.write(content.getBytes("GBK"));
            fos.flush();
            fos.close();
        }
    }

    /***
     * 通过FileWriter将字符串写入文件
     * 覆盖.
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeFileByFileWriter(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            FileWriter fw = new FileWriter(filePath);
            fw.write(content);
            fw.close();
        }
    }

    /***
     * 通过FileWriter将字符串写入文件
     * 追加<------------------------
     *
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeFileByFileWriterAdd(String filePath, String content) throws IOException {
        File file = new File(filePath);
        synchronized (file) {
            // // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter fw = new FileWriter(filePath,true);
            fw.write("\n");//回车
            fw.write(content);
            fw.close();
        }
    }

    public static void main(String[] args) throws IOException {
//        createFile("/Users/wangany/tem/a.txt");

//        String image = readFileByBytes("/Users/wangany/Downloads/44438178_2.png");
//        System.out.println(image);

//        String txt = readFileByChars("/Users/wangany/tem/a.txt");
//        System.out.println(txt);

//        List<String> strings = readFileByLines("/Users/wangany/tem/a.txt");
//        for (String str : strings) {
//            System.out.println(str);
//        }

//        writeFileByFileOutputStream("/Users/wangany/tem/a.txt","fine");
//        writeFileByBufferedOutputStream("/Users/wangany/tem/a.txt","fine good");
//        writeFileByFileWriter("/Users/wangany/tem/a.txt", "fine good perfect");
        writeFileByFileWriterAdd("/Users/wangany/tem/a.txt", "fine good perfect ok");

    }
}
