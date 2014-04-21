/**
 * Configuration.java
 *
 * 配置文件
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
 * 读取properties文件
 */
public class Configuration
{

    private Properties property;    // 属性对象
    private String filename;        // 文件路径

    /**
     * 初始化Configuration类
     */
    public Configuration()
    {
        property = new Properties();
    }

    /**
     * 初始化Configuration类
     * @param filestream 文件流对象
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
     * 初始化Configuration类
     * @param filePath 要读取的配置文件的路径+名称
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
     * 得到key的值
     * @param key 取得其值的键
     * @return key的值
     */
    public String getValue(String key)
    {
        if (property.containsKey(key))
        {
            String value = property.getProperty(key);//得到某一属性的值
            return value;
        }
        else
        {
            return "";
        }
    }

    /**
     * 清除properties文件中所有的key和其值
     */
    public void clear()
    {
        property.clear();
    }

    /**
     * 改变或添加一个key的值，当key存在于properties文件中时该key的值被value所代替，
     * 当key不存在时，该key的值是value
     * @param key 要存入的键
     * @param value 要存入的值
     */
    public void setValue(String key, String value)
    {
        property.setProperty(key, value);
    }

    /**
     * 将更改后的文件数据存入指定的文件中，该文件可以事先不存在。
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
     * 获取所有属性
     * @return 属性集合
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
