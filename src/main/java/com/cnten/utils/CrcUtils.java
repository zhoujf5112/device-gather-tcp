package com.cnten.utils;

public class CrcUtils {
    /**
     * 计算CRC16校验码(低字节在后)
     *
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        String crcCode = Integer.toHexString(CRC).toUpperCase();
        // crcCode 如果长度不足四位的话自动在前面补零
        while(crcCode.length() < 4) {
            crcCode = "0" + crcCode;
        }
        return crcCode;
    }

    /**
     * 计算CRC16校验码(低字节在前)
     * @param bytes
     * @return
     */
    public static String getCrcHigh(byte[] bytes){
        String crc = getCRC(bytes);
        return crc.substring(2, 4) + crc.substring(0, 2);
    }

    /**
     * 和校验
     * @param bs
     * @return
     */
    public static byte SumCheck(byte[] bs) {
        int num = 0;
        //所有字节累加
        for (int i = 0; i < bs.length; i++) {
            num = (num + bs[i]) % 0xFFFF;
        }
        byte ret = (byte)(num & 0xff);//只要最后一个字节
        return ret;
    }

    /**
     * 生成异或校验码
     * @param datas
     * @return
     */
    public static  String getXOR(byte[] datas){
        int result=0;
        for (int i = 0; i <datas.length; i++) {
            result=  result^byteToInt(datas[i]);
        }
        String s = Integer.toHexString(result);
        return s;
    }

    private static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }


}
