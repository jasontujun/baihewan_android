package com.morln.app.lbstask.data.model;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 十大帖子类。
 * Created by jasontujun.
 * Date: 12-2-26
 * Time: 上午10:06
 */
public class Top10ArticleBase extends ArticleBase {

    private Date firstTime;
    private Date lastTime;
    private List<Integer> rankList;// 排名从1~10

    public Top10ArticleBase() {
    }

    public Top10ArticleBase(List<Integer> rankList1, String title, String url, String authorName,
                            String board, String reply, Date firstTime, Date lastTime) {
        this.setFirstTime(firstTime);
        this.setLastTime(lastTime);
        this.setReplyCount(Integer.parseInt(reply));
        this.setBoard(board);
        this.setAuthorName(authorName);
        this.setId(url);
        this.setTitle(title);
        this.setRankList(rankList1);
    }

    public void setRankList(List<Integer> rankList) {
        this.rankList = rankList;
    }

    public void addRank(int rank){
        if(rankList != null){
            rankList.add(rank);
        }
    }

    /**
     * 获取此十大帖子当前排名
     * @return
     */
    public int getCurrentRank() {
        if (rankList != null || rankList.size() != 0) {
            return rankList.get(rankList.size() - 1);
        } else {
            return -1;
        }
    }

    /**
     * 获取此十大的最高排名
     *
     * @return
     */
    public int getHighestRank() {
        if (rankList != null || rankList.size() != 0) {
            int result = rankList.get(0);
            for (int i = 1; i < rankList.size(); i++) {
                if (rankList.get(i) < result) {
                    result = rankList.get(i);
                }
            }
            return result;
        } else {
            return -1;
        }
    }

    public Date getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Date firstTime1) {
        this.firstTime = firstTime1;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime1) {
        this.lastTime = lastTime1;
        // TODO 时间的字符串格式问题
        setDate(lastTime1.toLocaleString());
    }

    public static Comparator<Top10ArticleBase> getTopComparator(){
        return new TopComparator();
    }

    /**
     * 十大帖子的比较规则.
     * 排名小的靠前，或者退出十大比较晚的靠前
     */
    private static class TopComparator implements Comparator<Top10ArticleBase> {
        @Override
        public int compare(Top10ArticleBase top10ArticleBase, Top10ArticleBase top10ArticleBase1) {
            // 先比较当前排名(-1认为是没有排名的)
            int rank1 = top10ArticleBase.getCurrentRank();
            int rank2 = top10ArticleBase1.getCurrentRank();
            if(rank1 < rank2){
                if(rank1 == -1){
                    return 1;
                }else {
                    return -1;
                }
            }else if(rank1 > rank2){
                if(rank2 == -1){
                    return -1;
                }else {
                    return 1;
                }
            }

            // 再比较最后下十大的时间
            long time1 =0;
            if(top10ArticleBase.getLastTime()!=null){
                top10ArticleBase.getLastTime().getTime();
            }
            long time2 = 0;
            if(top10ArticleBase1.getLastTime()!=null){
                top10ArticleBase1.getLastTime().getTime();
            }
            if(time1 < time2){
                return 1;
            }else if(time1 > time2){
                return -1;
            }

            return 0;
        }
    }
}
