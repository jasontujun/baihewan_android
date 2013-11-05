package com.morln.app.lbstask.engine;

import android.content.Context;
import com.xengine.android.media.graphics.XAndroidScreen;
import com.xengine.android.media.graphics.XScreen;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-13
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */
public class ScreenHolder {

    private static Context context;

    private static XScreen mScreen;

    /**
     * 请使用getApplicationContext()来初始化
     */
    public static void init(Context c) {
        context = c;
    }

    public static synchronized XScreen getInstance() {
        if (mScreen == null) {
            mScreen = new XAndroidScreen(context);
        }
        return mScreen;
    }
}
