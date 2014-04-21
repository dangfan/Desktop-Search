package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ds.handlingtypes.filehandler.*;

/**
 * �����ı��ļ�
 */
public class PlainTextHandler implements FileHandler
{

    /**
     * �Ӵ��ı��ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = Utility.readTextFile(file);
        Document doc = new Document();
        //����ļ����ֶ�
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //����ļ������ֶ�
        doc.add(new Field("type", "txt", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //����ļ��޸������ֶ�
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //���Ĳ�Ϊ�գ���������ֶ�
        if (bodyText != null)
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        return doc;
    }
}
