/*
 * FolderTraverser.java
 *
 * 文件夹遍历
 *
 * Created on 2010-9-1, 14:37:36
 */
package org.ds.indexer;

import java.io.File;
import org.ds.configuration.Configuration;

/**
 * 文件夹遍历
 */
public class FolderTraverser
{

    private Configuration folderConfig;     // 文件夹黑名单配置文件
    private Indexer indexer;                // 索引器
    javax.swing.filechooser.FileSystemView fileSystemView =
            javax.swing.filechooser.FileSystemView.getFileSystemView();
    // 获取文件系统信息

    /**
     * 创建文件夹遍历对象的实例
     */
    public FolderTraverser()
    {
        // 在新线程中完成初始化
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                folderConfig = new Configuration("folder.config");
                indexer = new Indexer();    // 初始化索引器
                traverseDrivers();          // 遍历驱动器
            }
        }).start();
    }

    // 遍历驱动器
    private void traverseDrivers()
    {
        // 判断系统，只处理Windows，因为文件监视只支持Windows
        if (Utility.getSystem().equals("Linux"))    //Linux，忽略
        {
            System.err.println("暂不支持Linux");
            return;
        }
        else    // Windows
        {
            File[] drivers = File.listRoots();  // 获取驱动器列表

            // 添加监视线程，监视采用JNotify库来完成
            for (final File drive : drivers)
            {
                // 驱动器描述信息
                String description = fileSystemView.getSystemTypeDescription(
                        drive);
                // 仅处理本地磁盘（或英文系统的"Local Disk"）
                if (description.equals("本地磁盘") || description.equals(
                        "Local Disk"))
                {
                    new Thread(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            new FileMonitor(drive.getAbsolutePath(), indexer);  // 监视文件夹
                        }
                    }).start();
                }
            }

            // 递归处理子文件夹
            for (File drive : drivers)
            {
                // 驱动器描述信息
                String description = fileSystemView.getSystemTypeDescription(
                        drive);
                // 仅处理本地磁盘（或英文系统的"Local Disk"）
                if (description.equals("本地磁盘") || description.equals(
                        "Local Disk"))
                {
                    traverse(drive);    //遍历文件夹
                }
            }
        }
    }

    // 遍历访问文件夹及其子文件夹
    private void traverse(File folder)
    {
        File[] files = folder.listFiles();

        // 枚举每一项
        for (File file : files)
        {
            // 跳过隐藏文件（夹）
            if (fileSystemView.isHiddenFile(file))
            {
                continue;
            }

            if (file.isDirectory()) // 文件夹
            {
                // 不在黑名单中，则递归遍历
                String state = folderConfig.getValue(file.getAbsolutePath());
                if (!state.equals("forbidden"))
                {
                    traverse(file);
                }
                // 更新索引
                indexer.save();
            }
            else if (Utility.needToHandle(file))    // 文件
            {
                indexer.index(file);
            }
        }
    }

    /**
     * 停止遍历
     */
    public void stop()
    {
        indexer.finalize();
    }

    /**
     * 取得索引数量
     * @return 索引数量
     */
    public int getIndexCount()
    {
        return indexer.getCount();
    }
}
