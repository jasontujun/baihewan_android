package com.morln.app.lbstask.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by jasontujun.
 * Date: 12-2-25
 * Time: 下午8:07
 */
public class Board {
    /**
     * 版块Id
     */
    private String boardId;

    /**
     * 版块中文名
     */
    private String chinesName;

    /**
     * 版块类别
     */
    private String boardType;

    /**
     * 版主
     */
    private String boardOwnerId;

    /**
     * 所属讨论区
     */
    private String zoneBelong;

    /**
     * 在线人数
     */
    private int personNum;

    /**
     * TIP 此版块的所有非置顶帖
     */
    private List<ArticleBase> normalArticleList;

    /**
     * TIP 此版块所有置顶帖
     */
    private List<ArticleBase> upArticleList;

    /**
     * 版面每一页的帖子数量
     */
    public static final int PAGE_SIZE = 20;

    public Board() {
        normalArticleList = new ArrayList<ArticleBase>();
        upArticleList = new ArrayList<ArticleBase>();
    }

    /**
     * 把通信用的Board转成客户端所用的Board数据类型
     * @param b
     */
    public Board(com.morln.app.lbstask.session.bean.board.Board b) {
        this.normalArticleList = new ArrayList<ArticleBase>();
        this.upArticleList = new ArrayList<ArticleBase>();
        this.boardId = b.getBoardName();
        this.chinesName = b.getChnDsp();
        this.boardType = b.getKind();
        this.boardOwnerId = b.getWebmaster();
        this.zoneBelong = b.getArea();
    }

    /**
     * 该构造函数用于创建默认版面
     * @param boardId
     * @param chinesName
     * @param boardType
     * @param boardOwnerId
     * @param zoneBelong
     */
    public Board(String boardId, String chinesName, String boardType,
                 String boardOwnerId, String zoneBelong) {
        this.normalArticleList = new ArrayList<ArticleBase>();
        this.upArticleList = new ArrayList<ArticleBase>();
        this.boardId = boardId;
        this.chinesName = chinesName;
        this.boardType = boardType;
        this.boardOwnerId = boardOwnerId;
        this.zoneBelong = zoneBelong;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getChinesName() {
        return chinesName;
    }

    public void setChinesName(String chinesName) {
        this.chinesName = chinesName;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public String getBoardOwnerId() {
        return boardOwnerId;
    }

    public void setBoardOwnerId(String boardOwnerId) {
        this.boardOwnerId = boardOwnerId;
    }

    public String getZoneBelong() {
        return zoneBelong;
    }

    public void setZoneBelong(String zoneBelong) {
        this.zoneBelong = zoneBelong;
    }

    public int getPersonNum() {
        return personNum;
    }

    public void setPersonNum(int personNum) {
        this.personNum = personNum;
    }

    /**
     * 获取所有的非置顶帖子
     * @return
     */
    public List<ArticleBase> getNormalArticleList() {
        return normalArticleList;
    }

    /**
     * 获取版块中最早的主题帖。即序号最小的
     * @return
     */
    public int getBoardEarliestNo() {
        if(normalArticleList == null || normalArticleList.size() == 0) {
            return -1;
        }
        // 先筛选出有no的帖子
        ArrayList<Integer> noList = new ArrayList<Integer>();
        for(int i=0; i<normalArticleList.size();i++) {
            if(normalArticleList.get(i).getNo() > 0) {
                noList.add(normalArticleList.get(i).getNo());
            }
        }
        if(normalArticleList == null || normalArticleList.size() == 0) {
            return -1;
        }
        int no = noList.get(0);
        for(int i=0; i<noList.size();i++) {
            if(noList.get(i) < no) {
                no = noList.get(i);
            }
        }
        return no;
    }

    /**
     * 获取版块中最近的主题帖。即序号最大的
     * @return
     */
    public int getBoardLatestNo() {
        if(normalArticleList == null || normalArticleList.size() == 0) {
            return -1;
        }
        // 先筛选出有no的帖子
        ArrayList<Integer> noList = new ArrayList<Integer>();
        for(int i=0; i<normalArticleList.size();i++) {
            if(normalArticleList.get(i).getNo() > 0) {
                noList.add(normalArticleList.get(i).getNo());
            }
        }
        if(normalArticleList == null || normalArticleList.size() == 0) {
            return -1;
        }
        int no = noList.get(0);
        for(int i=0; i<noList.size();i++) {
            if(noList.get(i) > no) {
                no = noList.get(i);
            }
        }
        return no;
    }

    /**
     * 根据页码返回本页本版面的帖子列表
     * @param page 页数（从0开始计数，每页20个，首页再加上置顶帖。如果page=-1，则返回此版面所有帖子）
     * @param isTheme 是否是主题模式？
     * @return
     */
    public List<ArticleBase> getArticleList(int page, boolean isTheme) {

        List<ArticleBase> result = new ArrayList<ArticleBase>();
        // 返回所有帖子（置顶+非置顶）
        if(page == -1) {
            for(int i = 0; i<upArticleList.size(); i++) {
                result.add(upArticleList.get(i));
            }
            for(int i = 0; i<normalArticleList.size(); i++) {
                result.add(normalArticleList.get(i));
            }
        // 返回某一页帖子（首页或非首页）
        }else {
            // 如果首页，加置顶帖
            if(page == 0) {
                for(int i = 0; i<upArticleList.size(); i++) {
                    result.add(upArticleList.get(i));
                }
            }
            // 添加非置顶帖的20个
            for(int i = 0; i<PAGE_SIZE; i++) {
                int aIndex = i+page*PAGE_SIZE;
                if(aIndex < normalArticleList.size()) {
                    result.add(normalArticleList.get(aIndex));
                }
            }
        }
        return result;
    }

    public List<ArticleBase> getAllArticle() {
        List<ArticleBase> resultList = new ArrayList<ArticleBase>();
        resultList.addAll(normalArticleList);
        resultList.addAll(upArticleList);
        return resultList;
    }

    /**
     * 添加此版块的帖子（主题帖）
     * 不重复添加！
     * @param article
     */
    public void addArticleBase(ArticleBase article) {
        if(article == null) {
            return;
        }

        int i = indexOf(article);
        if(article.isUp()) {
            if(i == -1) {
                upArticleList.add(article);
            }else {
                upArticleList.remove(i);
                upArticleList.add(i, article);
            }
        }else {
            if(i == -1) {
                normalArticleList.add(article);
            }else {
                normalArticleList.remove(i);
                normalArticleList.add(i, article);
            }
        }
    }

    /**
     * 删除帖子
     * @param article
     */
    public void deleteArticleBase(ArticleBase article) {
        if(article == null) {
            return;
        }

        if(article.isUp()) {
            upArticleList.remove(article);
        }else {
            normalArticleList.remove(article);
        }
    }

    /**
     * 判断在此贴在非置顶帖列表或置顶帖中的位置
     * @param article
     * @return
     */
    private int indexOf(ArticleBase article) {
        if(article.isUp()) {
            for(int i = 0; i < upArticleList.size(); i++) {
                if(upArticleList.get(i).getId().equals(article.getId())
                        && boardId.equals(article.getBoard())) {
                    return i;
                }
            }
        }else {
            for(int i = 0; i < normalArticleList.size(); i++) {
                if(normalArticleList.get(i).getId().equals(article.getId())
                        && boardId.equals(article.getBoard())) {
                    return i;
                }
            }
        }
        return -1;
    }
}
