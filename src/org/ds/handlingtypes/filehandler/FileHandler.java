package org.ds.handlingtypes.filehandler;

import java.io.File;
import org.apache.lucene.document.Document;

/**
 * ����getDocument����
 */
public interface FileHandler
{

    /**
     * ���ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    Document getDocument(File file)
            throws FileHandlerException;
}
