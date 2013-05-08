package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterIdDataSource;
import com.morln.app.lbstask.bbs.model.Mail;
import com.morln.app.lbstask.cache.SourceName;

/**
 * Created by jasontujun.
 * Date: 12-7-16
 * Time: 上午10:19
 */
public class MailSource extends XBaseAdapterIdDataSource<Mail> {

    /**
     * 获取新邮件数量
     * @return
     */
    public int getNewMailNumber() {
        int result = 0;
        for(int i = 0; i<size(); i++) {
            if(get(i).getStatus() == Mail.NEW) {
                result++;
            }
        }
        return result;
    }

    /**
     * 获取最小的邮件序号
     * @return
     */
    public int getMinNo() {
        int result = Integer.MAX_VALUE;
        for(int i = 0; i<size(); i++) {
            Mail mail = get(i);
            if(mail.getNo() < result) {
                result = mail.getNo();
            }
        }
        return result;
    }

    /**
     * 删除邮件后，本地要重新设置邮件的no属性
     * @param index
     */
    @Override
    public synchronized void delete(int index) {
        Mail deleteMail = get(index);
        super.delete(index);

        // 本地重新排序no
        if(deleteMail != null) {
            resortNo(deleteMail.getNo());
        }
    }

    /**
     * 删除邮件后，本地要重新设置邮件的no属性
     * @param item
     */
    @Override
    public synchronized void delete(Mail item) {
        super.delete(item);

        // 本地重新排序no
        if(item != null) {
            resortNo(item.getNo());
        }
    }

    /**
     * 重新排序no
     * @param deletedNo
     */
    private void resortNo(int deletedNo) {
        for(int i = 0; i<size(); i++) {
            Mail mail = get(i);
            if(mail.getNo() > deletedNo) {
                mail.setNo(mail.getNo() - 1);
            }
        }
    }


    @Override
    public void replace(int index, Mail newItem) {
        Mail oldMail = itemList.get(index);
        oldMail.resetImg();
        itemList.set(index, newItem);
    }

    @Override
    public String getId(Mail item) {
        return item.getId();
    }

    @Override
    public String getSourceName() {
        return SourceName.USER_MAIL;
    }
}
