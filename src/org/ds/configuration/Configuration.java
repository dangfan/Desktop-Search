/**
 * Configuration.java
 *
 * �����ļ�
 *
 * Modified on 2010/9/10
 */
package org.ds.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * ��ȡproperties�ļ�
 */
public class Configuration
{

    private Properties property;    // ���Զ���
    private String filename;        // �ļ�·��

    /**
     * ��ʼ��Configuration��
     */
    public Configuration()
    {
        property = new Properties();
    }

    /**
     * ��ʼ��Configuration��
     * @param filestream �ļ�������
     */
    public Configuration(InputStream filestream)
    {
        property = new Properties();
        try
        {
            property.load(filestream);
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace(System.err);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * ��ʼ��Configuration��
     * @param filePath Ҫ��ȡ�������ļ���·��+����
     */
    public Configuration(String filePath)
    {
        filename = filePath;
        property = new Properties();
        try
        {
            FileInputStream inputFile = new FileInputStream(filename);
            property.load(inputFile);
            inputFile.close();
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace(System.err);
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * �õ�key��ֵ
     * @param key ȡ����ֵ�ļ�
     * @return key��ֵ
     */
    public String getValue(String key)
    {
        if (property.containsKey(key))
        {
            String value = property.getProperty(key);//�õ�ĳһ���Ե�ֵ
            return value;
        }
        else
        {
            return "";
        }
    }

    /**
     * ���properties�ļ������е�key����ֵ
     */
    public void clear()
    {
        property.clear();
    }

    /**
     * �ı�����һ��key��ֵ����key������properties�ļ���ʱ��key��ֵ��value�����棬
     * ��key������ʱ����key��ֵ��value
     * @param key Ҫ����ļ�
     * @param value Ҫ�����ֵ
     */
    public void setValue(String key, String value)
    {
        property.setProperty(key, value);
    }

    /**
     * �����ĺ���ļ����ݴ���ָ�����ļ��У����ļ��������Ȳ����ڡ�
     */
    public void saveFile()
    {
        try
        {
            FileOutputStream outputFile = new FileOutputStream(filename);
            property.store(outputFile, "Desktop Search Configuration");
            outputFile.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace(System.err);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * ��ȡ��������
     * @return ���Լ���
     */
    public Enumeration getProperties()
    {
        return property.propertyNames();
    }

    @Override
    protected void finalize()
    {
        saveFile();
    }
}
