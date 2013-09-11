package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.data.cache.ImageSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.media.image.loader.XScrollLocalLoader;

/**
 * Created with IntelliJ IDEA.
 * User: tujun
 * Date: 13-8-7
 * Time: 上午9:48
 * To change this template use File | Settings | File Templates.
 */
public class MyImageScrollLocalLoader extends XScrollLocalLoader {

    private static MyImageScrollLocalLoader instance;

    public synchronized static MyImageScrollLocalLoader getInstance() {
        if (instance == null) {
            instance = new MyImageScrollLocalLoader();
        }
        return instance;
    }

    private MyImageScrollLocalLoader() {
        super();
    }

    @Override
    public String getLocalImage(String imgUrl) {
        ImageSource imageSource = (ImageSource) DefaultDataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }
}
