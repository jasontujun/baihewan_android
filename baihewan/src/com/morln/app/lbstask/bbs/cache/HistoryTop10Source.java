package com.morln.app.lbstask.bbs.cache;

import com.morln.app.lbstask.bbs.model.Top10ArticleBase;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.data.cache.XBaseAdapterIdDataSource;
import com.xengine.android.utils.XStringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 存储历史十大（ArticleBase）
 * Created by jasontujun.
 * Date: 12-2-25
 * Time: 下午8:05
 */
public class HistoryTop10Source extends XBaseAdapterIdDataSource<Top10ArticleBase> {

    /**
     * 根据帖子发布日期获取帖子
     * @param date
     * @return
     */
    public List<Top10ArticleBase> getBaseDate(String date) {
        List<Top10ArticleBase> resultList = new ArrayList<Top10ArticleBase>();
        for(int i =0; i<itemList.size(); i++) {
            Date lastTime = itemList.get(i).getLastTime();
            String d = XStringUtil.date2calendarStr(lastTime);
            if(d != null && d.equals(date)) {
                resultList.add(itemList.get(i));
            }
        }
        return resultList;
    }

    /**
     * 根据最新排名获取十大帖子。
     * @param rank 0~9
     * @return 如果有两个帖子是相同排名，则返回第一个。如果没有，则返回null
     */
    public Top10ArticleBase getBaseRank(int rank) {
        for (int i = 0; i < itemList.size(); i++) {
            if(itemList.get(i).getCurrentRank() == rank) {
                return itemList.get(i);
            }
        }
        return null;
    }

    /**
     * 获取所有十大帖子的日期列表。
     * @return
     */
    public List<String> getHistoryDateList() {
        List<String> resultList = new ArrayList<String>();
        for(int i = 0; i < itemList.size(); i++) {
            Date lastTime = itemList.get(i).getLastTime();
            String date = XStringUtil.date2calendarStr(lastTime);
            if(!resultList.contains(date)) {
                resultList.add(date);
            }
        }
        return resultList;
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_TOP10;
    }

    @Override
    public String getId(Top10ArticleBase item) {
        return item.createId();
    }
}
