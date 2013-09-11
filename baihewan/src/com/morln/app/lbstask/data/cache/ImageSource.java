package com.morln.app.lbstask.data.cache;

import android.text.TextUtils;
import com.xengine.android.data.cache.XDataSource;

import java.util.HashMap;

/**
 * Created by jasontujun.
 * Date: 12-10-5
 * Time: 下午8:44
 */
public class ImageSource implements XDataSource {

    /**
     * 映射 <imageUrl网络地址, localImageFile本地缓存的文件名>
     */
    private HashMap<String, String> images;

    public ImageSource() {
        images = new HashMap<String, String>();
    }

    @Override
    public String getSourceName() {
        return SourceName.IMAGE;
    }

    public boolean containsLocalImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl))
            return false;

        if (!images.containsKey(imageUrl))
            return false;

        String localImageFile = images.get(imageUrl);
        return TextUtils.isEmpty(localImageFile);
    }

    public String getLocalImage(String imageUrl) {
        return images.get(imageUrl);
    }


    public void remove(String imageUrl) {
        images.remove(imageUrl);
    }

    public void putImage(String imageUrl, String localImageFile) {
        images.put(imageUrl, localImageFile);
    }
}
