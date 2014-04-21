package org.ds.handlingtypes.filehandler;

/**
 * DocumentHandler的异常类型
 */
public class FileHandlerException extends Exception
{

    /**
     * 构造一个新的DocumentHandlerException对象
     */
    public FileHandlerException()
    {
        super();
    }

    /**
     * 构造一个新的DocumentHandlerException对象
     * @param msg 错误信息
     */
    public FileHandlerException(String msg)
    {
        super(msg);
    }

    /**
     * 构造一个新的DocumentHandlerException对象
     * @param msg 错误信息
     * @param cause 异常信息
     */
    public FileHandlerException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * 构造一个新的DocumentHandlerException对象
     * @param cause 异常信息
     */
    public FileHandlerException(Throwable cause)
    {
        super(cause);
    }
}
