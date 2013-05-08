package com.morln.app.lbstask.cache;

import com.morln.app.data.cache.XBaseAdapterIdDBDataSource;
import com.morln.app.data.db.XDBTable;
import com.morln.app.lbstask.bbs.db.UserTable;
import com.morln.app.lbstask.model.UserBase;

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
