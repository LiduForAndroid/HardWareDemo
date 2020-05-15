package com.leedoo.printerdemo;

import android.content.Context;



import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;

public class PosUsbPrinter {

    private int cutter = 0;       // 切纸距离
    public static final int POS_SUCCESS=1000;		//success
    public static final int ERR_PROCESSING = 1001;	//processing error
    public static final int ERR_PARAM = 1002;		//parameter error
    //打印模式
    private static final int PRINT_MODE_STANDARD = 0;//标准
    private static final int PRINT_MODE_PAGE = 1;//页打印

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA); // 国际化标志时间格式类

    private ExecutorService mSingleThreadPool = Executors.newSingleThreadExecutor();

    private POSInterfaceAPI usbApi;
    private POSSDK pos_sdk;
    public boolean initFlag=false;
    private static PosUsbPrinter mInstance;

    public static PosUsbPrinter newInstance(Context context){
        if(mInstance==null){
            synchronized (PosUsbPrinter.class){
                if(mInstance==null){
                    mInstance=new PosUsbPrinter(context);
                }
            }
        }
        return mInstance;
    }

    private PosUsbPrinter(Context context){
        if(usbApi==null) {
            usbApi = new POSUSBAPI(context);
        }
        pos_sdk = new POSSDK(usbApi);
        //connectUsb();
    }

    public void connectUsb(){
        if(POS_SUCCESS==usbApi.OpenDevice()){
            initFlag=true;
        }
    }

    public void disconnectUsb(){
        if(POS_SUCCESS == usbApi.CloseDevice()){
            initFlag=false;
        }
    }

    private void printStringText(String text){
        try {
            byte []send_buf = text.getBytes("GB18030");
            pos_sdk.textPrint(send_buf,send_buf.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 指定格式小票打印
     * @param packageName 商品名称
     * @param amount 金额（元）
     * @param orderId 订单编号
     */
    public void printTicketData(final String packageName, final String amount, final String orderId, final PrintCallBack inter) {
        final String shopName= "京东旗舰店";
        Future<?> future=mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                try{
                   /* int stauts=getStatus();
                    if(stauts!=0){
                        inter.onError();
                        return;
                    }*/
                    pos_sdk.systemSetMotionUnit(100, 100);
                    // 小票标题
                    pos_sdk.textSetLineHeight(1);//设置行高
                    pos_sdk.textStandardModeAlignment(1);
                    pos_sdk.textSelectFontMagnifyTimes(2,2);
                    printStringText("消费凭据");
                    pos_sdk.textSetLineHeight(60);
                    pos_sdk.systemFeedLine(1);

                    // 小票号码
                    pos_sdk.textSetLineHeight(1);
                    pos_sdk.textStandardModeAlignment(1);
                    pos_sdk.textSelectFontMagnifyTimes(1,1);
                    printStringText(shopName+"\n");

                    pos_sdk.textStandardModeAlignment(1);
                    pos_sdk.textSetLineHeight(1);
                    pos_sdk.textSelectFontMagnifyTimes(2,2);
                    printStringText("------------------------"+"\n");

                    pos_sdk.textStandardModeAlignment(0);
                    pos_sdk.textSelectFontMagnifyTimes(1,1);
                    printStringText(packageName+"     x 1");

                    pos_sdk.textStandardModeAlignment(2);
                    pos_sdk.textSelectFontMagnifyTimes(1,1);
                    printStringText(amount+"元");
                    pos_sdk.systemFeedLine(1);

                    // 小票主要内容
                    pos_sdk.textSetLineHeight(1);
                    pos_sdk.textStandardModeAlignment(1);
                    pos_sdk.textSelectFontMagnifyTimes(2,2);
                    printStringText("------------------------"+"\n");

                    pos_sdk.textStandardModeAlignment(2);
                    pos_sdk.textSelectFontMagnifyTimes(1,1);
                    printStringText("合计："+amount+"元");
                    pos_sdk.systemFeedLine(2);

                    // 日期时间
                    pos_sdk.textStandardModeAlignment(0);
                    printStringText("订单编号："+orderId+ "\n");
                    printStringText("消费时间："+sdf.format(new Date())+"\n");
                    printStringText("欢迎光临，谢谢惠顾！");
                    pos_sdk.textSetLineHeight(1);
                    pos_sdk.systemFeedLine(2);
                    cutPaper(pos_sdk,PRINT_MODE_STANDARD);//切纸
                    inter.onSucess(0);

                } catch(Exception e){
                    inter.onSucess(0);
                    e.printStackTrace();
                }
            }
        });

        try{
            future.get(2, TimeUnit.SECONDS);
        }catch (Exception e){
            future.cancel(true);
            inter.onError();
            e.printStackTrace();
        }
    }

    // 检测打印机状态
    public void getPrinterStatus(final PrintCallBack printCallBack) {
        Future<?> future=mSingleThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                printCallBack.onSucess(getStatus());
            }
        });
        try{
            future.get(500, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            printCallBack.onSucess(120);
            future.cancel(true);
            e.printStackTrace();
        }
    }


    public int getStatus() {
        byte[] posStateTemp = new byte[1];
        int result;
        if(initFlag){
            result = POSNETQueryStatus(pos_sdk,posStateTemp);
            if (result == POS_SUCCESS) {
                byte btemp[] = new byte[8];
                byte bitindex = 1;
                for (int i = 0; i < btemp.length; i++) {
                    btemp[i] = (byte) (posStateTemp[0] & bitindex);
                    bitindex = (byte) (bitindex << 1);
                }
                if (btemp[0] == 0) {	//CashDrawer Open
                    return 0;
                }
                if (btemp[1] == 0) {	//Offline
                    return 1;
                }
                if (btemp[2] == 0) {	//Cover Open
                    return 2;
                }
                if (btemp[4] == 0) {	//Printer Error
                    return 1;
                }
                if (btemp[5] == 0) {	//Cutter Error
                    return 4;
                }
                if (btemp[6] == 0) {	//Paper	Near End
                    return 0;
                }
                if (btemp[7] == 0) {	//Paper End
                    return 7;
                }
                btemp = null;
                return 0;
            } else {
                return 1;
            }
        }else {
            return 1;
        }
    }

    public int POSNETQueryStatus(POSSDK pos_sdk, byte[] pStatus){
        int result = POS_SUCCESS;
        byte[] recbuf = new byte[64];// accept buffer
        pos_sdk.systemQueryStatus(recbuf, 4, 1);
        if ((recbuf[0] & 0x04) == 0x04) {
            // Drawer open/close signal is HIGH (connector pin 3).
            pStatus[0] |= 0x01;
        } else {
            pStatus[0] &= 0xFE;
        }

        if ((recbuf[0] & 0x08) == 0x08) {//打印机离线
            // Printer is Off-line.
            pStatus[0] |= 0x02;
        } else {
            pStatus[0] &= 0xFD;
        }

        if ((recbuf[0] & 0x20) == 0x20) {
            // Cover is open.
            pStatus[0] |= 0x04;
        } else {
            pStatus[0] &= 0xFB;
        }

        if ((recbuf[0] & 0x40) == 0x40) {
            // Paper is being fed by the FEED button.
            pStatus[0] |= 0x08;
        } else {
            pStatus[0] &= 0xF7;
        }

        if ((recbuf[1] & 0x40) == 0x40) {//未知异常
            // Error occurs.
            pStatus[0] |= 0x10;
        } else {
            pStatus[0] &= 0xEF;
        }

        if ((recbuf[1] & 0x08) == 0x08) {//切纸异常
            // Auto-cutter error occurs.
            pStatus[0] |= 0x20;
        } else {
            pStatus[0] &= 0xDF;
        }

        if ((recbuf[2] & 0x03) == 0x03) {//纸快完了
            // Paper near-end is detected by the paper roll near-end sensor.
            pStatus[0] |= 0x40;
        } else {
            pStatus[0] &= 0xBF;
        }

        if ((recbuf[2] & 0x0C) == 0x0C) {//没纸了
            // Paper roll end detected by paper roll sensor.
            pStatus[0] |= 0x80;
        } else {
            pStatus[0] &= 0x7F;
        }

        return result;
    }


    //切纸
    public void cutPaper(POSSDK pos_sdk, int printMode)
    {
        if(printMode == PRINT_MODE_PAGE)
        {
            //print in page mode
             pos_sdk.pageModePrint();
             pos_sdk.systemCutPaper(66, 0);
             pos_sdk.pageModeClearBuffer();
        }else{
            pos_sdk.systemCutPaper(66, 0);
        }
    }

    public interface PrintCallBack{
        void onSucess(int status);
        void onError();
    }

}
