package com.morln.app.lbstask.bbs.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.bbs.model.Zone;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 上午10:44
 */
public class ZoneTable extends XBaseDBTable<Zone> {
    @Override
    public void initiateColumns() {
        addColumn("zone_name", XSQLiteDataType.TEXT, null);
        addColumn("zone_sec", XSQLiteDataType.INTEGER, null);
    }

    @Override
    public String getName() {
        return "zone_table";
    }

    @Override
    public ContentValues getContentValues(Zone instance) {
        ContentValues values = new ContentValues();
        values.put("zone_name", instance.getName());
        values.put("zone_sec", instance.getSec());
        return values;
    }

    @Override
    public Zone getFilledInstance(Cursor cursor) {
        // TODO 其他属性在外部添加
        Zone instance = new Zone(cursor.getString(cursor.getColumnIndex("zone_name")),
                cursor.getInt(cursor.getColumnIndex("zone_sec")));
        return instance;
    }
}
