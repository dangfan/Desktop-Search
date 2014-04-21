/*
 * Utility.java
 *
 * 索引的相关工具
 *
 * Created on 2010-8-30, 14:37:36
 */
package org.ds.indexer;

import java.io.*;
import java.util.Enumeration;
import org.ds.configuration.Configuration;

/**
 * 提供索引的相关工具
 */
public class Utility
{

    private static Configuration folderConfig = new Configuration(
            "folder.config");       //文件夹黑名单
    private static Configuration fobiddenTypeConfig = new Configuration(
            "file.config"); //文件类型黑名单
    private static String currentDir = System.getProperty("user.dir"); //当前目录

    /**
     * 获得操作系统
     * @return 操作系统名称
     */
    public static String getSystem()
    {
        return System.getProperty("os.name");
    }

    /**
     * 取得用户根目录
     * @return 用户根目录路径
     */
    public static String getRoot()
    {
        //获取用户目录的查询字符串
        String queryString;

        //判断操作系统
        if (getSystem().equals("Linux"))
        {
            queryString = "";
        }
        else
        {
            queryString = "cmd /c echo %UserProfile%";
        }

        //得到用户目录
        try
        {
            Process p = Runtime.getRuntime().exec(queryString);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            return br.readLine();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return "";
        }
    }

    /**
     * 得到扩展名
     * @param name 文件名/路径
     * @return 扩展名
     */
    public static String getExtension(String name)
    {
        String ext = "";        //扩展名
        int dotIndex = name.lastIndexOf(".");     //扩展名的位置
        if ((dotIndex > 0) && (dotIndex < name.length())) //有扩展名
        {
            ext = name.substring(dotIndex + 1, name.length()).toLowerCase();   //扩展名
        }
        return ext;
    }

    /**
     * 判断文件是否需要解析
     * @param file 文件对象
     * @return true，需要解析；否则为false
     */
    public static boolean needToHandle(File file)
    {
        String filename = file.getAbsolutePath();           //文件路径

        //判断是否可读
        if (!file.canRead())
        {
            return false;
        }

        //排除索引本身
        if (file.getAbsolutePath().startsWith(currentDir))
        {
            return false;
        }

        //判断是否在类型黑名单中
        if (fobiddenTypeConfig.getValue(getExtension(filename)).equals("true"))
        {
            return false;
        }

        //判断是否在文件夹黑名单中
        Enumeration folders = folderConfig.getProperties(); //读取配置
        while (folders.hasMoreElements())                   //枚举每一个黑名单目录
        {
            String key = (String) folders.nextElement();
            if (folderConfig.getValue(key).equals("forbidden")
                    && filename.startsWith(key))    //在黑名单中
            {
                return false;
            }
        }
        return true;
    }
}
