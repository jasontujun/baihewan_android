package com.morln.app.lbstask.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Mail;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.controls.DragLayer;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.about.CAbout;
import com.morln.app.lbstask.ui.board.CBoardAndSearch;
import com.morln.app.lbstask.ui.collection.CRssAndCollection;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.ui.mail.CMail;
import com.morln.app.lbstask.ui.person.CArticleAndInfo;
import com.morln.app.lbstask.ui.setting.CSetting;
import com.morln.app.lbstask.ui.top10.CTopAndHot;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XBaseLayer;
import com.xengine.android.system.ui.XUIFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面类。
 * Created by jasontujun.
 * Date: 12-2-24
 * Time: 下午9:28
 */
public class LMain extends XBaseLayer implements Linear<ArticleBase> {
    private GlobalStateSource globalStateSource;

    private String otherUserId;// 被查看的用户ID
    private int currentScreen;// 当前屏
    private int currentComponentIndex;// 当前子界面的编号

    // 界面
    private RelativeLayout frame;
    private DragLayer dragFrame;// 可拖动图层
    private RelativeLayout contentFrame;
    private CFunctionBar functionBar;
    private CFriendBar friendBar;
    // 各功能界面
    private CTopAndHot cTopAndHot;
    private CBoardAndSearch cBoardAndSearch;
    private CArticleAndInfo myArticleAndInfo;
    private CRssAndCollection cRssAndCollection;
    private CArticleAndInfo otherArticleAndInfo;
    private CMail cMail;
    private CSetting cSetting;
    private CAbout cAbout;
    private List<XBaseComponent> componentList = new ArrayList<XBaseComponent>();
    // 教程图层
    private CTutorial cTutorial;
    private boolean tutorialVisible;

    public LMain(XUIFrame uiFrame) {
        super(uiFrame);
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.main_frame);
        frame = (RelativeLayout) findViewById(R.id.frame);
        dragFrame = (DragLayer) findViewById(R.id.drag_frame);
        dragFrame.setBufferDragging(screen().dp2px(20));
        dragFrame.setMarginSize(screen().dp2px(50));
        dragFrame.setOnViewSwitchListener(new DragLayer.ViewSwitchListener() {
            @Override
            public void onSwitched(View view, int position) {
                currentScreen = position;
                // 弹出左侧教程
                if (position == 0) {
                    showTutorial(CTutorial.TUTORIAL_TYPE_LEFT_BAR);
                }
            }
        });

        // 中间三屏式容器
        contentFrame = new RelativeLayout(getContext());
        contentFrame.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        // 左边栏
        functionBar = new CFunctionBar(this);
        functionBar.setOtherInfoItemVisible(false);
        // 右边栏
        friendBar = new CFriendBar(this);

        cTopAndHot = new CTopAndHot(this);
        cBoardAndSearch = new CBoardAndSearch(this);
        myArticleAndInfo = new CArticleAndInfo(this);
        cRssAndCollection = new CRssAndCollection(this);
        otherArticleAndInfo = new CArticleAndInfo(this);
        cMail = new CMail(this);
        cSetting = new CSetting(this);
        cAbout = new CAbout(this);
        componentList.add(cTopAndHot);
        componentList.add(cRssAndCollection);
        componentList.add(cBoardAndSearch);
        componentList.add(myArticleAndInfo);
        componentList.add(otherArticleAndInfo);
        componentList.add(cMail);
        componentList.add(cSetting);
        componentList.add(cAbout);

        // 设置三屏式容器的内容（左、中、右）
        dragFrame.setContent(functionBar.getContent(),
                contentFrame,
                friendBar.getContent());

