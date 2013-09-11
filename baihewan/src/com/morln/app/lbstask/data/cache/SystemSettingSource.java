package com.morln.app.lbstask.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.xengine.android.data.cache.XDataSource;

/**
 * 系统配置数据源。
 * 目前系统的配置只包含音效和背景音乐，还有已经登陆过的用户及其密码。
 * 本数据源中的数据都保存在SharedPreferences里面。
 * Created by 赵之韵.
 * Date: 11-12-10
 * Time: 上午11:19
 */
public class SystemSettingSource implements XDataSource {

    private static final String PREF_NAME = "lbstask.systemSetting";

    private static final String REMEMBER = "remember";// 记住密码

    private static final String AUTO_LOGIN = "autoLogin";// 自动登录

    private static final String AUTO_LOGOUT = "autoLogout";// 退出程序时自动注销

    private static final String AUTO_UPDATE_COLLECT = "autoUpdateCollect";// 自动更新版面
    private static final String AUTO_UPDATE_COLLECT_INTERVAL = "autoUpdateCollectInterval";// 自动更新版面间隔
    private static final String AUTO_UPDATE_COLLECT_LAST_TIME = "autoUpdateCollectLastTime";// 上次更新版面的时间
    public static final long AUTO_UPDATE_COLLECT_INTERVAL_SHORT = 3*60*60*1000;// 3小时（毫秒）
    public static final long AUTO_UPDATE_COLLECT_INTERVAL_MIDDLE = 6*60*60*1000;// 6小时（毫秒）
    public static final long AUTO_UPDATE_COLLECT_INTERVAL_LONG = 24*60*60*1000;// 1天（毫秒） 推荐

    private static final String AUTO_DOWNLOAD_IMG = "autoDownloadImg";// 自动下载图片
    public static final int AUTO_DOWNLOAD_IMG_CLOSE = 0;// 关
    public static final int AUTO_DOWNLOAD_IMG_WIFI = 1;// wifi时候开
    public static final int AUTO_DOWNLOAD_IMG_ALWAYS = 2;// 任何时候开

    private static final String NEW_MAIL_REMIND = "newMailRemind";// 站内信提醒
    private static final String NEW_MAIL_REMIND_INTERVAL = "newMailRemindInterval";// 站内信提醒的间隔
    public static final long NEW_MAIL_REMIND_INTERVAL_SHORT = 90*1000;// 1.5分钟（毫秒） 推荐
    public static final long NEW_MAIL_REMIND_INTERVAL_MIDDLE = 3*60*1000;// 3分钟（毫秒）
    public static final long NEW_MAIL_REMIND_INTERVAL_LONG = 10*60*1000;// 10分钟（毫秒）

    private static final String MOBILE_SIGNATURE = "signature";// 手机个性签名
    
    private SharedPreferences pref;

    public SystemSettingSource(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setRememberPassword(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(REMEMBER, enable);
        editor.commit();
    }

    public boolean isRememberPassword() {
        return pref.getBoolean(REMEMBER, true);
    }

    public void setAutoLogin(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(AUTO_LOGIN, enable);
        editor.commit();
    }

    public boolean isAutoLogin() {
        return pref.getBoolean(AUTO_LOGIN, false);
    }

    public void setAutoLogout(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(AUTO_LOGOUT, enable);
        editor.commit();
    }

    public boolean isAutoLogout() {
        return pref.getBoolean(AUTO_LOGOUT, true);
    }
    
    public void setMobileSignature(String mobileSignature) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(MOBILE_SIGNATURE, mobileSignature);
        editor.commit();
    }

    public String getMobileSignature() {
        return pref.getString(MOBILE_SIGNATURE, "");
    }

    public void setAutoUpdateCollect(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(AUTO_UPDATE_COLLECT, enable);
        editor.commit();
    }

    public boolean isAutoUpdateCollect() {
        return pref.getBoolean(AUTO_UPDATE_COLLECT, true);
    }

    public void setAutoUpdateCollectInterval(long interval) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(AUTO_UPDATE_COLLECT_INTERVAL, interval);
        editor.commit();
    }

    public long getAutoUpdateCollectInterval() {
        return pref.getLong(AUTO_UPDATE_COLLECT_INTERVAL, AUTO_UPDATE_COLLECT_INTERVAL_LONG);
    }

    public void setAutoUpdateCollectLastTime(long interval) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(AUTO_UPDATE_COLLECT_LAST_TIME, interval);
        editor.commit();
    }

    public long getAutoUpdateCollectLastTime() {
        return pref.getLong(AUTO_UPDATE_COLLECT_LAST_TIME, 0);
    }


    public void setAutoDownloadImg(int downloadImgChoice) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(AUTO_DOWNLOAD_IMG, downloadImgChoice);
        editor.commit();
    }

    public int getAutoDownloadImg() {
        return pref.getInt(AUTO_DOWNLOAD_IMG, AUTO_DOWNLOAD_IMG_WIFI);
    }

    public void setNewMailRemind(boolean enable) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(NEW_MAIL_REMIND, enable);
        editor.commit();
    }

    public boolean isNewMailRemind() {
        return pref.getBoolean(NEW_MAIL_REMIND, true);
    }

    public void setNewMailRemindInterval(long interval) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(NEW_MAIL_REMIND_INTERVAL, interval);
        editor.commit();
    }

    public long getNewMailRemindInterval() {
        return pref.getLong(NEW_MAIL_REMIND_INTERVAL, NEW_MAIL_REMIND_INTERVAL_SHORT);
    }

    @Override
    public String getSourceName() {
        return SourceName.SYSTEM_SETTING;
    }
}
