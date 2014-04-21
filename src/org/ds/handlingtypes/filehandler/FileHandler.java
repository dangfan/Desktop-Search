package org.ds.handlingtypes.filehandler;

import java.io.File;
import org.apache.lucene.document.Document;

/**
 * 定义getDocument方法
 */
public interface FileHandler
{

    /**
     * 从文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    Document getDocument(File file)
            throws FileHandlerException;
}
