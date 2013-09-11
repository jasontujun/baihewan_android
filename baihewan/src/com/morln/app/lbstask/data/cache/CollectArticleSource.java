package com.morln.app.lbstask.data.cache;

import com.morln.app.lbstask.data.db.CollectedArticleTable;
import com.morln.app.lbstask.data.model.CollectedArticleBase;
import com.xengine.android.data.cache.XBaseAdapterIdUsernameDBDataSource;
import com.xengine.android.data.db.XDBTable;

/**
 * 收藏帖子类（只收藏ArticleBase）
 * Created by jasontujun.
 * Date: 12-3-6
 * Time: 下午4:09
 */
public class CollectArticleSource extends XBaseAdapterIdUsernameDBDataSource<CollectedArticleBase> {

    @Override
    public String getSourceName() {
        return SourceName.BBS_COLLECTION_ARTICLE;
    }

    @Override
    public String getId(CollectedArticleBase item) {
        return item.getArticleId();
    }

    @Override
    public XDBTable<CollectedArticleBase> getDatabaseTable() {
        return new CollectedArticleTable();
    }

    @Override
    public String getUsername(CollectedArticleBase item) {
        return item.getUserName();
    }
}
