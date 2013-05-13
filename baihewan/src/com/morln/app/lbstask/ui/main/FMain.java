package com.morln.app.lbstask.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Mail;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemSettingSource;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.logic.SystemMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.service.LogoutService;
import com.morln.app.lbstask.session.HttpClientHolder;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.article.LReadArticle;
import com.morln.app.lbstask.ui.article.LReplyArticle;
import com.morln.app.lbstask.ui.article.LWriteArticle;
import com.morln.app.lbstask.ui.board.LBoard;
import com.morln.app.lbstask.ui.image.LImageDetail;
import com.morln.app.lbstask.ui.login.LLogin;
import com.morln.app.lbstask.ui.login.LLogo;
import com.morln.app.lbstask.ui.mail.LReadMail;
import com.morln.app.lbstask.ui.mail.LWriteMail;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.img.ImgMgrHolder;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.xengine.android.media.image.XAndroidImageLocalMgr;
import com.xengine.android.session.http.XNetworkUtil;
import com.xengine.android.system.file.XAndroidFileMgr;
import com.xengine.android.system.file.XFileMgr;
import com.xengine.android.system.heartbeat.XAndroidHBM;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;

import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-2-14
 * Time: 下午8:01
 */
public class FMain extends XBaseFrame {
    private long lastBackTime;// 上一次back键的时间
    private static final int PRESS_BACK_INTERVAL = 1500; // back按键间隔，单位：毫秒


    // 界面
    private LLogo logoLayer;
    private LLogin loginLayer;
    private LMain mainLayer;
    private LReadMail readMailLayer;
    private LWriteMail writeMailLayer;
    private LReadArticle readArticleLayer;// 读贴界面
    private LWriteArticle writeArticleLayer;// 写帖界面
    private LReplyArticle replyArticleLayer;// 回复界面
    private LBoard boardLayer;// 版面的帖子界面

    /**
     * 这个函数是进入系统以后调用的第一个初始化函数，在这里初始化系统的基础组件
     * 没有scree(),没有audio(),什么都还没初始化
     * @param context
     */
    @Override
    public void preInit(Context context) {
        // 禁掉XLog
//        XLog.setDebugEnabled(false);
//        XLog.setErrorEnabled(false);
//        XLog.setInfoEnabled(false);

        XNetworkUtil.init(getApplicationContext());
        HttpClientHolder.init(getApplicationContext());
        AnimationUtil.init(getApplicationContext());
        // 初始化图片管理器
        XAndroidFileMgr.getInstance().setRootName("baihewan");
    }

    /**
     * 初始化函数。
     * 设置XAndroidImageLocalMgr的缓存文件夹。
     * 启动Logo界面。
     * @param context
     */
    @Override
    public void init(Context context) {

        // 初始化图片下载管理器
        int screenWidth = screen().getScreenWidth();
        int screenHeight = screen().getScreenHeight();
        ImgMgrHolder.init(screenWidth, screenHeight);

        // 注册心跳
        getSystemStateManager().registerSystemStateListener(XAndroidHBM.getInstance());

        // 启动logo界面
        if (logoLayer == null) {
            logoLayer = new LLogo(this, new Runnable() {
                @Override
                public void run() {
                    // 预加载主界面，为了提高整体启动速度
                    mainLayer = new LMain(FMain.this);
                }
            });
        }
        addLayer(logoLayer);

        // 检测版本
        UmengUpdateAgent.update(context);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setUpdateAutoPopup(true);
        MobclickAgent.onError(context);
    }


    @Override
    public void onFrameDisplay() {
        // 检测网络和GPS的状态
        if (!XNetworkUtil.isNetworkAvailable())
            DialogUtil.createWarningDialog(this).show("有点小问题~", "您的手机网络没打开哦~");
    }


