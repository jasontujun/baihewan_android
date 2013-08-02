package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.reflect.TypeToken;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemUserSource;
import com.morln.app.lbstask.model.UserBase;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.CollectionArticle;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

            HttpPost request = new HttpPost(hostUrl + apiUrl);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userName", userName));
            params.add(new BasicNameValuePair("timeStamp", String.valueOf(timeStamp)));
            params.add(new BasicNameValuePair("articleList", GsonUtil.toString(articleList)));
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            
            HttpResponse response = http.execute(request, false);
            if(response == null){
                return StatusCode.HTTP_EXCEPTION;
            }

            int status = response.getStatusLine().getStatusCode();
            XLog.d("API", "同步收藏的返回码:" + status);
                SystemUserSource systemUserSource = (SystemUserSource)
                        DataRepo.getInstance().getSource(SourceName.SYSTEM_USER);
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (header.getName().equals("collectionTimeStamp")) {
                    UserBase user = systemUserSource.getById(userName);
                    if(user != null) {
                        long collectionTimeStamp = Long.parseLong(header.getValue());
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
                InputStream is = response.getEntity().getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<ArrayList<CollectionArticle>>() {
                    }.getType();
                    List<CollectionArticle> list = (ArrayList<CollectionArticle>)
                            GsonUtil.toObjectArray(cnt, type);
                    articleList.addAll(list);
                }
                is.close();
            }
            response.getEntity().consumeContent();
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
