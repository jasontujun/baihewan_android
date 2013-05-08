package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.reflect.TypeToken;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.session.HttpClientHolder;
import com.morln.app.lbstask.session.bean.board.Board;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.session.http.XHttp;
import com.morln.app.session.http.XURLBuilder;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fdp.
 * Modified by jasontujun.
 * Date: 12-7-19
 * Time: 下午12:59
 */
public class BoardAPINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrl;

    public BoardAPINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrl = res.getString(R.string.board_update);
    }

    /**
     * 更新版面数据
     * @param boardList
     * @param timeStamp
     * @return SUCCESS 总会返回boardList和timeStamp
     */
    public int downloadBoard(List<Board> boardList, long timeStamp) {
        XURLBuilder.setDefaultHost(hostUrl);
        String url = XURLBuilder.createInstance(apiUrl)
                .addLongQueryParam("timeStamp", timeStamp)
                .build();

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = http.execute(httpGet, false);
            if(response == null) {
                return StatusCode.HTTP_EXCEPTION;
            }

            int status = response.getStatusLine().getStatusCode();
            if(StatusCode.isSuccess(status)) {
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    if (header.getName().equals("boardTimeStamp")) {
                        long boardTimeStamp = Long.parseLong(header.getValue());
                        XLog.d("API", "更新版面的返回时间戳:" + boardTimeStamp);
                        GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
                        globalStateSource.setBoardTimeStamp(boardTimeStamp);
                    }
                }
                InputStream is = response.getEntity().getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<ArrayList<Board>>() {}.getType();
                    List<Board> list = (ArrayList<Board>) GsonUtil.toObjectArray(cnt, type);
                    boardList.clear();
                    boardList.addAll(list);
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
