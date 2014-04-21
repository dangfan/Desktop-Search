/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SearchResultItem.java
 *
 * Created on 2010-9-8, 0:35:04
 */
package org.ds.GUI;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import sun.awt.shell.ShellFolder;

/**
 *
 * @author Terro
 */
public class SearchResultItem extends javax.swing.JPanel
{

    private String filename;        // 文件名
    private String path;            // 完整路径
    private String parentPath;      // 所在文件夹
    private String content;         // 正文
    private String date;            // 日期
    private String title;           // 标题
    private String author;          // 作者
    private Image normalImage;      // 正常的背景
    private Image overImage;        // 鼠标滑过的背景
    private Image img;              // 真正的背景

    /**
     * 创建一个新的结果对象
     * @param filename
     * @param path
     * @param date
     * @param content
     * @param author
     * @param title
     */
    public SearchResultItem(String filename, String path, String content, String date, String title, String author)
    {
        // 初始化图形
        this.normalImage = new ImageIcon(getClass().getResource(
                "Resources/item.png")).getImage();
        this.overImage = new ImageIcon(getClass().getResource(
                "Resources/itemover.png")).getImage();
        this.img = this.normalImage;

        // 设置属性
        this.filename = filename;
        this.path = path;
        this.parentPath = new File(path).getParent();
        this.content = content;
        this.date = "修改日期：" + date;
        this.title = title;
        this.author = author;

        initComponents();   // 初始化控件
        setText();          // 设置文字
    }

