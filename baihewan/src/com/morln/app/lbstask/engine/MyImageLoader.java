package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.media.image.loader.XBaseImageLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-2
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class MyImageLoader extends XBaseImageLoader {

    private static MyImageLoader instance;

    public synchronized static MyImageLoader getInstance() {
        if (instance == null) {
            instance = new MyImageLoader();
        }
        return instance;
    }

    private MyImageLoader() {
        super();
    }

    @Override
    public String getLocalImage(String imgUrl) {
        ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }
}
