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

    private String filename;        // �ļ���
    private String path;            // ����·��
    private String parentPath;      // �����ļ���
    private String content;         // ����
    private String date;            // ����
    private String title;           // ����
    private String author;          // ����
    private Image normalImage;      // �����ı���
    private Image overImage;        // ��껬���ı���
    private Image img;              // �����ı���

    /**
     * ����һ���µĽ������
     * @param filename
     * @param path
     * @param date
     * @param content
     * @param author
     * @param title
     */
    public SearchResultItem(String filename, String path, String content, String date, String title, String author)
    {
        // ��ʼ��ͼ��
        this.normalImage = new ImageIcon(getClass().getResource(
                "Resources/item.png")).getImage();
        this.overImage = new ImageIcon(getClass().getResource(
                "Resources/itemover.png")).getImage();
        this.img = this.normalImage;

        // ��������
        this.filename = filename;
        this.path = path;
        this.parentPath = new File(path).getParent();
        this.content = content;
        this.date = "�޸����ڣ�" + date;
        this.title = title;
        this.author = author;

        initComponents();   // ��ʼ���ؼ�
        setText();          // ��������
    }

    private void setText()
    {
        // �����ļ���
        filenameJLabel.setText("<html>" + getShort(filename, 23, false));

        // ������ʾ��·��
        if (parentPath.length() > 30)       //·��̫����ֻ��ʾǰ30���ַ�
        {
            pathJLabel.setText(parentPath.substring(0, 30) + "...");
        }
        else
        {
            pathJLabel.setText(parentPath);
        }

        // ��������
        if (filename.endsWith("mp3"))   //mp3�ļ����⴦��
        {
            contentJLabel.setText("<html>" + title + "<br>" + author);
        }
        else
        {
            contentJLabel.setText("<html>" + getShort(content, 92, false));
        }

        // ��������
        dateJLabel.setText(date);

        // ����tooltip
        String tip = "<html><body width=400>";
        if (title != null)
        {
            tip += "<b>���⣺</b>" + title + "<br>";
        }
        if (author != null)
        {
            tip += "<b>���ߣ�</b>" + author + "<br>";
        }
        if (content != null)
        {
            tip += "<b>ժҪ��</b>" + dealTip(content);
            this.setToolTipText(tip);
        }

        // ����ͼ��
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

    // ����tooltip������
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
     * �����ַ������Ա���contentJLabel����ʾ
     * @param str ���������ַ���
     * @param total �������ַ���
     * @param isFromBeginning�Ƿ��ͷ��ʼ��ʾ
     * @return ��������ַ���
     */
    private String getShort(String str, int total, boolean isFromBeginning)
    {
        if (str == null)
        {
            return "����Ԥ��";
        }

        // ȡָ�����ַ���
        int redPos = isFromBeginning // �Ƿ��ͷ��ʼ��ʾ
                ? 0 // �ǣ���ɾȥ��ͷ�ַ�
                : str.indexOf("<font color='red'>"); // �ҵ���ɫ���λ��
        int count = 0, pre, post;   // ����ַ���������ʼλ�ã�����λ��
        boolean skip = false;       // ����Ƿ������ַ���html��ǩ��Ҫ����
        // ��ǰ��
        for (pre = redPos - 1; pre >= 0 && count <= total / 2; --pre)
        {
            if (str.charAt(pre) == '>')     // ��ǩ��ʼ������
            {
                skip = true;
            }
            else if (str.charAt(pre) == '<')// ��ǩ�������ָ�
            {
                skip = false;
            }
            else if (!skip)     // �������������
            {
                ++count;
                if (str.charAt(pre) > 256)  // ���ģ����һ
                {
                    ++count;
                }
            }
        }

        // ���û���ҵ�"<font color='red'>"����redPos��0
        if (redPos == -1)
        {
            redPos = 0;
        }

        // ������
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
                if (str.charAt(post) > 256)  //���ģ����һ
                {
                    ++count;
                }
            }
        }

        //pre�п���С��0����Ϊ0
        if (pre < 0)
        {
            pre = 0;
        }

        // �ж��Ƿ��ڽ�β����...
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

        filenameJLabel.setFont(new java.awt.Font("����", 0, 14)); // NOI18N
        filenameJLabel.setText("����");

        dateJLabel.setText("�޸����ڣ�2008/10/30 23:00");

        contentJLabel.setText("content");

        openFileJButton.setText("���ļ�");
        openFileJButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        openFileJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileJButtonActionPerformed(evt);
            }
        });

        openFolderJButton.setText("�����ļ���");
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

    // ���ļ�
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

    // ���ļ���
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

    // ������룬������
    private void formMouseEntered(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseEntered
    {//GEN-HEADEREND:event_formMouseEntered
        img = overImage;
        repaint();
    }//GEN-LAST:event_formMouseEntered

    // ����Ƴ���������
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
