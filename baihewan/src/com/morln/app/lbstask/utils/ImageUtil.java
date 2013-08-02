package com.morln.app.lbstask.utils;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.engine.ImgMgrHolder;
import com.xengine.android.media.image.loader.XImageLocalUrl;
import com.xengine.android.session.series.XSerialDownloadListener;
import com.xengine.android.utils.XStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片下载和上传的辅助类。
 * 下载部分：主要封装了对ImageSource的操作
 * 上传部分：……
 * Created by jasontujun.
 * Date: 12-9-16
 * Time: 下午8:36
 */
public class ImageUtil {

    /**
     * 下载图片,下载完成后通知界面更新。
     * (添加进线性下载队列尾部，并启动下载)
     * @param imgUrl
     * @param listener
     * @return 返回true，表示正在下载。返回false表示内存或本地已经加载了
     */
    public static void serialDownloadImage(String imgUrl,
                                           final XSerialDownloadListener listener) {
        final ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        // 图片设置成正在下载
        imageSource.putImage(imgUrl, XImageLocalUrl.IMG_LOADING);

        // 添加进任务队列末尾。对listener再添加一层(将imgUrl添加进ImageSource)
        ImgMgrHolder.getImageSerialDownloadMgr().startTask(
                imgUrl,
                new XSerialDownloadListener() {
                    @Override
                    public void onStart(String url) {
                        if (listener != null)
                            listener.onStart(url);
                    }

                    @Override
                    public void doDownload(String url, long l) {
                    }

                    @Override
                    public void onComplete(String url, String result) {
                        if (!XStringUtil.isNullOrEmpty(result))
                            imageSource.putImage(url, result);
                        else
                            imageSource.putImage(url, XImageLocalUrl.IMG_ERROR);
                        if (listener != null)
                            listener.onComplete(url, result);
                    }

                    @Override
                    public void onError(String url, String error) {
                        imageSource.putImage(url, XImageLocalUrl.IMG_ERROR);
                        if (listener != null)
                            listener.onError(url, error);
                    }

                    @Override
                    public void beforeDownload(String url) {
                        if (listener != null)
                            listener.beforeDownload(url);
                    }

                    @Override
                    public void afterDownload(String url) {
                        if (listener != null)
                            listener.beforeDownload(url);
                    }
                });
    }


    /**
     * 线性下载一堆图片,每一张下载完成后通知界面更新。
     * (添加进线性下载队列尾部，并启动下载)
     * @param imgUrlList
     * @param listenerList
     * @return
     */
    public static void serialDownloadImage(List<String> imgUrlList,
                                           final List<XSerialDownloadListener> listenerList) {
        if (imgUrlList == null || listenerList == null) {
            return;
        }

        final ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);

        // 将每个imgUrl对应的本地图片设置成正在下载
        for (int i = 0; i < imgUrlList.size(); i++) {
            String imgUrl = imgUrlList.get(i);
            String localImgFile = imageSource.getLocalImage(imgUrl);
            if (XStringUtil.isNullOrEmpty(localImgFile) ||
                    XImageLocalUrl.IMG_ERROR.equals(localImgFile))
                imageSource.putImage(imgUrl, XImageLocalUrl.IMG_LOADING);
        }

        // 对每个listener再添加一层(将imgUrl添加进ImageSource)
        List<XSerialDownloadListener> wrapperListenerList =
                new ArrayList<XSerialDownloadListener>();
        for (int i = 0; i < listenerList.size(); i++) {
            final XSerialDownloadListener listener = listenerList.get(i);
            XSerialDownloadListener wrapperListener = new XSerialDownloadListener() {
                @Override
                public void onStart(String url) {
                    if (listener != null)
                        listener.onStart(url);
                }

                @Override
                public void doDownload(String url, long l) {
                }

                @Override
                public void onComplete(String url, String result) {
                    if (!XStringUtil.isNullOrEmpty(result))
                        imageSource.putImage(url, result);
                    else
                        imageSource.putImage(url, XImageLocalUrl.IMG_ERROR);
                    if (listener != null)
                        listener.onComplete(url, result);
                }

                @Override
                public void onError(String url, String error) {
                    imageSource.putImage(url, XImageLocalUrl.IMG_ERROR);
                    if (listener != null)
                        listener.onError(url, error);
                }

                @Override
                public void beforeDownload(String url) {
                    if (listener != null)
                        listener.beforeDownload(url);
                }

                @Override
                public void afterDownload(String url) {
                    if (listener != null)
                        listener.beforeDownload(url);
                }
            };
            wrapperListenerList.add(wrapperListener);
        }

        ImgMgrHolder.getImageSerialDownloadMgr().startTasks(imgUrlList, wrapperListenerList);
    }

}
