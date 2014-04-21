package org.ds.handlingtypes;

import java.io.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.ds.handlingtypes.filehandler.*;

/**
 * 处理Microsoft Excel 97-2003文件
 */
public class ExcelXlsHandler implements FileHandler
{

    /**
     * 从Excel文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = "";   //保存文本内容
        SummaryInformation summary = null;  //保存文件信息

        POIFSFileSystem poifsfs = null; //office文件流
        //读入文件
        try
        {
            poifsfs = new POIFSFileSystem(new FileInputStream(file));
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "无法读取文件", e);
        }

        //读取正文
        try
        {
            bodyText = new ExcelExtractor(poifsfs).getText();
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "读取文件出现错误："
                    + file.getAbsolutePath(), e);
        }

        //读取文件信息
        try
        {
            summary = new HSSFWorkbook(poifsfs).getSummaryInformation();
        }
        catch (Exception e)
        {
            throw new FileHandlerException("读取文件出现错误", e);
        }

        //生成Document对象
        Document doc = new Document();
        //添加文件名字段
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //添加文件类型字段
        doc.add(new Field("type", "xls", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //添加修改日期字段
        doc.add(new Field("date", Utility.getFormattedDate(
                summary.getLastSaveDateTime()), Field.Store.YES,
                Field.Index.NO));
        //添加文件路径
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //标题不为空，添加标题字段
        if (summary.getTitle() != null)
        {
            doc.add(new Field("title", summary.getTitle(), Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        //作者不为空，添加作者字段
        if (summary.getAuthor() != null)
        {
            doc.add(new Field("author", summary.getAuthor(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        //正文不为空，添加正文字段
        if (!bodyText.isEmpty())
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        return doc;
    }
}
