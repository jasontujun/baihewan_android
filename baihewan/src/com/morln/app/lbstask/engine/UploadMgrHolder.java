package com.morln.app.lbstask.engine;

import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.series.XSerialUploadMgr;
import com.xengine.android.session.upload.XHttpUploadMgr;
import com.xengine.android.session.upload.XUploadMgr;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午3:04
 * To change this template use File | Settings | File Templates.
 */
public class UploadMgrHolder {

    private static XUploadMgr mUploadMgrInstance;
    private static XSerialUploadMgr mSerialUploadMgrInstance;
    private static XHttp mHttpClient;

    public static void init(XHttp httpClient) {
        mHttpClient = httpClient;
    }

    public static synchronized XUploadMgr getUploadMgr() {
        if (mUploadMgrInstance == null)
            mUploadMgrInstance = new XHttpUploadMgr(mHttpClient);
        return mUploadMgrInstance;
    }

    public static synchronized XSerialUploadMgr getSerialUploadMgr() {
        if (mSerialUploadMgrInstance == null)
            mSerialUploadMgrInstance = new XSerialUploadMgr(getUploadMgr());
        return mSerialUploadMgrInstance;
    }
}