        // 初始化教程
        cTutorial = new CTutorial(this);
        tutorialVisible = false;
    }


    private boolean firstRefresh = true;
    /**
     * 主界面第一次初始化。
     * 设置第一个版面(先显示十大)
     */
    public void firstRefresh() {
        currentScreen = 1;
        currentComponentIndex = 0;

        refresh();

        if (firstRefresh) {
            firstRefresh = false;
            contentFrame.addView(componentList.get(currentComponentIndex).getContent());
            cTopAndHot.firstGetTop10();
        }
    }

    /**
     * 注册三页式容器的横向拖动监听
     * @param listener
     */
    public void registerDragHorizontallyListener(DragLayer.DragHorizontallyListener listener) {
        dragFrame.registerDragHorizontallyListener(listener);
    }


    /**
     * 显示教程
     * @param type
     */
    public void showTutorial(int type) {
        if (tutorialVisible) {
            return;
        }
        // 已经读过。则退出。
        switch (type) {
            case CTutorial.TUTORIAL_TYPE_LEFT_BAR:
                if (globalStateSource.getTutorialLeftBar()) {
                    return;
                }
                break;
            case CTutorial.TUTORIAL_TYPE_TOP10:
                if (globalStateSource.getTutorialTop10()) {
                    return;
                }
                break;
            case CTutorial.TUTORIAL_TYPE_RSS:
                if (globalStateSource.getTutorialRss()) {
                    return;
                }
                break;
        }

        cTutorial.show(type);
        frame.addView(cTutorial.getContent());
        tutorialVisible = true;
    }

    /**
     * 隐藏教程
     */
    public void hideTutorial() {
        if (!tutorialVisible) {
            return;
        }
        tutorialVisible = false;
        frame.removeView(cTutorial.getContent());
    }


    /**
     * 功能切换(用于左边栏)
     * @param index
     */
    public void switchBoardFromLeft(int index) {
        // 登陆权限检测
        if (index == 1 || index == 3 || index == 5) {
            if (!globalStateSource.isLogin()) {
                new DLogin(this, true).show();
                return;
            }
        }

        // funcBar的图片效果
        functionBar.selectItem(index);

        // funBar自动收缩
        dragFrame.snapToScreen(1);


        // 对应主界面的切换
        if (0 <= index && index < componentList.size() && index != currentComponentIndex) {
            contentFrame.removeView(componentList.get(currentComponentIndex).getContent());
            contentFrame.addView(componentList.get(index).getContent());
            currentComponentIndex = index;
            if (index == 1) {
                showTutorial(CTutorial.TUTORIAL_TYPE_RSS);
            }
            if (index == 3) {
                myArticleAndInfo.show(globalStateSource.getCurrentUserName());
            }
            else if (index == 4) {
                otherArticleAndInfo.show(otherUserId);
            }
            else if (index == 5) {
                cMail.initMailList();// 刷新站内信列表
            }
        }
    }

    /**
     * 功能切换(用于右边栏)
     */
    public void switchBoardFromRight(String userId) {
        this.otherUserId = userId;

        // 收起侧边栏
        dragFrame.snapToScreen(1);


        // funcBar的图片效果
        functionBar.selectItem(4);
        functionBar.setOtherInfoItemVisible(true);
        functionBar.initOtherId(userId);

        // 对应主界面的切换 TIP 应该异步执行下列动作，否则界面会不流畅
        contentFrame.removeView(componentList.get(currentComponentIndex).getContent());
        contentFrame.addView(componentList.get(4).getContent());
        currentComponentIndex = 4;
        CArticleAndInfo cArticleAndInfo = (CArticleAndInfo)componentList.get(4);
        cArticleAndInfo.show(otherUserId);
    }


    /**
     * 调用对应子组件查看下一篇帖子
     */
    public ArticleBase getNext() {
        Linear<ArticleBase> l = (Linear<ArticleBase>) componentList.get(currentComponentIndex);
        return l.getNext();
    }

    /**
     * 调用对应子组件查看上一篇帖子
     */
    public ArticleBase getPre() {
        Linear<ArticleBase> l = (Linear<ArticleBase>) componentList.get(currentComponentIndex);
        return l.getPre();
    }




    /**
     * 调用对应子组件查看下一篇帖子
     */
    public Mail seeNextMail() {
        Linear<Mail> l = (Linear<Mail>) componentList.get(currentComponentIndex);
        return l.getNext();
    }

    /**
     * 调用对应子组件查看上一篇帖子
     */
    public Mail seePreMail() {
        Linear<Mail> l = (Linear<Mail>) componentList.get(currentComponentIndex);
        return l.getPre();
    }

    public void refresh() {
        functionBar.refreshTopFrame();
        friendBar.allRefresh();
        cRssAndCollection.refresh();
    }


    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();

        componentList.get(currentComponentIndex).onLayerUnCovered();
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();

        componentList.get(currentComponentIndex).onLayerCovered();
    }


    @Override
    public int back() {
        // 隐藏教程
        if (tutorialVisible) {
            hideTutorial();
            return XBackType.CHILD_BACK;
        }
        // 右侧栏返回
        if (currentScreen == 2) {
            dragFrame.snapToScreen(1);
            return XBackType.CHILD_BACK;
        }
        // 左侧栏退出
        if (currentScreen == 0) {
            return XBackType.NOTHING_TO_BACK;
        }

        // 中间模块某个模块
        int result = componentList.get(currentComponentIndex).back();
        if (result != XBackType.NOTHING_TO_BACK) {
            return result;
        }
        // 左侧栏弹出
        dragFrame.snapToScreen(0);
        return XBackType.CHILD_BACK;
    }


    @Override
    public boolean onMenu() {
        if (currentScreen == 1) {
            dragFrame.snapToScreen(0);
            return true;
        }
        if (currentScreen == 0 || currentScreen == 2) {
            dragFrame.snapToScreen(1);
            return true;
        }

        return false;
    }


    @Override
    public Handler getLayerHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 教程
                case BbsMsg.TUTORIAL_SHOW:
                    showTutorial(msg.arg1);
                    break;
                case BbsMsg.TUTORIAL_HIDE:
                    hideTutorial();
                    break;
                // 横屏切换
                case BbsMsg.SWITCH_LEFT:
                    if (currentScreen != 0) {
                        dragFrame.snapToScreen(0);
                    } else {
                        dragFrame.snapToScreen(1);
                    }
                    break;
                case BbsMsg.SWITCH_RIGHT:
                    if (currentScreen != 2) {
                        dragFrame.snapToScreen(2);
                    } else {
                        dragFrame.snapToScreen(1);
                    }
                    break;
                // 弹出左侧菜单
                case BbsMsg.BBS_FUNC_SWITCH:
                    switchBoardFromLeft(msg.arg1);
                    break;
                // 弹出右侧好友
                case BbsMsg.BBS_PERSON_INFO:{
                    Bundle bundle = msg.getData();
                    String userId = bundle.getString("id");
                    if (userId != null){
                        switchBoardFromRight(userId);
                    }
                    break;
                }
            }
        }
    };

}
