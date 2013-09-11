package com.morln.app.lbstask.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.data.model.Board;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 上午10:44
 */
public class BoardTable extends XBaseDBTable<Board> {
    @Override
    public void initiateColumns() {
        addColumn("board_id", XSQLiteDataType.TEXT, null);
        addColumn("board_chinese", XSQLiteDataType.TEXT, null);
        addColumn("board_type", XSQLiteDataType.TEXT, null);
        addColumn("board_owner", XSQLiteDataType.TEXT, null);
        addColumn("board_zone", XSQLiteDataType.TEXT, null);
    }

    @Override
    public String getName() {
        return "board_table";
    }

    @Override
    public ContentValues getContentValues(Board instance) {
        ContentValues values = new ContentValues();
        values.put("board_id", instance.getBoardId());
        values.put("board_chinese", instance.getChinesName());
        values.put("board_type", instance.getBoardType());
        values.put("board_owner", instance.getBoardOwnerId());
        values.put("board_zone", instance.getZoneBelong());
        return values;
    }

    @Override
    public Board getFilledInstance(Cursor cursor) {
        Board instance = new Board();
        instance.setBoardId(cursor.getString(cursor.getColumnIndex("board_id")));
        instance.setChinesName(cursor.getString(cursor.getColumnIndex("board_chinese")));
        instance.setBoardType(cursor.getString(cursor.getColumnIndex("board_type")));
        instance.setBoardOwnerId(cursor.getString(cursor.getColumnIndex("board_owner")));
        instance.setZoneBelong(cursor.getString(cursor.getColumnIndex("board_zone")));
        return instance;
    }
}
