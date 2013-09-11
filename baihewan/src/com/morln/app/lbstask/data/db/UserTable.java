package com.morln.app.lbstask.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.data.model.UserBase;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 上午8:35
 */
public class UserTable extends XBaseDBTable<UserBase> {
    @Override
    public void initiateColumns() {
        addColumn("name", XSQLiteDataType.TEXT, null);
        addColumn("gender", XSQLiteDataType.INTEGER, null);
        addColumn("friend_time_stamp", XSQLiteDataType.LONG, null);
        addColumn("collection_time_stamp", XSQLiteDataType.LONG, null);
    }

    @Override
    public String getName() {
        return "user_table";
    }

    @Override
    public ContentValues getContentValues(UserBase instance) {
        ContentValues values = new ContentValues();
        values.put("name", instance.getUsername());
        values.put("gender", instance.getGender());
        values.put("friend_time_stamp", instance.getFriendTimeStamp());
        values.put("collection_time_stamp", instance.getCollectionTimeStamp());
        return values;
    }

    @Override
    public UserBase getFilledInstance(Cursor cursor) {
        UserBase instance = new UserBase();
        instance.setUsername(cursor.getString(cursor.getColumnIndex("name")));
        instance.setGender(cursor.getInt(cursor.getColumnIndex("gender")));
        instance.setFriendTimeStamp(cursor.getLong(cursor.getColumnIndex("friend_time_stamp")));
        instance.setCollectionTimeStamp(cursor.getLong(cursor.getColumnIndex("collection_time_stamp")));
        return instance;
    }
}
