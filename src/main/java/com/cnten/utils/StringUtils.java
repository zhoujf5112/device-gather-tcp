package com.cnten.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

    //根据位置截取对应的数据
    public static String getSubIndex(String data,Integer index){
        return getSubIndex(data, index, index);
    }

    //根据开始和结束截取对应的数据
    public static String getSubIndex(String data,Integer start,Integer end){
        return data.substring(2 * (start - 1), 2 * end);
    }

    //根据位置截取对应的数据并转换成整形
    public static Integer subIndexToInt(String data,Integer start,Integer end){
        return Integer.parseInt(getSubIndex(data, start, end), 16);
    }

    //根据开始和结束截取对应的数据并转换成整形
    public static Integer subIndexToInt(String data,Integer index){
        return subIndexToInt(data, index, index);
    }

    // 将十六进制字符串格式的 DTU ID 根据 Ascii 码表转换为十进制字符串格式的 DTU ID
    // 例如：将 3138363030303030303032 转换为
    // 18600000002
    public static String getDeviceId(String data){
        /*int length = data.length();
        if (length % 2 != 0) {
            throw new RuntimeException(data + "数据长度不是偶数位");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i += 2) {
            sb.append(data.substring(i + 1, i + 2));
        }
        return sb.toString();*/
        return new String(toBytes(data));
    }

    //字符串转换时间
    public static Date stringToDate(String data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddhhmmss");
        Date date = null;
        try {
            date = sdf.parse(data);
        } catch (ParseException e) {
            log.info(data+"转换日期出错");
        }
        return date;
    }

    /**
     * 将16进制的字符串转换为字节数组
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    /**
     * 将时间转换为BCD码
     */
    public static String getCurrTimeStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return sdf.format(date);
    }

    /**
     * 将十进制数字字符串中每一个字符均转换为 Ascii 码表中对应的十六进制字符并组成字符串返回
     * 例如：将十进制字符串 18600000002 转换为
     * 3138363030303030303032
     */
    public static String decStrToHexStr(String decStr) throws Exception {
        if(decStr == null || decStr.length() == 0) {
            throw new Exception("传入待转换的十进制字符串必须不能为空");
        }
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) decStr);
        if(!matcher.matches()) {
            throw new Exception("传入待转换的十进制字符串必须只包含数字");
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < decStr.length(); i++) {
            sb.append(HexUtil.decToHexString(decStr.charAt(i)));
        }
        return sb.toString();
    }

    /**
     *  Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src
     * @return
     */
      public static String bytesToHexString(byte src){
         StringBuilder stringBuilder = new StringBuilder("");
             int v = src & 0xFF;
             String hv = Integer.toHexString(v);
             if (hv.length() < 2) {
                 stringBuilder.append(0);
             }
             stringBuilder.append(hv);
         return stringBuilder.toString();
      }


    /**
     * 截取字符串str中指定字符 strStart、strEnd之间的字符串
     *
     * @param str
     * @param strStart
     * @param strEnd
     * @return
     */
    public static String subString(String str, String strStart, String strEnd) {

        /* 找出指定的2个字符在 该字符串里面的 位置 */
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd);

        /* index 为负数 即表示该字符串中 没有该字符 */
        if (strStartIndex < 0) {
            return "字符串 :---->" + str + "<---- 中不存在 " + strStart + ", 无法截取目标字符串";
        }
        if (strEndIndex < 0) {
            return "字符串 :---->" + str + "<---- 中不存在 " + strEnd + ", 无法截取目标字符串";
        }
        /* 开始截取 */
        String result = str.substring(strStartIndex, strEndIndex).substring(strStart.length());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String s = new String("01 CB 04 F2");
        System.out.println("1.203.4.242");
        System.out.println(s);

        String id = getDeviceId("3138363030303030303032");
        System.out.println(id);
        String idd = decStrToHexStr("18600000002");
        System.out.println(idd);
    }
}
