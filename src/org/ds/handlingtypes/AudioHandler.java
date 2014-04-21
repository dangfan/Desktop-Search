package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ds.handlingtypes.filehandler.*;
import org.farng.mp3.*;
import org.farng.mp3.id3.*;

/**
 * ����mp3�ļ�
 */
public class AudioHandler implements FileHandler
{

    /**
     * �����������ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        Document doc = new Document();
        //����ļ����ֶ�
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //����ļ������ֶ�
        doc.add(new Field("type", "audio", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //����ļ��޸������ֶ�
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));

        //���������Ϣ
        try
        {
            MP3File mp3file = new MP3File(file);        // MP3�ļ�
            AbstractID3v2 id3 = mp3file.getID3v2Tag();  // ID3v2��ǩ

            String title = id3.getSongTitle();          // ����
            String albumTitle = id3.getAlbumTitle();    // ��Ƭ��
            String year = id3.getYearReleased();        // ���
            String author = id3.getLeadArtist();        // ����

            // ������ⲻ��
            if (!title.equals(""))
            {
                if (!year.equals(""))
                {
                    title += " - " + year;
                }
                if (!albumTitle.equals(""))
                {
                    title += " - " + albumTitle;
                }
                doc.add(new Field("title", title, Field.Store.YES,
                        Field.Index.ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }

            // ������ֲ���
            if (!author.equals(""))
            {
                doc.add(new Field("author", author, Field.Store.YES,
                        Field.Index.ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
        }
        catch (Exception e)
        {
            throw new FileHandlerException("�޷������ļ�");
        }

        return doc;
    }
}
