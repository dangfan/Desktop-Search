/*
 * Utility.java
 *
 * ��������ع���
 *
 * Created on 2010-8-30, 14:37:36
 */
package org.ds.indexer;

import java.io.*;
import java.util.Enumeration;
import org.ds.configuration.Configuration;

/**
 * �ṩ��������ع���
 */
public class Utility
{

    private static Configuration folderConfig = new Configuration(
            "folder.config");       //�ļ��к�����
    private static Configuration fobiddenTypeConfig = new Configuration(
            "file.config"); //�ļ����ͺ�����
    private static String currentDir = System.getProperty("user.dir"); //��ǰĿ¼

    /**
     * ��ò���ϵͳ
     * @return ����ϵͳ����
     */
    public static String getSystem()
    {
        return System.getProperty("os.name");
    }

    /**
     * ȡ���û���Ŀ¼
     * @return �û���Ŀ¼·��
     */
    public static String getRoot()
    {
        //��ȡ�û�Ŀ¼�Ĳ�ѯ�ַ���
        String queryString;

        //�жϲ���ϵͳ
        if (getSystem().equals("Linux"))
        {
            queryString = "";
        }
        else
        {
            queryString = "cmd /c echo %UserProfile%";
        }

        //�õ��û�Ŀ¼
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
     * �õ���չ��
     * @param name �ļ���/·��
     * @return ��չ��
     */
    public static String getExtension(String name)
    {
        String ext = "";        //��չ��
        int dotIndex = name.lastIndexOf(".");     //��չ����λ��
        if ((dotIndex > 0) && (dotIndex < name.length())) //����չ��
        {
            ext = name.substring(dotIndex + 1, name.length()).toLowerCase();   //��չ��
        }
        return ext;
    }

    /**
     * �ж��ļ��Ƿ���Ҫ����
     * @param file �ļ�����
     * @return true����Ҫ����������Ϊfalse
     */
    public static boolean needToHandle(File file)
    {
        String filename = file.getAbsolutePath();           //�ļ�·��

        //�ж��Ƿ�ɶ�
        if (!file.canRead())
        {
            return false;
        }

        //�ų���������
        if (file.getAbsolutePath().startsWith(currentDir))
        {
            return false;
        }

        //�ж��Ƿ������ͺ�������
        if (fobiddenTypeConfig.getValue(getExtension(filename)).equals("true"))
        {
            return false;
        }

        //�ж��Ƿ����ļ��к�������
        Enumeration folders = folderConfig.getProperties(); //��ȡ����
        while (folders.hasMoreElements())                   //ö��ÿһ��������Ŀ¼
        {
            String key = (String) folders.nextElement();
            if (folderConfig.getValue(key).equals("forbidden")
                    && filename.startsWith(key))    //�ں�������
            {
                return false;
            }
        }
        return true;
    }
}
