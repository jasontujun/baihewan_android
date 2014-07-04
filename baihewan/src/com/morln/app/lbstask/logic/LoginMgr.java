package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.session.bbs.BbsAPI;
import com.morln.app.lbstask.data.cache.*;
import com.morln.app.lbstask.data.model.UserBase;
import com.morln.app.lbstask.session.apinew.LoginAPINew;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.data.cache.XDataRepository;

/**
 * Created by jasontujun.
 * Date: 12-2-19
 * Time: 下午12:50
 */
public class LoginMgr {
    private static LoginMgr instance;

    public synchronized static LoginMgr getInstance() {
        if (instance == null) {
            instance = new LoginMgr();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private GlobalStateSource globalStateSource;
    private SystemSettingSource systemSettingSource;
    private SystemUserSource systemUserSource;

    private LoginMgr() {
        XDataRepository repo = DefaultDataRepo.getInstance();
        globalStateSource = (GlobalStateSource) repo.getSource(SourceName.GLOBAL_STATE);
        systemSettingSource = (SystemSettingSource) repo.getSource(SourceName.SYSTEM_SETTING);
        systemUserSource = (SystemUserSource) repo.getSource(SourceName.SYSTEM_USER);
    }

    /**
     * 注销
     * @return
     */
    public int logout() {
        int statusCode = BbsAPI.logout();
        if (StatusCode.isSuccess(statusCode)) {
            // 停止邮件更新
            BbsMailMgr.getInstance().stopMailRemindTask();
        }
        return statusCode;
    }

    /**
     * 调用通信模块。登陆（本系统和南大小百合同时登陆）
     * @param username
     * @param password
     * @return 登陆成功返回LOGIN_SUCCESS（必须二者都登陆成功），否则返回第一个登陆的错误码。
     */
    public int login(Context context, String username, String password) {
        // 登陆Bbs
        int bbsLoginResult = BbsAPI.login(username, password);
        if (!StatusCode.isSuccess(bbsLoginResult)) {
            return bbsLoginResult;
        }
        globalStateSource.setCurrentUser(username, password);// 登陆成功，记住账户名和密码
        globalStateSource.setLastUser(username, password);// 登陆成功，记录历史记录
        globalStateSource.setLoginStatus(GlobalStateSource.LOGIN_STATUS_BBS_LOGIN);

        // 登陆Bbs后，开始刷用户状态（邮件）
        BbsMailMgr.getInstance().startMailRemindTask();

//        // 登陆系统
//        int systemLoginResult = loginSystem(context, username, password);
//        if (!StatusCode.isSuccess(systemLoginResult))
//            return systemLoginResult;

        if (systemUserSource.getIndexById(username) == -1) {
            // 添加系统用户
            UserBase userBase = new UserBase(username);
            systemUserSource.add(userBase);
            systemUserSource.saveToDatabase();
        }
        // 登陆系统成功
        globalStateSource.setLoginStatus(GlobalStateSource.LOGIN_STATUS_ALL_LOGIN);
        return StatusCode.LOGIN_SUCCESS;
    }

    /**
     * 登陆本软件系统
     */
    private int loginSystem(Context context, String username, String password) {
        int resultCode = new LoginAPINew(context).login(username, password);

        // 登陆不成功
        if (!StatusCode.isSuccess(resultCode))
            return StatusCode.SYSTEM_LOGIN_FAIL;

        return resultCode;
    }

    public boolean isRememberPassword() {
        return systemSettingSource.isRememberPassword();
    }

    /**
     * 设置是否记住密码
     * @param enable
     */
    public void setRememberPassword(boolean enable) {
        systemSettingSource.setRememberPassword(enable);
        if (!enable)
            globalStateSource.setLastUser(globalStateSource.getLastUserName(), "");
    }


    public boolean isAutoLogin() {
        return systemSettingSource.isAutoLogin();
    }

    /**
     * 设置是否自动登录
     * @param enable
     */
    public void setAutoLogin(boolean enable) {
        systemSettingSource.setAutoLogin(enable);
    }
}
