package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ds.handlingtypes.filehandler.*;
import org.farng.mp3.*;
import org.farng.mp3.id3.*;

/**
 * 处理mp3文件
 */
public class AudioHandler implements FileHandler
{

    /**
     * 从其他类型文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        Document doc = new Document();
        //添加文件名字段
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //添加文件类型字段
        doc.add(new Field("type", "audio", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //添加文件修改日期字段
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //添加文件路径
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));

        //添加其他信息
        try
        {
            MP3File mp3file = new MP3File(file);        // MP3文件
            AbstractID3v2 id3 = mp3file.getID3v2Tag();  // ID3v2标签

            String title = id3.getSongTitle();          // 标题
            String albumTitle = id3.getAlbumTitle();    // 唱片名
            String year = id3.getYearReleased();        // 年份
            String author = id3.getLeadArtist();        // 歌手

            // 如果标题不空
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

            // 如果歌手不空
            if (!author.equals(""))
            {
                doc.add(new Field("author", author, Field.Store.YES,
                        Field.Index.ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
        }
        catch (Exception e)
        {
            throw new FileHandlerException("无法解析文件");
        }

        return doc;
    }
}
