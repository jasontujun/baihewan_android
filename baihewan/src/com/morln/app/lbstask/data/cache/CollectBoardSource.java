package com.morln.app.lbstask.data.cache;

import com.morln.app.lbstask.data.db.CollectedBoardTable;
import com.morln.app.lbstask.data.model.CollectedBoard;
import com.xengine.android.data.cache.XBaseAdapterIdUsernameDBDataSource;
import com.xengine.android.data.db.XDBTable;

/**
 * 订阅版面的数据源。
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 上午1:18
 */
public class CollectBoardSource extends XBaseAdapterIdUsernameDBDataSource<CollectedBoard> {

    @Override
    public String getId(CollectedBoard item) {
        return item.getBoardId();
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_COLLECTION_BOARD;
    }

    @Override
    public XDBTable<CollectedBoard> getDatabaseTable() {
        return new CollectedBoardTable();
    }

    @Override
    public String getUsername(CollectedBoard item) {
        return item.getUserName();
    }
}
