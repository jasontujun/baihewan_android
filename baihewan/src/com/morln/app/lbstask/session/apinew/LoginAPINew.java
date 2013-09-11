package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.engine.HttpClientHolder;
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
 * Modified by jasontujun.
 * Date: 12-2-29
 * Time: 下午8:56
 */
public class LoginAPINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrl;

    public LoginAPINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrl = res.getString(R.string.login);
    }


    public int login(String userName, String password) {
        XHttpRequest request = http
                .newRequest(hostUrl + apiUrl)
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addStringParam("userName", userName)
                .addStringParam("password", password);
        XHttpResponse response = http.execute(request);

        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        int resultCode = response.getStatusCode();
        XLog.d("API", "登陆返回码：" + resultCode);
        if(StatusCode.isSuccess(resultCode)) {
            String token = "";
            Map<String, List<String>> headers = response.getAllHeaders();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey().equals("token")) {
                    token = header.getValue().get(0);
                    GlobalStateSource globalStateSource = (GlobalStateSource)
                            DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
                    globalStateSource.setToken(token);
                }
            }
        }
        response.consumeContent();
        return resultCode;
    }

}
