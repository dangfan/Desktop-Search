/*
 * AppLoader.java
 *
 * 加载各个线程
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
 * 加载程序的各个线程
 * 
 * @author Terro
 */
public class AppLoader
{
    // 执行索引文件的清理
    // 判断是否存在reset文件
    // 如果存在，则清空索引
    private static void cleanUp()
    {
        File resetFile = new File("reset");
        if (resetFile.exists())     // 判断是否存在
        {
            try
            {
                Runtime.getRuntime().exec("cmd /c del indexes /q"); // 清空
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
     * main方法
     * 
     * @param args 启动参数
     */
    public static void main(String[] args)
    {        
        // 清空索引
        cleanUp();

        // 开始索引
        final FolderTraverser traverser = new FolderTraverser();

        // 加载UI线程
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                // 设置LookAndFeel
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
                // 显示窗体
                new SearchBox(traverser).setVisible(true);
            }
        });
    }
}
