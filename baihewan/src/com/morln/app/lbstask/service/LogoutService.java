package com.morln.app.lbstask.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.morln.app.lbstask.bbs.session.BbsAPI;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.utils.XLog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jasontujun.
 * Date: 12-7-19
 * Time: 下午7:18
 */
public class LogoutService extends Service{
    public static final String ACTION_BACKGROUND = "com.mroln.app.baihewan.service.Logout";
    private boolean isRunning;

    private String bbsCode;

    @Override
    public void onCreate() {
        super.onCreate();
        XLog.d("SERVICE", "create service");

        GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        if(globalStateSource == null) {
            globalStateSource = new GlobalStateSource(getApplicationContext());
        }
        bbsCode = globalStateSource.getBbsCode();
        isRunning = false;

        // 启动轮询任务相关消息
        startRequestBackService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XLog.d("SERVICE", "destroy service");
        isRunning = false;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        XLog.d("SERVICE", "onStart service");
        if(intent == null) {
            return;
        }
        if (ACTION_BACKGROUND.equals(intent.getAction())) {
            startRequestBackService();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        XLog.d("SERVICE", "onStartCommand service");
        if(intent == null) {
            return START_NOT_STICKY;
        }
        if (ACTION_BACKGROUND.equals(intent.getAction())) {
            startRequestBackService();
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_NOT_STICKY;
    }


    /**
     * 启动轮询任务相关消息
     */
    private void startRequestBackService() {
        if(!isRunning) {
            XLog.d("SERVICE", "启动任务");
            RequestMsgTask requestMsgTask = new RequestMsgTask(bbsCode);
            new Timer().schedule(requestMsgTask, 0);
            isRunning = true;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class RequestMsgTask extends TimerTask {
        private String bbsCode;

        RequestMsgTask(String bbsCode) {
            this.bbsCode = bbsCode;
        }

        @Override
        public void run() {
            isRunning = true;

            int resultCode = BbsAPI.logout(bbsCode);
            XLog.d("SERVICE", "注销bbs的返回码："+resultCode);
            if(StatusCode.isSuccess(resultCode)) {
                XLog.d("SERVICE", "注销bbs成功！");
            }else {
                XLog.d("SERVICE", "注销bbs失败！");
            }

            stopSelf();
        }
    }
}
