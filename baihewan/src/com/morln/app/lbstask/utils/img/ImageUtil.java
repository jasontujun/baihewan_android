package com.morln.app.lbstask.utils.img;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.media.image.XImageDownloadListener;
import com.morln.app.utils.XStringUtil;

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
                                           final XImageDownloadListener listener) {
        final ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        // 图片设置成正在下载
        imageSource.putImage(imgUrl, ImageUrlType.IMG_LOADING);

        // 添加进任务队列末尾。对listener再添加一层(将imgUrl添加进ImageSource)
        ImgMgrHolder.getSerialDownloadMgr().startTask(
                imgUrl,
                new XImageDownloadListener() {
                    @Override
                    public void onBeforeDownload(String id) {
                        if(listener != null)
                            listener.onBeforeDownload(id);
                    }

                    @Override
                    public void onFinishDownload(String id, String result) {
                        if(!XStringUtil.isNullOrEmpty(result)) {
                            imageSource.putImage(id, result);
                        }else {
                            imageSource.putImage(id, ImageUrlType.IMG_ERROR);
                        }
                        if(listener != null)
                            listener.onFinishDownload(id, result);
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
                                           final List<XImageDownloadListener> listenerList) {
        if(imgUrlList == null || listenerList == null) {
            return;
        }

        final ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);

        // 将每个imgUrl对应的本地图片设置成正在下载
        for(int i = 0; i < imgUrlList.size(); i++) {
            String imgUrl = imgUrlList.get(i);
            String localImgFile = imageSource.getLocalImage(imgUrl);
            if(XStringUtil.isNullOrEmpty(localImgFile) ||
                    ImageUrlType.IMG_ERROR.equals(localImgFile))
                imageSource.putImage(imgUrl, ImageUrlType.IMG_LOADING);
        }

        // 对每个listener再添加一层(将imgUrl添加进ImageSource)
        List<XImageDownloadListener> wrapperListenerList =
                new ArrayList<XImageDownloadListener>();
        for(int i = 0; i < listenerList.size(); i++) {
            final XImageDownloadListener listener = listenerList.get(i);
            XImageDownloadListener wrapperListener = new XImageDownloadListener() {
                @Override
                public void onBeforeDownload(String id) {
                    if(listener != null)
                        listener.onBeforeDownload(id);
                }

                @Override
                public void onFinishDownload(String id, String result) {
                    if (!XStringUtil.isNullOrEmpty(result)) {
                        imageSource.putImage(id, result);
                    } else {
                        imageSource.putImage(id, ImageUrlType.IMG_ERROR);
                    }
                    if(listener != null)
                        listener.onFinishDownload(id, result);
                }
            };
            wrapperListenerList.add(wrapperListener);
        }

        ImgMgrHolder.getSerialDownloadMgr().startTasks(imgUrlList, wrapperListenerList);
    }

}
