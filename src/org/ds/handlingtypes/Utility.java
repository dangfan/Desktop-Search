package org.ds.handlingtypes;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.ds.handlingtypes.filehandler.FileHandlerException;

/**
 * ���ù���
 */
public class Utility
{

    /**
     * ��ȡ�ļ�����޸�����
     * @param file �ļ�
     * @return ����޸����ڣ�XXXX/XX/XX XX:XX��
     */
    public static String getLastModifiedDate(File file)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/ HH:mm");
        return new String(sdf.format(new Date(file.lastModified())));
    }

    /**
     * ��ȡ�ļ�����޸�����
     * @param date Date����
     * @return ����޸����ڣ�XXXX��XX��XX�� XXʱXX�֣�
     */
    public static String getFormattedDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return new String(sdf.format(date));
    }

    /**
     * ��ȡ�ı��ļ�
     * @param file �ļ�
     * @return �ļ�����
     * @throws FileHandlerException DocumentHandler�쳣
     */
    public static String readTextFile(File file)
            throws FileHandlerException
    {
        StringBuffer sb = new StringBuffer();
        //�����ļ�
        try
        {
            BufferedReader br =
                    new BufferedReader(new FileReader(file));

            String line = null; //����ÿһ��
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append("\r\n");  //��ӻ���
            }
            br.close();
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "�޷���ȡ�ļ�", e);
        }
        return sb.toString();
    }
}
