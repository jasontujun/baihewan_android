package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.data.cache.ImageSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.media.image.loader.XImageViewLocalLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class MyImageViewLocalLoader extends XImageViewLocalLoader {

    private static MyImageViewLocalLoader instance;

    public synchronized static MyImageViewLocalLoader getInstance() {
        if (instance == null) {
            instance = new MyImageViewLocalLoader();
        }
        return instance;
    }

    private MyImageViewLocalLoader() {
        super();
    }

    @Override
    public String getLocalImage(String imgUrl) {
        ImageSource imageSource = (ImageSource) DefaultDataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }
}
