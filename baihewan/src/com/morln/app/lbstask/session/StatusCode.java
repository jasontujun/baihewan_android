package com.morln.app.lbstask.session;

/**
 * Created by 赵之韵.
 * Date: 12-2-29
 * Time: 下午6:50
 */
public class StatusCode {
    public static final int SUCCESS = 2000; //the usr's info has been stored in db

    public static final int EXSIT_SUCCESS = 2100; //the usr's info has been stored in db

    public static final int NOT_EXIST_SUCCESS = 2200; //the usr's info hasn't been stored in db

    public static final int CREAT_SUCCESS=2300;

    public static final int GET_SUCCESS=2400;

    public static final int UPDATE_SUCCESS=2500;

    public static final int DELETE_SUCCESS=2600;

    public static final int OUT_OF_SIZE_SUCCESS=2700;



    public static final int FAIL = 4000;
    
    public static final int HTTP_EXCEPTION = 4004;

    public static final int BAD_REQUEST = 4100;

    public static final int NOT_AUTHORIZED = 4200;

    public static final int NOT_FOUND=4300;

    public static final int DATA_FAILED=4400;

    public static final int CREAT_FAILED=4500;

    public static final int GET_FAILED=4600;

    public static final int UPDATE_FAILED=4700;

    public static final int DELETE_FAILED=4800;

    public static final int ILLEGAL_DATE_FAILED=4900;

    
    public static final int LOGIN_SUCCESS = SUCCESS;
    public static final int LOGIN_BAD_REQUEST = BAD_REQUEST;
    public static final int SYSTEM_LOGIN_FAIL = 5001;// 登陆软件本系统失败
    
    public static final int NO_MORE_SUCCESS = 3001;// 没有更多十大了
    
    public static final int BBS_TOKEN_LOSE_EFFECTIVE = 7000;// bbs的token失效

    public static final int NOT_EXIST_USER = 9000;// 用户不存在
    public static final int ALREADY_FRIEND = 9001;// 已经是好友

    public static final int ARTICLE_IS_LATEST = 2030;// 服务器当前存储的收藏列表时最新的
    public static final int ARTICLE_NOT_EXIST = 4020;// 用户没有上传过帖子

    public static final int BOARD_IS_LATEST = 4040;// 版面无需更新

    // 检查版本号
    public static final int VERSION_IS_LATEST = 4050;
    public static final int VERSION_HAS_NEW = 2050;


    public static boolean isSuccess(int status) {
        if((SUCCESS <= status && status < FAIL)
                || status == 200) {
            return true;
        }else {
            return false;
        }
    }
}
