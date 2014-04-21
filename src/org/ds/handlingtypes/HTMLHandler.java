package org.ds.handlingtypes;

import java.io.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.ds.handlingtypes.filehandler.*;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * 处理HTML文件
 */
public class HTMLHandler implements FileHandler
{

    /**
     * 从HTML文件获取一个Document类的实例
     * @param file 文件对象
     * @return 一个新的Document类的实例
     * @throws FileHandlerException DocumentHandler异常
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = Utility.readTextFile(file); //HTML文件内容
        String title = null;       //标题

        //访问所有节点
        try
        {
            StringBean sb = new StringBean();       //Visitor
            Parser parser = new Parser();           //解析器
            parser.setInputHTML(bodyText);          //设置源
            sb.setLinks(false);                     //设置不需要得到页面所包含的链接信息
            sb.setReplaceNonBreakingSpaces(true);   //设置将不间断空格由正规空格所替代
            sb.setCollapse(true);                   //设置将一序列空格由一个单一空格所代替
            parser.visitAllNodesWith(sb);           //访问所有节点
            parser.setInputHTML(bodyText);          //设置源，为了提取标题
            bodyText = sb.getStrings();             //生成字符串
            //提取标题
            NodeList nodes = parser.extractAllNodesThatMatch(new NodeClassFilter(
                    TitleTag.class));
            title = nodes.elementAt(0).toPlainTextString();
        }
        catch (ParserException e)
        {
            bodyText = null;
            throw new FileHandlerException(
                    "解析文件出错", e);
        }

        //生成文档
        Document doc = new Document();
        //添加文件名
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //添加文件类型字段
        doc.add(new Field("type", "html", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //标题不为空，添加
        if (title != null)
        {
            doc.add(new Field("title", title, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        //添加文件修改日期字段
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //添加文件路径
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //正文不为空，添加
        if (bodyText != null)
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }

        return doc;
    }
}
