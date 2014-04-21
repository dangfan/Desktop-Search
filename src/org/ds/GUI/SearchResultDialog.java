/*
 * SearchResultDialog.java
 *
 * Created on 2010-9-5, 22:24:26
 *
 * 本文件由SearchResultDialog和SearchResultItem两个类组成
 * SearchResultItem继承于JPanel，定义了搜索结果中的一项
 */
package org.ds.GUI;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.swing.*;
import javax.swing.JCheckBox;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 搜索结果对话框
 */
public class SearchResultDialog extends JDialog
{

    private boolean isDragging = false;   // 是否在拖拽中
    private int prevX, prevY;             // 拖拽前的坐标
    private String queryString;           // 查询字符串
    private IndexReader reader;           // 索引读入器
    private Searcher searcher;            // 搜索器
    private QueryParser parser;           // 查询解析器
    private TopDocs docs;                 // 文档编号数组
    private Analyzer analyzer;            // 语法分析器
    private int totalPages;               // 总页数
    private static int COUNT_IN_PAGE = 5; // 每页数量
    private int currentPage = 1;          // 当前页
    private int recordsInStore = 50;      // 内存中加载数量
    private boolean isCalledByAll;        // 标记是否由全选调用loadResult
    private Image img;                    // 背景图片
    private ImageIcon normalButton;       // 正常状态的图片
    private ImageIcon overButton;         // 鼠标移入时的图片

    /** 
     * 创建新的SearchResultDialog窗体
     * @param parent
     * 父对象
     * @param modal
     * true，如果是模态窗口
     * @param queryString
     * 查询字符串
     */
    public SearchResultDialog(Frame parent, boolean modal, String queryString)
    {
        super(parent, modal);
        this.queryString = queryString;
        this.img = new ImageIcon(getClass().getResource(
                "Resources/result.png")).getImage();
        this.normalButton = new ImageIcon(getClass().getResource(
                "Resources/close.png"));
        this.overButton = new ImageIcon(getClass().getResource(
                "Resources/closeover.png"));

        setLooks();                         // 设置外观
        initComponents();                   // 初始化UI
        initSearcher();                     // 初始化搜索器
        loadResult();                       // 载入搜索结果
    }

    // 设置外观
    private void setLooks()
    {
        // 将在匿名类中使用的当前JDialog
        final JDialog jd = this;

        // 改变窗口形状为圆角矩形
        // 方法来自于：http://is.gd/eKXBh
        this.addComponentListener(new ComponentAdapter()
        {

            @Override
            public void componentResized(ComponentEvent evt)
            {
                Shape shape = new RoundRectangle2D.Float(
                        0, 0, jd.getWidth(), jd.getHeight(),
                        15, 15);
                com.sun.awt.AWTUtilities.setWindowShape(jd, shape);
            }
        });

        //设置背景
        this.setContentPane(new JPanel()
        {

            @Override
            public void paintComponent(Graphics g)
            {
                g.drawImage(img, 0, 0, null);
            }
        });
    }

