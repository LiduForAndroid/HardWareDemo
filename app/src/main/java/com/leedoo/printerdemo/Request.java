package com.leedoo.printerdemo;

import android.util.Log;

import java.io.IOException;

public  class Request extends RequestProtocol{

    public Request(byte[] CM, byte[] DATA) {
        super(CM, DATA);
    }
    /**
     * 获取寻卡的请求对象
     * @param DATA 发送数据()
     * @return 需要的对象
     */
    public static Request searchCardRequest(byte[] DATA) {
        byte[] CM={(byte) 0xCA,(byte)0x00,(byte)0x00};
        return new Request(CM, DATA);
    }

    /**
     * 在指定内存加载 秘钥 0x00 ~0x01
     * @param Key 要加载的秘钥\
     * @param position 秘钥加载地址
     * @return
     */
    public static Request loadKey(byte[] Key,byte position){
        byte[] CM={(byte) 0x82,(byte)0x00,position,(byte)0x06};
        return new Request(CM,Key);
    }

    /**
     * 验密请求对象
     * @param block 绝对块号 0x14
     * @param passwordType 密钥类型(A密钥:0x60 B密钥:0x61)
     * @param position  秘钥存放位置
     * @return 需要的对象
     */
    public static Request checkPasswordRequest(byte block,byte passwordType, byte position){
        byte[] CM = {(byte)0x86,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x01,(byte)0x00};
        byte[] DATA={block,passwordType,position};
        return new Request(CM,DATA);

    }

    /**
     *
     * @param length 待读取的字节数
     * @param block 待读取的块号
     * @return
     */
    public static  Request readBlock(byte length,byte block){
        byte[] CM = {(byte)0xB0,(byte)0x00};
        byte[] DATA={block,length};
        return new Request(CM,DATA);
    }

    public static Request getPiccOperateParam(){
        byte[] CM = {(byte)0x00,(byte)0x51};
        byte[] DATA={(byte)0x71,(byte)0x00};
        return new Request(CM,DATA);
    }
}
