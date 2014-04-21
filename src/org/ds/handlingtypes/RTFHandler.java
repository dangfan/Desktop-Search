package org.ds.handlingtypes;

import java.io.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.ds.handlingtypes.filehandler.*;

/**
 * 处理RTF文件
 */
public class RTFHandler implements FileHandler
{

    /**
     * 从RTF文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = null; //正文
        DefaultStyledDocument styledDoc = new DefaultStyledDocument();  //RTF文档
        try
        {
            new RTFEditorKit().read(new FileReader(file), styledDoc, 0);    //读取RTF文档
            bodyText = styledDoc.getText(0, styledDoc.getLength());         //获取正文
        }
        catch (IOException e)
        {               //IO异常
            throw new FileHandlerException(
                    "IO错误", e);
        }
        catch (BadLocationException e)
        {
            throw new FileHandlerException(
                    "无法提取文本", e);
        }
        Document doc = new Document();
        //添加文件名字段
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //添加文件类型字段
        doc.add(new Field("type", "rtf", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //添加文件修改日期字段
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //添加文件路径
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        if (bodyText != null)  //正文不为空，添加正文字段
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        return doc;
    }
}
