package com.morln.app.lbstask.utils.img;

import com.morln.app.lbstask.session.HttpClientHolder;
import com.xengine.android.media.image.XBaseImageDownloadMgr;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * 继承自XBaseImageDownloadMgr，负责图片的下载管理。
 * Created by jasontujun.
 * Date: 12-11-1
 * Time: 下午10:45
 */
public class BbsImgDownloadMgr extends XBaseImageDownloadMgr {

    public BbsImgDownloadMgr(int screenWidth, int screenHeight) {
        super(screenWidth, screenHeight);
    }

    @Override
    public HttpResponse download(String imgUrl) {
        HttpGet httpGet = new HttpGet(imgUrl);
        return HttpClientHolder.getImageHttpClient().execute(httpGet, false);
    }
}
