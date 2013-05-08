package com.morln.app.lbstask.session.bean.top10;

/**
 * Created by Beryl.
 * Date: 12-5-17
 * Time: 下午8:53
 */
public class Top10Content {
    String url;
    String board;
    String content;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public void copy(Top10Content top10Content){
        this.url = top10Content.url;
        this.content = top10Content.content;
        this.board = top10Content.board;
    }
}
