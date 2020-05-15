package com.leedoo.printerdemo;

import java.io.ByteArrayOutputStream;

public abstract class RequestProtocol {
    //设备通讯的起始符
    public static final byte STX =(byte) 0xFF;
    //命令位
   private byte[] CM;
    //发送的数据
   private byte[] DATA;

    public RequestProtocol(byte[] CM, byte[] DATA) {
        this.CM = CM;
        this.DATA = DATA;
    }

    /**
     * 获取实际的发送的指令
     * @return 发送到机器的指令 命令写入异常时 可能为null
     */
    public byte[] getByteArray() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bao.write(STX);
        if(CM!=null){
            for (byte cm:CM) {
                bao.write(cm);
            }
        }
        if (DATA != null) {
            for(byte data:DATA){
                bao.write(data);
            }
        }

        return bao.toByteArray();
    }

}
