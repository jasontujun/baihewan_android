package com.morln.app.lbstask.engine;

import com.xengine.android.media.image.download.XHttpImageDownloadMgr;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.session.download.XDownload;
import com.xengine.android.session.download.XHttpDownloadMgr;
import com.xengine.android.session.download.XSerialDownloadMgr;
import com.xengine.android.session.http.XHttp;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午3:04
 * To change this template use File | Settings | File Templates.
 */
public class DownloadMgrHolder {

    private static int mScreenWidth;
    private static int mScreenHeight;
    private static XHttp mHttpClient;

    private static XDownload mDownloadMgrInstance;
    private static XSerialDownloadMgr mSerialDownloadMgrInstance;
    private static XImageDownload mImageDownloadMgrInstance;

    public static void init(XHttp httpClient, int sWidth, int sHeight) {
        mHttpClient = httpClient;
        mScreenWidth = sWidth;
        mScreenHeight = sHeight;
    }

    public static synchronized XDownload getDownloadMgr() {
        if (mDownloadMgrInstance == null)
            mDownloadMgrInstance = new XHttpDownloadMgr(mHttpClient);
        return mDownloadMgrInstance;
    }

    public static synchronized XSerialDownloadMgr getSerialDownloadMgr() {
        if (mSerialDownloadMgrInstance == null)
            mSerialDownloadMgrInstance = new XSerialDownloadMgr(getDownloadMgr());
        return mSerialDownloadMgrInstance;
    }

    public static synchronized XImageDownload getImageDownloadMgr() {
        if (mImageDownloadMgrInstance == null)
            mImageDownloadMgrInstance = new XHttpImageDownloadMgr(
                    mHttpClient, mScreenWidth, mScreenHeight);
        return mImageDownloadMgrInstance;
    }
}
