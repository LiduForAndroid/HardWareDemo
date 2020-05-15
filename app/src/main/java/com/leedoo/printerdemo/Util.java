package com.leedoo.printerdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;


/**
 * 开发工具类
 * 
 * @author luoli
 * 
 */
public class Util {

	/**
	 * 获取屏幕宽度
	 * @param context 上下文变量
	 * @return 屏幕宽度（像素）
	 * **/
	public static int getWindowWidth(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	/**
	 * 获取屏幕高度
	 * @param context 上下文变量
	 * @return 屏幕高度（像素）
	 * **/
	public static int getWindowHeight(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static byte[] parseIntToByteArrayReversal(int i ,int length){
		byte[] bLocalArr = new byte[length];
		for (int index = 0; (index < 4) && (index < length); index++) {
			bLocalArr[index] = (byte) (i >> 8 * (length-index-1) & 0xFF);
		}
		return bLocalArr;
	}

	public static int parseByteArrayToInt(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;

		for (int i = 0; i < bRefArr.length; i++) {
			bLoop = bRefArr[i];
			iOutcome += (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	public static int parseByteArrayToIntReversal(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;

		for (int i = 0; i < bRefArr.length; i++) {
			bLoop = bRefArr[i];
			iOutcome += (bLoop & 0xFF) << (8 * (bRefArr.length-i-1));
		}
		return iOutcome;
	}

	/**
	 * 获取内网IP
	 */
	public static String getLocalIpAddress() {
		try {
			String ipv4;
			List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface ni : nilist) {
				List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
				for (InetAddress address : ialist) {
					ipv4 = address.getHostAddress();
					if (!address.isLoopbackAddress() && Util.isIPv4Address(ipv4)) {
						return ipv4;
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return "0.0.0.0";
	}

	public static String getKeyStringEncrypte(String oldkey) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < 6; i++){
			str.append(oldkey.substring(i, i + 2));
		}
		return str.toString();
	}

    public static  String getLogicalCardNumberFromHex(byte[] cardNumber){
        StringBuilder cardNumberLogic=new StringBuilder("");
        for (byte aTemp : cardNumber) {
            char a = (char) Integer.parseInt(Integer.valueOf(aTemp+"",16).toString());
            if (aTemp != 0) {
                cardNumberLogic.append(a);
            }
        }
        return cardNumberLogic.toString();
    }



	private static final String SHAREDPREFERENCES_NAME_UNIQUEID = "unique_id";
	private static final String KEY_FOR_UNIQUEID = "unique_id";

	/**
	 * 获取mac地址
	 * @author 吴超
	 * */
	@SuppressLint("HardwareIds")
	public static String getLocalMacAddress(Context context) {
		String uniqueID;
		uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		if(uniqueID == null){
			SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCES_NAME_UNIQUEID, Context.MODE_PRIVATE);
			uniqueID = sharedPreferences.getString(KEY_FOR_UNIQUEID, null);
			if(uniqueID == null){
				uniqueID = UUID.randomUUID().toString();
				sharedPreferences.edit().putString(KEY_FOR_UNIQUEID, uniqueID).apply();
			}
		}
//		return "ba13168fc4646a25";
		return uniqueID;
	}

	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null&& info.isConnected()) {
					// 判断当前网络是否已经连接
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error",e.toString());
		}
		return false;
	}

	/**
	 * 通过身份证号获取生日日期
	 * @param idNumber 身份证号
	 * @return 生日日期
	 * **/
	public static String getDateFromIdNumber(String idNumber){
		if(!isBlank(idNumber)){
			return idNumber.substring(6,14);
		}else{
			return null;
		}
	}

	public static String getFormatTime(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		return sdf.format(date);
	}

	public static String getCurrentFormatTime(){
		return getFormatTime(new Date());
	}

	public static boolean isBlank(String str) {
		int strLen;

		if ((str == null) || ((strLen = str.length()) == 0)) {
			return true;
		}

		for (int i = 0; i < strLen; i++) {
			if ((!Character.isWhitespace(str.charAt(i)))) {
				return false;
			}
		}

		return true;
	}
	/**
	 * 获取META元素值（字符串）
	 * @param context 上下文变量
	 * @param key key值
	 * @return 判断地址的ip（true是ip地址，false不是地址）
	 * **/
	public static String getMetaData(Context context,String key){
		ApplicationInfo ai;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
		return ai.metaData.getString(key);
	}
	/**
	 * 判断地址的ip
	 * @param ip ip地址
	 * @return 判断地址的ip（true是ip地址，false不是地址）
	 * **/
	public static boolean isIPv4Address(String ip) {
		//正则表达式判断IP正确性
		Matcher m = Patterns.IP_ADDRESS.matcher(ip);
		return m.matches();
	}
	/**
	 * 获取版本号
	 * @param context 上下文变量
	 * @return 版本号
	 * **/
	public static int getAppVersionCode(Context context) {
		int versionName = 0;
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionCode;
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}

	/**
	 * 获取版本名称
	 * @param context 上下文变量
	 * @return 版本名称
	 * **/
	public static String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),0);
			versionName = pi.versionName;
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}

	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager!=null){
			List<ActivityManager.RunningServiceInfo> serviceList = activityManager
					.getRunningServices(30);
			if (serviceList.size() <= 0) {
				return false;
			}
			for (int i = 0; i < serviceList.size(); i++) {
				if (serviceList.get(i).service.getClassName().contains(className)) {
					isRunning = true;
					break;
				}
			}
		}
		return isRunning;
	}

	public static boolean isAppRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		if(activityManager!=null){
			List<ActivityManager.RunningTaskInfo> runningAppProcessesList = activityManager
					.getRunningTasks(100);
			if (runningAppProcessesList.size() <= 0) {
				return false;
			}
			for (int i = 0; i < runningAppProcessesList.size(); i++) {
				if (runningAppProcessesList.get(i).topActivity.getPackageName().contains(className)) {
					isRunning = true;
					break;
				}
			}
		}
		return isRunning;
	}

	/**
	 * 将名字前面几个字用*表示，只留最后一个字显示 例如: 张三 --> *三
	 * @param name
	 * @return
	 */
	public static String getNameWithCipherText(String name){
		if(!isBlank(name)){
			if(name.length() > 1){
				return "*" + name.substring(name.length() - 1);
			}
		}
		return name;
	}

	/**
	 * 将11位手机号中间4为隐藏，*表示，例如:13012341111 --> 130****1111
	 * @param phoneNumber
	 * @return
	 */
	public static String getPhoneNumberWithCipherText(String phoneNumber){
		if(!isBlank(phoneNumber)){
			if(phoneNumber.length() == 11){
				return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7, phoneNumber.length());
			}
		}
		return phoneNumber;
	}

	public static String getDateNoSeconds(String dateStr){
		if(isBlank(dateStr)){
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		try {
			Date date = sdf.parse(dateStr);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
			return format.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateStr;
	}
    /**
     * 根据response回传的结果 拿到卡片的物理卡号
     *
     * @param bytes response回传的卡片数据
     * @return 转换过的卡号
     */
    public static String getPhysicCardNumber(byte[] bytes) {
        byte[] tempBuf = new byte[bytes.length];
        // 十六进制3A2EFFC4=十进制976158660
        for (int i = 0; i < bytes.length; i++) {
            tempBuf[bytes.length - i - 1] = bytes[i];
        }
        String physicsNumber = "";
        for (int i = 0; i < tempBuf.length; i++) {
            physicsNumber = physicsNumber + HexUtil.GetHexString(tempBuf[i]).replace("0x", "");
        }
        return physicsNumber;// 这里的1代表正数
    }
}
