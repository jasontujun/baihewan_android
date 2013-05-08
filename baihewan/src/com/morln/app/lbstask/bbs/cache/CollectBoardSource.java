package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterIdUsernameDBDataSource;
import com.morln.app.data.db.XDBTable;
import com.morln.app.lbstask.bbs.db.CollectedBoardTable;
import com.morln.app.lbstask.bbs.model.CollectedBoard;
import com.morln.app.lbstask.cache.SourceName;

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
