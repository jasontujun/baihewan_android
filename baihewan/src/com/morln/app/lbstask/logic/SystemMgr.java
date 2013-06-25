package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.bbs.cache.*;
import com.morln.app.lbstask.cache.*;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.lbstask.utils.img.ImageLoader;
import com.morln.app.lbstask.utils.img.ImgMgrHolder;
import com.xengine.android.data.db.XSQLiteHelper;
import com.xengine.android.system.file.XAndroidFileMgr;
import com.xengine.android.system.file.XFileMgr;

/**
 * Created by jasontujun.
 * Date: 12-4-27
 * Time: 下午3:08
 */
public class SystemMgr {
    private static SystemMgr instance;

    public synchronized static SystemMgr getInstance() {
        if(instance == null) {
            instance = new SystemMgr();
        }
        return instance;
    }

    private SystemMgr() {}

    /**
     * 接受反馈
     * 反馈直接发送至官方账号的站内信。(baihewan)
     * @param content
     * @return
     */
    public int feedback(String content) {
        GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.
                getInstance().getSource(SourceName.GLOBAL_STATE);
        if(globalStateSource.getLoginStatus() == GlobalStateSource.LOGIN_STATUS_NO_LOGIN) {
            return StatusCode.NOT_AUTHORIZED;// 未登陆
        }
        String username = globalStateSource.getCurrentUserName();
        String title = "【反馈】来自" + username;
        return BbsMailMgr.getInstance().sendMail(title, content, "baihewan");
    }


    /**
     * 初始化百荷湾系统
     * @param context
     */
    public static void initSystem(Context context) {
        clearSystem();
        initFileMgr();
        initDB(context);
        initDataSources(context);
    }

    private static void initFileMgr() {
        XFileMgr fileMgr = XAndroidFileMgr.getInstance();
        ImgMgrHolder.getImgDownloadMgr().setDownloadDirectory(
                fileMgr.getDir(XFileMgr.FILE_TYPE_TMP).getAbsolutePath());
    }

    /**
     * 初始化数据库
     * @param context
     */
    private static void initDB(Context context) {
        // 初始化数据库
        XSQLiteHelper.initiate(context, "morln_bhw_db", 1);
    }

    /**
     * 初始化非公共数据源。
     * 一部分是空数据源。
     * 一部分从sharePreference导入。
     * 一部分从SQLite导入。
     */
    private static void initDataSources(Context context) {
        DataRepo repo = DataRepo.getInstance();
        // 公用数据源
        GlobalStateSource globalStateSource = new GlobalStateSource(context);
        globalStateSource.setLoginStatus(GlobalStateSource.LOGIN_STATUS_NO_LOGIN);
        repo.registerDataSource(globalStateSource);
        repo.registerDataSource(new ImageSource());
        repo.registerDataSource(new SystemSettingSource(context));
        repo.registerDataSource(new UserDataSource(context));
        repo.registerDataSource(new MailSource());
        repo.registerDataSource(new BbsUserSource());
        UserFriendSource userFriendSource = new UserFriendSource();
        userFriendSource.loadFromDatabase();
        repo.registerDataSource(userFriendSource);
        SystemUserSource systemUserSource = new SystemUserSource();
        systemUserSource.loadFromDatabase();
        repo.registerDataSource(systemUserSource);

        // BBS数据源
        BoardSource boardSource = new BoardSource();// 版面数据源
        boardSource.init();
        ZoneSource zoneSource = new ZoneSource();// 区数据源 （初始化，并把版面数据源和区数据源进行映射）
        zoneSource.initBoardOfZone(boardSource);
        CollectBoardSource collectBoardSource = new CollectBoardSource();// 版面订阅数据源（从数据库中初始化，并由版面数据源进行赋值）
        collectBoardSource.loadFromDatabase();
        CollectArticleSource collectArticleSource = new CollectArticleSource();// 帖子收藏数据源（从数据库中初始化，把帖子数据加载进对应版面中）
        collectArticleSource.loadFromDatabase();

        repo.registerDataSource(zoneSource);
        repo.registerDataSource(boardSource);
        repo.registerDataSource(collectBoardSource);
        repo.registerDataSource(collectArticleSource);
        repo.registerDataSource(new PersonArticleSource());
        repo.registerDataSource(new HistoryTop10Source());
        repo.registerDataSource(new ZoneHotSource(boardSource));
        repo.registerDataSource(new TodayHotBoardSource());
        repo.registerDataSource(new ArticleSource());
    }

    public static void clearSystem() {
        // clear image cache
        ImageLoader.getInstance().clearImageCache();

        // clear tmp file
        XAndroidFileMgr.getInstance().clearDir(XFileMgr.FILE_TYPE_TMP);
        XAndroidFileMgr.getInstance().clearDir(XFileMgr.FILE_TYPE_PHOTO);

        // clear Mgr
        LoginMgr.clearInstance();
        BbsArticleMgr.clearInstance();
        BbsBoardMgr.clearInstance();
        BbsMailMgr.clearInstance();
        BbsPersonMgr.clearInstance();

        // clear DataSource
        DataRepo.clearInstance();
    }
}
