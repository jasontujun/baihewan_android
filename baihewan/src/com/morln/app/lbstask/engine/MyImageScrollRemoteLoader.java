package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.data.cache.ImageSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.xengine.android.data.cache.DefaultDataRepo;
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
        super(DownloadMgrHolder.getImageDownloadMgr(),
                MyImageScrollLocalLoader.getInstance());
    }

    @Override
    public String getLocalImage(String imgUrl) {
        ImageSource imageSource = (ImageSource) DefaultDataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }

    @Override
    public void setLocalImage(String imgUrl, String localImageFile) {
        ImageSource imageSource = (ImageSource) DefaultDataRepo.
                getInstance().getSource(SourceName.IMAGE);
        imageSource.putImage(imgUrl, localImageFile);
    }
}
