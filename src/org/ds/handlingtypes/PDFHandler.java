package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import org.ds.handlingtypes.filehandler.*;

/**
 * 处理PDF文件
 */
public class PDFHandler implements FileHandler
{

    /**
     * 从PDF文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        StringBuffer docText = new StringBuffer();  // 文档内容
        PDDocument pdfDDocument = null;             // PDF文档

        // 读入文档
        try
        {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfDDocument = PDDocument.load(file);
            StringWriter writer = new StringWriter();
            stripper.writeText(pdfDDocument, writer);
            docText.append(writer.getBuffer());
        }
        catch (Exception e)
        {
            throw new FileHandlerException(
                    "无法解析PDF文件", e);
        }

        //生成文档对象
        Document doc = new Document();
        //添加文件名
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //文件类型
        doc.add(new Field("type", "pdf", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //修改日期
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //添加文件路径
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //正文
        if (docText != null)
        {
            doc.add(new Field("contents", docText.toString(), Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }

        //提取其他信息
        try
        {
            PDDocumentInformation docInfo =
                    pdfDDocument.getDocumentInformation();
            String author = docInfo.getAuthor();
            String title = docInfo.getTitle();
            String keywords = docInfo.getKeywords();
            String summary = docInfo.getSubject();
            //作者
            if ((author != null) && !author.equals(""))
            {
                doc.add(new Field("author", author, Field.Store.YES,
                        Field.Index.NOT_ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
            //标题
            if ((title != null) && !title.equals(""))
            {
                doc.add(new Field("title", title, Field.Store.YES,
                        Field.Index.ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
        }
        catch (Exception e)
        {
            throw new FileHandlerException(
                    "无法解析PDF文件", e);
        }

        //关闭文档
        if (pdfDDocument != null)
        {
            org.apache.pdfbox.cos.COSDocument cos = pdfDDocument.getDocument();
            try
            {
                cos.close();
                pdfDDocument.close();
            }
            catch (IOException e)
            {
                //不处理
            }
        }
        return doc;
    }
}
