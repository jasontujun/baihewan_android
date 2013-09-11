package com.morln.app.lbstask.data.model;

import com.morln.app.lbstask.R;
import com.morln.app.lbstask.res.BbsPic;

import java.util.ArrayList;
import java.util.List;

/**
 * 区（包括个人收藏区、今日热门区、首页推荐区和讨论区）
 * Created by jasontujun.
 * Date: 12-3-1
 * Time: 上午12:35
 */
public class Zone {
    private String name;

    private int sec;
    
    private List<Board> boardList = new ArrayList<Board>();

    /**
     * 颜色（界面相关）
     * 用于字体和装饰符
     */
    private int colorRes = R.color.light_green;

    /**
     * 图标名（界面相关）
     * 默认图标为boardIconDefault.png
     */
    private String iconFileName = BbsPic.ICON_DEFAULT;

    /**
     * 跳转箭头按钮
     */
    private String btnImgFileName = BbsPic.GOTO_BTN_GREEN;
    
    public Zone(String name, int sec) {
        this.name = name;
        this.sec = sec;
    }

    /**
     * 不重复添加！
     * @param board
     */
    public void addBoard(Board board) {
        for(int i = 0; i<boardList.size(); i++) {
            if(boardList.get(i).getBoardId().equals(board.getBoardId())) {
                return;
            }
        }
        boardList.add(board);
    }

    /**
     * 根据id删除版面
     * @param boardId
     */
    public void deleteBoard(String boardId) {
        for(int i = 0; i<boardList.size(); i++) {
            if(boardList.get(i).getBoardId().equals(boardId)) {
                boardList.remove(i);
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public List<Board> getBoardList() {
        return boardList;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }

    public String getBtnImgFileName() {
        return btnImgFileName;
    }

    public void setBtnImgFileName(String btnImgFileName) {
        this.btnImgFileName = btnImgFileName;
    }


    /**
     * 颜色变换矩阵（变成灰色）
     * 相对于默认图片的颜色偏移量
     */
    private static float[] grayColorMatrix = new float[] {
            0, 0, 0, 0, 235,
            0, 0, 0, 0, 235,
            0, 0, 0, 0, 235,
            0, 0, 0, 1, 0
    };

    public static float[] getGrayColorMatrix() {
        return grayColorMatrix;
    }
}
