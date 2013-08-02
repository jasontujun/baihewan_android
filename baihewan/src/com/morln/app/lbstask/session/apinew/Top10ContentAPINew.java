package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.top10.Top10Content;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XURLBuilder;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Beryl.
 * Date: 12-5-17
 * Time: 下午8:55
 */
public class Top10ContentAPINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrl;

    public Top10ContentAPINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrl = res.getString(R.string.top10_content);
    }

    /**
     * 获取十大内容
     * @param articleId
     * @param top10Content
     * @return
     */
    public int getTopContent(String articleId, String board, Top10Content top10Content) {
        XURLBuilder.setDefaultHost(hostUrl);
        String url = XURLBuilder.createInstance(apiUrl).
                addStringQueryParam("url", articleId).
                addStringQueryParam("board", board).
                build();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = http.execute(httpGet, false);

            int status = response.getStatusLine().getStatusCode();
            XLog.d("API", "向服务器获取十大内容的返回码：" + status);
            if (StatusCode.isSuccess(status)) {
                InputStream is = response.getEntity().getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                XLog.d("API", "向服务器获取十大内容：" + cnt);
                if (cnt != null) {
                    Top10Content returnData = (Top10Content) GsonUtil.toObjectArray(cnt, Top10Content.class);
                    if (top10Content != null) {
                        top10Content.copy(returnData);
                    }
                }
                is.close();
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
