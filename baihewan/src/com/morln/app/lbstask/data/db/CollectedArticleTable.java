package com.morln.app.lbstask.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.morln.app.lbstask.data.model.CollectedArticleBase;
import com.xengine.android.data.db.XBaseDBTable;
import com.xengine.android.data.db.XSQLiteDataType;

/**
 * 帖子收藏类（只能收藏主题帖）
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 上午10:00
 */
public class CollectedArticleTable extends XBaseDBTable<CollectedArticleBase> {
    @Override
    public void initiateColumns() {
        addColumn("username", XSQLiteDataType.TEXT, null);// 收藏者
        addColumn("article_id", XSQLiteDataType.TEXT, null);// 文章id
        addColumn("article_authorName", XSQLiteDataType.TEXT, null);// 文章
        addColumn("article_title", XSQLiteDataType.TEXT, null);// 文章标题
        addColumn("article_board", XSQLiteDataType.TEXT, null);// 文章所属版面
        addColumn("article_pop", XSQLiteDataType.INTEGER, null);// 文章人气
        addColumn("article_reply", XSQLiteDataType.INTEGER, null);// 文章回复数
        addColumn("article_date", XSQLiteDataType.TEXT, null);// 文章日期
    }

    @Override
    public String getName() {
        return "collect_article";
    }

    @Override
    public ContentValues getContentValues(CollectedArticleBase instance) {
        ContentValues values = new ContentValues();
        values.put("username", instance.getUserName());
        values.put("article_id", instance.getArticleId());
        values.put("article_authorName", instance.getAuthorName());
        values.put("article_title", instance.getTitle());
        values.put("article_board", instance.getBoard());
        values.put("article_pop", instance.getPopularity());
        values.put("article_reply", instance.getReplyCount());
        values.put("article_date", instance.getDate());
        return values;
    }

    @Override
    public CollectedArticleBase getFilledInstance(Cursor cursor) {
        CollectedArticleBase instance = new CollectedArticleBase();
        instance.setUserName(cursor.getString(cursor.getColumnIndex("username")));
        instance.setArticleId(cursor.getString(cursor.getColumnIndex("article_id")));
        instance.setAuthorName(cursor.getString(cursor.getColumnIndex("article_authorName")));
        instance.setTitle(cursor.getString(cursor.getColumnIndex("article_title")));
        instance.setBoard(cursor.getString(cursor.getColumnIndex("article_board")));
        instance.setPopularity(cursor.getInt(cursor.getColumnIndex("article_pop")));
        instance.setReplyCount(cursor.getInt(cursor.getColumnIndex("article_reply")));
        instance.setDate(cursor.getString(cursor.getColumnIndex("article_date")));
        return instance;
    }
}
