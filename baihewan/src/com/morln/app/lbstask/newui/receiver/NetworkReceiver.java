package com.morln.app.lbstask.newui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * 监听网络状况的变化。
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-9-2
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public class NetworkReceiver extends BroadcastReceiver {

    /**
     * 此方法默认在UI主线程中执行。
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
            return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();

        if (activeInfo == null)  //如果无网络连接activeInfo为null
            Toast.makeText(context, "网络链接已断开！", Toast.LENGTH_SHORT).show();
         else
            switch (activeInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    Toast.makeText(context, "数据流量连接成功！", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    Toast.makeText(context, "wifi网络连接成功！", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    Toast.makeText(context, "wimax网络连接成功！", Toast.LENGTH_SHORT).show();
                    break;
            }
    }

}
