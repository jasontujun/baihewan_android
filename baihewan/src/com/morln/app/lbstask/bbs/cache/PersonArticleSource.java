package com.morln.app.lbstask.bbs.cache;

import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.data.cache.XBaseAdapterIdUsernameDataSource;

/**
 * Created by jasontujun.
 * Date: 12-9-15
 * Time: 下午7:31
 */
public class PersonArticleSource extends XBaseAdapterIdUsernameDataSource<ArticleBase> {
    @Override
    public String getSourceName() {
        return SourceName.BBS_USER_ARTICLE;
    }

    @Override
    public String getId(ArticleBase item) {
        return item.createId();
    }

    @Override
    public String getUsername(ArticleBase item) {
        return item.getAuthorName();
    }
}
