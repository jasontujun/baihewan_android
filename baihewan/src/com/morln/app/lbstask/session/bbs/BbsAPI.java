package com.morln.app.lbstask.session.bbs;

import com.morln.app.lbstask.data.model.*;
import com.morln.app.lbstask.data.model.Friend;

import java.io.File;
import java.util.List;

/**
 * 小百合的API层
 * Created by jasontujun.
 * Date: 12-4-16
 * Time: 上午11:57
 */
public class BbsAPI {
    private static boolean isNewAPI = true;

    /**
     * 登陆bbs
     * @param username
     * @param password
     * @return
     */
    public static int login(String username, String password) {
        if (isNewAPI)
            return BbsAPINew.login(username, password);
        else
            return BbsAPIOld.login(username, password);
    }

    /**
     * 注销登录
     * @return
     */
    public static int logout() {
        if (isNewAPI)
            return BbsAPINew.logout();
        else
            return BbsAPIOld.logout();
    }


    /**
     * 获取十大列表
     * @param top10List
     * @return
     */
    public static int getTop10FromWeb(List<Top10ArticleBase> top10List) {
        if (isNewAPI)
            return BbsAPINew.getTop10FromWeb(top10List);
        else
            return BbsAPIOld.getTop10FromWeb(top10List);
    }

    /**
     * 获取主题模式下的帖子详情
     * @param boardStr
     * @param articleIdStr
     * @param pageStr
     * @param articleDetailList 返回数据 ： 主贴 + 回帖
     * @return
     */
    public static int getThemeArticleFromWeb(String boardStr, String articleIdStr,
                                             int pageStr, List<ArticleDetail> articleDetailList) {
        if (isNewAPI)
            return BbsAPINew.getThemeArticleFromWeb(boardStr, articleIdStr,
                    pageStr, articleDetailList);
        else
            return BbsAPIOld.getThemeArticleFromWeb(boardStr, articleIdStr,
                    pageStr, articleDetailList);
    }


    /**
     * 结构化解析一般模式下的帖子内容
     * @param articleStr
     * @return
     */
    public static ArticleDetail parseNormalArticleContent(String articleStr) {
        if (isNewAPI)
            return BbsAPINew.parseNormalArticleContent(articleStr);
        else
            return BbsAPIOld.parseNormalArticleContent(articleStr);
    }

    /**
     * 发表文章
     * @param board
     * @param title
     * @param content
     * @param pid
     * @param reid
     * @return
     */
    public static int sendArticle(String board, String title, String content, String pid, String reid) {
        if (isNewAPI)
            return BbsAPINew.sendArticle(board, title, content, pid, reid);
        else
            return BbsAPIOld.sendArticle(board, title, content, pid, reid);
    }


    public static String getPid(String articleId, String board) {
        if (isNewAPI)
            return BbsAPINew.getPid(articleId, board);
        else
            return BbsAPIOld.getPid(articleId, board);
    }

    /**
     * 上传图片
     * @param imgFile
     * @return 返回图片在bbs上的有效url
     */
    public static String uploadImage(String board, File imgFile, String description) {
        if (isNewAPI)
            return BbsAPINew.uploadImage(board, imgFile, description);
        else
            return BbsAPIOld.uploadImage(board, imgFile, description);
    }

    /**
     * 获取各区热点
     * @param sec
     * @param articleBaseList
     * @return
     */
    public static int getZoneHotFromWeb(int sec, List<ArticleBase> articleBaseList){
        if (isNewAPI)
            return BbsAPINew.getZoneHotFromWeb(sec, articleBaseList);
        else
            return BbsAPIOld.getZoneHotFromWeb(sec, articleBaseList);
    }

    //----------------------------------------------讨论区
    public static int getThemeArticleListFromWeb(Board board, int page) {
        if (isNewAPI)
            return BbsAPINew.getThemeArticleListFromWeb(board, page);
        else
            return BbsAPIOld.getThemeArticleListFromWeb(board, page);
    }


    /**
     * 获取一般模式的帖子
     * @param board
     * @param page
     * @return
     */
    public static int getNormalArticleListFromWeb(Board board, int page) {
        if (isNewAPI)
            return BbsAPINew.getNormalArticleListFromWeb(board, page);
        else
            return BbsAPIOld.getNormalArticleListFromWeb(board, page);
    }

    /**
     * 删除帖子
     * @param board
     * @param articleId
     * @return
     */
    public static int deleteArticle(String board, String articleId) {
        if (isNewAPI)
            return BbsAPINew.deleteArticle(board, articleId);
        else
            return BbsAPIOld.deleteArticle(board, articleId);
    }


    /**
     * 搜索帖子
     * @param author
     * @param contain1
     * @param contain2
     * @param notcontain
     * @param startDay
     * @param endDay
     * @return
     */
    public static int searchArticle(String author, String contain1, String contain2,
                                    String notcontain, String startDay, String endDay,
                                    List<ArticleBase> resultList) {
        if (isNewAPI)
            return BbsAPINew.searchArticle(author, contain1, contain2,
                    notcontain, startDay, endDay, resultList);
        else
            return BbsAPIOld.searchArticle(author, contain1, contain2,
                    notcontain, startDay, endDay, resultList);
    }


    /**
     * 获取热门讨论区（抓取js）
     * @param hotBoardList
     * @return
     */
    public static int getHotBoard(List<Board> hotBoardList) {
        if (isNewAPI)
            return BbsAPINew.getHotBoard(hotBoardList);
        else
            return BbsAPIOld.getHotBoard(hotBoardList);
    }


