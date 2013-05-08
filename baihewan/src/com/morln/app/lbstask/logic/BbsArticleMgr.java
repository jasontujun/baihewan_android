package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.bbs.cache.ArticleSource;
import com.morln.app.lbstask.bbs.cache.HistoryTop10Source;
import com.morln.app.lbstask.bbs.cache.ZoneHotSource;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.ArticleDetail;
import com.morln.app.lbstask.bbs.model.Top10ArticleBase;
import com.morln.app.lbstask.bbs.session.BbsAPI;
import com.morln.app.lbstask.bbs.utils.BbsSignature;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemSettingSource;
import com.morln.app.lbstask.session.apinew.Top10APINew;
import com.morln.app.lbstask.session.apinew.Top10ContentAPINew;
import com.morln.app.lbstask.session.bean.top10.Top10Content;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-2-25
 * Time: 下午8:00
 */
public class BbsArticleMgr {
    private static BbsArticleMgr instance;

    public synchronized static BbsArticleMgr getInstance() {
        if(instance == null) {
            instance = new BbsArticleMgr();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private ArticleSource articleSource;
    private HistoryTop10Source top10Source;
    private ZoneHotSource zoneHotSource;
    private SystemSettingSource systemSettingSource;

    private BbsArticleMgr() {
        DataRepo repo = DataRepo.getInstance();
        articleSource = (ArticleSource) repo.getSource(SourceName.BBS_ARTICLE);
        top10Source = (HistoryTop10Source) repo.getSource(SourceName.BBS_TOP10);
        zoneHotSource = (ZoneHotSource) repo.getSource(SourceName.BBS_ZONE_HOT);
        systemSettingSource = (SystemSettingSource) repo.getSource(SourceName.SYSTEM_SETTING);
    }

    public List<String> getTop10DateList() {
        return top10Source.getHistoryDateList();
    }

    public List<Top10ArticleBase> getTop10ListBaseDate(String date) {
        return top10Source.getBaseDate(date);
    }

    /**
     * 获取网页十大。
     * @return 返回结果码
     */
    public synchronized  int getTop10FromWeb() {
        List<Top10ArticleBase> top10 = new ArrayList<Top10ArticleBase>();
        int resultCode = BbsAPI.getTop10FromWeb(top10);
        if(StatusCode.isSuccess(resultCode)) {
            top10Source.addAll(top10);
        }
        return resultCode;
    }

    /**
     * 向服务器获取历史十大，根据索引
     * @return 返回结果码
     */
    public int getTop10FromServerByIndex(Context context, int startIndex, int endIndex) {
        List<com.morln.app.lbstask.session.bean.top10.Top10ArticleBase> top10List =
                new ArrayList<com.morln.app.lbstask.session.bean.top10.Top10ArticleBase>();
        int resultCode = new Top10APINew(context).getTop10ByIndex(startIndex, endIndex, top10List);
        if(StatusCode.isSuccess(resultCode)) {
            // 检测有无更新数据
            if(top10List.size() == 0) {
                XLog.d("TOP10", "没有更新的十大了……");
                return StatusCode.NO_MORE_SUCCESS;
            }
            // 将十大存入数据源
            List<Top10ArticleBase> resultList = new ArrayList<Top10ArticleBase>();
            for(int i = 0; i < top10List.size(); i++) {
                com.morln.app.lbstask.session.bean.top10.Top10ArticleBase serverTopArticle = top10List.get(i);
                Date firstTime = new Date(serverTopArticle.getFirstTime());
                Date lastTime = new Date(serverTopArticle.getLastTime());
                Top10ArticleBase localTopArticle = new Top10ArticleBase(serverTopArticle.getRankList(),
                        serverTopArticle.getTitle(), serverTopArticle.getUrl(), serverTopArticle.getAuthorName(),
                        serverTopArticle.getBoard(), ""+serverTopArticle.getReplyCount(), firstTime, lastTime);
                localTopArticle.setDate(XStringUtil.date2str(lastTime));// 存入字符串形式的日期
                resultList.add(localTopArticle);
            }
            top10Source.addAll(resultList);
        }
        return resultCode;
    }

    /**
     * 向服务器获取历史十大,根据日期
     * @return 返回结果码
     */
    public synchronized  int getTop10FromServerByDate(Context context, int year, int month, int day) {
        List<com.morln.app.lbstask.session.bean.top10.Top10ArticleBase> top10List =
                new ArrayList<com.morln.app.lbstask.session.bean.top10.Top10ArticleBase>();
        int resultCode = new Top10APINew(context).getTop10ByDate(year, month, day, top10List);
        XLog.d("TOP10", "向服务器获取历史十大的返回码:"+resultCode);
        XLog.d("TOP10", "list size:"+top10List.size());
        if(StatusCode.isSuccess(resultCode)) {
            // 检测有无更新数据
            if(top10List.size() == 0) {
                XLog.d("TOP10","没有更新的十大了……");
                return StatusCode.NO_MORE_SUCCESS;
            }
            // 将十大存入数据源
            List<Top10ArticleBase> resultList = new ArrayList<Top10ArticleBase>();
            for(int i = 0; i < top10List.size(); i++) {
                com.morln.app.lbstask.session.bean.top10.Top10ArticleBase serverTopArticle = top10List.get(i);
                Date firstTime = new Date(serverTopArticle.getFirstTime());
                Date lastTime = new Date(serverTopArticle.getLastTime());
                Top10ArticleBase localTopArticle = new Top10ArticleBase(serverTopArticle.getRankList(),
                        serverTopArticle.getTitle(), serverTopArticle.getUrl(), serverTopArticle.getAuthorName(),
                        serverTopArticle.getBoard(), ""+serverTopArticle.getReplyCount(), firstTime, lastTime);
                localTopArticle.setDate(XStringUtil.date2calendarStr(lastTime));// 存入字符串形式的日期
                resultList.add(localTopArticle);
            }
            top10Source.addAll(resultList);
        }
        return resultCode;
    }

    /**
     * 从本地缓存中读取帖子
     * @param articleId
     * @param boardId
     * @return
     */
    public ArticleDetail getThemeArticleFromLocal(String articleId, String boardId) {
        return articleSource.getById(ArticleDetail.createId(boardId, articleId));
    }

    /**
     * 从网页抓取主题模式的帖子
     * @param boardStr
     * @param articleIdStr
     * @param pageStr
     * @return 如果获取失败，返回null
     */
    public ArticleDetail getThemeArticleFromWeb(String boardStr, String articleIdStr, int pageStr) {
        List<ArticleDetail> articleDetailList = new ArrayList<ArticleDetail>();
        int resultCode = BbsAPI.getThemeArticleFromWeb(boardStr, articleIdStr, pageStr, articleDetailList);
        if(StatusCode.isSuccess(resultCode)) {
            for(int i = 0; i<articleDetailList.size(); i++) {
                articleSource.add(articleDetailList.get(i));
            }
            if(articleDetailList.size() > 0) {
                return articleDetailList.get(0);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }

    /**
     * 从服务器获取帖子（一般模式的帖子）
     * @param articleId
     * @return
     */
    public ArticleDetail getArticleFromServer(Context context, String articleId, String boardId) {
        Top10Content articleContent = new Top10Content();
        int resultCode = new Top10ContentAPINew(context).getTopContent(articleId, boardId, articleContent);
        if(!StatusCode.isSuccess(resultCode)) {
            return null;
        }

        ArticleDetail articleDetail = BbsAPI.parseNormalArticleContent(articleContent.getContent());
        if(articleDetail != null) {
            articleDetail.setId(articleId);
            articleDetail.setDeleted(true);
            articleSource.add(articleDetail);
        }

        return articleDetail;
    }

    /**
     * 从网页抓取一般模式的帖子。
     * (一般模式下的帖子没有楼层，没有回复！)
     * @param boardStr
     * @param articleIdStr
     * @return 如果获取失败，返回null
     */
    public ArticleDetail getNormalArticleFromWeb(String boardStr, String articleIdStr) {
        return null;
    }

    /**
     * 根据page页数获取某帖子的跟帖.
     * @param themeId
     * @param boardId
     * @param page 页数。-1表示全部回帖，大于等于0表示对应页数。
     * @return
     */
    public List<ArticleDetail> getArticleReplyList(String themeId, String boardId, int page) {
        return articleSource.getArticleReplyList(boardId, themeId);
    }



    /**
     * 上传图片
     * @param board
     * @param description
     * @return
     */
    public String uploadImage(String board, File imageFile, String description) {
        return BbsAPI.uploadImage(board, imageFile, description);
    }


    /**
     * 发帖功能(Http POST方式)
     * @param board
     * @param title
     * @param content
     * @param pid
     * @param reid
     * @return
     */
    public int sendArticle(String board, String title, String content, String pid, String reid) {
        content = content + "\n"+"\n"+"-\n";
        String signature = systemSettingSource.getMobileSignature();
        content = content + BbsSignature.signature;// TIP 内容结尾填上产品签名
        if(!XStringUtil.isNullOrEmpty(signature)) {
            content = content + ": " + signature +"\n";// 添加手机签名
        }
        return BbsAPI.sendArticle(board, title, content, pid, reid);
    }

    /**
     * 回复帖子
     * @param articleId
     * @param board
     * @param title
     * @param content
     * @return
     */
    public int replyArticle(String articleId, String board, String title, String content) {
        String pid = BbsAPI.getPid(articleId, board);
        if(pid == null || pid.equals("")) {
            return StatusCode.FAIL;
        }
        String reid = articleId.substring(2, articleId.indexOf(".A"));
        int resultCode = sendArticle(board, title, content, pid, reid);
        return resultCode;
    }

    /**
     * 获取各区热点
     * @param sec 区编号
     * @return
     */
    public synchronized int getZoneHotFromWeb(int sec) {
        List<ArticleBase> articleBaseList = new ArrayList<ArticleBase>();
        int resultCode = BbsAPI.getZoneHotFromWeb(sec, articleBaseList);
        if(StatusCode.isSuccess(resultCode)) {
            // 存入数据源
            for(int i = 0; i<articleBaseList.size(); i++) {
                zoneHotSource.add(articleBaseList.get(i));
            }
        }
        return resultCode;
    }

    public List<String> getZoneNameList() {
        return zoneHotSource.getZoneList();
    }

    public List<ArticleBase> getZoneListBaseName(String zoneName) {
        return zoneHotSource.getBaseZone(zoneName);
    }
}
