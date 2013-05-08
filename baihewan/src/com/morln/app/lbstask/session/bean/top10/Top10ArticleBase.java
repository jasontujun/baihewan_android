package com.morln.app.lbstask.session.bean.top10;

import java.util.List;

/**
 * Created by Beryl.
 * Date: 12-3-3
 * Time: 下午7:45
 */
public class Top10ArticleBase {
    List<Integer> rankList;
    String title;
    String url;
    String authorName;
    String board;
    int replyCount;
    String no;
    long firstTime;
    long lastTime;

    public Top10ArticleBase() {
    }

    public Top10ArticleBase(List<Integer> rankList1, String title, String url, String authorName, String board, int replyCount, long firstTime, long lastTime) {
        this.setFirstTime(firstTime);
        this.setLastTime(lastTime);
        this.setReplyCount(replyCount);
        this.setBoard(board);
        this.setAuthorName(authorName);
        this.setUrl(url);
        this.setTitle(title);
        this.setRankList(rankList1);
    }

    public Top10ArticleBase(List<Integer> rankList1, String title, String url, String authorName, String board, String reply, long firstTime, long lastTime) {
        this.setFirstTime(firstTime);
        this.setLastTime(lastTime);
        this.setReplyCount(Integer.parseInt(reply));
        this.setBoard(board);
        this.setAuthorName(authorName);
        this.setUrl(url);
        this.setTitle(title);
        this.setRankList(rankList1);
    }


    @Override
    public String toString() {
        return "作者:" + authorName + "标题:" + title + "版面:" + board + "链接:" + url + "首次上十大时间:" + firstTime + "当前排名:" + rankList.get(rankList.size() - 1) + "跟帖数:" + replyCount + "/" + lastTime;
    }

    public List<Integer> getRankList() {
        return rankList;
    }

    public void setRankList(List<Integer> rankList) {
        this.rankList = rankList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public long getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(long firstTime) {
        this.firstTime = firstTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }
}
