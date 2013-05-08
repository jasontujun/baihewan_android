package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterIdDataSource;
import com.morln.app.lbstask.bbs.model.BbsUserBase;
import com.morln.app.lbstask.cache.SourceName;

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
