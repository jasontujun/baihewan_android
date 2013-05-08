package com.morln.app.lbstask.session.bean.friend;

/**
 * Created by Beryl.
 * Date: 12-3-3
 * Time: 下午7:23
 */
public class Friend {

    /** 来自FriendRelation对象的信息 */
    /**
     * 好友用户名
     */
    private String friendName;

    /**
     * 表明是谁的好友
     */
    private String ownerName;

    /**
     * 好友的备注名称
     */
    private String customName;


    /**
     * 所在分组的编号
     */
    private int groupIndex;


    /**
     * 来自UserDetail的信息
     */
    private int gender;

    public Friend() {
        super();
    }

    public Friend(String friendName1, String ownerName1, String customName1, int gender1, int groupIndex1) {
        this.friendName = friendName1;
        this.ownerName = ownerName1;
        this.customName = customName1;
        this.gender = gender1;
        this.groupIndex = groupIndex1;
    }

    public void copy(Friend friend){
        this.friendName = friend.getFriendName();
        this.ownerName = friend.getOwnerName();
        this.customName = friend.getCustomName();
        this.groupIndex = friend.getGroupIndex();
    }


    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
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

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }


    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

}
