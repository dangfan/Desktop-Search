package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.ds.handlingtypes.filehandler.*;

/**
 * ����Microsoft PowerPoint 97-2003�ļ�
 */
public class PowerPointPptHandler implements FileHandler
{

    /**
     * ��PPT�ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = "";   //�����ı�����
        SummaryInformation summary = null;  //�����ļ���Ϣ

        POIFSFileSystem poifsfs = null; //office�ļ���
        //�����ļ�
        try
        {
            poifsfs = new POIFSFileSystem(new FileInputStream(file));
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "�޷���ȡ�ļ�", e);
        }
        //��ȡ����
        try
        {
            bodyText = new PowerPointExtractor(poifsfs).getText();
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "��ȡ�ļ����ִ���"
                    + file.getAbsolutePath(), e);
        }
        //��ȡ�ļ���Ϣ
        try
        {
            summary = new HSLFSlideShow(poifsfs).getSummaryInformation();
        }
        catch (Exception e)
        {
            throw new FileHandlerException("��ȡ�ļ����ִ���", e);
        }
        //����Document����
        Document doc = new Document();
        //����ļ����ֶ�
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //����ļ������ֶ�
        doc.add(new Field("type", "ppt", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //����޸������ֶ�
        doc.add(new Field("date", Utility.getFormattedDate(
                summary.getLastSaveDateTime()), Field.Store.YES,
                Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //���ⲻΪ�գ���ӱ����ֶ�
        if (summary.getTitle() != null)
        {
            doc.add(new Field("title", summary.getTitle(), Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        //���߲�Ϊ�գ���������ֶ�
        if (summary.getAuthor() != null)
        {
            doc.add(new Field("author", summary.getAuthor(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        if (!bodyText.isEmpty()) //���Ĳ�Ϊ�գ���������ֶ�
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        return doc;
    }
}
