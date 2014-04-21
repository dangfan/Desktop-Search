/*
 * SearchResultDialog.java
 *
 * Created on 2010-9-5, 22:24:26
 *
 * ���ļ���SearchResultDialog��SearchResultItem���������
 * SearchResultItem�̳���JPanel����������������е�һ��
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
 * ��������Ի���
 */
public class SearchResultDialog extends JDialog
{

    private boolean isDragging = false;   // �Ƿ�����ק��
    private int prevX, prevY;             // ��קǰ������
    private String queryString;           // ��ѯ�ַ���
    private IndexReader reader;           // ����������
    private Searcher searcher;            // ������
    private QueryParser parser;           // ��ѯ������
    private TopDocs docs;                 // �ĵ��������
    private Analyzer analyzer;            // �﷨������
    private int totalPages;               // ��ҳ��
    private static int COUNT_IN_PAGE = 5; // ÿҳ����
    private int currentPage = 1;          // ��ǰҳ
    private int recordsInStore = 50;      // �ڴ��м�������
    private boolean isCalledByAll;        // ����Ƿ���ȫѡ����loadResult
    private Image img;                    // ����ͼƬ
    private ImageIcon normalButton;       // ����״̬��ͼƬ
    private ImageIcon overButton;         // �������ʱ��ͼƬ

