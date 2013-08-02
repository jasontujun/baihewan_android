package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.Gson;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemUserSource;
import com.morln.app.lbstask.model.UserBase;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.friend.Friend;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.utils.XLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beryl.
 * Date: 12-3-10
 * Time: 下午4:12
 */
public class FriendAPINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrl;

    public FriendAPINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrl = res.getString(R.string.friend_sync);
    }


    /**
     * 上传好友接口
     * @param userName
     * @param friendList
     * @return
     */
    public int syncFriend(String userName, List<Friend> friendList) {
        try {
            HttpPost request = new HttpPost(hostUrl + apiUrl);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userName", userName));
            params.add(new BasicNameValuePair("friendList", new Gson().toJson(friendList)));
            request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

            HttpResponse response = http.execute(request, false);

            int status = response.getStatusLine().getStatusCode();
            XLog.d("API", "上传好友的返回返回码:" + status);

            if (StatusCode.isSuccess(status)) {
                SystemUserSource systemUserSource = (SystemUserSource) DataRepo.
                        getInstance().getSource(SourceName.SYSTEM_USER);
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("friendTimeStamp")) {
                        UserBase user = systemUserSource.getById(userName);
                        if(user != null) {
                            long friendTimeStamp = Long.parseLong(header.getValue());
                            user.setFriendTimeStamp(friendTimeStamp);
                            XLog.d("API", "更新好友时间戳：" + friendTimeStamp);
                            systemUserSource.saveToDatabase();
                        }
                        break;
                    }
                }
            }
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
