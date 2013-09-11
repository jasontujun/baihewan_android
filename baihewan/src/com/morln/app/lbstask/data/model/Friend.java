package com.morln.app.lbstask.data.model;

import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-2-14
 * Time: 下午9:53
 */
public class Friend {
    /**
     * 好友基本信息
     */
    private UserBase userInfo;

    /**
     * 谁的好友
     */
    private String ownerName;

    /**
     * 备注名
     */
    private String customName;

    /**
     * 好友所属的组别
     */
    private List<String> groupIdList;

    public Friend(){}

    /**
     * 构造函数。通过通信bean类Friend构造此Friend类实例对象
     * @param f
     */
    public Friend(com.morln.app.lbstask.session.bean.friend.Friend f){
        userInfo = new UserBase();
        userInfo.setUsername(f.getFriendName());
        userInfo.setGender(f.getGender());
        ownerName = f.getOwnerName();// TODO
        customName = f.getCustomName();
    }

    public UserBase getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserBase userInfo) {
        this.userInfo = userInfo;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public List<String> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<String> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public com.morln.app.lbstask.session.bean.friend.Friend createSessionFriend(){
        if(userInfo == null){
            return null;
        }
        com.morln.app.lbstask.session.bean.friend.Friend f = new com.morln.app.lbstask.session.bean.friend.Friend();
        f.setFriendName(userInfo.getUsername());
        f.setOwnerName(ownerName);
        f.setCustomName(customName);
        f.setGender(userInfo.getGender());
        return f;
    }
}
