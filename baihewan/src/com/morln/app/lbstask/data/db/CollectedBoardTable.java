package com.morln.app.lbstask.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.data.model.CollectedBoard;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 上午9:59
 */
public class CollectedBoardTable extends XBaseDBTable<CollectedBoard> {
    @Override
    public void initiateColumns() {
        addColumn("username", XSQLiteDataType.TEXT, null);// 收藏用户
        addColumn("board_id", XSQLiteDataType.TEXT, null);// 版面id
    }

    @Override
    public String getName() {
        return "collect_board";
    }

    @Override
    public ContentValues getContentValues(CollectedBoard instance) {
        ContentValues values = new ContentValues();
        values.put("username", instance.getUserName());
        values.put("board_id", instance.getBoardId());
        return values;
    }

    @Override
    public CollectedBoard getFilledInstance(Cursor cursor) {
        CollectedBoard instance = new CollectedBoard();
        instance.setUserName(cursor.getString(cursor.getColumnIndex("username")));
        instance.setBoardId(cursor.getString(cursor.getColumnIndex("board_id")));
        return instance;
    }
}
