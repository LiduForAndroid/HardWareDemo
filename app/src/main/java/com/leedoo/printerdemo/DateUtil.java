package com.leedoo.printerdemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * com.toonyoo.smartterminal_superplayer.model.event
 * 〈时间处理类〉
 * @author by xuan on 2017/10/19
 * @version [版本号, 2017/10/19]
 * @update by xuan on 2017/10/19
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DateUtil {
    /**
     * 时间戳格式
     */
    public final static String DATE_FORMAT_FINE_TO_DAY = "yyyy-MM-dd";//精确到天
    public final static String DATE_FORMAT_FINE_TO_MILLISECOND = "yyyy-MM-dd HH:mm:ss.SSS";//精确到毫秒

    /**
     * 获得当前时间戳
     * @param dateFormat 需要的时间戳格式:DATE_FORMAT_FINE_TO_DAY、DATE_FORMAT_FINE_TO_MILLISECOND
     * @return string格式的时间戳
     */
    public static String CurrentTimeStamp(String dateFormat){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.CHINA);
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获得当前时间戳
     * @param dateFormat 需要的时间戳格式:DATE_FORMAT_FINE_TO_DAY、DATE_FORMAT_FINE_TO_MILLISECOND
     * @param time 毫秒数
     * @return string格式的时间戳
     */
    public static String longFormatString(long time,String dateFormat){
        SimpleDateFormat format=new SimpleDateFormat(dateFormat,Locale.CHINA);
        return format.format(new Date(time));
    }

    /**
     * 获得当前时间毫秒数
     * @param dateFormat 需要的时间戳格式:DATE_FORMAT_FINE_TO_DAY、DATE_FORMAT_FINE_TO_MILLISECOND
     * @param time string格式的时间戳
     * @return 毫秒数
     */
    public static long stringFormatLong(String time, String dateFormat){
        long date=0;
        SimpleDateFormat format=new SimpleDateFormat(dateFormat,Locale.CHINA);
        try {
            date=format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}
