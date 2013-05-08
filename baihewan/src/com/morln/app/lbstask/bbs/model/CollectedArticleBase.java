package com.morln.app.lbstask.bbs.model;

/**
 * Created by jasontujun.
 * Date: 12-9-12
 * Time: 下午5:15
 */
public class CollectedArticleBase {
    private String userName;

    private ArticleBase article;

    public CollectedArticleBase() {
        article = new ArticleBase();
    }

    public CollectedArticleBase(String userName, ArticleBase article) {
        this.userName = userName;
        if(article == null) {
            this.article = new ArticleBase();
        }else {
            this.article = article;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArticleBase getArticle() {
        return article;
    }

    public void setArticle(ArticleBase article) {
        this.article = article;
    }

    public String getArticleId() {
        return article.getId();
    }

    public void setArticleId(String id) {
        article.setId(id);
    }

    public String getAuthorName() {
        return article.getAuthorName();
    }

    public void setAuthorName(String authorName) {
        article.setAuthorName(authorName);
    }

    public String getTitle() {
        return article.getTitle();
    }

    public void setTitle(String title) {
        article.setTitle(title);
    }

    public String getBoard() {
        return article.getBoard();
    }

    public void setBoard(String board) {
        article.setBoard(board);
    }

    public int getPopularity() {
        return article.getPopularity();
    }

    public void setPopularity(int popularity) {
        article.setPopularity(popularity);
    }

    public int getReplyCount() {
        return article.getReplyCount();
    }

    public void setReplyCount(int replyCount) {
        article.setReplyCount(replyCount);
    }

    public String getDate() {
        return article.getDate();
    }

    public void setDate(String date) {
        article.setDate(date);
    }
}
