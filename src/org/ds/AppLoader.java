/*
 * AppLoader.java
 *
 * ���ظ����߳�
 *
 * Created on 2010-8-30, 14:37:36
 */
package org.ds;

import java.io.File;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.ds.indexer.FolderTraverser;
import org.ds.GUI.SearchBox;

/**
 * AppLoader
 * ���س���ĸ����߳�
 * 
 * @author Terro
 */
public class AppLoader
{
    // ִ�������ļ�������
    // �ж��Ƿ����reset�ļ�
    // ������ڣ����������
    private static void cleanUp()
    {
        File resetFile = new File("reset");
        if (resetFile.exists())     // �ж��Ƿ����
        {
            try
            {
                Runtime.getRuntime().exec("cmd /c del indexes /q"); // ���
                Thread.sleep(500);
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
            resetFile.delete();
        }
    }

    /**
     * main����
     * 
     * @param args ��������
     */
    public static void main(String[] args)
    {        
        // �������
        cleanUp();

        // ��ʼ����
        final FolderTraverser traverser = new FolderTraverser();

        // ����UI�߳�
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                // ����LookAndFeel
                try
                {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                    {
                        if ("Nimbus".equals(info.getName()))
                        {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
                // ��ʾ����
                new SearchBox(traverser).setVisible(true);
            }
        });
    }
}
