/*
 * FolderTraverser.java
 *
 * �ļ��б���
 *
 * Created on 2010-9-1, 14:37:36
 */
package org.ds.indexer;

import java.io.File;
import org.ds.configuration.Configuration;

/**
 * �ļ��б���
 */
public class FolderTraverser
{

    private Configuration folderConfig;     // �ļ��к����������ļ�
    private Indexer indexer;                // ������
    javax.swing.filechooser.FileSystemView fileSystemView =
            javax.swing.filechooser.FileSystemView.getFileSystemView();
    // ��ȡ�ļ�ϵͳ��Ϣ

    /**
     * �����ļ��б��������ʵ��
     */
    public FolderTraverser()
    {
        // �����߳�����ɳ�ʼ��
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                folderConfig = new Configuration("folder.config");
                indexer = new Indexer();    // ��ʼ��������
                traverseDrivers();          // ����������
            }
        }).start();
    }

    // ����������
    private void traverseDrivers()
    {
        // �ж�ϵͳ��ֻ����Windows����Ϊ�ļ�����ֻ֧��Windows
        if (Utility.getSystem().equals("Linux"))    //Linux������
        {
            System.err.println("�ݲ�֧��Linux");
            return;
        }
        else    // Windows
        {
            File[] drivers = File.listRoots();  // ��ȡ�������б�

            // ��Ӽ����̣߳����Ӳ���JNotify�������
            for (final File drive : drivers)
            {
                // ������������Ϣ
                String description = fileSystemView.getSystemTypeDescription(
                        drive);
                // �������ش��̣���Ӣ��ϵͳ��"Local Disk"��
                if (description.equals("���ش���") || description.equals(
                        "Local Disk"))
                {
                    new Thread(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            new FileMonitor(drive.getAbsolutePath(), indexer);  // �����ļ���
                        }
                    }).start();
                }
            }

            // �ݹ鴦�����ļ���
            for (File drive : drivers)
            {
                // ������������Ϣ
                String description = fileSystemView.getSystemTypeDescription(
                        drive);
                // �������ش��̣���Ӣ��ϵͳ��"Local Disk"��
                if (description.equals("���ش���") || description.equals(
                        "Local Disk"))
                {
                    traverse(drive);    //�����ļ���
                }
            }
        }
    }

    // ���������ļ��м������ļ���
    private void traverse(File folder)
    {
        File[] files = folder.listFiles();

        // ö��ÿһ��
        for (File file : files)
        {
            // ���������ļ����У�
            if (fileSystemView.isHiddenFile(file))
            {
                continue;
            }

            if (file.isDirectory()) // �ļ���
            {
                // ���ں������У���ݹ����
                String state = folderConfig.getValue(file.getAbsolutePath());
                if (!state.equals("forbidden"))
                {
                    traverse(file);
                }
                // ��������
                indexer.save();
            }
            else if (Utility.needToHandle(file))    // �ļ�
            {
                indexer.index(file);
            }
        }
    }

    /**
     * ֹͣ����
     */
    public void stop()
    {
        indexer.finalize();
    }

    /**
     * ȡ����������
     * @return ��������
     */
    public int getIndexCount()
    {
        return indexer.getCount();
    }
}
