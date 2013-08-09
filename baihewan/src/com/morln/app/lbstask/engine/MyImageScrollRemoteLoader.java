package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.media.image.loader.XScrollRemoteLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 上午9:48
 * To change this template use File | Settings | File Templates.
 */
public class MyImageScrollRemoteLoader extends XScrollRemoteLoader {

    private static MyImageScrollRemoteLoader instance;

    public synchronized static MyImageScrollRemoteLoader getInstance() {
        if (instance == null) {
            instance = new MyImageScrollRemoteLoader();
        }
        return instance;
    }

    private MyImageScrollRemoteLoader() {
        super(ImgMgrHolder.getImageDownloadMgr(),
                MyImageScrollLocalLoader.getInstance());
    }

    @Override
    public String getLocalImage(String imgUrl) {
        ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }

    @Override
    public void setLocalImage(String imgUrl, String localImageFile) {
        ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        imageSource.putImage(imgUrl, localImageFile);
    }
}
