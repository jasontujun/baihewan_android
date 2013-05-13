package com.morln.app.lbstask.cache;

import com.morln.app.lbstask.bbs.db.FriendTable;
import com.morln.app.lbstask.model.Friend;
import com.xengine.android.data.cache.XBaseAdapterIdUsernameDBDataSource;
import com.xengine.android.data.db.XDBTable;

/**
 * Created by jasontujun.
 * Date: 12-2-14
 * Time: 下午9:50
 */
public class UserFriendSource extends XBaseAdapterIdUsernameDBDataSource<Friend> {

    @Override
    public String getId(Friend item) {
        return item.getUserInfo().getUsername();
    }

    @Override
    public String getSourceName() {
        return SourceName.USER_FRIEND;
    }

    @Override
    public XDBTable<Friend> getDatabaseTable() {
        return new FriendTable();
    }

    @Override
    public void replace(int index, Friend newItem) {
        Friend oldItem = itemList.get(index);
        oldItem.setUserInfo(newItem.getUserInfo());
    }

    @Override
    public String getUsername(Friend item) {
        return item.getOwnerName();
    }
}
