package com.morln.app.lbstask.res;

/**
 * Created by jasontujun.
 * Date: 12-2-22
 * Time: 下午4:59
 */
public class MainMsg {

    public static final int BACK = 0;

    public static final int GO_TO_LOGIN = 2;
    
    public static final int GO_TO_MAIN = 3;

    // 重新实例化主界面
    public static final int INIT_MAIN = 5;

    // 登录成功后的界面刷新（用户游客登录）
    public static final int LOGIN_REFRESH = 6;

    // 注销后返回登录界面
    public static final int LOGOUT = 7;

    // 浏览版面
    public static final int SEE_BOARD = 10;
    public static final int SEE_BOARD_BACK = 11;

    // 看帖子
    public static final int SEE_ARTICLE_DETAIL = 20;
    public static final int SEE_ARTICLE_BACK = 21;
    public static final int SEE_PRE_ARTICLE = 22;
    public static final int SEE_NEXT_ARTICLE = 23;

    // 看图片详情
    public static final int SEE_IMAGE_DETAIL = 50;

    // 写帖子
    public static final int WRITE_ARTICLE = 24;
    public static final int WRITE_ARTICLE_BACK = 25;
    // 回复帖子
    public static final int BBS_REPLY_ARTICLE = 26;
    public static final int BBS_REPLY_ARTICLE_BACK = 27;

    // 站内信
    public static final int SEE_MAIL_DETAIL = 31;
    public static final int SEE_MAIL_DETAIL_BACK = 32;
    public static final int SEE_PRE_MAIL = 33;
    public static final int SEE_NEXT_MAIL = 34;
    public static final int WRITE_MAIL = 35;
    public static final int WRITE_MAIL_BACK = 36;

    // 设置
    public static final int SETTING = 40;

}
