package com.morln.app.lbstask.session.bbs;

import android.text.TextUtils;

/**
 * Created by jasontujun.
 * Date: 12-3-2
 * Time: 下午8:44
 */
public class BbsUrlUtil {
    public static final String bbsHost = "http://bbs.nju.edu.cn/";
    public static final String bbsDomain = ".nju.edu.cn";
    public static final String bbsPath = "/";

    /**
     * 获取登陆url
     * @param code
     * @param username
     * @param password
     * @return
     */
    public static String loginUrl(int code, String username, String password){
        return bbsHost + "vd" + String.valueOf(code) + "/bbslogin?type=2&id=" + username + "&pw=" + password;
    }

    public static String logoutUrl(String code){
        return bbsHost + "vd"+code+"/bbslogout";
    }

    /**
     * 获取十大url
     * @param bbsCode bbs登陆序号
     * @return
     */
    public static String getTop10Url(String bbsCode){
        if (TextUtils.isEmpty(bbsCode)){
            return bbsHost + "bbstop10";
        } else {
            return bbsHost + "vd" + bbsCode + "/bbstop10";
        }
    }

    /**
     * 获取版面帖子列表url
     * @param boardName 版块名
     * @param no        开始序号
     * @param isTheme   是否主题模式
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getBoardArticleListUrl(String boardName, int no,
                                                boolean isTheme, String bbsCode) {
        if (TextUtils.isEmpty(bbsCode)) {
            if (isTheme)
                return bbsHost + "bbstdoc?board="+boardName+"&start="+no+"&type=doc";
            else
                return bbsHost + "bbsdoc?board="+boardName+"&start="+no+"&type=doc";
        } else {
            if (isTheme)
                return bbsHost + "vd" + bbsCode + "/bbstdoc?board="+boardName+"&start="+no+"&type=doc";
            else
                return bbsHost + "vd" + bbsCode + "/bbsdoc?board="+boardName+"&start="+no+"&type=doc";
        }
    }

    /**
     * 获取版面首页的帖子列表url
     * @param boardName 版块名
     * @param isTheme   是否主题模式
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getBoardFirstPageUrl(String boardName,
                                              boolean isTheme, String bbsCode) {
        if (TextUtils.isEmpty(bbsCode)) {
            if(isTheme)
                return bbsHost + "bbstdoc?board="+boardName+"&start=999999&type=doc";
            else
                return bbsHost + "bbsdoc?board="+boardName+"&start=999999&type=doc";
        } else {
            if(isTheme)
                return bbsHost + "vd" + bbsCode + "/bbstdoc?board="+boardName+"&start=999999&type=doc";
            else
                return bbsHost + "vd" + bbsCode + "/bbsdoc?board="+boardName+"&start=999999&type=doc";
        }
    }

    /**
     * 获取主题模式下某帖子的详细信息
     * @param board
     * @param articleId
     * @param page
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getThemeArticleDetailUrl(String board, String articleId,
                                                  int page, String bbsCode){
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbstcon?board="+board
                    +"&file="+articleId+"&start="+page;
        else
            return bbsHost + "vd" + bbsCode + "/bbstcon?board="+board
                    +"&file="+articleId+"&start="+page;
    }

    /**
     * 获取一般模式下帖子的详细信息
     * @param board
     * @param articleId
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getNormalArticleDetailUrl(String board,
                                                   String articleId, String bbsCode){
        // TIP 小技巧，num的值能获取最新的一般模式下的帖子数据
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbscon?board="+board
                    +"&file="+articleId+"&num=999999";
        else
            return bbsHost + "vd" + bbsCode + "/bbscon?board="+board
                    +"&file="+articleId+"&num=999999";
    }


    public static String deleteArticleUrl(String code, String board, String articleId) {
        return bbsHost + "vd" + code + "/bbsdel?board=" + board + "&file=" + articleId;
    }


    /**
     * 获取发送帖子的url
     * @param code
     * @param board
     * @return
     */
    public static String sendArticleUrl(String code, String board) {
        return bbsHost + "vd" + code + "/bbssnd?board=" + board;
    }

    /**
     * 搜索文章url
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String searchArticleUrl(String bbsCode) {
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbsfind";
        else
            return bbsHost + "vd" + bbsCode + "/bbsfind";
    }

    /**
     * 回复文章时候需要获取pid参数的url
     * @param code
     * @param board
     * @param id
     * @return
     */
    public static String getPidUrl(String code, String board, String id) {
        return bbsHost + "vd" + code + "/bbspst?board=" + board + "&file=" + id;
    }

    /**
     * 获取用户个人信息的url
     * @param username
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getBbsUserInfoUrl(String username, String bbsCode) {
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbsqry?userid=" + username;
        else
            return bbsHost + "vd" + bbsCode + "/bbsqry?userid=" + username;
    }

    /**
     * 获取各区热点的url
     * @param sec
     * @param bbsCode   bbs登陆序号
     * @return
     */
    public static String getZoneHotUrl(int sec, String bbsCode) {
        if(sec < 0){
            sec = 0;
        }
        if(sec > 11){
            sec = 11;
        }
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbstop10s?sec=" + sec;
        else
            return bbsHost + "vd" + bbsCode + "/bbstop10s?sec=" + sec;
    }

    public static String getHotBoardUrl(){
        return bbsHost + "cache/t_hotbrd.js";
    }

    public static String getRssBoardUrl(String code){
        return bbsHost + "vd" + code + "/bbsleft";
    }

    public static String syncRssBoardUrl(String code){
        return bbsHost + "vd" + code + "/bbsmybrd?type=1&confirm1=1";
    }

    public static String getUploadUrl(String code) {
        return bbsHost + "vd" + code + "/bbsdoupload";
    }

    public static String getBbsImageLocationUrl(String code, String board, String file,
                                                String name, String exp, String ptext) {
        return bbsHost + "vd" + code + "/bbsupload2?board=" + board + "&file="
                + file + "&name=" + name + "&exp=" + exp + "&ptext=" + ptext;
    }

    public static String getFriendUrl(String code) {
        return bbsHost + "vd" + code + "/bbsfall";
    }

    public static String addFriendUrl(String code, String username, String customName) {
        return bbsHost + "vd" + code + "/bbsfadd?userid=" + username + "&exp=" + customName;
    }

    public static String deleteFriendUrl(String code, String username) {
        return bbsHost + "vd" + code + "/bbsfdel?userid=" + username;
    }

    public static String getBbsMailListUrl(String code) {
        return bbsHost + "vd" + code + "/bbsmail";
    }

    public static String getBbsMailListUrl(String code, int start) {
        return bbsHost + "vd" + code + "/bbsmail?start=" + start;
    }

    public static String getBbsMailDetailUrl(String code, String url, int num) {
        return bbsHost + "vd" + code + "/bbsmailcon?file=" + url + "&num=" + num;
    }

    public static String sendBbsMailUrl(String code,String fromUser){
        return bbsHost + "vd" + code + "/bbssndmail?pid=0&userid="+fromUser;
    }

    public static String getBbsDeleteMailUrl(String code, String file){
        return bbsHost + "vd" + code +"/bbsdelmail?file="+file;
    }

    public static String getBbsStatusUrl(String bbsCode){
        if (TextUtils.isEmpty(bbsCode))
            return bbsHost + "bbsfoot";
        else
            return bbsHost + "vd" + bbsCode + "/bbsfoot";
    }
}
