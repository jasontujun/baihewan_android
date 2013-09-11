package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.reflect.TypeToken;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.cache.SystemUserSource;
import com.morln.app.lbstask.data.model.UserBase;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.CollectionArticle;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fdp.
 * Date: 12-7-17
 * Time: 下午4:48
 */
public class CollectionAPINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrl;

    public CollectionAPINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrl = res.getString(R.string.collection_upload);
    }

    /**
     * 同步个人收藏
     * （如果本地时间戳大，则为下载，返回服务器的时间戳
     * 如果本地时间戳小，则为下载，同时本地更新为服务器的时间戳）
     * @param articleList
     * @param timeStamp
     * @return
     */
    public int syncCollection(String userName, List<CollectionArticle> articleList, long timeStamp) {
        try {
            String s = GsonUtil.toString(articleList);
            XLog.d("API", "同步收藏的帖子:" + s);

            XHttpRequest request = http
                    .newRequest(hostUrl + apiUrl)
                    .setMethod(XHttpRequest.HttpMethod.POST)
                    .addStringParam("userName", userName)
                    .addStringParam("timeStamp", String.valueOf(timeStamp))
                    .addStringParam("articleList", GsonUtil.toString(articleList));
            
            XHttpResponse response = http.execute(request);
            if (response == null)
                return StatusCode.HTTP_EXCEPTION;

            int status = response.getStatusCode();
            XLog.d("API", "同步收藏的返回码:" + status);
                SystemUserSource systemUserSource = (SystemUserSource)
                        DefaultDataRepo.getInstance().getSource(SourceName.SYSTEM_USER);
            Map<String, List<String>> headers = response.getAllHeaders();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey().equals("collectionTimeStamp")) {
                    UserBase user = systemUserSource.getById(userName);
                    if(user != null) {
                        long collectionTimeStamp = Long.parseLong(header.getValue().get(0));
                        user.setCollectionTimeStamp(collectionTimeStamp);
                        XLog.d("API", "返回时，收藏时间戳：" + collectionTimeStamp);
                        systemUserSource.saveToDatabase();
                    }
                    break;
                }
            }
            articleList.clear();// TIP!!
            if(status == StatusCode.ARTICLE_IS_LATEST) {
                //如果服务器是最新的则下载最新的收藏列表
                InputStream is = response.getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<ArrayList<CollectionArticle>>() {}.getType();
                    List<CollectionArticle> list = (ArrayList<CollectionArticle>)
                            GsonUtil.toObjectArray(cnt, type);
                    articleList.addAll(list);
                }
                is.close();
            }
            response.consumeContent();
            return status;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
