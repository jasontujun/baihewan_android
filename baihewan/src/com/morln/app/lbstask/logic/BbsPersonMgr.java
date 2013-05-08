package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.bbs.cache.*;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.ArticleDetail;
import com.morln.app.lbstask.bbs.model.BbsUserBase;
import com.morln.app.lbstask.bbs.model.CollectedArticleBase;
import com.morln.app.lbstask.bbs.session.BbsAPI;
import com.morln.app.lbstask.cache.*;
import com.morln.app.lbstask.model.Friend;
import com.morln.app.lbstask.model.UserBase;
import com.morln.app.lbstask.session.apinew.CollectionAPINew;
import com.morln.app.lbstask.session.apinew.FriendAPINew;
import com.morln.app.lbstask.session.bean.CollectionArticle;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-6
 * Time: 下午7:13
 */
public class BbsPersonMgr {
    private static BbsPersonMgr instance;

    public synchronized static BbsPersonMgr getInstance() {
        if(instance == null) {
            instance = new BbsPersonMgr();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private SystemUserSource systemUserSource;
    private CollectArticleSource collectArticleSource;
    private SystemSettingSource systemSettingSource;
    private PersonArticleSource personArticleSource;
    private GlobalStateSource globalStateSource;
    private UserFriendSource userFriendSource;
    private BbsUserSource bbsUserSource;
    private ArticleSource articleSource;
    private BoardSource boardSource;
    private HistoryTop10Source historyTop10Source;
    private ZoneHotSource zoneHotSource;

    private BbsPersonMgr() {
        DataRepo repo = DataRepo.getInstance();
        systemUserSource = (SystemUserSource) repo.getSource(SourceName.SYSTEM_USER);
        collectArticleSource = (CollectArticleSource) repo.getSource(SourceName.BBS_COLLECTION_ARTICLE);
        systemSettingSource = (SystemSettingSource) repo.getSource(SourceName.SYSTEM_SETTING);
        personArticleSource = (PersonArticleSource) repo.getSource(SourceName.BBS_USER_ARTICLE);
        globalStateSource = (GlobalStateSource) repo.getSource(SourceName.GLOBAL_STATE);
        userFriendSource = (UserFriendSource) repo.getSource(SourceName.USER_FRIEND);
        bbsUserSource = (BbsUserSource) repo.getSource(SourceName.BBS_USER_DATA);
        articleSource = (ArticleSource) repo.getSource(SourceName.BBS_ARTICLE);
        boardSource = (BoardSource) repo.getSource(SourceName.BBS_BOARD);
        historyTop10Source = (HistoryTop10Source) repo.getSource(SourceName.BBS_TOP10);
        zoneHotSource = (ZoneHotSource) repo.getSource(SourceName.BBS_ZONE_HOT);
    }


    /**
     * 获取收藏的帖子Id的列表
     * @return
     */
    public List<ArticleBase> getCollectedArticleIdList() {
        List<ArticleBase> result = new ArrayList<ArticleBase>();
        List<CollectedArticleBase> source = collectArticleSource.
                getByUsername(globalStateSource.getCurrentUserName());
        for(int i = 0; i<source.size(); i++) {
            result.add(source.get(i).getArticle());
        }
        return result;
    }

    /**
     * 添加收藏
     * @param articleId
     */
    public void addCollectArticle(String boardId, String articleId) {
        ArticleBase article = boardSource.getArticle(boardId, articleId);
        if(article == null) {
            article = historyTop10Source.getById(ArticleBase.createId(boardId, articleId));
        }
        if(article == null) {
            article = zoneHotSource.getById(ArticleBase.createId(boardId, articleId));
        }
        if(article == null) {
            ArticleDetail articleDetail = articleSource.getById(ArticleDetail.createId(boardId, articleId));
            if(articleDetail != null) {
                article = articleDetail.createArticleBase();
            }
        }
        addCollectArticle(article);
    }

    /**
     * 添加收藏
     * @param article
     */
    public void addCollectArticle(ArticleBase article) {
        if(article == null) {
            return;
        }

        String username = globalStateSource.getCurrentUserName();
        CollectedArticleBase collectedArticle = new CollectedArticleBase(username, article);
        collectArticleSource.add(collectedArticle);
        collectArticleSource.saveToDatabase();
        updateCollectionTimeStamp(username);// 更新时间戳
    }

    /**
     * 删除一堆收藏
     * @param articleIds
     */
    public void deleteAllCollectedArticle(List<String> articleIds) {
        String username = globalStateSource.getCurrentUserName();
        collectArticleSource.deleteAllByUsernameId(username, articleIds);
        collectArticleSource.saveToDatabase();
        updateCollectionTimeStamp(username);
    }

    /**
     * 删除单个收藏
     * @param articleId
     */
    public void deleteCollectedArticle(String articleId) {
        String username = globalStateSource.getCurrentUserName();
        collectArticleSource.deleteByUsernameId(username, articleId);
        collectArticleSource.saveToDatabase();
        updateCollectionTimeStamp(username);// 更新时间戳
    }


    /**
     * 判断是否已收藏此帖子
     * @param articleId
     * @return
     */
    public boolean containsCollectedArticle(String articleId) {
        if(XStringUtil.isNullOrEmpty(articleId)) {
            return false;
        }

        String username = globalStateSource.getCurrentUserName();
        return collectArticleSource.getIndexByUsernameId(username, articleId) != -1;
    }

    /**
     * 更新收藏帖子的时间戳（内部调用）
     */
    private void updateCollectionTimeStamp(String username) {
        UserBase user = systemUserSource.getById(username);
        if(user != null) {
            long oldTimeStamp = user.getCollectionTimeStamp();
            user.setCollectionTimeStamp(oldTimeStamp + 1);
            systemUserSource.saveToDatabase();
        }
    }

    /**
     * 同步个人收藏。
     * 如果客户端的时间戳比服务器端新，则服务器更新为上传的数据。
     * @return
     */
    public int uploadCollection(Context context) {
        String username = globalStateSource.getCurrentUserName();
        List<CollectedArticleBase> localCollection = collectArticleSource.getByUsername(username);
        List<CollectionArticle> sessionList = new ArrayList<CollectionArticle>();
        for(int i = 0; i<localCollection.size(); i++) {
            ArticleBase localArticle = localCollection.get(i).getArticle();
            CollectionArticle sessionArticle = CollectionArticle.toSessionBean(localArticle);
            sessionArticle.setUsername(username);
            sessionList.add(sessionArticle);
        }
        long collectionTimeStamp = 0;
        UserBase user = systemUserSource.getById(username);
        if(user != null) {
            collectionTimeStamp = user.getCollectionTimeStamp();
        }
        XLog.d("API", "请求时,时间戳:" + collectionTimeStamp);
        XLog.d("API", "请求时,用户名:" + username + ",本地列表数量：" + sessionList.size());
        int resultCode = new CollectionAPINew(context).syncCollection(username,
                sessionList, collectionTimeStamp);
        XLog.d("API", "同步收藏返回的列表size:" + sessionList.size());
        if(resultCode == StatusCode.ARTICLE_IS_LATEST) {
            collectArticleSource.deleteByUsername(username);
            List<CollectedArticleBase> localBeans = new ArrayList<CollectedArticleBase>();
            for(CollectionArticle sessionArticle : sessionList) {
                XLog.d("API", "下载的帖子id:" + sessionArticle.getUrl());
                ArticleBase article = CollectionArticle.toLocalBean(sessionArticle);
                localBeans.add(new CollectedArticleBase(username, article));
            }
            collectArticleSource.addAll(localBeans);
            collectArticleSource.saveToDatabase();
        }
        return resultCode;
    }

    /**
     * 判断某人是否为你的好友
     * @param friendName
     * @return
     */
    public boolean isFriend(String friendName) {
        if(XStringUtil.isNullOrEmpty(friendName)) {
            return false;
        }

        String username = globalStateSource.getCurrentUserName();
        return userFriendSource.getIndexByUsernameId(username, friendName) != -1;
    }

    /**
     * 从网页抓取好友
     * @return
     */
    public int getFriendsFromWeb() {
        List<Friend> friendList = new ArrayList<Friend>();
        int resultCode = BbsAPI.getFriendsFromWeb(friendList);
        if(StatusCode.isSuccess(resultCode)) {
            userFriendSource.deleteByUsername(globalStateSource.getCurrentUserName());
            userFriendSource.addAll(friendList);// 添加到数据源中
            userFriendSource.saveToDatabase();
        }
        return resultCode;

    }


    /**
     * 上传好友数据
     * @return
     */
    public int uploadFriendList(Context context) {
        List<com.morln.app.lbstask.session.bean.friend.Friend> friendList = new ArrayList<com.morln.app.lbstask.session.bean.friend.Friend>();
        for(int i = 0; i< userFriendSource.size(); i++) {
            Friend f = userFriendSource.get(i);
            friendList.add(f.createSessionFriend());
        }
        int resultCode = new FriendAPINew(context).syncFriend(globalStateSource.getCurrentUserName(),
                new ArrayList<com.morln.app.lbstask.session.bean.friend.Friend>());
        return resultCode;
    }


    /**
     * 获取好友列表
     * @param ownerName
     * @return
     */
    public List<Friend> getFriendList(String ownerName) {
        if(XStringUtil.isNullOrEmpty(ownerName)) {
            return new ArrayList<Friend>();
        }
        
        List<Friend> result = new ArrayList<Friend>();
        for(int i = 0; i<userFriendSource.size(); i++) {
            if(ownerName.equals(userFriendSource.get(i).getOwnerName())) {
                result.add(userFriendSource.get(i));
            }
        }
        return result;
    }

    /**
     * 添加好友
     * @param username
     * @param customName
     * @return
     */
    public int addFriend(String username, String customName) {
        int resultCode = BbsAPI.addFriend(username, customName);
        if(StatusCode.isSuccess(resultCode)) {
            Friend friend = new Friend();
            BbsUserBase userInfo = getBbsUserInfoFromLocal(username);
            if(userInfo == null) {
                userInfo = new BbsUserBase();
                userInfo.setUsername(username);
            }
            friend.setUserInfo(userInfo);
            friend.setCustomName(customName);
            friend.setOwnerName(globalStateSource.getCurrentUserName());
            userFriendSource.add(friend);
            userFriendSource.saveToDatabase();// 同步到数据库
        }
        return resultCode;
    }

    /**
     * 删除好友
     * @param friendName
     * @return
     */
    public int deleteFriend(String friendName) {
        int resultCode = BbsAPI.deleteFriend(friendName);
        if(StatusCode.isSuccess(resultCode)) {
            userFriendSource.deleteByUsernameId(globalStateSource.getCurrentUserName(), friendName);
            userFriendSource.saveToDatabase();
        }
        return resultCode;
    }



    /**
     * 从网页获取某人的帖子列表
     * @param userId
     * @return
     */
    public int getPersonArticlesFromWeb(String userId, List<ArticleBase> resultList) {
        int resultCode = BbsAPI.searchArticle(userId, "", "", "", "0", "9999", resultList);
        if(StatusCode.isSuccess(resultCode)) {
            personArticleSource.addAll(resultList);
        }
        return resultCode;
    }

    /**
     * 从本地获取某人的帖子列表
     * @param userId
     * @return
     */
    public List<ArticleBase> getPersonArticlesFromLocal(String userId) {
        return personArticleSource.getByUsername(userId);
    }


    /**
     * 删除自己的帖子
     * @param board
     * @param articleId
     * @return
     */
    public int deleteArticle(String board, String articleId) {
        int resultCode = BbsAPI.deleteArticle(board, articleId);
        if(StatusCode.isSuccess(resultCode)) {
            articleSource.deleteById(ArticleDetail.createId(board, articleId));// 帖子详情中删除此贴
            personArticleSource.deleteById(ArticleBase.createId(board, articleId));
        }
        return resultCode;
    }

    /**
     * 从本地缓存获取用户资料
     * @param username
     * @return
     */
    public BbsUserBase getBbsUserInfoFromLocal(String username)  {
        return bbsUserSource.getById(username);
    }

    /**
     * 从网上抓取用户资料
     * @param username
     * @return
     */
    public BbsUserBase getBbsUserInfoFromWeb(String username)  {
        BbsUserBase result = BbsAPI.getBbsUserInfoFromWeb(username);
        if(result != null) {
            bbsUserSource.add(result);// 存入本地缓存中
        }
        return result;
    }



    public void setMobileSignature(String signature) {
        systemSettingSource.setMobileSignature(signature);
    }
    
    public String getMobileSignature() {
        return systemSettingSource.getMobileSignature();
    }
}
