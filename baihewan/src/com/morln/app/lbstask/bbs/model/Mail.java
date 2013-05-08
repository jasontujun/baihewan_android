package com.morln.app.lbstask.bbs.model;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.utils.img.ImageUrlType;

import java.util.Comparator;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-7-15
 * Time: 下午8:55
 */
public class Mail {

    private int status;// 状态
    public static final int READ = 0;
    public static final int NEW = 1;

    private int no;// 站内信的序号(从0开始计数)
    private String id;
    private String title;
    private String date;
    private String sender;// 发件人
    private String senderNickName;
    private String receiver;// 收件人
    private String receiverNickName;

    // 内容
    private String ip;

    /**
     * TIP 文字块。注意文字和图片一定是交叉排版……文字先显示。
     */
    private List<String> wordBlocks;

    /**
     * TIP 图片链接。注意文字和图片一定是交叉排版……文字先显示。
     */
    private List<String> imgUrls;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSenderNickName() {
        return senderNickName;
    }

    public void setSenderNickName(String senderNickName) {
        this.senderNickName = senderNickName;
    }

    public String getReceiverNickName() {
        return receiverNickName;
    }

    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public List<String> getWordBlocks() {
        return wordBlocks;
    }

    public void setWordBlocks(List<String> wordBlocks) {
        this.wordBlocks = wordBlocks;
    }

    public int getImgSize() {
        if(imgUrls == null)
            return 0;
        return imgUrls.size();
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public int getWordBlockSize() {
        if(wordBlocks == null)
            return 0;
        return wordBlocks.size();
    }

    /**
     * 设置图片url。并在设置的同时添加空的图片对象。
     * @param imgUrls
     */
    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
    }


    /**
     * 还原图片
     */
    public void resetImg() {
        ImageSource imageSource = (ImageSource) DataRepo.getInstance().getSource(SourceName.IMAGE);
        for(int i = 0; i<imgUrls.size(); i++) {
            String imageUrl = imgUrls.get(i);
            String localImageFile = imageSource.getLocalImage(imageUrl);
            if(localImageFile.equals(ImageUrlType.IMG_LOADING)
                    || localImageFile.equals(ImageUrlType.IMG_ERROR)) {
                imageSource.putImage(imageUrl, "");
            }
        }
    }

    private int indexOfImg(String imgUrl) {
        for(int i = 0; i<imgUrls.size(); i++) {
            if(imgUrls.get(i).equals(imgUrl)) {
                return i;
            }
        }
        return -1;
    }


    public interface NewMailListener {
        void remind(int newMailNumber);
    }



    /**
     * 版面帖子的排序规则。
     * 先判断是否是置顶；然后比较no,no大的排前面(没有no的帖子no=0）
     */
    public static Comparator<Mail> getMailComparator() {
        return new Comparator<Mail>() {
            @Override
            public int compare(Mail mail, Mail mail1) {
                int no1 = mail.getNo();
                int no2 = mail1.getNo();
                return no2 - no1;
            }
        };
    }
}
