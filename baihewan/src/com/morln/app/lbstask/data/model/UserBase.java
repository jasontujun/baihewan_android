package com.morln.app.lbstask.data.model;

import java.util.List;

/**
 * 系统用户类。
 * 用于存放每个账户的相关信息（如）
 * Created by jasontujun.
 * Date: 12-2-10
 * Time: 上午1:05
 */
public class UserBase {
    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private int gender = -1;
    public static final int GENDER_FEMALE = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_UNKNOWN = 2;

    /**
     * 经验值
     */
    private String exp;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 邮箱
     */
    private String email;

    /**
     * qq号码
     */
    private String qqNum;

    /**
     * 人人
     */
    private String renren;

    /**
     * 微博
     */
    private String microBlog;

    /**
     * 用户自我描述特征
     */
    private List<UserFeasure> feasureList;

    private long friendTimeStamp;// 好友时间戳

    private long collectionTimeStamp;// 收藏时间戳

    public class UserFeasure {
        /**
         * 特点Id
         */
        private String feasureId;

        /**
         * 特点名
         */
        private String feasureName;

        /**
         * 特点等级
         */
        private int feasureLevel;
    }

    public UserBase() {
    }

    public UserBase(String username) {
        this.username = username;
        friendTimeStamp = 0;
        collectionTimeStamp = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQqNum() {
        return qqNum;
    }

    public void setQqNum(String qqNum) {
        this.qqNum = qqNum;
    }

    public String getRenren() {
        return renren;
    }

    public void setRenren(String renren) {
        this.renren = renren;
    }

    public String getMicroBlog() {
        return microBlog;
    }

    public void setMicroBlog(String microBlog) {
        this.microBlog = microBlog;
    }

    public List<UserFeasure> getFeasureList() {
        return feasureList;
    }

    public void setFeasureList(List<UserFeasure> feasureList) {
        this.feasureList = feasureList;
    }

    public long getFriendTimeStamp() {
        return friendTimeStamp;
    }

    public void setFriendTimeStamp(long friendTimeStamp) {
        this.friendTimeStamp = friendTimeStamp;
    }

    public long getCollectionTimeStamp() {
        return collectionTimeStamp;
    }

    public void setCollectionTimeStamp(long collectionTimeStamp) {
        this.collectionTimeStamp = collectionTimeStamp;
    }
}
