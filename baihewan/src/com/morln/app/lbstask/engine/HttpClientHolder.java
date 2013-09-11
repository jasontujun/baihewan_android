package com.morln.app.lbstask.engine;

import android.content.Context;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.apache.XApacheHttpClient;

/**
 * Created by jasontujun.
 * Date: 12-4-11
 * Time: 下午7:18
 */
public class HttpClientHolder {

    private static Context context;
    private static String userAgent;// 客户端程序名称

    private static XHttp mainClient;// 主要的通信线程池
    private static XHttp imageClient;// 图片的通信线程池

    /**
     * 请使用getApplicationContext()来初始化
     */
    public static void init(Context c, String ua) {
        context = c;
        userAgent = ua;
    }

    public static synchronized XHttp getMainHttpClient() {
        if (mainClient == null) {
            mainClient = new XApacheHttpClient(context, userAgent);
//            mainClient = new XJavaHttpClient(context, userAgent);
        }
        return mainClient;
    }

    public static synchronized XHttp getImageHttpClient() {
        if (imageClient == null) {
            imageClient = new XApacheHttpClient(context, userAgent);
//            imageClient = new XJavaHttpClient(context, userAgent);
        }
        return imageClient;
    }
}
