package org.ds.handlingtypes.filehandler;

import java.io.*;

import org.apache.lucene.document.Document;
import org.ds.configuration.Configuration;
import org.ds.handlingtypes.DefaultTypeHandler;

/**
 * FileHandler�ӿڵ�һ��ʵ�֡�
 */
public class ExtensionFileHandler implements FileHandler
{

    private Configuration config;    //���ö���

    /**
     * ͨ��ָ��һ������������һ��ExtensionFileHandler����ʵ��
     * 
     * @param configuration ���ö���
     */
    public ExtensionFileHandler(Configuration configuration)
    {
        config = configuration;
    }

    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String name = file.getName();             //�ļ���
        String ext = "";                          //��չ��
        int dotIndex = name.lastIndexOf(".");     //��չ����λ��
        
        if ((dotIndex > 0) && (dotIndex < name.length())) //����չ��
        {
            ext = name.substring(dotIndex + 1, name.length()).toLowerCase();
        }
        String handlerClassName = config.getValue(ext);    //�õ���������͵�����

        //�ж��Ƿ����Handler
        if (handlerClassName.equals(""))    //������
        {
            try
            {
                return (new DefaultTypeHandler()).getDocument(file);
            }
            catch (Exception e)
            {
                throw new FileHandlerException(
                        "�ĵ��޷�����"
                        + file.getAbsolutePath(), e);
            }
        }
        else     //����
        {
            try
            {
                Class handlerClass = Class.forName(handlerClassName);   //��ȡ����
                FileHandler handler =
                        (FileHandler) handlerClass.newInstance();   //��������ʵ��
                return handler.getDocument(file);  //����Docment����
            }
            catch (ClassNotFoundException e)
            {        //��δ�ҵ�
                throw new FileHandlerException(
                        "�޷��ҵ� "
                        + handlerClassName
                        + " ��", e);
            }
            catch (InstantiationException e)
            {        //ʵ����ʧ��
                throw new FileHandlerException(
                        "�޷����� "
                        + handlerClassName
                        + "���ʵ��", e);
            }
            catch (IllegalAccessException e)
            {        //�޷�����
                throw new FileHandlerException(
                        "�޷����� "
                        + handlerClassName
                        + "���ʵ��", e);
            }
            catch (FileHandlerException e)
            {      //DocumentHandler�쳣
                throw new FileHandlerException(
                        "�ĵ��޷�����"
                        + file.getAbsolutePath(), e);
            }
        }
    }
}
