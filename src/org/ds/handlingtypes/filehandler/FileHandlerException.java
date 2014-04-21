package org.ds.handlingtypes.filehandler;

/**
 * DocumentHandler���쳣����
 */
public class FileHandlerException extends Exception
{

    /**
     * ����һ���µ�DocumentHandlerException����
     */
    public FileHandlerException()
    {
        super();
    }

    /**
     * ����һ���µ�DocumentHandlerException����
     * @param msg ������Ϣ
     */
    public FileHandlerException(String msg)
    {
        super(msg);
    }

    /**
     * ����һ���µ�DocumentHandlerException����
     * @param msg ������Ϣ
     * @param cause �쳣��Ϣ
     */
    public FileHandlerException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * ����һ���µ�DocumentHandlerException����
     * @param cause �쳣��Ϣ
     */
    public FileHandlerException(Throwable cause)
    {
        super(cause);
    }
}