    /** 
     * �����µ�SearchResultDialog����
     * @param parent
     * ������
     * @param modal
     * true�������ģ̬����
     * @param queryString
     * ��ѯ�ַ���
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

        setLooks();                         // �������
        initComponents();                   // ��ʼ��UI
        initSearcher();                     // ��ʼ��������
        loadResult();                       // �����������
    }

    // �������
    private void setLooks()
    {
        // ������������ʹ�õĵ�ǰJDialog
        final JDialog jd = this;

        // �ı䴰����״ΪԲ�Ǿ���
        // ���������ڣ�http://is.gd/eKXBh
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

        //���ñ���
        this.setContentPane(new JPanel()
        {

            @Override
            public void paintComponent(Graphics g)
            {
                g.drawImage(img, 0, 0, null);
            }
        });
    }

    // ��ʼ��������
    private void initSearcher()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(new File(
                    "indexes")), true);     // ����������
            searcher = new IndexSearcher(reader);
            analyzer = new ComplexAnalyzer();   // �﷨������
            parser = new QueryParser(Version.LUCENE_29,
                    "contents",
                    analyzer); // ��ѯ������
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.exit(1);                 // ���ش���ֱ���˳�
        }
    }

    // �����߳��������������
    private void loadResult()
    {
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    // ���ɼ����ַ���
                    String tmpString = "(" + queryString;

                    // ��filename, title��author��ǩ�в�ѯ
                    for (String str : queryString.split(" "))
                    {
                        tmpString += " filename:" + str;
                        tmpString += " title:" + str;
                        tmpString += " author:" + str;
                    }

                    // �޶�����
                    tmpString += ") AND (";
                    for (Component component : optionsJPanel.getComponents())
                    {
                        String type = "type:" + component.getName() + " ";
                        if (type.equals("type: "))   // ����allJCheckBox
                        {
                            continue;
                        }
                        if (((JCheckBox) component).isSelected())   //���
                        {
                            tmpString += type;
                        }
                        else
                        {
                            tmpString += "-" + type;
                        }
                    }
                    tmpString += ")";
                    Query query = parser.parse(tmpString);        // ���ɲ�ѯ
                    docs = searcher.search(query, recordsInStore);// ����
                    statusJLabel.setText("���ҵ�"
                            + docs.totalHits + "���ļ�");         // ��ʾ�������
                    totalPages = (int) Math.ceil(
                            docs.totalHits / (double) COUNT_IN_PAGE);// ����ҳ��
                    currentPage = 1;
                    showResult(1);                                // ��ʾ��һҳ
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }).start();
    }

    // ��ʾ���
    private void showResult(int page)
    {
        if (docs == null)   // �޽��
        {
            return;
        }

        // �ڴ��еļ�¼�Ѿ�ȫ����ʾ�����¼���
        if (page * COUNT_IN_PAGE > recordsInStore)
        {
            recordsInStore += recordsInStore;   // ��������
            loadResult();
        }

        int start = (page - 1) * COUNT_IN_PAGE;    // ��ʼ�±�
        int end = Math.min(docs.totalHits,
                page * COUNT_IN_PAGE);             // �����±�

        pageJLabel.setText(currentPage + "/" + totalPages);
        resultJPanel.removeAll();               // ���resultJPanel������
        for (int i = start; i != end; ++i)      // ö��ÿһ�����
        {
            addItem(docs.scoreDocs[i]);         // ��ӵ�JPanel��
        }
        resultJPanel.updateUI();                // ����resultJPanel
    }

    /**
     * ��ָ���ַ�����������
     * @param docID �ĵ�id
     * @param doc �ĵ�(@see Document)����
     * @param field ��Ҫ��������
     * @param value ��Ҫ�������ַ���
     * @return �������html�ַ���
     */
    private String hightlight(int docID, Document doc, String field, String value)
    {
        // ����ֶβ�����
        if (value == null)
        {
            return null;
        }

        // ��ʼ����
        try
        {
            // ���ڸ�����ʾ��������
            TokenStream stream = TokenSources.getAnyTokenStream(reader,
                    docID,
                    field,
                    doc,
                    analyzer);
            // HTML��ʽ��ǩ���ѹؼ�����ʾΪ��ɫ
            SimpleHTMLFormatter sHtmlF = new SimpleHTMLFormatter(
                    "<font color='red'>", "</font>");
            // ������
            Highlighter highlighter = new Highlighter(sHtmlF,
                    new QueryScorer(parser.parse(queryString)));
            highlighter.setTextFragmenter(new SimpleFragmenter(300));
            // �õ��ֶ�֮�������
            String tmp = highlighter.getBestFragment(stream, value);

            // ��������ﲻ����Ҫ���������ݣ���highlight�᷵�ؿ�
            // ��ʱ���س�ʼֵ
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

    // ��ָ����ScoreDoc��ӵ�JPanel��
    private void addItem(ScoreDoc scoreDoc)
    {
        try
        {
            Document doc = searcher.doc(scoreDoc.doc);          // �õ��ĵ�����
            String filename = doc.get("filename");              // �õ��ļ���
            String path = doc.get("path");                      // �õ�����·��
            String date = doc.get("date");                      // �õ��޸�����
            String title = doc.get("title");                    // �õ�����
            String author = doc.get("author");                  // �õ�����
            String contents = doc.get("contents");              // �õ�����

            // ��������
            contents = hightlight(scoreDoc.doc, doc, "contents", contents);
            filename = hightlight(scoreDoc.doc, doc, "filename", filename);
            title = hightlight(scoreDoc.doc, doc, "title", title);
            author = hightlight(scoreDoc.doc, doc, "author", author);

            // ���ɽ����
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

        titleJLable.setFont(new java.awt.Font("΢���ź�", 1, 14));
        titleJLable.setText(queryString + " - �������");   // ���ñ���
        titleJLable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        searchOnlineJLable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/ds/GUI/Resources/google.jpg"))); // NOI18N
        searchOnlineJLable.setText("��Google������");
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
        allJCheckBox.setText("ȫѡ/ȫ��ѡ");
        allJCheckBox.setName(""); // NOI18N
        allJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allJCheckBoxActionPerformed(evt);
            }
        });

        plainTextJCheckBox.setSelected(true);
        plainTextJCheckBox.setText("���ı�");
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
        htmlJCheckBox.setText("��ҳ");
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
        othersJCheckBox.setText("�����ļ�");
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

        prevJButton.setText("��һҳ");
        prevJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevJButtonActionPerformed(evt);
            }
        });

        nextJButton.setText("��һҳ");
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

        jLabel1.setText("�µ�������");

        searchJButon.setText("����");
        searchJButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchJButonActionPerformed(evt);
            }
        });

        jLabel2.setText("�ļ����ͣ�");

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

    // ��갴�£�������ק����
    private void formMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMousePressed
    {//GEN-HEADEREND:event_formMousePressed
        // ���������
        if (evt.getButton() == MouseEvent.BUTTON1)
        {
            isDragging = true;  // ���Ϊ��ק��
            prevX = evt.getX(); // ��¼����
            prevY = evt.getY();
        }
    }//GEN-LAST:event_formMousePressed

    // ����ͷţ�������ק����
    private void formMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseReleased
    {//GEN-HEADEREND:event_formMouseReleased
        // ���������
        if (evt.getButton() == MouseEvent.BUTTON1)
        {
            isDragging = false; // ���Ϊδ��ק
        }
    }//GEN-LAST:event_formMouseReleased

    // ��ק
    private void formMouseDragged(java.awt.event.MouseEvent evt)//GEN-FIRST:event_formMouseDragged
    {//GEN-HEADEREND:event_formMouseDragged
        // �������ק�������λ��
        if (isDragging)
        {
            int left = this.getLocation().x;    // ��ǰ���λ��
            int top = this.getLocation().y;
            this.setLocation(left + evt.getX() - prevX,
                    top + evt.getY() - prevY);  // ���ô�����λ��
        }
    }//GEN-LAST:event_formMouseDragged

    // ����رհ�ť
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeJButtonActionPerformed
    {//GEN-HEADEREND:event_closeJButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_closeJButtonActionPerformed

    // ȫѡ��ť
    private void allJCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_allJCheckBoxActionPerformed
    {//GEN-HEADEREND:event_allJCheckBoxActionPerformed
        isCalledByAll = true;   // ���ĿǰΪȫѡ����������������ť���¼�
        for (Component component : optionsJPanel.getComponents())   // ö��ÿһ��JCheckBox
        {
            if (component.getClass() == JCheckBox.class)
            {
                ((JCheckBox) component).setSelected(allJCheckBox.isSelected()); // ����ѡ��
            }
        }
        loadResult();   // ���ؽ��
        isCalledByAll = false;  // ���ȫѡ�ı��
    }//GEN-LAST:event_allJCheckBoxActionPerformed

    // ǰһҳ
    private void prevJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevJButtonActionPerformed
    {//GEN-HEADEREND:event_prevJButtonActionPerformed
        // ���ҳ������1����ת����һҳ
        if (currentPage > 1)
        {
            showResult(--currentPage);
        }
    }//GEN-LAST:event_prevJButtonActionPerformed

    // ��һҳ
    private void nextJButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextJButtonActionPerformed
    {//GEN-HEADEREND:event_nextJButtonActionPerformed
        // ����������һҳ����ת����һҳ
        if (currentPage < totalPages)
        {
            showResult(++currentPage);
        }
    }//GEN-LAST:event_nextJButtonActionPerformed

    // ����JCheckBox
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

    // ����
    private void searchJButonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_searchJButonActionPerformed
    {//GEN-HEADEREND:event_searchJButonActionPerformed
        if (searchJTextField.getText().equals(""))
        {
            searchJTextField.setToolTipText("����������");   //��ʾ����
            ToolTipManager.sharedInstance().setDismissDelay(1500);  //��ʾʱ��
            Action action = searchJTextField.getActionMap().get("postTip"); //�õ�action����
            ActionEvent ae = new ActionEvent(searchJTextField,
                    ActionEvent.ACTION_PERFORMED,
                    "postTip", EventQueue.getMostRecentEventTime(), 0); //�����¼�
            action.actionPerformed(ae);     //��ʾtooltip
            searchJTextField.setToolTipText(null);  //�������
            return;
        }
        queryString = searchJTextField.getText();
        titleJLable.setText(queryString + " - �������");
        loadResult();
    }//GEN-LAST:event_searchJButonActionPerformed

    // ������
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
