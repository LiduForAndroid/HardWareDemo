package com.leedoo.printerdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * 打印管理器
 * 统一管理所有接入的打印机设备（PID,VID）
 * @author 韩国桐
 * @version [0.1,2017-09-07]
 * @see [UsbPrinter]
 * @since [打印机]
 */
public class NewPrinterManager {
    private static NewPrinterManager mInstance;
    private int type=0;
    private PosUsbPrinter posUsbPrinter;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public UsbCheckSRT[] posUsbCheckSRT = new UsbCheckSRT[]{new UsbCheckSRT(5455,5455)};


    public static NewPrinterManager createInstance(Context context){
        if(mInstance==null){
            synchronized (NewPrinterManager.class){
                if(mInstance==null){
                    mInstance=new NewPrinterManager(context);
                }
            }
        }
        return mInstance;
    }

    public static NewPrinterManager getInstance(){
        if(mInstance==null){
            throw new RuntimeException("getInstance() is null,please call createInstance() first!");
        }
        return mInstance;
    }

    /**
     * Check id boolean. 检测打印机pid vid是否相同 判断新老机器
     *
     * @param vid the vid
     * @param pid the pid
     *
     * @return the boolean
     */
    public int checkIDType(int vid, int pid) {
        for (UsbCheckSRT aPosUsbCheckSRT : posUsbCheckSRT) {
            if (aPosUsbCheckSRT.checkId(vid, pid)) {
                return 2;
            }
        }
        return -1;
    }

    public boolean checkID(int vid, int pid) {
        for (UsbCheckSRT aPosUsbCheckSRT : posUsbCheckSRT) {
            if (aPosUsbCheckSRT.checkId(vid, pid)) {
                return true;
            }
        }
        return false;
    }

    private NewPrinterManager(Context context){
        posUsbPrinter=PosUsbPrinter.newInstance(context);
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        //权限申请
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 1, new Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_ONE_SHOT);
        //查询本机所有已经连接的USB设备
        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            Log.e("will", "Devices : " + usbDevice.toString());
            type=checkIDType(usbDevice.getVendorId(),usbDevice.getProductId());
             if(type==2){
                //pos打印机
                 if(usbManager.hasPermission(usbDevice)){//有权限
                     Log.e("will", "打印机已经有权限 ");
                     Log.e("will", "去打开打印机 ");
                     context.registerReceiver(mUsbReceiver, filter);
                     onOpen();
                 }else {
                     Log.e("will", "打印机没有权限，去申请权限 ");
                     Log.e("will",usbDevice.getDeviceName());
                     filter.addAction(ACTION_USB_PERMISSION);
                     context.registerReceiver(mUsbReceiver, filter);
                     usbManager.requestPermission(usbDevice, mPermissionIntent);
                 }
                 break;
            }
        }
    }

    //打印小票
    public void printTicketData(final PosUsbPrinter.PrintCallBack callBack, final String packageName,
                                final String amount, final String orderId) {
        switch (this.type){
            case 2:
                posUsbPrinter.printTicketData(packageName,amount,orderId,callBack);
                break;
            default:
                callBack.onError();
                break;
        }
    }


    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                //USB连接设备
                onOpen();
            } else if(type==2&&ACTION_USB_PERMISSION.equals(action)){
                synchronized (this) {
                    context.unregisterReceiver(mUsbReceiver);
                    UsbDevice device =intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e("will","打印机:"+device.getVendorId()+":"+device.getProductId());
                    if (device != null&&checkID(device.getVendorId(),device.getProductId())) {
                        Log.e("will","打印机权限获取成功");
                        Log.e("will","去打开打印机...");
                        onOpen();
                    }
                }
            }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                //USB设备断开
                onClose();
            }
        }
    };

    public void onOpen(){
        switch (this.type){
            case 2:
                posUsbPrinter.connectUsb();
                break;
        }
    }

    public void onClose(){
        switch (this.type){
            case 2:
                posUsbPrinter.disconnectUsb();
                break;
        }
    }

    public class UsbCheckSRT {
        /**
         * The Vid.
         */
        public int VID = 0;
        /**
         * The Pid.
         */
        public int PID = 0;

        /**
         * Instantiates a new Usb check srt.
         *
         * @param vid the vid
         * @param pid the pid
         */
        public UsbCheckSRT(int vid, int pid) {
            this.VID = vid;
            this.PID = pid;
        }

        /**
         * Check id boolean.
         *
         * @param vid the vid
         * @param pid the pid
         *
         * @return the boolean
         */
        public boolean checkId(int vid, int pid) {
            return this.PID == pid && this.VID == vid;
        }
    }
}
