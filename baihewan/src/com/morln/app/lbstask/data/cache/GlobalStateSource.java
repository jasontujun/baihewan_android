package com.morln.app.lbstask.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.xengine.android.data.cache.XDataSource;
import com.xengine.android.utils.XLog;

/**
 * 记录全局都要用到的状态。
 * 存储在SharedPreferences中。
 * 包括当前登陆的用户名、密码和通信token。
 * Created by jasontujun.
 * Date: 12-2-23
 * Time: 下午3:34
 */
public class GlobalStateSource implements XDataSource {

    private static final String PREF_NAME = "lbstask.globalState";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String LAST_USERNAME = "lastUsername";

    private static final String LAST_PASSWORD = "lastPassword";


    /**
     * 登录状态。共三种。
     * 未登录=0，只有BBS登录=1，全登录=2
     */
    private static final String LOGIN_STATUS = "loginStatus";
    public static final int LOGIN_STATUS_NO_LOGIN = 0;
    public static final int LOGIN_STATUS_BBS_LOGIN = 1;
    public static final int LOGIN_STATUS_ALL_LOGIN = 2;


    /**
     * 版面数据的时间戳
     */
    private static final String BOARD_TIME_STAMP = "boardTimeStamp";


    private static final String TUTORIAL_RSS = "tutorialRss";
    private static final String TUTORIAL_TOP10 = "tutorialTop10";
    private static final String TUTORIAL_LEFT_BAR = "tutorialLeftBar";

    private SharedPreferences pref;

    private String token ;// 和服务器通信的token
    private String bbsCode ;// 和小百合通信的code
    private String bbsCookies ;// 和小百合通信的Cookies

    private int newMailNumber;// 新邮件数量

    /**
     * 全局状态数据源
     * @param context 请使用由getApplicationContext()获得的context
     */
    public GlobalStateSource(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 返回当前登陆的用户的用户名
     */
    public String getCurrentUserName() {
        return pref.getString(USERNAME, null);
    }

    /**
     * 返回当前用户使用的密码
     */
    public String getCurrentUserPassword() {
        return pref.getString(PASSWORD, null);
    }

    /**
     * 返回当前登陆的用户的用户名
     */
    public String getLastUserName() {
        return pref.getString(LAST_USERNAME, null);
    }

    /**
     * 返回当前用户使用的密码
     */
    public String getLastUserPassword() {
        return pref.getString(LAST_PASSWORD, null);
    }

    /**
     * 设置token
     * @param token 通信token
     */
    public void setToken(String token){
        this.token = token;
    }

    /**
     * 返回当前的通信token
     */
    public String getToken() {
        return token;
    }

    /**
     * 当心跳超时或者注销的时候清除当前用户通信使用的token字符串
     */
    public void clearToken() {
        token = null;
    }

    /**
     * 设置当前登陆的用户名、密码
     * @param userName 用户名
     * @param password 密码
     */
    public void setCurrentUser(String userName, String password) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USERNAME, userName);
        editor.putString(PASSWORD, password);
        editor.commit();
    }

    /**
     * 设置上次登陆的用户名、密码
     * @param userName 用户名
     * @param password 密码
     */
    public void setLastUser(String userName, String password) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LAST_USERNAME, userName);
        editor.putString(LAST_PASSWORD, password);
        editor.commit();
    }

    /**
     * REVISED 注销时情况当前登录的用户信息
     */
    public void removeCurrentUser() {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(USERNAME);
        editor.remove(PASSWORD);
        editor.commit();
        clearToken();
    }

    public boolean isLogin() {
        int loginStatus = pref.getInt(LOGIN_STATUS, LOGIN_STATUS_NO_LOGIN);
        if(loginStatus == LOGIN_STATUS_NO_LOGIN) {
            return false;
        }else {
            return true;
        }
    }
    
    public int getLoginStatus() {
        return pref.getInt(LOGIN_STATUS, LOGIN_STATUS_NO_LOGIN);
    }

    public void setLoginStatus(int loginStatus) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LOGIN_STATUS, loginStatus);
        editor.commit();
        XLog.d("ABC", "设置了登陆属性！！！！！！！！" + loginStatus);
    }

    public void setBbsCookies(String bbsCookies) {
        this.bbsCookies = bbsCookies;
    }

    public String getBbsCookies() {
        return bbsCookies;
    }
    
    public void setBbsCode(String bbsCode) {
        this.bbsCode = bbsCode;
    }
    
    public String getBbsCode() {
        return this.bbsCode;
    }

    public void setBoardTimeStamp(long boardTimeStamp) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(BOARD_TIME_STAMP, boardTimeStamp);
        editor.commit();
    }

    public long getBoardTimeStamp() {
        return pref.getLong(BOARD_TIME_STAMP, 0);
    }


    public int getNewMailNum() {
        return newMailNumber;
    }

    public void setNewMailNum(int num) {
        this.newMailNumber = num;
    }

    // ----------------------------------------------教程

    public void setTutorialTop10(boolean isRead) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(TUTORIAL_TOP10, isRead);
        editor.commit();
    }

    public boolean getTutorialTop10() {
        return pref.getBoolean(TUTORIAL_TOP10, false);
    }

    public void setTutorialLeftBar(boolean isRead) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(TUTORIAL_LEFT_BAR, isRead);
        editor.commit();
    }

    public boolean getTutorialLeftBar() {
        return pref.getBoolean(TUTORIAL_LEFT_BAR, false);
    }

    public void setTutorialRss(boolean isRead) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(TUTORIAL_RSS, isRead);
        editor.commit();
    }

    public boolean getTutorialRss() {
        return pref.getBoolean(TUTORIAL_RSS, false);
    }


    @Override
    public String getSourceName() {
        return SourceName.GLOBAL_STATE;
    }
}