    private void setText()
    {
        // 设置文件名
        filenameJLabel.setText("<html>" + getShort(filename, 23, false));

        // 设置显示的路径
        if (parentPath.length() > 30)       //路径太长，只显示前30个字符
        {
            pathJLabel.setText(parentPath.substring(0, 30) + "...");
        }
        else
        {
            pathJLabel.setText(parentPath);
        }

        // 设置正文
        if (filename.endsWith("mp3"))   //mp3文件特殊处理
        {
            contentJLabel.setText("<html>" + title + "<br>" + author);
        }
        else
        {
            contentJLabel.setText("<html>" + getShort(content, 92, false));
        }

        // 设置日期
        dateJLabel.setText(date);

        // 设置tooltip
        String tip = "<html><body width=400>";
        if (title != null)
        {
            tip += "<b>标题：</b>" + title + "<br>";
        }
        if (author != null)
        {
            tip += "<b>作者：</b>" + author + "<br>";
        }
        if (content != null)
        {
            tip += "<b>摘要：</b>" + dealTip(content);
            this.setToolTipText(tip);
        }

        // 设置图标
        try
        {
            ShellFolder sf = ShellFolder.getShellFolder(
                    new File(path));
            Image image = sf.getIcon(true);
            iconJLabel.setIcon(new ImageIcon(image));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    // 处理tooltip的文字
    @SuppressWarnings("empty-statement")
    private String dealTip(String content)
    {
        if (content.length() > 800)
        {
            return content.substring(0, 800);
        }
        return content;
    }

    /**
     * 缩减字符串，以便在contentJLabel中显示
     * @param str 待缩减的字符串
     * @param total 保留的字符数
     * @param isFromBeginning是否从头开始显示
     * @return 缩减后的字符串
     */
    private String getShort(String str, int total, boolean isFromBeginning)
    {
        if (str == null)
        {
            return "暂无预览";
        }

        // 取指定的字符数
        int redPos = isFromBeginning // 是否从头开始显示
                ? 0 // 是，则不删去开头字符
                : str.indexOf("<font color='red'>"); // 找到红色标记位置
        int count = 0, pre, post;   // 获得字符总数，起始位置，结束位置
        boolean skip = false;       // 标记是否跳过字符，html标签需要跳过
        // 往前找
        for (pre = redPos - 1; pre >= 0 && count <= total / 2; --pre)
        {
            if (str.charAt(pre) == '>')     // 标签开始，跳过
            {
                skip = true;
            }
            else if (str.charAt(pre) == '<')// 标签结束，恢复
            {
                skip = false;
            }
            else if (!skip)     // 不跳过，则计数
            {
                ++count;
                if (str.charAt(pre) > 256)  // 中文，多加一
                {
                    ++count;
                }
            }
        }

        // 如果没有找到"<font color='red'>"，则将redPos置0
        if (redPos == -1)
        {
            redPos = 0;
        }

        // 往后找
        for (post = redPos; post != str.length() && count <= total; ++post)
        {
            if (str.charAt(post) == '<')
            {
                skip = true;
            }
            else if (str.charAt(post) == '>')
            {
                skip = false;
            }
            else if (!skip)
            {
                ++count;
                if (str.charAt(post) > 256)  //中文，多加一
                {
                    ++count;
                }
            }
        }

        //pre有可能小于0，置为0
        if (pre < 0)
        {
            pre = 0;
        }

        // 判断是否在结尾加上...
        if (post == str.length())
        {
            return str.substring(pre, post);
        }
        else
        {
            return str.substring(pre, post) + "...";
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.drawImage(img, 0, 0, null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        iconJLabel = new javax.swing.JLabel();
        filenameJLabel = new javax.swing.JLabel();
        dateJLabel = new javax.swing.JLabel();
        contentJLabel = new javax.swing.JLabel();
        pathJLabel = new javax.swing.JLabel();
        openFileJButton = new javax.swing.JButton();
        openFolderJButton = new javax.swing.JButton();

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                formMouseExited(evt);
            }
        });

        filenameJLabel.setFont(new java.awt.Font("宋体", 0, 14)); // NOI18N
        filenameJLabel.setText("中文");

        dateJLabel.setText("修改日期：2008/10/30 23:00");

        contentJLabel.setText("content");

        openFileJButton.setText("打开文件");
        openFileJButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        openFileJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileJButtonActionPerformed(evt);
            }
        });

        openFolderJButton.setText("所在文件夹");
        openFolderJButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        openFolderJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFolderJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(iconJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(filenameJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(dateJLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pathJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(contentJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(openFolderJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                            .addComponent(openFileJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filenameJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(openFileJButton)
                                .addGap(3, 3, 3)
                                .addComponent(openFolderJButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(contentJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pathJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE))))
                    .addComponent(iconJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // 打开文件
    private void openFileJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileJButtonActionPerformed
    {//GEN-HEADEREND:event_openFileJButtonActionPerformed
        try
        {
            Runtime.getRuntime().exec("cmd /c start \"\" \""
                    + path + "\"");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }//GEN-LAST:event_openFileJButtonActionPerformed

    // 打开文件夹
    private void openFolderJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFolderJButtonActionPerformed
    {//GEN-HEADEREND:event_openFolderJButtonActionPerformed
        try
        {
            Runtime.getRuntime().exec("cmd /c start \"\" \""
                    + parentPath + "\"");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }//GEN-LAST:event_openFolderJButtonActionPerformed

    // 鼠标移入，换背景
    private void formMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseEntered
    {//GEN-HEADEREND:event_formMouseEntered
        img = overImage;
        repaint();
    }//GEN-LAST:event_formMouseEntered

    // 鼠标移出，换背景
    private void formMouseExited(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseExited
    {//GEN-HEADEREND:event_formMouseExited
        img = normalImage;
        repaint();
    }//GEN-LAST:event_formMouseExited
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel contentJLabel;
    private javax.swing.JLabel dateJLabel;
    private javax.swing.JLabel filenameJLabel;
    private javax.swing.JLabel iconJLabel;
    private javax.swing.JButton openFileJButton;
    private javax.swing.JButton openFolderJButton;
    private javax.swing.JLabel pathJLabel;
    // End of variables declaration//GEN-END:variables
}
