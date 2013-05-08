package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.session.HttpClientHolder;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.session.http.XHttp;
import com.morln.app.utils.XLog;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        try {
            HttpPost request = new HttpPost(hostUrl + apiUrl);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userName", userName));
            params.add(new BasicNameValuePair("password", password));
            request.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = http.execute(request, false);

            if(response == null) {
                return StatusCode.HTTP_EXCEPTION;
            }

            int resultCode = response .getStatusLine().getStatusCode();
            XLog.d("API", "登陆返回码：" + resultCode);
            if(StatusCode.isSuccess(resultCode)) {
                String token = "";
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("token")) {
                        token = header.getValue();
                        GlobalStateSource globalStateSource = (GlobalStateSource)
                                DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
                        globalStateSource.setToken(token);
                    }
                }
            }
            response.getEntity().consumeContent();
            return resultCode;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
