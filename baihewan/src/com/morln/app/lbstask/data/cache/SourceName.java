package com.morln.app.lbstask.data.cache;

/**
 * Created by jasontujun.
 * Date: 12-4-9
 * Time: 下午9:43
 */
public interface SourceName {
    /**
     * 全局状态数据源
     */
    public static final String GLOBAL_STATE = "global";

    /**
     * 图片本地sd卡缓存数据源
     */
    public static final String IMAGE = "image";

    /**
     * 系统设置数据源
     */
    public static final String SYSTEM_SETTING = "systemSetting";

    /**
     * 系统用户
     */
    public static final String SYSTEM_USER = "systemUser";

    /**
     * 用户数据的数据源
     */
    public static final String USER_DATA = "userData";

    /**
     * 用户的好友数据源
     */
    public static final String USER_FRIEND = "userFriend";

    /**
     * 用户的邮件数据源
     */
    public static final String USER_MAIL = "userMail";

    /**
     * 小百合10大
     */
    public static final String BBS_TOP10 = "top10";

    /**
     * 小百合各区热点
     */
    public static final String BBS_ZONE_HOT = "zoneHot";

    /**
     * 今日热门讨论区
     */
    public static final String BBS_TODAY_HOT_BOARD = "hotBoard";

    /**
     * 小百合区块
     */
    public static final String BBS_ZONE = "zone";

    /**
     * 小百合版块
     */
    public static final String BBS_BOARD = "board";

    /**
     * 小百合帖子缓存
     */
    public static final String BBS_ARTICLE = "article";

    /**
     * 小百合定制版面
     */
    public static final String BBS_COLLECTION_BOARD = "collectedBoard";

    /**
     * 小百合本地收藏帖子
     */
    public static final String BBS_COLLECTION_ARTICLE = "collectedArticle";

    /**
     * 小百合用户信息
     */
    public static final String BBS_USER_DATA = "bbsUserData";

    /**
     * 小百合用户帖子
     */
    public static final String BBS_USER_ARTICLE = "bbsUserArticle";

}
