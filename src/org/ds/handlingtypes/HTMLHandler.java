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
 * ����HTML�ļ�
 */
public class HTMLHandler implements FileHandler
{

    /**
     * ��HTML�ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        String bodyText = Utility.readTextFile(file); //HTML�ļ�����
        String title = null;       //����

        //�������нڵ�
        try
        {
            StringBean sb = new StringBean();       //Visitor
            Parser parser = new Parser();           //������
            parser.setInputHTML(bodyText);          //����Դ
            sb.setLinks(false);                     //���ò���Ҫ�õ�ҳ����������������Ϣ
            sb.setReplaceNonBreakingSpaces(true);   //���ý�����Ͽո�������ո������
            sb.setCollapse(true);                   //���ý�һ���пո���һ����һ�ո�������
            parser.visitAllNodesWith(sb);           //�������нڵ�
            parser.setInputHTML(bodyText);          //����Դ��Ϊ����ȡ����
            bodyText = sb.getStrings();             //�����ַ���
            //��ȡ����
            NodeList nodes = parser.extractAllNodesThatMatch(new NodeClassFilter(
                    TitleTag.class));
            title = nodes.elementAt(0).toPlainTextString();
        }
        catch (ParserException e)
        {
            bodyText = null;
            throw new FileHandlerException(
                    "�����ļ�����", e);
        }

        //�����ĵ�
        Document doc = new Document();
        //����ļ���
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //����ļ������ֶ�
        doc.add(new Field("type", "html", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //���ⲻΪ�գ����
        if (title != null)
        {
            doc.add(new Field("title", title, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }
        //����ļ��޸������ֶ�
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //���Ĳ�Ϊ�գ����
        if (bodyText != null)
        {
            doc.add(new Field("contents", bodyText, Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }

        return doc;
    }
}
