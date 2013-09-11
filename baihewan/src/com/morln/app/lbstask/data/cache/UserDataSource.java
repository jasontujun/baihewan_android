package com.morln.app.lbstask.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.morln.app.lbstask.data.model.UserBase;
import com.xengine.android.data.cache.XDataSource;

/**
 * Created by jasontujun.
 * Date: 12-2-14
 * Time: 下午9:18
 */
public class UserDataSource implements XDataSource {
    private static final String PREF_NAME = "lbstask.user";
    private SharedPreferences pref;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户基本信息
     */
    private UserBase userInfo;
    
    public UserDataSource(Context context){
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getSourceName() {
        return SourceName.USER_DATA;
    }
}
