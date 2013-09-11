package com.morln.app.lbstask.logic;

import android.text.TextUtils;
import com.morln.app.lbstask.data.cache.MailSource;
import com.morln.app.lbstask.data.model.BbsStatus;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.session.bbs.BbsAPI;
import com.morln.app.lbstask.utils.BbsSignature;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.cache.SystemSettingSource;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.data.cache.XDataRepository;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jasontujun.
 * Date: 12-7-17
 * Time: 下午1:24
 */
public class BbsMailMgr {
    private static BbsMailMgr instance;

    public synchronized static BbsMailMgr getInstance() {
        if (instance == null) {
            instance = new BbsMailMgr();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private GlobalStateSource globalStateSource;
    private MailSource mailSource;
    private SystemSettingSource systemSettingSource;

    private GetBbsStatusTask getBbsStatusTask;// 刷新bbs状态的线程
    private List<Mail.NewMailListener> listeners;// 未读邮件的监听者

    private BbsMailMgr() {
        XDataRepository repo = DefaultDataRepo.getInstance();
        globalStateSource = (GlobalStateSource) repo.getSource(SourceName.GLOBAL_STATE);
        mailSource = (MailSource) repo.getSource(SourceName.USER_MAIL);
        systemSettingSource = (SystemSettingSource) repo.getSource(SourceName.SYSTEM_SETTING);

        listeners = new ArrayList<Mail.NewMailListener>();
    }

    /**
     * 获取站内信列表首页（默认为最后20个）
     * @return
     */
    public int getDefaultMailListFromWeb() {
        List<Mail> mailList = new ArrayList<Mail>();
        int resultCode = BbsAPI.getDefaultMailList(mailList);
        if (StatusCode.isSuccess(resultCode)) {
            mailSource.clear();
            mailSource.addAll(mailList);

            // 更新新邮件数量
            setNewMailNumber(mailSource.getNewMailNumber());

            // 刷新顺序
            mailSource.sort(Mail.getMailComparator());
        }
        return resultCode;
    }

    /**
     * 获取邮件列表（根据起始索引，获取20个）
     * @param start
     * @return
     */
    public int getMailListFromWeb(int start) {
        XLog.d("API", "获取邮件列表的起始值:" + start);
        List<Mail> mailList = new ArrayList<Mail>();
        int resultCode = BbsAPI.getMailList(mailList, start);
        if (StatusCode.isSuccess(resultCode)) {
            mailSource.addAll(mailList);

            // 更新新邮件数量
            setNewMailNumber(mailSource.getNewMailNumber());

            // 刷新顺序
            mailSource.sort(Mail.getMailComparator());
        }
        return resultCode;
    }

    /**
     * 从本地缓存中读取站内信
     * @param id
     * @return
     */
    public Mail getMailDetailFromLocal(String id) {
        Mail mail = mailSource.getById(id);
        if (mail == null)
            return null;

        if (mail.getWordBlocks() == null) {
            return null;
        } else {
            return mail;
        }
    }

    /**
     * 从网页抓取站内信详情
     * @param id
     * @return 如果获取失败，返回null
     */
    public Mail getMailDetailFromWeb(String id) {
        Mail mail = mailSource.getById(id);
        if(mail == null) {
            return null;
        }
        int resultCode = BbsAPI.getMailDetail(id, mail.getNo(), mail);// 获取站内信详情
        XLog.d("API", "站内信页码num：" + mail.getNo());
        if(StatusCode.isSuccess(resultCode)) {
            if(mail.getStatus() == Mail.NEW) {
                mail.setStatus(Mail.READ);// TIP 设置状态为已读
            }
            // 更新新邮件数量
            setNewMailNumber(mailSource.getNewMailNumber());
            return mail;
        }else {
            return null;
        }
    }

    /**
     * 发站内信
     * @param title
     * @param content
     * @param receiver
     * @return
     */
    public int sendMail(String title, String content, String receiver) {
        content = content + "\n" + "\n" + "-\n";
        String signature = systemSettingSource.getMobileSignature();
        content = content + BbsSignature.signature;// TIP 内容结尾填上产品签名
        if (!TextUtils.isEmpty(signature)) {
            content = content + ": " + signature + "\n";// 添加手机签名
        }
        return BbsAPI.sendMail(title, content, receiver);
    }

    /**
     * 删除站内信
     * @param id
     * @return
     */
    public int deleteMail(String id) {
        int resultCode = BbsAPI.deleteMail(id);
        if (StatusCode.isSuccess(resultCode)) {
            mailSource.deleteById(id);
            // 更新新邮件数量
            setNewMailNumber(mailSource.getNewMailNumber());
            // 刷新顺序
            mailSource.sort(Mail.getMailComparator());
        }
        return resultCode;
    }


    /**
     * 启动邮件刷新线程
     */
    public void startMailRemindTask() {
        if (systemSettingSource.isNewMailRemind()) {
            stopMailRemindTask();
            if (getBbsStatusTask == null) {
                getBbsStatusTask = new GetBbsStatusTask();
            }
            // 启动刷新线程
            new Timer().scheduleAtFixedRate(getBbsStatusTask, 0, systemSettingSource.getNewMailRemindInterval());
        }
    }

    /**
     * 停止邮件刷新线程
     */
    public void stopMailRemindTask() {
        if (getBbsStatusTask != null) {
            getBbsStatusTask.cancel();
            getBbsStatusTask = null;
            setNewMailNumber(0);
        }
    }

    /**
     * 监听新邮件数量的变化
     * @param listener
     */
    public void registerNewMailListener(Mail.NewMailListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterNewMailListener(Mail.NewMailListener listener) {
        listeners.remove(listener);
    }

    /**
     * 设置新邮件数量
     * @param newMailNumber
     */
    private synchronized void setNewMailNumber(int newMailNumber) {
        globalStateSource.setNewMailNum(newMailNumber);
        for (Mail.NewMailListener listener : listeners) {
            listener.remind(newMailNumber);
        }
    }

    /**
     * 获取新邮件数量
     * @return
     */
    public int getNewMailNumber() {
        return globalStateSource.getNewMailNum();
    }

    /**
     * 抓取登录用户状态的异步线程（邮件状态信息）
     */
    class GetBbsStatusTask extends TimerTask {
        @Override
        public void run() {
            BbsStatus result = new BbsStatus();
            BbsAPI.getBbsStatus(result);
            setNewMailNumber(result.getNewMailNumber());
        }
    }

}
