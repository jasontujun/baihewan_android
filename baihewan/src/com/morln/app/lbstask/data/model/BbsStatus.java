package com.morln.app.lbstask.data.model;

/**
 * 百合站内状态
 * Created by jasontujun.
 * Date: 12-7-20
 * Time: 下午6:35
 */
public class BbsStatus {
    private String date;
    private int onlineNumber;
    private int totalMailNumber;
    private int newMailNumber;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOnlineNumber() {
        return onlineNumber;
    }

    public void setOnlineNumber(int onlineNumber) {
        this.onlineNumber = onlineNumber;
    }

    public int getTotalMailNumber() {
        return totalMailNumber;
    }

    public void setTotalMailNumber(int totalMailNumber) {
        this.totalMailNumber = totalMailNumber;
    }

    public int getNewMailNumber() {
        return newMailNumber;
    }

    public void setNewMailNumber(int newMailNumber) {
        this.newMailNumber = newMailNumber;
    }
}
