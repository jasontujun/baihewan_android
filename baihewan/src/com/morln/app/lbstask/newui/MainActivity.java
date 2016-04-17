package com.morln.app.lbstask.newui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.morln.app.lbstask.engine.ScreenHolder;
import com.morln.app.lbstask.logic.SystemMgr;
import com.morln.app.lbstask.newui.receiver.NetworkReceiver;
import com.xengine.android.media.graphics.XScreen;

/**
 * 基础Activity。
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends FragmentActivity {

    private static final int PRESS_BACK_INTERVAL = 1500; // back按键间隔，单位：毫秒
    private long lastBackTime;// 上一次back键的时间

    private ViewPager mDragLayer;// 可拖动图层
    private Fragment mMenuFragment;// 左边菜单栏
    private Fragment mFriendFragment;// 右边好友栏
    private MainContentFragment mContentFragment;// 中间主界面

    private static final int NUM_FRAGMENTS = 3;// 三页式
    private static final int FRAGMENT_MENU = 0;
    private static final int FRAGMENT_CONTENT = 1;
    private static final int FRAGMENT_FRIEND = 2;
    private int mShowingIndex;// 当前显示的Fragment的索引
    private boolean mShowingMenu;// 是否显示左边栏

    private BroadcastReceiver mNetworkReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化系统相关组件
        SystemMgr.getInstance().initEngine(getApplicationContext());
        SystemMgr.getInstance().initSystem(getApplicationContext());

        // ui
        mDragLayer = new ViewPager(this);
        mDragLayer.setId(1);// TIP:手动创建的ViewPager必须设置id
        setContentView(mDragLayer);
        mMenuFragment = new MainMenuFragment();
        mFriendFragment = new MainFriendFragment();
        mContentFragment = new MainContentFragment();
        mDragLayer.setAdapter(new DragLayerAdapter(getSupportFragmentManager()));
        mDragLayer.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mShowingIndex = position;
                mShowingMenu = (mShowingIndex == FRAGMENT_MENU);
            }
        });
        mShowingIndex = FRAGMENT_MENU;
        mShowingMenu = true;// 默认为弹出菜单按钮
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mNetworkReceiver == null)
            mNetworkReceiver = new NetworkReceiver();

        // 注册对网络状况的监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 取消对网络状况的监听
        unregisterReceiver(mNetworkReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return false;
                } else if (mShowingIndex > FRAGMENT_MENU) {
                    mDragLayer.setCurrentItem(mShowingIndex - 1, true);
                    return true;
                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBackTime <= PRESS_BACK_INTERVAL) {
                        SystemMgr.getInstance().clearSystem();
                        finish();
                    } else {
                        lastBackTime = currentTime;
                        Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            case KeyEvent.KEYCODE_MENU:
                if (!mShowingMenu)
                    mDragLayer.setCurrentItem(FRAGMENT_MENU, true);
                else
                    mDragLayer.setCurrentItem(FRAGMENT_CONTENT, true);
                return true;
        }
        return false;
    }

    public void pressMenuBtn() {
        if (mShowingMenu)
            mDragLayer.setCurrentItem(FRAGMENT_CONTENT);
        else
            mDragLayer.setCurrentItem(FRAGMENT_MENU);
    }

    public void selectMenuItem(int i) {
        // 切换成相应的Fragment界面
        mContentFragment.selectMenu(i);
        // 收起侧边栏
        if (mShowingMenu)
            mDragLayer.setCurrentItem(FRAGMENT_CONTENT);
    }

    /**
     * 当前主题页面下添加子页面
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        mContentFragment.addFragment(fragment);
    }


    /**
     * 三页式容器的adapter
     */
    private class DragLayerAdapter extends FragmentPagerAdapter {

        private float mLeftWidthProportion;// 左边栏宽度比例
        private float mRightWidthProportion;// 右边栏宽度比例

        public DragLayerAdapter(FragmentManager fm) {
            super(fm);
            // 计算左边栏的宽度比例
            XScreen screen = ScreenHolder.getInstance();
            float sWidthPx = screen.getScreenWidth();// 单位：像素
            float menuWidthPx = screen.dp2px(60);
            mLeftWidthProportion = (1 - menuWidthPx / sWidthPx);
            mRightWidthProportion = mLeftWidthProportion;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == FRAGMENT_MENU)
                return mMenuFragment;
            if (position == FRAGMENT_FRIEND)
                return mFriendFragment;
            else
                return mContentFragment;
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        @Override
        public float getPageWidth(int position) {
            if (position == FRAGMENT_MENU)
                return mLeftWidthProportion;
            else if (position == FRAGMENT_FRIEND)
                return mRightWidthProportion;
            else
                return 1.f;
        }
    }
}
