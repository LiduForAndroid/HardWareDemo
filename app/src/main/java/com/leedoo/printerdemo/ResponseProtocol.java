package com.leedoo.printerdemo;

import java.io.IOException;

public class ResponseProtocol {
    //返回的字节
    public final byte[] mByteArray;

    public byte[] resultByte;
    public byte[] statusByte;
    private final String SUCCESS="9000";
    private final String FAILED="6300";
    private final byte[] NO_SUPPORT=new byte[]{(byte) 0X6A ,(byte) 0x81};
    /**
     * 使用机器返回的数据 构造response对象
     *
     * @param byteArray 机器返回的数据
     */
    public ResponseProtocol(byte[] byteArray) throws IOException {
        mByteArray = byteArray;
        if (mByteArray.length - 2 < 0) {
            throw new IOException("返回的结果长度有误（长度：" + mByteArray.length + "）");
        }else if(mByteArray.length-2==0){
            statusByte=mByteArray;
            resultByte=null;
            if(!SUCCESS.equals(ACSEVKCardReaderManager.bytesToHex(statusByte))&&!FAILED.equals(ACSEVKCardReaderManager.bytesToHex(statusByte))){
                throw new IOException("返回的结果与约定不符");
            }
        }else if(mByteArray.length>=6){
            statusByte=new byte[]{mByteArray[mByteArray.length-2],mByteArray[mByteArray.length-1]};
            byte[] temp=new byte[mByteArray.length-2];
            for (int i = 0; i <=mByteArray.length-3 ; i++) {
                temp[i]=mByteArray[i];
            }
            resultByte=temp;
        }
    }


    /**
     * 获取返回的结果状态字节
     *
     * @return 结果字节
     */
    public byte[] getStatusByte() {
        return statusByte;
    }

    /**
     * 获取返回的结果字节
     *
     * @return 结果字节
     */
    public byte[] getResultByte() {
        return resultByte;
    }

    public boolean checkStatus(byte[] status){
        return  ACSEVKCardReaderManager.bytesToHex(status).equals(SUCCESS);
    }
}
