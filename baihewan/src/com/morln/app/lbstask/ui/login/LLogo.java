package com.morln.app.lbstask.ui.login;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemSettingSource;
import com.morln.app.lbstask.logic.SystemMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.res.SystemPic;
import com.morln.app.lbstask.utils.LoadingWordUtil;
import com.morln.app.system.ui.XBackType;
import com.morln.app.system.ui.XBaseLayer;
import com.morln.app.system.ui.XUIFrame;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;

/**
 * 欢迎界面，是程序的第一个界面。
 * 显示应用的Logo，同时检查系统的设置是否满足要求
 * 检查网络和GPS状态，并提示用户更改
 * Created by jasontujun.
 * Date: 12-3-18
 * Time: 下午9:45
 */
public class LLogo extends XBaseLayer {

    private static final int LOGO_LENGTH = 2500;// logo最短时长

    private long startTime, endTime;

    private Runnable initTask;// UI线程中执行的初始化任务

    private TLogin autoLoginTask;// 自动登录线程


    /**
     * 构造函数，记得调用setContentView()哦
     *
     * @param uiFrame
     */
    public LLogo(XUIFrame uiFrame, Runnable initTask) {
        super(uiFrame);
        this.initTask = initTask;

        setContentView(R.layout.entrance_logo);
        ImageView title = (ImageView) findViewById(R.id.title);
        TextView word = (TextView) findViewById(R.id.loading_word);

        setImageViewPic(title, SystemPic.TITLE);

        String loadingWord = LoadingWordUtil.getInstance().getWord();
        word.setText(loadingWord);
    }

    /**
     * 开始动画计时
     */
    private void startTiming() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 结束动画计时
     */
    private void endTiming(boolean isAutoLogin) {
        endTime = System.currentTimeMillis();
        // 计算差值
        long delta = endTime - startTime;
        XLog.d("FK", "LOGO的时间间隔：" + delta);
        if(delta < LOGO_LENGTH) {
            long remain = LOGO_LENGTH - delta;
            try {
                Thread.sleep(remain);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            // 如果是自动登录，且登录时间过长，则弹出直接跳转的按钮
            if(isAutoLogin) {

                if(autoLoginTask != null) {
                    autoLoginTask.cancel(true);
                    Handler handler = getFrameHandler();
                    handler.sendMessage(handler.obtainMessage(MainMsg.GO_TO_MAIN));
                }
            }
        }
    }

    @Override
    public void onLayerAddedToFrame() {
        // 启动异步线程加载数据
        new AsyncTask<Void, Long, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                startTiming();

                // 初始化数据源、数据库等
                SystemMgr.initSystem(getContext());
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                // 外部传入的初始化任务
                if(initTask != null) {
                    initTask.run();
                }

                // 界面跳转
                GlobalStateSource globalStateSource = (GlobalStateSource)
                        DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
                SystemSettingSource systemSettingSource = (SystemSettingSource)
                        DataRepo.getInstance().getSource(SourceName.SYSTEM_SETTING);
                String userName = globalStateSource.getLastUserName();// 上一次用户名
                String password = globalStateSource.getLastUserPassword();// 上一次密码
                if(!systemSettingSource.isAutoLogin() ||
                        XStringUtil.isNullOrEmpty(userName) ||
                        XStringUtil.isNullOrEmpty(password)) {
                    // 非自动登录状态。（未设置自动登录，或没有记住密码）
                    // 跳转到登陆界面
                    endTiming(false);
                    Handler handler = getFrameHandler();
                    Message msg = handler.obtainMessage(MainMsg.GO_TO_LOGIN);
                    handler.sendMessage(msg);
                }else {
                    // 自动登录状态
                    // 跳转到主界面
                    endTiming(true);
                    autoLoginTask = new TLogin(getUIFrame(), userName, password, new TLogin.LoginListener() {
                        @Override
                        public void onSucceeded(String username, String password) {
                            Handler handler = getFrameHandler();
                            handler.sendMessage(handler.obtainMessage(MainMsg.GO_TO_MAIN));
                        }
                        @Override
                        public void onFailed(String username, String password) {
                            Handler handler = getFrameHandler();
                            handler.sendMessage(handler.obtainMessage(MainMsg.GO_TO_MAIN));
                        }
                        @Override
                        public void onCanceled(String username, String password) {
                        }
                    }, false);
                    autoLoginTask.execute(null);
                }
            }
        }.execute(null);
    }

    @Override
    public Handler getLayerHandler() {
        return null;
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }
}
