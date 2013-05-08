package com.morln.app.lbstask.bbs.model;

import java.util.Comparator;

/**
 * Created by jasontujun.
 * Date: 12-2-20
 * Time: 下午10:10
 */
public class ArticleBase {
    /**
     * 帖子类型。主题帖=0，非主题帖=1
     */
    private int type;
    public static final int ARTICAL_THEME = 0;
    public static final int ARTICAL_REPLY = 1;

    /**
     * 帖子的id,即帖子M.1328612593.A这个东西
     */
    private String id;

    /**
     * 帖子在版块中的序号(置顶帖此项为空)
     */
    private int no;

    /**
     * 作者Id
     */
    private String authorName;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 版块
     */
    private String board;

    /**
     * 人气
     */
    private int popularity;

    /**
     * 跟帖数
     */
    private int replyCount;

    /**
     * 发帖时间
     */
    private String date;// TODO 需要把String转换为Date类比较好

    /**
     * 是否置顶
     */
    private boolean isUp;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    /**
     * 根据版面和帖子Id生成唯一Id
     * @return
     */
    public String createId() {
        return board + id;
    }

    /**
     * 根据版面和帖子Id生成唯一Id
     * @param boardId
     * @param articleId
     * @return
     */
    public static String createId(String boardId, String articleId) {
        return boardId + articleId;
    }


    /**
     * 版面帖子的排序规则。
     * 先判断是否是置顶；然后比较no,no大的排前面(没有no的帖子no=0）
     */
    public static Comparator<ArticleBase> getBoardComparator() {
        return new Comparator<ArticleBase>() {
            @Override
            public int compare(ArticleBase articleBase, ArticleBase articleBase1) {
                // 先判断是否置顶
                if(articleBase.isUp()) {
                    return -1;
                }
                if(articleBase1.isUp()) {
                    return 1;
                }
                // 然后根据no排序
                int no1 = articleBase.getNo();
                int no2 = articleBase1.getNo();
                if(no1 < no2) {
                    return 1;
                }else if(no1 > no2) {
                    return -1;
                }
                return 0;
            }
        };
    }
}
