package org.ds.handlingtypes;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import org.ds.handlingtypes.filehandler.*;

/**
 * ����PDF�ļ�
 */
public class PDFHandler implements FileHandler
{

    /**
     * ��PDF�ļ���ȡһ��Document���ʵ��
     * @param file �ļ�����
     * @return һ���µ�Document���ʵ��
     * @throws FileHandlerException DocumentHandler�쳣
     */
    @Override
    public Document getDocument(File file)
            throws FileHandlerException
    {
        StringBuffer docText = new StringBuffer();  // �ĵ�����
        PDDocument pdfDDocument = null;             // PDF�ĵ�

        // �����ĵ�
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
                    "�޷�����PDF�ļ�", e);
        }

        //�����ĵ�����
        Document doc = new Document();
        //����ļ���
        doc.add(new Field("filename", file.getName(), Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        //�ļ�����
        doc.add(new Field("type", "pdf", Field.Store.NO,
                Field.Index.NOT_ANALYZED));
        //�޸�����
        doc.add(new Field("date", Utility.getLastModifiedDate(file),
                Field.Store.YES, Field.Index.NO));
        //����ļ�·��
        doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        //����
        if (docText != null)
        {
            doc.add(new Field("contents", docText.toString(), Field.Store.YES,
                    Field.Index.ANALYZED,
                    Field.TermVector.WITH_POSITIONS_OFFSETS));
        }

        //��ȡ������Ϣ
        try
        {
            PDDocumentInformation docInfo =
                    pdfDDocument.getDocumentInformation();
            String author = docInfo.getAuthor();
            String title = docInfo.getTitle();
            String keywords = docInfo.getKeywords();
            String summary = docInfo.getSubject();
            //����
            if ((author != null) && !author.equals(""))
            {
                doc.add(new Field("author", author, Field.Store.YES,
                        Field.Index.NOT_ANALYZED,
                        Field.TermVector.WITH_POSITIONS_OFFSETS));
            }
            //����
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
                    "�޷�����PDF�ļ�", e);
        }

        //�ر��ĵ�
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
                //������
            }
        }
        return doc;
    }
}
