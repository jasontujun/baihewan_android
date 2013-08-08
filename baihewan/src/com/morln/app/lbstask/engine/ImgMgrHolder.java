package com.morln.app.lbstask.engine;

import com.xengine.android.media.image.download.XHttpImageDownloadMgr;
import com.xengine.android.media.image.download.XImageDownload;
import com.xengine.android.media.image.download.XImageSerialDownloadMgr;
import com.xengine.android.session.http.XHttp;

/**
 * 持有一个图片下载管理器对象和一个线性下载对象
 * Created by jasontujun.
 * Date: 12-11-1
 * Time: 下午10:54
 */
public class ImgMgrHolder {

    private static XImageDownload mImageDownloadMgrInstance;
    private static XImageSerialDownloadMgr mImageSerialDownloadMgrInstance;

    private static XHttp mHttpClient;
    private static int mScreenWidth;
    private static int mScreenHeight;

    public static void init(XHttp httpClient, int sWidth, int sHeight) {
        mHttpClient = httpClient;
        mScreenWidth = sWidth;
        mScreenHeight = sHeight;
    }

    public static synchronized XImageDownload getImageDownloadMgr() {
        if (mImageDownloadMgrInstance == null)
            mImageDownloadMgrInstance = new XHttpImageDownloadMgr(
                    mHttpClient, mScreenWidth, mScreenHeight);
        return mImageDownloadMgrInstance;
    }

    public static synchronized XImageSerialDownloadMgr getImageSerialDownloadMgr() {
        if (mImageSerialDownloadMgrInstance == null)
            mImageSerialDownloadMgrInstance = new XImageSerialDownloadMgr(
                    getImageDownloadMgr());
        return mImageSerialDownloadMgrInstance;
    }
}
