/*
 * Indexer.java
 *
 * ������
 *
 * Created on 2010-8-30, 14:37:36
 */
package org.ds.indexer;

import java.io.File;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.ds.configuration.Configuration;

import org.ds.handlingtypes.filehandler.ExtensionFileHandler;

/**
 * ��������
 */
public class Indexer
{

    private IndexWriter writer = null;            //IndexWriter
    private Configuration fileTypeConfig = null;  //�ļ����������ļ�
    private ExtensionFileHandler handler = null;  //�ļ�Handler
    private Directory directory = null;           //����Ŀ¼
    private IndexSearcher searcher = null;        //������
    private boolean isClosed = false;             //IndexWriter�Ƿ�ر�

    /**
     * ����һ���µ�Indexer����
     */
    public Indexer()
    {
        //��ȡ�����ļ�
        try
        {
            fileTypeConfig = new Configuration(getClass().getResourceAsStream(
                    "/org/ds/GUI/Resources/filetypes.config"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        //����Handler
        handler = new ExtensionFileHandler(fileTypeConfig);

        //�½�Index����
        boolean create = true;    //�½�����
        try
        {
            directory = FSDirectory.open(new File("indexes"));
            if (IndexReader.indexExists(directory)) //�����ļ��Ѵ��ڣ���׷��
            {
                create = false;
            }
            writer = new IndexWriter((directory), //���������Ŀ¼
                    new ComplexAnalyzer(), //ʹ��mmseg4j�ķִ�
                    create, //׷��/�½�
                    IndexWriter.MaxFieldLength.LIMITED);
            searcher = new IndexSearcher(directory, true);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * д��indexes���ر�IndexWriter
     */
    @Override
    protected void finalize()
    {
        save();
        isClosed = true;
        try
        {
            writer.optimize();  //�����Ż�
            writer.close();     //�ر�����
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * ����ָ���ļ�
     * @param file ���������ļ�
     */
    public void index(File file)
    {
        if (isClosed || isExisting(file.getAbsolutePath()))
        {
            return;
        }

        try
        {
            Document doc = handler.getDocument(file);
            writer.addDocument(doc);
            //System.out.println("��������:" + file.getAbsoluteFile());
        }
        catch (Exception e)
        {
            System.err.println("�����ļ�:" + file.getAbsoluteFile() + "ʱ����");
            e.printStackTrace(System.err);
        }
    }

    // �ж������Ƿ����
    private boolean isExisting(String path)
    {
        Term term = new Term("path", path); // ��path��ǩ�м���
        try
        {
            return searcher.search(new TermQuery(term), 1).totalHits > 0; // ���ؽ��
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     * ����ָ���ļ�
     * @param path ���������ļ�·��
     */
    public void index(String path)
    {
        // �����Ѵ��ڣ�����
        if (isClosed || isExisting(path))
        {
            return;
        }

        // �½�����
        try
        {
            Document doc = handler.getDocument(new File(path));
            writer.addDocument(doc);
            //System.out.println("��������:" + path);
        }
        catch (Exception e)
        {
            System.err.println("�����ļ�:" + path + "ʱ����");
           e.printStackTrace(System.err);
        }
    }

    /**
     *
     * @param path
     */
    public void delete(String path)
    {
        try
        {
            writer.deleteDocuments(new Term("path", path));
            //System.out.println("����ɾ��:" + path);
        }
        catch (Exception ex)
        {
            System.err.println("ɾ���ļ�:" + path + "����ʱ����");
            ex.printStackTrace(System.err);
        }
    }

    /**
     *
     * @param oldPath
     * @param newPath
     */
    public void update(String oldPath, String newPath)
    {
        try
        {
            Document doc = handler.getDocument(new File(newPath));
            writer.updateDocument(new Term("path", oldPath), doc);
            //System.out.println("���ڸ���:" + oldPath);
        }
        catch (Exception ex)
        {
            System.err.println("�����ļ�:" + oldPath + "����ʱ����");
            ex.printStackTrace(System.err);
        }
    }

    /**
     * д��indexes���ر�IndexWriter
     */
    public void save()
    {
        if (isClosed)
        {
            return;
        }
        try
        {
            writer.commit();    //�������
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * �õ���¼����
     * @return ������¼����
     */
    public int getCount()
    {
        try
        {
            return writer.numDocs();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return 0;
        }
    }
}