    /**
     * 抓取订阅版面
     * @param orderBoardList
     * @return
     */
    public static int getRssBoard(List<String> orderBoardList) {
        if (isNewAPI)
            return BbsAPINew.getRssBoard(orderBoardList);
        else
            return BbsAPIOld.getRssBoard(orderBoardList);
    }


    /**
     * 同步订阅版面
     * @param boardList
     * @return
     */
    public static int sendRssBoard(List<String> boardList) {
        if (isNewAPI)
            return BbsAPINew.sendRssBoard(boardList);
        else
            return BbsAPIOld.sendRssBoard(boardList);
    }



    /**
     * 获取用户性别
     * @param username
     * @return 返回性别。如果出现异常，返回-1
     */
    public static int getSexOfUser(String username) {
        if (isNewAPI)
            return BbsAPINew.getSexOfUser(username);
        else
            return BbsAPIOld.getSexOfUser(username);
    }


    //去掉异常字符之后,个人信息提取的标识符
    private static final String NICKNAME_LABEL_START = " (";
    private static final String NICKNAME_LABEL_END = ") 共上站 ";
    private static final String LOGIN_NUM_END = " 次，发表文章 ";
    private static final String SHOW_PAPER_NUM_END = " 篇";
    private static final String STAR_START = "[";
    private static final String STAR_IDENTIFY = "座";
    private static final String STAR_UNKNOWN = "不详";
    private static final String LAST__LOGIN_TIME_START = "]上次在 [";
    private static final String LAST__LOGIN_TIME_END = "] 从 [";
    private static final String LAST_LOGIN_IP_END = "] 到本站一游。";
    private static final String MAIL_START = "信箱：[";
    private static final String MAIL_END = "]  经验值：";
    private static final String EXPERIENCE_BETWEEN_LEVEL = "(";
    private static final String LEVEL_END = ") 表现值：[";
    private static final String SHOW_VALUE_END = "](";
    private static final String SHOW_STATE_END = ") 生命力：[";
    private static final String HP_END = "]。";
    private static final String AFTER_HP_ALL_INFO_END = "目前";
    // 提取版主信息
    private static final String ROLE_TAG = "★";
    /**
     * 获取个人信息
     * @param username
     * @return
     */
    public static BbsUserBase getBbsUserInfoFromWeb(String username) {
        if (isNewAPI)
            return BbsAPINew.getBbsUserInfoFromWeb(username);
        else
            return BbsAPIOld.getBbsUserInfoFromWeb(username);
    }



    /**
     * 从网页抓取好友
     * @return
     */
    public static int getFriendsFromWeb(List<Friend> friendList, String ownerName) {
        if (isNewAPI)
            return BbsAPINew.getFriendsFromWeb(friendList, ownerName);
        else
            return BbsAPIOld.getFriendsFromWeb(friendList, ownerName);
    }


    /**
     * 添加好友
     * @param username
     * @param customName
     * @return
     */
    public static int addFriend(String username, String customName) {
        if (isNewAPI)
            return BbsAPINew.addFriend(username, customName);
        else
            return BbsAPIOld.addFriend(username, customName);
    }

    /**
     * 删除好友
     * @param username
     * @return
     */
    public static int deleteFriend(String username) {
        if (isNewAPI)
            return BbsAPINew.deleteFriend(username);
        else
            return BbsAPIOld.deleteFriend(username);
    }

    /**
     * 获取站内信列表（默认首页）
     * @param resultList
     * @return
     */
    public static int getDefaultMailList(List<Mail> resultList) {
        if (isNewAPI)
            return BbsAPINew.getDefaultMailList(resultList);
        else
            return BbsAPIOld.getDefaultMailList(resultList);
    }

    /**
     * 获取站内信列表（任意起始位置开始20个）
     * @param resultList
     * @param start
     * @return
     */
    public static int getMailList(List<Mail> resultList, int start) {
        if (isNewAPI)
            return BbsAPINew.getMailList(resultList, start);
        else
            return BbsAPIOld.getMailList(resultList, start);
    }

    /**
     * 获取邮件详情
     * @param mailUrl
     * @param num 站内信的索引号（从0开始计数）
     * @param result
     * @return
     */
    public static int getMailDetail(String mailUrl, int num, Mail result) {
        if (isNewAPI)
            return BbsAPINew.getMailDetail(mailUrl, num, result);
        else
            return BbsAPIOld.getMailDetail(mailUrl, num, result);
    }

    /**
     * 发送邮件
     * @param title
     * @param content
     * @param receiver
     * @return
     */
    public static int sendMail(String title, String content, String receiver) {
        if (isNewAPI)
            return BbsAPINew.sendMail(title, content, receiver);
        else
            return BbsAPIOld.sendMail(title, content, receiver);
    }

    /**
     * 删除邮件
     * @param mailId
     * @return
     */
    public static int deleteMail(String mailId) {
        if (isNewAPI)
            return BbsAPINew.deleteMail(mailId);
        else
            return BbsAPIOld.deleteMail(mailId);
    }

    /**
     * 获取bbs站点当前状态
     * @return
     */
    public static int getBbsStatus(BbsStatus bbsStatus) {
        if (isNewAPI)
            return BbsAPINew.getBbsStatus(bbsStatus);
        else
            return BbsAPIOld.getBbsStatus(bbsStatus);
    }
}
