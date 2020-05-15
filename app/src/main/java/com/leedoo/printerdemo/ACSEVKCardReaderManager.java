package com.leedoo.printerdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ACSEVKCardReaderManager {
    private static ACSEVKCardReaderManager mInstance;

    private Reader mReader;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private  UsbManager usbManager;
    private int slotNum;
    ExecutorService mSingleThreadPool = Executors.newSingleThreadExecutor();

    public int getSlotNum() {
        return slotNum;
    }

    private int slots;
    //逻辑卡号块密码
    public final byte[] LOGICAL_CARD_NUMBER_BLOCK_PASSWORD = {(byte) 0xFF, 0x00, (byte) 0xFF, 0x11, (byte) 0xFF, 0x22};
    private Reader.OnStateChangeListener mOnReaderStateChangeListener;
    public UsbCheckSRT[] posUsbCheckSRT = new UsbCheckSRT[]{new UsbCheckSRT(1839, 8704)};


    public ACSEVKCardReaderManager(Context context, final Reader.OnStateChangeListener onReaderStateChangeListener) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mReader = new Reader(usbManager);
        this.mOnReaderStateChangeListener=onReaderStateChangeListener;
        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {
            @Override
            public void onStateChange(int i, int i1, int i2) {
                Log.e("will","卡状态改变了:"+"之前："+i1+"->"+"现在："+i2);
                if (i2 == Reader.CARD_PRESENT) {
                    Log.e("will", "卡在");
                } else if (i2 == Reader.CARD_ABSENT) {
                    Log.e("will", "卡离开");
                }
                mOnReaderStateChangeListener.onStateChange(i, i1, i2);
            }
        });

        //权限申请
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);//检测到usb插入
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);//检测到usb拔掉
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 1000, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
        //查询本机所有已经连接的USB设备
        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
            Log.e("will", "Devices : " + usbDevice.toString());
            if (mReader.isSupported(usbDevice)) {//是该刷卡器
                if (usbManager.hasPermission(usbDevice)) {//已经有了USB权限
                    context.registerReceiver(mUsbReceiver, filter);//注册广播
                    Log.e("will", "刷卡器已经有权限");
                    Log.e("will", "去打开刷卡器");
                    onOpen(usbDevice);//USB连接设备
                } else {//没有权限就去请求权限
                    Log.e("will", "刷卡器没有权限去请求权限");
                    Log.e("will", usbDevice.getDeviceName());
                    filter.addAction(ACTION_USB_PERMISSION);
                    context.registerReceiver(mUsbReceiver, filter);//注册广播
                    usbManager.requestPermission(usbDevice, mPermissionIntent);
                }
                break;
            }
        }
    }

    public static ACSEVKCardReaderManager createInstance(Context context,final Reader.OnStateChangeListener onReaderStateChangeListener) {
        if (mInstance == null) {
            synchronized (ACSEVKCardReaderManager.class) {
                if (mInstance == null) {
                    mInstance = new ACSEVKCardReaderManager(context,onReaderStateChangeListener);
                }
            }
        }
        return mInstance;
    }

    public static ACSEVKCardReaderManager getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("getInstance() is null,please call createInstance() first!");
        }
        return mInstance;
    }
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.equals(mReader.getDevice())) {
                    Log.e("will", "去打开设备");
                    onOpen(device);//USB连接设备
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {//权限获取成功
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    context.unregisterReceiver(mUsbReceiver);
                    Log.e("will", "刷卡器:" + device.getVendorId() + ":" + device.getProductId());
                    if (device != null && mReader.isSupported(device)) {//是该刷卡器) {
                        Log.e("will", "刷卡器权限获取成功");
                        Log.e("will", "去打开刷卡器设备");
                        onOpen(device);//USB连接设备
                    }


                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.equals(mReader.getDevice())) {
                    Log.e("will", "检测到usb设备断开");
                    onClose();
                }
            }
        }
    };

    public void onClose() {
        mSingleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mReader.close();
            }
        });
    }

    public void onOpen(final UsbDevice usbDevice) {
        mSingleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mReader.open(usbDevice);
                slots = mReader.getNumSlots();
                int[] num = new int[slots];
                for (int i = 0; i < slots; i++) {
                    num[i] = i;
                }
                slotNum = num[0];
                Log.e("will", "获取slot:" + slots);
            }
        });
    }

    public void powerCard(int slot){
        try {
            mReader.power(slot, Reader.CARD_WARM_RESET);
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }

    public void powerDownCard(int slot) {
        try {
            mReader.power(slot, Reader.CARD_POWER_DOWN);
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }

    public int getStatus(int slot) {
        return mReader.getState(slot);
    }

    public void setProtocol(int slot,int protocolType){
        try {
            mReader.setProtocol(slot,protocolType);
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }

    public int getSlots() {
        return slots;
    }

    /**
     * 寻卡
     *
     * @return 返回卡片的物理卡号
     */
    public String searchCard() {
        Request request = Request.searchCardRequest(new byte[]{0x04});
        Response res;
        try {
            byte[] result = sendRequest(6, mReader, request.getByteArray(),100);
            res = new Response(result);
            if (0 == res.mByteArray.length) {
                return null;
            }
            byte[] data = res.getResultByte();
            if (data == null || data.length == 0) {
                return null;
            }
            return getPhysicCardNumber(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向指定块加载块秘钥
     *
     * @return
     */
    public boolean loadAuthKey() {
        Request request = Request.loadKey(getLogicalCardNubmer_blockDecryptPassword(), (byte) 0x00);
        Response res;
        try {
            res = new Response(sendRequest(2, mReader, request.getByteArray(),100));
            if (0 == res.mByteArray.length) {
                return false;
            }
            return res.checkStatus(res.getStatusByte());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 认证指定块0x14的秘钥
     * 密钥类型(A密钥:0x60 B密钥:0x61)
     * 秘钥所在块 (byte)0x00
     */
    public boolean checkPasswordRequest() {
        Request request = Request.checkPasswordRequest((byte) 0x14, (byte) 0x60, (byte) 0x00);
        Response res;
        try {
            res = new Response(sendRequest(2, mReader, request.getByteArray(),100));
            if (0 == res.mByteArray.length) {
                return false;
            }
            return res.checkStatus(res.getStatusByte());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 读块  并返回块中逻辑卡号的数据
     *
     * @return 块中的数据
     */
    public String readLogicCarNumBlock() {
        Request request = Request.readBlock((byte) 0x10, (byte) (0x14));
        Response res;
        try {
            res = new Response(sendRequest(18, mReader, request.getByteArray(), 100));
            if (0 == res.mByteArray.length) {
                return null;
            }
            byte[] data = res.getResultByte();
            if (data == null || data.length == 0) {
                return null;
            }
            return convertHexToString(bytesToHex(data)).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据response回传的结果 拿到卡片的物理卡号
     *
     * @param bytes response回传的卡片数据
     * @return 转换过的卡号
     */
    private String getPhysicCardNumber(byte[] bytes) {
        byte[] tempBuf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            tempBuf[bytes.length - i - 1] = bytes[i];
        }
        return bytesToHex(tempBuf);// 这里的1代表正数
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    //获取加密后的逻辑卡号块密码
    //逻辑卡号存储于20号块 读取此块密码需加密后验证
    private byte[] getLogicalCardNubmer_blockDecryptPassword() {
        String hexPassword = "";
        for (byte b : LOGICAL_CARD_NUMBER_BLOCK_PASSWORD) {
            String h = HexUtil.GetHexString(b);
            hexPassword += h.substring(2);
        }
        return HexUtil.GetBytesFormHexString(Util.getKeyStringEncrypte(hexPassword));
    }


    private byte[] sendRequest(int responseLength, Reader reader, byte[] command) {
        byte[] response;
        response = new byte[responseLength];
        try {
            reader.transmit(slotNum, command, command.length, response, response.length);
            Log.e("will", "result:" + bytesToHex(response));
            return response;
        } catch (ReaderException e) {
            e.printStackTrace();
        }
        return response;
    }

    private byte[] sendRequest(int responseLength, Reader reader, byte[] command, int waitTime) {
        byte[] response;
        response = new byte[responseLength];
        try {
            reader.transmit(slotNum, command, command.length, response, response.length);
            Thread.sleep(waitTime);
            Log.e("will", "result:" + bytesToHex(response));
            return response;
        } catch (ReaderException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return sb.toString();
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
         * @return the boolean
         */
        public boolean checkId(int vid, int pid) {
            return this.PID == pid && this.VID == vid;
        }
    }

    public boolean checkID(int vid, int pid) {
        for (UsbCheckSRT aPosUsbCheckSRT : posUsbCheckSRT) {
            if (aPosUsbCheckSRT.checkId(vid, pid)) {
                return true;
            }
        }
        return false;
    }

}
