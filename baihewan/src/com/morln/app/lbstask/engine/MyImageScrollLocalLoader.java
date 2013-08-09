package com.morln.app.lbstask.engine;

import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
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
        ImageSource imageSource = (ImageSource) DataRepo.
                getInstance().getSource(SourceName.IMAGE);
        return imageSource.getLocalImage(imgUrl);
    }
}
