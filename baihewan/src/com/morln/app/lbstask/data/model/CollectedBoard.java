package com.morln.app.lbstask.data.model;

/**
 * Created by jasontujun.
 * Date: 12-9-12
 * Time: 下午4:58
 */
public class CollectedBoard {

    private String userName;

    private String boardId;

    public CollectedBoard() {
    }

    public CollectedBoard(String userName, String boardId) {
        this.userName = userName;
        this.boardId = boardId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }
}
