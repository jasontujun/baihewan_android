package com.morln.app.lbstask.newui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class ActivityMain extends FragmentActivity {

    private static final int PRESS_BACK_INTERVAL = 1500; // back按键间隔，单位：毫秒
    private long lastBackTime;// 上一次back键的时间

    private ViewPager mDragLayer;// 可拖动图层
    private Fragment mLeftFragment;// 左边菜单栏
    private Fragment mRightFragment;// 左边菜单栏
    private FragmentMiddle mMiddleFragment;// 中间主界面
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
        mLeftFragment = new FragmentLeft();
        mRightFragment = new FragmentRight();
        mMiddleFragment = new FragmentMiddle();
        mDragLayer.setAdapter(new DragLayerAdapter(getSupportFragmentManager()));
        mDragLayer.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mShowingMenu = (position == 0);
            }
        });
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
                } else if (!mShowingMenu) {
                    mDragLayer.setCurrentItem(0);
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
                    mDragLayer.setCurrentItem(0);
                else
                    mDragLayer.setCurrentItem(1);
                return true;
        }
        return false;
    }

    /**
     * 获取对每个fragment子页面的菜单按钮监听
     * @return
     */
    public View.OnClickListener getMenuBtnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShowingMenu)
                    mDragLayer.setCurrentItem(1);
                else
                    mDragLayer.setCurrentItem(0);
            }
        };
    }

    public AdapterView.OnItemClickListener getMenuItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 切换成相应的Fragment界面
                mMiddleFragment.selectMenu(i);
                // 收起侧边栏
                if (mShowingMenu)
                    mDragLayer.setCurrentItem(1);
            }
        };
    }


    /**
     * 当前主题页面下添加子页面
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        mMiddleFragment.addFragment(fragment);
    }


    /**
     * 三页式容器的adapter
     */
    private class DragLayerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_PAGES = 3;

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
            if (position == 0)
                return mLeftFragment;
            if (position == 2)
                return mRightFragment;
            else
                return mMiddleFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public float getPageWidth(int position) {
            if (position == 0)
                return mLeftWidthProportion;
            else if (position == 2)
                return mRightWidthProportion;
            else
                return 1.f;
        }
    }
}
