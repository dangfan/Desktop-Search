package org.ds.handlingtypes.filehandler;

import java.io.*;

import org.apache.lucene.document.Document;
import org.ds.configuration.Configuration;
import org.ds.handlingtypes.DefaultTypeHandler;

/**
 * FileHandler接口的一个实现。
 */
public class ExtensionFileHandler implements FileHandler
{

    private Configuration config;    //配置对象

    /**
     * 通过指定一个配置来构造一个ExtensionFileHandler对象实例
     * 
     * @param configuration 配置对象
     */
    public ExtensionFileHandler(Configuration configuration)
    {
        config = configuration;
    }

    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String name = file.getName();             //文件名
        String ext = "";                          //扩展名
        int dotIndex = name.lastIndexOf(".");     //扩展名的位置
        
        if ((dotIndex > 0) && (dotIndex < name.length())) //有扩展名
        {
            ext = name.substring(dotIndex + 1, name.length()).toLowerCase();
        }
        String handlerClassName = config.getValue(ext);    //得到处理此类型的类名

        //判断是否存在Handler
        if (handlerClassName.equals(""))    //不存在
        {
            try
            {
                return (new DefaultTypeHandler()).getDocument(file);
            }
            catch (Exception e)
            {
                throw new FileHandlerException(
                        "文档无法处理："
                        + file.getAbsolutePath(), e);
            }
        }
        else     //存在
        {
            try
            {
                Class handlerClass = Class.forName(handlerClassName);   //获取此类
                FileHandler handler =
                        (FileHandler) handlerClass.newInstance();   //构造此类的实例
                return handler.getDocument(file);  //返回Docment对象
            }
            catch (ClassNotFoundException e)
            {        //类未找到
                throw new FileHandlerException(
                        "无法找到 "
                        + handlerClassName
                        + " 类", e);
            }
            catch (InstantiationException e)
            {        //实例化失败
                throw new FileHandlerException(
                        "无法创建 "
                        + handlerClassName
                        + "类的实例", e);
            }
            catch (IllegalAccessException e)
            {        //无法访问
                throw new FileHandlerException(
                        "无法创建 "
                        + handlerClassName
                        + "类的实例", e);
            }
            catch (FileHandlerException e)
            {      //DocumentHandler异常
                throw new FileHandlerException(
                        "文档无法处理："
                        + file.getAbsolutePath(), e);
            }
        }
    }
}