    @Override
    public Handler getFrameHandler() {
        return mainFrameHandler;
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    /**
     * 按下back键后的事件响应
     * @return
     */
    @Override
    public boolean isBackKeyDisabled() {
        XLog.d("BACK", "点击back按钮！");

        XUILayer layer = getTopLayer();
        if (layer != null) {
            int result = layer.back();// 先调用顶部图层的back()函数
            if (result == XBackType.SELF_BACK) {
                removeLayer(layer);// 退出这一图层
                lastBackTime = 0;
            } else if (result == XBackType.NOTHING_TO_BACK) {
                back();// 没有可以退出的。调用自身的back()函数
            } else {
                lastBackTime = 0;// 如果图层back键操作成功，则重置退出程序的标识
            }
        } else {
            back();// 再调用自身的back()函数
        }
        return true;
    }

    /**
     * 按下menu键后的事件响应
     * @return
     */
    @Override
    public boolean isKeyMenuDisable() {
        XUILayer layer = getTopLayer();
        if (layer != null) {
            return layer.onMenu();
        }
        return true;
    }

    @Override
    public String getName() {
        return "LBSTask";
    }

    /**
     * 发送对话框消息
     * @param what 对话框的消息
     * @param data 需要附加的数据
     */
    private void sendDialogMsg(int what, Bundle data) {
        Message msg = dialogHandler.obtainMessage();
        msg.what = what;
        if (data != null) {
            msg.setData(data);
        }
        dialogHandler.sendMessage(msg);
    }

    private static final int SHOW_WARNING_DIALOG = 1;
    /**
     * 异步显示对话框的handler
     */
    private Handler dialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_WARNING_DIALOG:
                    Bundle data = msg.getData();
                    String title = data.getString("title");
                    String message = data.getString("msg");
                    DialogUtil.createWarningDialog(FMain.this).show(title, message);
                    break;
            }
        }
    };


    private Handler mainFrameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 系统基础
                case MainMsg.BACK:
                    isBackKeyDisabled();
                    break;
                case MainMsg.LOGOUT:
                    XUILayer layer = getTopLayer();
                    if (layer != null) {
                        removeLayer(layer);
                        if (loginLayer != null) {
                            loginLayer.setVisibility(true);
                        } else {
                            loginLayer = new LLogin(FMain.this);
                            addLayer(loginLayer);
                        }
                        SystemMgr.initSystem(getContext());
                        mainLayer = new LMain(FMain.this);
                    }
                    break;
                case MainMsg.LOGIN_REFRESH:
                    if(mainLayer != null) {
                        mainLayer.refresh();
                    }
                    break;
                case MainMsg.GO_TO_LOGIN:
                    loginLayer = new LLogin(FMain.this);
                    addLayer(loginLayer);
                    break;
                // 初始化主界面（Logo中调用）
                case MainMsg.INIT_MAIN:
                    mainLayer = new LMain(FMain.this);
                    break;
                // 进入主界面
                case MainMsg.GO_TO_MAIN:
                    if(mainLayer == null) {
                        mainLayer = new LMain(FMain.this);
                    }
                    addLayer(mainLayer);
                    mainLayer.firstRefresh();// 跳入第一个子项，历史十大刷新
                    break;
                // 浏览版面
                case MainMsg.SEE_BOARD: {
                    Bundle bundle = msg.getData();
                    String board = bundle.getString("board");
                    boardLayer = new LBoard(FMain.this, board);
                    addLayer(boardLayer);
                    break;
                }
                case MainMsg.SEE_BOARD_BACK:
                    removeLayer(boardLayer);
                    boardLayer = null;
                    break;
                // 看帖回帖发帖
                case MainMsg.SEE_ARTICLE_DETAIL: {
                    Bundle bundle = msg.getData();
                    String board = bundle.getString("board");
                    String id = bundle.getString("id");
                    readArticleLayer = new LReadArticle(FMain.this, id, board);
                    addLayer(readArticleLayer);
                    break;
                }
                case MainMsg.SEE_ARTICLE_BACK:
                    removeLayer(readArticleLayer);
                    readArticleLayer = null;
                    break;
                case MainMsg.WRITE_ARTICLE: {
                    Bundle bundle = msg.getData();
                    String board = bundle.getString("board");
                    writeArticleLayer = new LWriteArticle(FMain.this, board);
                    addLayer(writeArticleLayer);
                    break;
                }
                case MainMsg.WRITE_ARTICLE_BACK:
                    removeLayer(writeArticleLayer);
                    writeArticleLayer = null;
                    if (msg.arg1 != 0) {// 如果发帖成功，自动刷新  TODO
//                        mainLayer.refreshAfterWriteArticle();
                    }
                    break;
                case MainMsg.BBS_REPLY_ARTICLE: {
                    Bundle bundle = msg.getData();
                    String id = bundle.getString("id");
                    String board = bundle.getString("board");
                    String title = bundle.getString("title");
                    String content = bundle.getString("content");
                    int floor = bundle.getInt("floor");
                    String floorName = bundle.getString("author");
                    replyArticleLayer = new LReplyArticle(FMain.this, id, board, title, content, floor, floorName);
                    addLayer(replyArticleLayer);
                    break;
                }
                case MainMsg.BBS_REPLY_ARTICLE_BACK:
                    removeLayer(replyArticleLayer);
                    replyArticleLayer = null;
                    if (msg.arg1 != 0 && readArticleLayer != null) {// 如果回帖成功，自动刷新
                        readArticleLayer.refreshArticle();
                    }
                    break;
                // 看站内信/发站内信
                case MainMsg.SEE_MAIL_DETAIL: {
                    Bundle bundle = msg.getData();
                    String id = bundle.getString("id");
                    readMailLayer = new LReadMail(FMain.this, id);
                    addLayer(readMailLayer);
                    break;
                }
                case MainMsg.SEE_MAIL_DETAIL_BACK:
                    removeLayer(readMailLayer);
                    readMailLayer = null;
                    break;
                case MainMsg.WRITE_MAIL: {
                    Bundle bundle = msg.getData();
                    String title = bundle.getString("title");
                    String content = bundle.getString("content");
                    String receiver = bundle.getString("receiver");
                    writeMailLayer = new LWriteMail(FMain.this, title, content, receiver);
                    addLayer(writeMailLayer);
                    break;
                }
                case MainMsg.WRITE_MAIL_BACK:
                    removeLayer(writeMailLayer);
                    writeMailLayer = null;
                    break;
                // 上一篇、下一篇帖子
                case MainMsg.SEE_PRE_ARTICLE: {
                    Linear<ArticleBase> secondLayer = (Linear<ArticleBase>) getSecondTopLayer();
                    if (secondLayer != null) {
                        ArticleBase article = secondLayer.getPre();
                        if (article == null) {
                            Toast.makeText(getContext(), "木有上一篇帖子了~", Toast.LENGTH_SHORT).show();
                        } else {
                            readArticleLayer.showArticle(article.getId(), article.getBoard());
                        }
                    }
                    break;
                }
                case MainMsg.SEE_NEXT_ARTICLE: {
                    Linear<ArticleBase> secondLayer = (Linear<ArticleBase>) getSecondTopLayer();
                    if (secondLayer != null) {
                        ArticleBase article = secondLayer.getNext();
                        if (article == null) {
                            Toast.makeText(getContext(), "木有下一篇帖子了~", Toast.LENGTH_SHORT).show();
                        } else {
                            readArticleLayer.showArticle(article.getId(), article.getBoard());
                        }
                    }
                    break;
                }
                // 上一封、下一封站内信
                case MainMsg.SEE_PRE_MAIL:
                    if (mainLayer != null) {
                        Mail mail = mainLayer.seePreMail();
                        if (mail == null) {
                            Toast.makeText(getContext(), "木有上一封站内信了~", Toast.LENGTH_SHORT).show();
                        }else {
                            readMailLayer.showMail(mail.getId());
                        }
                    }
                    break;
                case MainMsg.SEE_NEXT_MAIL:
                    if (mainLayer != null) {
                        Mail mail = mainLayer.seeNextMail();
                        if (mail == null) {
                            Toast.makeText(getContext(), "木有下一封站内信了~", Toast.LENGTH_SHORT).show();
                        }else {
                            readMailLayer.showMail(mail.getId());
                        }
                    }
                    break;
                case MainMsg.SEE_IMAGE_DETAIL:
                    int initIndex = msg.arg1;
                    List<String> imageUrls = (List<String>) msg.obj;
                    addLayer(new LImageDetail(FMain.this, imageUrls, initIndex));
                    break;
            }
        }
    };

    private void quitApp() {
        // 停止刷新状态（邮件）
        BbsMailMgr.getInstance().stopMailRemindTask();
        // 退出程序自动注销
        GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.
                getInstance().getSource(SourceName.GLOBAL_STATE);
        SystemSettingSource systemSettingSource = (SystemSettingSource) DataRepo.
                getInstance().getSource(SourceName.SYSTEM_SETTING);
        if (globalStateSource.isLogin() && systemSettingSource.isAutoLogout()) {
            globalStateSource.setCurrentUser("", "");
            // 启动service
            XLog.d("SERVICE", "启动自动注销service！");
            Intent intent = new Intent(LogoutService.ACTION_BACKGROUND);
            intent.setClass(getContext(), LogoutService.class);
            String bbsCode = globalStateSource.getBbsCode();
            intent.putExtra("bbsCode", bbsCode);
            getContext().startService(intent);
        }
        // 退出前清空整个系统，如：临时文件，管理器等
        SystemMgr.clearSystem();
        exit();
    }

    /**
     * 两次back键退出程序
     * @return
     */
    @Override
    public int back() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackTime <= PRESS_BACK_INTERVAL) {
            quitApp();
        } else {
            lastBackTime = currentTime;
            Toast.makeText(getContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
        }
        return XBackType.SELF_BACK;
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        XLog.d("MAIN", "onConfigurationChanged!!@@@&&");
    }
}
