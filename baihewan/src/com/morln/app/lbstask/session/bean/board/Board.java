package com.morln.app.lbstask.session.bean.board;

/**
 * Created by Beryl.
 * Date: 12-3-9
 * Time: 下午9:27
 */
public class Board {
    private String area;
    private String boardName;
    private String kind;
    private String chnDsp;
    private String webmaster;

    public Board() {
    }

    public Board(String area1, String boardName1, String kind1, String chnDsp1, String webmaster1) {
        this.area = area1;
        this.kind = kind1;
        this.boardName = boardName1;
        this.chnDsp = chnDsp1;
        this.webmaster = webmaster1;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getChnDsp() {
        return chnDsp;
    }

    public void setChnDsp(String chnDsp) {
        this.chnDsp = chnDsp;
    }

    public String getWebmaster() {
        return webmaster;
    }

    public void setWebmaster(String webmaster) {
        this.webmaster = webmaster;
    }

    public boolean isEqualTo(Board board) {
        boolean result = (this.getBoardName().equals(board.getBoardName())) && (this.getKind().equals(board.getKind()))
                && (this.getChnDsp().equals(board.getChnDsp())) && (this.getWebmaster().equals(board.getWebmaster()));
        return result;
    }

    public void copy(Board board) {
        this.setBoardName(board.getBoardName());
        this.setKind(board.getKind());
        this.setChnDsp(board.getChnDsp());
        this.setWebmaster(board.getWebmaster());
    }
}
