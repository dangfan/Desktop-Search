package org.ds.handlingtypes;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.ds.handlingtypes.filehandler.FileHandlerException;

/**
 * 常用工具
 */
public class Utility
{

    /**
     * 获取文件最后修改日期
     * @param file 文件
     * @return 最后修改日期（XXXX/XX/XX XX:XX）
     */
    public static String getLastModifiedDate(File file)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/ HH:mm");
        return new String(sdf.format(new Date(file.lastModified())));
    }

    /**
     * 获取文件最后修改日期
     * @param date Date类型
     * @return 最后修改日期（XXXX年XX月XX日 XX时XX分）
     */
    public static String getFormattedDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return new String(sdf.format(date));
    }

    /**
     * 读取文本文件
     * @param file 文件
     * @return 文件内容
     * @throws FileHandlerException DocumentHandler异常
     */
    public static String readTextFile(File file)
            throws FileHandlerException
    {
        StringBuffer sb = new StringBuffer();
        //读入文件
        try
        {
            BufferedReader br =
                    new BufferedReader(new FileReader(file));

            String line = null; //保存每一行
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append("\r\n");  //添加换行
            }
            br.close();
        }
        catch (IOException e)
        {
            throw new FileHandlerException(
                    "无法读取文件", e);
        }
        return sb.toString();
    }
}
