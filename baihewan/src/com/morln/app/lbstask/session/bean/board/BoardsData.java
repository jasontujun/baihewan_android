package com.morln.app.lbstask.session.bean.board;

import java.util.List;

/**
 * Created by Beryl.
 * Date: 12-3-9
 * Time: 下午9:24
 */
public class BoardsData {
    private List<String> areaList;
    private List<Board> boardList;

    public List<String> getAreaList() {
        return areaList;
    }

    public void setAreaList(List<String> areaList) {
        this.areaList = areaList;
    }

    public List<Board> getBoardList() {
        return boardList;
    }

    public void setBoardList(List<Board> boardList) {
        this.boardList = boardList;
    }

    public BoardsData() {
    }

    public BoardsData(List<Board> boardList1, List<String> areaList1) {
        this.boardList = boardList1;
        this.areaList = areaList1;
    }
}
