package com.morln.app.lbstask.bbs.cache;

import com.morln.app.lbstask.bbs.model.BbsUserBase;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.data.cache.XBaseAdapterIdDataSource;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 下午3:08
 */
public class BbsUserSource extends XBaseAdapterIdDataSource<BbsUserBase> {

    @Override
    public String getId(BbsUserBase item) {
        return item.getUsername();
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_USER_DATA;
    }
}
