/*
 * FileMonitor.java
 *
 * �ļ�������
 *
 * Created on 2010-9-4, 20:10:05
 */
package org.ds.indexer;

import java.io.File;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * �ļ�������
 */
public class FileMonitor
{

    private Indexer indexer;

    /**
     * ����һ���µ��ļ�������
     * @param path Ҫ���ӵ�·��
     * @param indexer ������
     */
    public FileMonitor(String path, Indexer indexer)
    {
        //��ʼ��indexer
        this.indexer = indexer;

        // �����ļ��Ĵ�����ɾ�����޸ĺ�������
        int mask = JNotify.FILE_CREATED
                | JNotify.FILE_DELETED
                | JNotify.FILE_MODIFIED
                | JNotify.FILE_RENAMED;

        // ������Ŀ¼
        boolean watchSubtree = true;

        // add actual watch
        try
        {
            JNotify.addWatch(path, mask, watchSubtree, new Listener());
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        //�߳����ߣ��Ա���������
        while (true)
        {
            try
            {
                Thread.sleep(10000);
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    class Listener implements JNotifyListener
    {
        // ������
        @Override
        public void fileRenamed(int wd, String rootPath, String oldName,
                String newName)
        {
            try
            {
                if (Utility.needToHandle(new File(rootPath + oldName)))
                {
                    indexer.update(rootPath + oldName, rootPath + newName);
                }
            }
            catch (Exception e)
            {
                System.err.println(rootPath + newName);
                e.printStackTrace(System.err);
            }
        }

        // �޸�
        @Override
        public void fileModified(int wd, String rootPath, String name)
        {
            try
            {
                if (Utility.needToHandle(new File(rootPath + name)))
                {
                    indexer.update(rootPath + name, rootPath + name);
                }
            }
            catch (Exception e)
            {
                System.err.println(rootPath + name);
                e.printStackTrace(System.err);
            }
        }

        // ɾ��
        @Override
        public void fileDeleted(int wd, String rootPath, String name)
        {
            try
            {
                if (Utility.needToHandle(new File(rootPath + name)))
                {
                    indexer.delete(rootPath + name);
                }
            }
            catch (Exception e)
            {
                System.err.println(rootPath + name);
                e.printStackTrace(System.err);
            }
        }

        // ����
        @Override
        public void fileCreated(int wd, String rootPath, String name)
        {
            try
            {
                if (Utility.needToHandle(new File(rootPath + name)))
                {
                    indexer.index(rootPath + name);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
    }
}
