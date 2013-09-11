package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.Gson;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.cache.SystemUserSource;
import com.morln.app.lbstask.data.model.UserBase;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.friend.Friend;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;
import com.xengine.android.utils.XLog;

import java.util.List;
import java.util.Map;

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
        XHttpRequest request = http
                .newRequest(hostUrl + apiUrl)
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addStringParam("userName", userName)
                .addStringParam("friendList", new Gson().toJson(friendList));

        XHttpResponse response = http.execute(request);

        int status = response.getStatusCode();
        XLog.d("API", "上传好友的返回返回码:" + status);

        if (StatusCode.isSuccess(status)) {
            SystemUserSource systemUserSource = (SystemUserSource) DefaultDataRepo.
                    getInstance().getSource(SourceName.SYSTEM_USER);
            Map<String, List<String>> headers = response.getAllHeaders();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey().equals("friendTimeStamp")) {
                    UserBase user = systemUserSource.getById(userName);
                    if(user != null) {
                        long friendTimeStamp = Long.parseLong(header.getValue().get(0));
                        user.setFriendTimeStamp(friendTimeStamp);
                        XLog.d("API", "更新好友时间戳：" + friendTimeStamp);
                        systemUserSource.saveToDatabase();
                    }
                    break;
                }
            }
        }
        response.consumeContent();
        return status;
    }
}
