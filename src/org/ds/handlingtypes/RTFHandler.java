package org.ds.handlingtypes;

import java.io.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ds.handlingtypes.filehandler.*;

/**
 * ����RTF�ļ�
 */
public class RTFHandler implements FileHandler
{

    /**
     * ��RTF�ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = null; //����
        DefaultStyledDocument styledDoc = new DefaultStyledDocument();  //RTF�ĵ�
        try
        {
            new RTFEditorKit().read(new FileReader(file), styledDoc, 0);    //��ȡRTF�ĵ�
            bodyText = styledDoc.getText(0, styledDoc.getLength());         //��ȡ����
        }
        catch (IOException e)
        {               //IO�쳣
            throw new FileHandlerException(
                    "IO����", e);
        }
        catch (BadLocationException e)
        {
            throw new FileHandlerException(
                    "�޷���ȡ�ı�", e);
        }
        Document doc = new Document();
        //����ļ����ֶ�
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //����ļ������ֶ�
        doc.add(new Field("type", "rtf", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //����ļ��޸������ֶ�
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        if (bodyText != null)  //���Ĳ�Ϊ�գ���������ֶ�
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        return doc;
    }
}
