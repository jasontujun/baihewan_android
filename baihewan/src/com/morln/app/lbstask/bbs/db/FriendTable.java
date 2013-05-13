package com.morln.app.lbstask.bbs.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.model.Friend;
import com.morln.app.lbstask.model.UserBase;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 上午8:35
 */
public class FriendTable extends XBaseDBTable<Friend> {
    @Override
    public void initiateColumns() {
        addColumn("name", XSQLiteDataType.TEXT, null);
        addColumn("nickname", XSQLiteDataType.TEXT, null);
        addColumn("custom_name", XSQLiteDataType.TEXT, null);
        addColumn("owner_name", XSQLiteDataType.TEXT, null);
        addColumn("gender", XSQLiteDataType.INTEGER, null);
        addColumn("exp", XSQLiteDataType.TEXT, null);
        addColumn("signature", XSQLiteDataType.TEXT, null);
        addColumn("email", XSQLiteDataType.TEXT, null);
        addColumn("qq", XSQLiteDataType.TEXT, null);
        addColumn("renren", XSQLiteDataType.TEXT, null);
        addColumn("micro_blog", XSQLiteDataType.TEXT, null);
    }

    @Override
    public String getName() {
        return "friend_table";
    }

    @Override
    public ContentValues getContentValues(Friend instance) {
        ContentValues values = new ContentValues();
        values.put("name", instance.getUserInfo().getUsername());
        values.put("nickname", instance.getUserInfo().getNickname());
        values.put("custom_name", instance.getCustomName());
        values.put("owner_name", instance.getOwnerName());
        values.put("gender", instance.getUserInfo().getGender());
        values.put("exp", instance.getUserInfo().getExp());
        values.put("signature", instance.getUserInfo().getSignature());
        values.put("email", instance.getUserInfo().getEmail());
        values.put("qq", instance.getUserInfo().getQqNum());
        values.put("renren", instance.getUserInfo().getRenren());
        values.put("micro_blog", instance.getUserInfo().getMicroBlog());
        return values;
    }

    @Override
    public Friend getFilledInstance(Cursor cursor) {
        Friend instance = new Friend();
        UserBase userInfo = new UserBase();
        userInfo.setUsername(cursor.getString(cursor.getColumnIndex("name")));
        userInfo.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));
        userInfo.setGender(cursor.getInt(cursor.getColumnIndex("gender")));
        userInfo.setExp(cursor.getString(cursor.getColumnIndex("exp")));
        userInfo.setSignature(cursor.getString(cursor.getColumnIndex("signature")));
        userInfo.setEmail(cursor.getString(cursor.getColumnIndex("email")));
        userInfo.setQqNum(cursor.getString(cursor.getColumnIndex("qq")));
        userInfo.setRenren(cursor.getString(cursor.getColumnIndex("renren")));
        userInfo.setMicroBlog(cursor.getString(cursor.getColumnIndex("micro_blog")));
        instance.setUserInfo(userInfo);
        instance.setCustomName(cursor.getString(cursor.getColumnIndex("custom_name")));
        instance.setOwnerName(cursor.getString(cursor.getColumnIndex("owner_name")));
        return instance;
    }
}
