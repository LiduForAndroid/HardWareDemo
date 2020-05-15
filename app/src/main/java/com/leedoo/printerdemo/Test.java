package com.leedoo.printerdemo;


public class Test {
    public static void main(String[] agrs){
        String LOGICAL_NUMBER_PASS="FF00FF11FF22";
        byte[] response={(byte)0xFF,(byte)0xCA,(byte)0x00,(byte)0x00,(byte)0x04};
        byte[] response2={43,53,30,33,39,31,33,34,37,31};

       byte[] mLogicalNumberPass=HexUtil.GetBytesFormHexString(Util.getKeyStringEncrypte(LOGICAL_NUMBER_PASS));
        String v=HexUtil.GetHexString(mLogicalNumberPass);
        Util.getLogicalCardNumberFromHex(response);
       String RESULT= bytesToHex(response2);
    }
    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Hex字符串转byte
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    public static byte hexToByte(String inHex){
        return (byte)Integer.parseInt(inHex,16);
    }
}