    // 初始化搜索器
    private void initSearcher()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(new File(
                    "indexes")), true);     // 索引读入器
            searcher = new IndexSearcher(reader);
            analyzer = new ComplexAnalyzer();   // 语法分析器
            parser = new QueryParser(Version.LUCENE_29,
                    "contents",
                    analyzer); // 查询解析器
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.exit(1);                 // 严重错误，直接退出
        }
    }

    // 在新线程中载入搜索结果
    private void loadResult()
    {
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    // 生成检索字符串
                    String tmpString = "(" + queryString;

                    // 在filename, title和author标签中查询
                    for (String str : queryString.split(" "))
                    {
                        tmpString += " filename:" + str;
                        tmpString += " title:" + str;
                        tmpString += " author:" + str;
                    }

                    // 限定类型
                    tmpString += ") AND (";
                    for (Component component : optionsJPanel.getComponents())
                    {
                        String type = "type:" + component.getName() + " ";
                        if (type.equals("type: "))   // 跳过allJCheckBox
                        {
                            continue;
                        }
                        if (((JCheckBox) component).isSelected())   //添加
                        {
                            tmpString += type;
                        }
                        else
                        {
                            tmpString += "-" + type;
                        }
                    }
                    tmpString += ")";
                    Query query = parser.parse(tmpString);        // 生成查询
                    docs = searcher.search(query, recordsInStore);// 检索
                    statusJLabel.setText("共找到"
                            + docs.totalHits + "个文件");         // 显示结果数量
                    totalPages = (int) Math.ceil(
                            docs.totalHits / (double) COUNT_IN_PAGE);// 计算页数
                    currentPage = 1;
                    showResult(1);                                // 显示第一页
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }).start();
    }

    // 显示结果
    private void showResult(int page)
    {
        if (docs == null)   // 无结果
        {
            return;
        }

        // 内存中的记录已经全部显示，重新加载
        if (page * COUNT_IN_PAGE > recordsInStore)
        {
            recordsInStore += recordsInStore;   // 容量翻倍
            loadResult();
        }

        int start = (page - 1) * COUNT_IN_PAGE;    // 起始下标
        int end = Math.min(docs.totalHits,
                page * COUNT_IN_PAGE);             // 结束下标

        pageJLabel.setText(currentPage + "/" + totalPages);
        resultJPanel.removeAll();               // 清除resultJPanel的内容
        for (int i = start; i != end; ++i)      // 枚举每一个结果
        {
            addItem(docs.scoreDocs[i]);         // 添加到JPanel中
        }
        resultJPanel.updateUI();                // 更新resultJPanel
    }

    /**
     * 将指定字符串高亮处理
     * @param docID 文档id
     * @param doc 文档(@see Document)对象
     * @param field 需要高亮的域
     * @param value 需要高亮的字符串
     * @return 高亮后的html字符串
     */
    private String hightlight(int docID, Document doc, String field, String value)
    {
        // 如果字段不存在
        if (value == null)
        {
            return null;
        }

        // 开始高亮
        try
        {
            // 用于高亮显示的数据流
            TokenStream stream = TokenSources.getAnyTokenStream(reader,
                    docID,
                    field,
                    doc,
                    analyzer);
            // HTML格式标签，把关键词显示为红色
            SimpleHTMLFormatter sHtmlF = new SimpleHTMLFormatter(
                    "<font color='red'>", "</font>");
            // 高亮器
            Highlighter highlighter = new Highlighter(sHtmlF,
                    new QueryScorer(parser.parse(queryString)));
            highlighter.setTextFragmenter(new SimpleFragmenter(300));
            // 得到分段之后的文字
            String tmp = highlighter.getBestFragment(stream, value);

            // 如果文字里不含有要检索的内容，则highlight会返回空
            // 此时返回初始值
            if (tmp == null)
            {
                return value;
            }
            return tmp;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
        return value;
    }

    // 将指定的ScoreDoc添加到JPanel中
    private void addItem(ScoreDoc scoreDoc)
    {
        try
        {
            Document doc = searcher.doc(scoreDoc.doc);          // 得到文档对象
            String filename = doc.get("filename");              // 得到文件名
            String path = doc.get("path");                      // 得到完整路径
            String date = doc.get("date");                      // 得到修改日期
            String title = doc.get("title");                    // 得到标题
            String author = doc.get("author");                  // 得到作者
            String contents = doc.get("contents");              // 得到正文

            // 高亮处理
            contents = hightlight(scoreDoc.doc, doc, "contents", contents);
            filename = hightlight(scoreDoc.doc, doc, "filename", filename);
            title = hightlight(scoreDoc.doc, doc, "title", title);
            author = hightlight(scoreDoc.doc, doc, "author", author);

            // 生成结果项
            SearchResultItem item = new SearchResultItem(filename, path,
                    contents, date, title, author);
            resultJPanel.add(item);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeJButton = new javax.swing.JButton();
        titleJLable = new javax.swing.JLabel();
        searchOnlineJLable = new javax.swing.JLabel();
        optionsJPanel = new javax.swing.JPanel();
        allJCheckBox = new javax.swing.JCheckBox();
        plainTextJCheckBox = new javax.swing.JCheckBox();
        wordJCheckBox = new javax.swing.JCheckBox();
        excelJCheckBox = new javax.swing.JCheckBox();
        powerpointJCheckBox = new javax.swing.JCheckBox();
        pdfJCheckBox = new javax.swing.JCheckBox();
        htmlJCheckBox = new javax.swing.JCheckBox();
        rtfJCheckBox = new javax.swing.JCheckBox();
        othersJCheckBox = new javax.swing.JCheckBox();
        mp3JCheckBox = new javax.swing.JCheckBox();
        prevJButton = new javax.swing.JButton();
        nextJButton = new javax.swing.JButton();
        resultJPanel = new javax.swing.JPanel();
        statusJLabel = new javax.swing.JLabel();
        pageJLabel = new javax.swing.JLabel();
        searchJTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        searchJButon = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });

        closeJButton.setSize(normalButton.getIconWidth(),
            normalButton.getIconHeight());
        closeJButton.setIcon(normalButton);
        closeJButton.setMargin(new Insets(0, 0, 0, 0));
        closeJButton.setIconTextGap(0);
        closeJButton.setBorderPainted(false);
        closeJButton.setBorder(null);
        closeJButton.setText(null);
        closeJButton.setRolloverIcon(overButton);
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });

        titleJLable.setFont(new java.awt.Font("微软雅黑", 1, 14));
        titleJLable.setText(queryString + " - 搜索结果");   // 设置标题
        titleJLable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        searchOnlineJLable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ds/GUI/Resources/google.jpg"))); // NOI18N
        searchOnlineJLable.setText("在Google中搜索");
        searchOnlineJLable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchOnlineJLable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                try
                {
                    Runtime.getRuntime().exec("cmd /c start http://www.google.com/search?q=" + queryString);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace(System.err);
                }
            }
        });

        optionsJPanel.setOpaque(false);

        allJCheckBox.setSelected(true);
        allJCheckBox.setText("全选/全不选");
        allJCheckBox.setName(""); // NOI18N
        allJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allJCheckBoxActionPerformed(evt);
            }
        });

        plainTextJCheckBox.setSelected(true);
        plainTextJCheckBox.setText("纯文本");
        plainTextJCheckBox.setName("txt"); // NOI18N
        plainTextJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plainTextJCheckBoxActionPerformed(evt);
            }
        });

        wordJCheckBox.setSelected(true);
        wordJCheckBox.setText("Word");
        wordJCheckBox.setName("doc"); // NOI18N
        wordJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordJCheckBoxActionPerformed(evt);
            }
        });

        excelJCheckBox.setSelected(true);
        excelJCheckBox.setText("Excel");
        excelJCheckBox.setName("xls"); // NOI18N
        excelJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excelJCheckBoxActionPerformed(evt);
            }
        });

        powerpointJCheckBox.setSelected(true);
        powerpointJCheckBox.setText("PowerPoint");
        powerpointJCheckBox.setName("ppt"); // NOI18N
        powerpointJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerpointJCheckBoxActionPerformed(evt);
            }
        });

        pdfJCheckBox.setSelected(true);
        pdfJCheckBox.setText("PDF");
        pdfJCheckBox.setName("pdf"); // NOI18N
        pdfJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfJCheckBoxActionPerformed(evt);
            }
        });

        htmlJCheckBox.setSelected(true);
        htmlJCheckBox.setText("网页");
        htmlJCheckBox.setName("html"); // NOI18N
        htmlJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                htmlJCheckBoxActionPerformed(evt);
            }
        });

        rtfJCheckBox.setSelected(true);
        rtfJCheckBox.setText("RTF");
        rtfJCheckBox.setName("rtf"); // NOI18N
        rtfJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rtfJCheckBoxActionPerformed(evt);
            }
        });

        othersJCheckBox.setSelected(true);
        othersJCheckBox.setText("其他文件");
        othersJCheckBox.setName("other"); // NOI18N
        othersJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                othersJCheckBoxActionPerformed(evt);
            }
        });

        mp3JCheckBox.setSelected(true);
        mp3JCheckBox.setText("MP3");
        mp3JCheckBox.setName("audio"); // NOI18N
        mp3JCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mp3JCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsJPanelLayout = new javax.swing.GroupLayout(optionsJPanel);
        optionsJPanel.setLayout(optionsJPanelLayout);
        optionsJPanelLayout.setHorizontalGroup(
            optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(plainTextJCheckBox)
                    .addComponent(wordJCheckBox)
                    .addComponent(excelJCheckBox)
                    .addComponent(powerpointJCheckBox)
                    .addComponent(pdfJCheckBox)
                    .addComponent(htmlJCheckBox)
                    .addComponent(rtfJCheckBox)
                    .addComponent(allJCheckBox)
                    .addComponent(mp3JCheckBox)
                    .addComponent(othersJCheckBox))
                .addContainerGap(7, Short.MAX_VALUE))
        );
        optionsJPanelLayout.setVerticalGroup(
            optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsJPanelLayout.createSequentialGroup()
                .addComponent(allJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plainTextJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wordJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excelJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(powerpointJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pdfJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(htmlJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rtfJCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mp3JCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(othersJCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        prevJButton.setText("上一页");
        prevJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevJButtonActionPerformed(evt);
            }
        });

        nextJButton.setText("下一页");
        nextJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextJButtonActionPerformed(evt);
            }
        });

        resultJPanel.setOpaque(false);

        statusJLabel.setText("status");

        pageJLabel.setText("jLabel1");

        searchJTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchJTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("新的搜索：");

        searchJButon.setText("搜索");
        searchJButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchJButonActionPerformed(evt);
            }
        });

        jLabel2.setText("文件类型：");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleJLable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(statusJLabel)
                                .addGap(163, 163, 163)))
                        .addComponent(closeJButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(optionsJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addComponent(searchJButon))
                            .addComponent(searchOnlineJLable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2)
                            .addComponent(searchJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resultJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(pageJLabel))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(prevJButton)
                                .addComponent(nextJButton, javax.swing.GroupLayout.Alignment.TRAILING)))))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleJLable)
                    .addComponent(closeJButton)
                    .addComponent(statusJLabel))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(222, 222, 222)
                        .addComponent(prevJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pageJLabel)
                        .addGap(10, 10, 10)
                        .addComponent(nextJButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resultJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchJButon)
                                .addGap(18, 18, 18)
                                .addComponent(searchOnlineJLable)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addGap(5, 5, 5)
                                .addComponent(optionsJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-680)/2, (screenSize.height-650)/2, 680, 650);
    }// </editor-fold>//GEN-END:initComponents

    // 鼠标按下，处理拖拽操作
    private void formMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMousePressed
    {//GEN-HEADEREND:event_formMousePressed
        // 仅处理左键
        if (evt.getButton() == MouseEvent.BUTTON1)
        {
            isDragging = true;  // 标记为拖拽中
            prevX = evt.getX(); // 记录坐标
            prevY = evt.getY();
        }
    }//GEN-LAST:event_formMousePressed

    // 鼠标释放，处理拖拽操作
    private void formMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseReleased
    {//GEN-HEADEREND:event_formMouseReleased
        // 仅处理左键
        if (evt.getButton() == MouseEvent.BUTTON1)
        {
            isDragging = false; // 标记为未拖拽
        }
    }//GEN-LAST:event_formMouseReleased

    // 拖拽
    private void formMouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseDragged
    {//GEN-HEADEREND:event_formMouseDragged
        // 如果在拖拽，则更新位置
        if (isDragging)
        {
            int left = this.getLocation().x;    // 当前鼠标位置
            int top = this.getLocation().y;
            this.setLocation(left + evt.getX() - prevX,
                    top + evt.getY() - prevY);  // 设置窗口新位置
        }
    }//GEN-LAST:event_formMouseDragged

    // 点击关闭按钮
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeJButtonActionPerformed
    {//GEN-HEADEREND:event_closeJButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeJButtonActionPerformed

    // 全选按钮
    private void allJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_allJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_allJCheckBoxActionPerformed
        isCalledByAll = true;   // 标记目前为全选，以免引发其他按钮的事件
        for (Component component : optionsJPanel.getComponents())   // 枚举每一个JCheckBox
        {
            if (component.getClass() == JCheckBox.class)
            {
                ((JCheckBox) component).setSelected(allJCheckBox.isSelected()); // 设置选项
            }
        }
        loadResult();   // 加载结果
        isCalledByAll = false;  // 解除全选的标记
    }//GEN-LAST:event_allJCheckBoxActionPerformed

    // 前一页
    private void prevJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevJButtonActionPerformed
    {//GEN-HEADEREND:event_prevJButtonActionPerformed
        // 如果页数大于1，则转到上一页
        if (currentPage > 1)
        {
            showResult(--currentPage);
        }
    }//GEN-LAST:event_prevJButtonActionPerformed

    // 后一页
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextJButtonActionPerformed
    {//GEN-HEADEREND:event_nextJButtonActionPerformed
        // 如果不在最后一页，则转到下一页
        if (currentPage < totalPages)
        {
            showResult(++currentPage);
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

    // 各种JCheckBox
    private void plainTextJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_plainTextJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_plainTextJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_plainTextJCheckBoxActionPerformed

    private void wordJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wordJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_wordJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_wordJCheckBoxActionPerformed

    private void excelJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_excelJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_excelJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_excelJCheckBoxActionPerformed

    private void powerpointJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_powerpointJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_powerpointJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_powerpointJCheckBoxActionPerformed

    private void pdfJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pdfJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_pdfJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_pdfJCheckBoxActionPerformed

    private void htmlJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_htmlJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_htmlJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_htmlJCheckBoxActionPerformed

    private void rtfJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rtfJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_rtfJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_rtfJCheckBoxActionPerformed

    private void othersJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_othersJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_othersJCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_othersJCheckBoxActionPerformed

    // 索引
    private void searchJButonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_searchJButonActionPerformed
    {//GEN-HEADEREND:event_searchJButonActionPerformed
        if (searchJTextField.getText().equals(""))
        {
            searchJTextField.setToolTipText("请输入内容");   //提示内容
            ToolTipManager.sharedInstance().setDismissDelay(1500);  //显示时间
            Action action = searchJTextField.getActionMap().get("postTip"); //得到action对象
            ActionEvent ae = new ActionEvent(searchJTextField,
                    ActionEvent.ACTION_PERFORMED,
                    "postTip", EventQueue.getMostRecentEventTime(), 0); //产生事件
            action.actionPerformed(ae);     //显示tooltip
            searchJTextField.setToolTipText(null);  //清除内容
            return;
        }
        queryString = searchJTextField.getText();
        titleJLable.setText(queryString + " - 搜索结果");
        loadResult();
    }//GEN-LAST:event_searchJButonActionPerformed

    // 搜索框
    private void searchJTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_searchJTextFieldActionPerformed
    {//GEN-HEADEREND:event_searchJTextFieldActionPerformed
        searchJButonActionPerformed(evt);
    }//GEN-LAST:event_searchJTextFieldActionPerformed

    private void mp3JCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mp3JCheckBoxActionPerformed
    {//GEN-HEADEREND:event_mp3JCheckBoxActionPerformed
        if (!isCalledByAll)
        {
            loadResult();
        }
    }//GEN-LAST:event_mp3JCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allJCheckBox;
    private javax.swing.JButton closeJButton;
    private javax.swing.JCheckBox excelJCheckBox;
    private javax.swing.JCheckBox htmlJCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox mp3JCheckBox;
    private javax.swing.JButton nextJButton;
    private javax.swing.JPanel optionsJPanel;
    private javax.swing.JCheckBox othersJCheckBox;
    private javax.swing.JLabel pageJLabel;
    private javax.swing.JCheckBox pdfJCheckBox;
    private javax.swing.JCheckBox plainTextJCheckBox;
    private javax.swing.JCheckBox powerpointJCheckBox;
    private javax.swing.JButton prevJButton;
    private javax.swing.JPanel resultJPanel;
    private javax.swing.JCheckBox rtfJCheckBox;
    private javax.swing.JButton searchJButon;
    private javax.swing.JTextField searchJTextField;
    private javax.swing.JLabel searchOnlineJLable;
    private javax.swing.JLabel statusJLabel;
    private javax.swing.JLabel titleJLable;
    private javax.swing.JCheckBox wordJCheckBox;
    // End of variables declaration//GEN-END:variables
}
