package com.morln.app.lbstask.data.cache;

import com.morln.app.lbstask.data.db.UserTable;
import com.morln.app.lbstask.data.model.UserBase;
import com.xengine.android.data.cache.XBaseAdapterIdDBDataSource;
import com.xengine.android.data.db.XDBTable;

/**
 * Created by jasontujun.
 * Date: 12-9-22
 * Time: 下午2:02
 */
public class SystemUserSource extends XBaseAdapterIdDBDataSource<UserBase> {
    @Override
    public String getSourceName() {
        return SourceName.SYSTEM_USER;
    }

    @Override
    public XDBTable<UserBase> getDatabaseTable() {
        return new UserTable();
    }

    @Override
    public String getId(UserBase item) {
        return item.getUsername();
    }
}
