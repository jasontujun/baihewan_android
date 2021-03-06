package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.*;
import com.morln.app.lbstask.engine.*;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.data.cache.XDataRepository;
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
     * 初始化xengine的一些重要模块。
     * 在第一个Activity的最开始执行。
     * @param context
     */
    public void initEngine(Context context) {
        // 初始化文件管理模块
        XFileMgr fileMgr = XAndroidFileMgr.getInstance();
        fileMgr.setRootName("baihewan");
        fileMgr.setDir(XFileMgr.FILE_TYPE_TMP, "tmp", true);
        fileMgr.setDir(XFileMgr.FILE_TYPE_PHOTO, "photo", true);
        // 初始化网络模块
        HttpClientHolder.init(context, "baihewan");
        DownloadMgrHolder.init(HttpClientHolder.getImageHttpClient());
        UploadMgrHolder.init(HttpClientHolder.getImageHttpClient());
        // 初始化图片加载模块
//        XAndroidImageProcessor.getInstance().init(
//                ScreenHolder.getInstance().getScreenWidth(),
//                ScreenHolder.getInstance().getScreenHeight(),
//                fileMgr.getDir(XFileMgr.FILE_TYPE_TMP));
        MyImageViewLocalLoader.getInstance().init(
                context,
                R.drawable.img_loading,
                R.drawable.img_click_load,
                R.drawable.img_loading,
                R.drawable.img_load_fail
        );
        MyImageSwitcherLocalLoader.getInstance().init(
                context,
                R.drawable.img_loading,
                R.drawable.img_click_load,
                R.drawable.img_loading,
                R.drawable.img_load_fail
        );
        MyImageScrollLocalLoader.getInstance().init(
                context,
                R.drawable.img_loading,
                R.drawable.img_click_load,
                R.drawable.img_loading,
                R.drawable.img_load_fail
        );
        MyImageScrollRemoteLoader.getInstance().init(
                context,
                R.drawable.img_loading,
                R.drawable.img_click_load,
                R.drawable.img_loading,
                R.drawable.img_load_fail
        );
    }

    /**
     * 初始化软件系统（数据等）。
     * 可以在loading界面延迟执行。
     * @param context
     */
    public static void initSystem(Context context) {
        clearSystem();
        initDB(context);
        initDataSources(context);
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
        XDataRepository repo = DefaultDataRepo.getInstance();
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
        MyImageScrollLocalLoader.getInstance().stopAndClear();
        MyImageScrollRemoteLoader.getInstance().stopAndClear();

        // clear image cache
        MyImageViewLocalLoader.getInstance().clearImageCache();
        MyImageSwitcherLocalLoader.getInstance().clearImageCache();
        MyImageScrollLocalLoader.getInstance().clearImageCache();
        MyImageScrollRemoteLoader.getInstance().clearImageCache();

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
//        DefaultDataRepo.clearInstance();
    }

    /**
     * 接受反馈
     * 反馈直接发送至官方账号的站内信。(baihewan)
     * @param content
     * @return
     */
    public int feedback(String content) {
        GlobalStateSource globalStateSource = (GlobalStateSource) DefaultDataRepo.
                getInstance().getSource(SourceName.GLOBAL_STATE);
        if(globalStateSource.getLoginStatus() == GlobalStateSource.LOGIN_STATUS_NO_LOGIN) {
            return StatusCode.NOT_AUTHORIZED;// 未登陆
        }
        String username = globalStateSource.getCurrentUserName();
        String title = "【反馈】来自" + username;
        return BbsMailMgr.getInstance().sendMail(title, content, "baihewan");
    }
}
