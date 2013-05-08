package com.morln.app.lbstask.bbs.model;

import com.morln.app.lbstask.model.UserBase;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 下午2:47
 */
public class BbsUserBase extends UserBase {
    private String star;//星座
    private String loginNum;// 登陆时间
    private String sendNum;
    private String lastLoginTime;
    private String ip;
    private String mailBox;
    private String bbsExp;
    private String level;
    private String showNum;
    private String showWord;
    private String hp;
    private String role;

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public String getLoginNum() {
        return loginNum;
    }

    public void setLoginNum(String loginNum) {
        this.loginNum = loginNum;
    }

    public String getSendNum() {
        return sendNum;
    }

    public void setSendNum(String sendNum) {
        this.sendNum = sendNum;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMailBox() {
        return mailBox;
    }

    public void setMailBox(String mailBox) {
        this.mailBox = mailBox;
    }

    public String getBbsExp() {
        return bbsExp;
    }

    public void setBbsExp(String bbsExp) {
        this.bbsExp = bbsExp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getShowNum() {
        return showNum;
    }

    public void setShowNum(String showNum) {
        this.showNum = showNum;
    }

    public String getShowWord() {
        return showWord;
    }

    public void setShowWord(String showWord) {
        this.showWord = showWord;
    }

    public String getHp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
