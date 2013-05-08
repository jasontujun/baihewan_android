package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterIdDataSource;
import com.morln.app.lbstask.bbs.model.ArticleDetail;
import com.morln.app.lbstask.cache.SourceName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 帖子类（ArticleDetail）
 * TIP 以articleId和boardId来确定唯一的
 * Created by jasontujun.
 * Date: 12-2-20
 * Time: 下午10:21
 */
public class ArticleSource extends XBaseAdapterIdDataSource<ArticleDetail> {

    /**
     * 获取某帖子的跟帖列表。
     * TODO 还要把搜索结果按照楼层排序！
     * @param boardId
     * @param themeId
     * @return
     */
    public List<ArticleDetail> getArticleReplyList(String boardId, String themeId) {
        List<ArticleDetail> resultList = new ArrayList<ArticleDetail>();
        for(int i = 0; i < size(); i++){
            if(get(i).getType() == ArticleDetail.ARTICLE_REPLY) {
                if(get(i).getHostId().equals(themeId) &&
                        get(i).getBoard().equals(boardId)) {
                    resultList.add(get(i));
                }
            }
        }
        Collections.sort(resultList, ArticleDetail.getComparator());
        return resultList;
    }

    @Override
    public void replace(int index, ArticleDetail newItem) {
        ArticleDetail oldArticle = itemList.get(index);
        oldArticle.resetImg();
        itemList.set(index, newItem);
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_ARTICLE;
    }

    @Override
    public String getId(ArticleDetail item) {
        return item.createId();
    }
}
