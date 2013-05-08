package com.morln.app.lbstask.session.bean;

import com.morln.app.lbstask.bbs.model.ArticleBase;

/**
 * Created by fdp.
 * 用于存储个人收藏的帖子
 * Date: 12-7-16
 * Time: 下午7:46
 */
public class CollectionArticle {
        
    private String username;// 收藏者名称
    private String url;
    private String authorName;
    private String title;
    private String board;
    private int popularity;
    private int replyCount;
    private String date;
    private String comment;

    public CollectionArticle() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static ArticleBase toLocalBean(CollectionArticle sessionArticle) {
        ArticleBase localArticle = new ArticleBase();
        localArticle.setId(sessionArticle.getUrl());
        localArticle.setTitle(sessionArticle.getTitle());
        localArticle.setBoard(sessionArticle.getBoard());
        localArticle.setDate(sessionArticle.getDate());
        localArticle.setAuthorName(sessionArticle.getAuthorName());
        localArticle.setPopularity(sessionArticle.getPopularity());
        localArticle.setReplyCount(sessionArticle.getReplyCount());
        return localArticle;
    }

    public static CollectionArticle toSessionBean(ArticleBase localArticle) {
        CollectionArticle sessionArticle = new CollectionArticle();
        sessionArticle.setUrl(localArticle.getId());
        sessionArticle.setTitle(localArticle.getTitle());
        sessionArticle.setBoard(localArticle.getBoard());
        sessionArticle.setDate(localArticle.getDate());
        sessionArticle.setAuthorName(localArticle.getAuthorName());
        sessionArticle.setPopularity(localArticle.getPopularity());
        sessionArticle.setReplyCount(localArticle.getReplyCount());
        return sessionArticle;
    }
}
