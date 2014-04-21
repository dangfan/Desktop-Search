/*
 * FileMonitor.java
 *
 * 文件监视器
 *
 * Created on 2010-9-4, 20:10:05
 */
package org.ds.indexer;

import java.io.File;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * 文件监视器
 */
public class FileMonitor
{

    private Indexer indexer;

    /**
     * 创建一个新的文件监视器
     * @param path 要监视的路径
     * @param indexer 索引器
     */
    public FileMonitor(String path, Indexer indexer)
    {
        //初始化indexer
        this.indexer = indexer;

        // 监视文件的创建、删除、修改和重命名
        int mask = JNotify.FILE_CREATED
                | JNotify.FILE_DELETED
                | JNotify.FILE_MODIFIED
                | JNotify.FILE_RENAMED;

        // 监视子目录
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

        //线程休眠，以避免程序结束
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
        // 重命名
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

        // 修改
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

        // 删除
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

        // 创建
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
