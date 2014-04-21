/*
 * Indexer.java
 *
 * 索引器
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
 * 索引器类
 */
public class Indexer
{

    private IndexWriter writer = null;            //IndexWriter
    private Configuration fileTypeConfig = null;  //文件类型配置文件
    private ExtensionFileHandler handler = null;  //文件Handler
    private Directory directory = null;           //索引目录
    private IndexSearcher searcher = null;        //搜索器
    private boolean isClosed = false;             //IndexWriter是否关闭

    /**
     * 构造一个新的Indexer对象
     */
    public Indexer()
    {
        //读取配置文件
        try
        {
            fileTypeConfig = new Configuration(getClass().getResourceAsStream(
                    "/org/ds/GUI/Resources/filetypes.config"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        //构造Handler
        handler = new ExtensionFileHandler(fileTypeConfig);

        //新建Index对象
        boolean create = true;    //新建索引
        try
        {
            directory = FSDirectory.open(new File("indexes"));
            if (IndexReader.indexExists(directory)) //索引文件已存在，则追加
            {
                create = false;
            }
            writer = new IndexWriter((directory), //存放索引的目录
                    new ComplexAnalyzer(), //使用mmseg4j的分词
                    create, //追加/新建
                    IndexWriter.MaxFieldLength.LIMITED);
            searcher = new IndexSearcher(directory, true);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 写入indexes并关闭IndexWriter
     */
    @Override
    protected void finalize()
    {
        save();
        isClosed = true;
        try
        {
            writer.optimize();  //进行优化
            writer.close();     //关闭索引
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 索引指定文件
     * @param file 待索引的文件
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
            //System.out.println("正在索引:" + file.getAbsoluteFile());
        }
        catch (Exception e)
        {
            System.err.println("索引文件:" + file.getAbsoluteFile() + "时出错");
            e.printStackTrace(System.err);
        }
    }

    // 判断索引是否存在
    private boolean isExisting(String path)
    {
        Term term = new Term("path", path); // 在path标签中检索
        try
        {
            return searcher.search(new TermQuery(term), 1).totalHits > 0; // 返回结果
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     * 索引指定文件
     * @param path 待索引的文件路径
     */
    public void index(String path)
    {
        // 索引已存在，跳过
        if (isClosed || isExisting(path))
        {
            return;
        }

        // 新建索引
        try
        {
            Document doc = handler.getDocument(new File(path));
            writer.addDocument(doc);
            //System.out.println("正在索引:" + path);
        }
        catch (Exception e)
        {
            System.err.println("索引文件:" + path + "时出错");
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
            //System.out.println("正在删除:" + path);
        }
        catch (Exception ex)
        {
            System.err.println("删除文件:" + path + "索引时出错");
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
            //System.out.println("正在更新:" + oldPath);
        }
        catch (Exception ex)
        {
            System.err.println("更新文件:" + oldPath + "索引时出错");
            ex.printStackTrace(System.err);
        }
    }

    /**
     * 写入indexes并关闭IndexWriter
     */
    public void save()
    {
        if (isClosed)
        {
            return;
        }
        try
        {
            writer.commit();    //保存更新
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 得到记录总数
     * @return 索引记录总数
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
