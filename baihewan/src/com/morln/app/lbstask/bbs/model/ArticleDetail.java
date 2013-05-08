package com.morln.app.lbstask.bbs.model;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.utils.img.ImageUrlType;
import com.morln.app.utils.XStringUtil;

import java.util.Comparator;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-2-20
 * Time: 下午9:43
 *
 * TODO 帖子类。存储bbs帖子的最基本的数据结构。主题帖和回帖都属于帖子
 */
public class ArticleDetail {
    /**
     * 帖子类型。主题帖=0，非主题帖=1
     */
    private int type;
    public static final int ARTICLE_THEME = 0;
    public static final int ARTICLE_REPLY = 1;

    /**
     * 是否被删除
     */
    private boolean isDeleted;

    /**
     * 帖子的id,即帖子M.1328612593.A这个东西
     */
    private String id;

    /**
     * 此帖子所属的主帖子的id。（跟帖才有，主题帖为null）
     */
    private String hostId;

    /**
     * 作者Id
     */
    private String authorName;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 版块
     */
    private String board;

    /**
     * 人气
     */
    private int popularity;

    /**
     * 回帖数(主题帖才有)
     */
    private int replyCount;

    /**
     * 帖子所属楼层（跟帖才有，主题帖为0）
     */
    private int floorCount;

    /**
     * 回帖url
     */
    private String replyLink;

    /**
     * 发帖Ip
     */
    private String ip;

    /**
     * 发帖时间
     */
    private String date;// TODO 把String转换为Date类比较好

    /**
     * 帖子标题
     */
    private String title;

    /**
     * TIP 文字块。注意文字和图片一定是交叉排版……文字先显示。
     */
    private List<String> wordBlocks;

    /**
     * TIP 图片链接。注意文字和图片一定是交叉排版……文字先显示。
     */
    private List<String> imgUrls;


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

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getWordBlocks() {
        return wordBlocks;
    }

    public int getWordBlockSize() {
        if(wordBlocks == null)
            return 0;
        return wordBlocks.size();
    }

    public void setWordBlocks(List<String> wordBlocks) {
        this.wordBlocks = wordBlocks;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public int getImgSize() {
        if(imgUrls == null)
            return 0;
        return imgUrls.size();
    }

    /**
     * 设置图片url。并在设置的同时添加空的图片对象。
     * @param imgUrls
     */
    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
    }


    /**
     * 还原图片
     */
    public void resetImg() {
        ImageSource imageSource = (ImageSource) DataRepo.getInstance().getSource(SourceName.IMAGE);
        for(int i = 0; i<imgUrls.size(); i++) {
            String imageUrl = imgUrls.get(i);
            String localImageFile = imageSource.getLocalImage(imageUrl);
            if(!XStringUtil.isNullOrEmpty(localImageFile) &&
                    (localImageFile.equals(ImageUrlType.IMG_LOADING)
                            || localImageFile.equals(ImageUrlType.IMG_ERROR))) {
                imageSource.putImage(imageUrl, "");
            }
        }
    }


    public String getReplyLink() {
        return replyLink;
    }

    public void setReplyLink(String replyLink) {
        this.replyLink = replyLink;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public ArticleBase createArticleBase() {
        ArticleBase result = new ArticleBase();
        result.setId(id);
        result.setAuthorName(authorName);
        result.setBoard(board);
        result.setTitle(title);
        result.setDate(date);
        result.setPopularity(popularity);
        result.setReplyCount(replyCount);
        return result;
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
     * 帖子按楼层从小到大的排序规则。
     */
    public static Comparator<ArticleDetail> getComparator() {
        return new Comparator<ArticleDetail>() {
            @Override
            public int compare(ArticleDetail articleDetail, ArticleDetail articleDetail1) {
                int replyFloor1 = articleDetail.getFloorCount();
                int replyFloor2 = articleDetail1.getFloorCount();
                return replyFloor1 - replyFloor2;
            }
        };
    }
}
