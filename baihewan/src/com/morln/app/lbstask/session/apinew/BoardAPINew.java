package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.reflect.TypeToken;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.bean.board.Board;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;
import com.xengine.android.session.http.XURLBuilder;
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
            XHttpRequest request = http.newRequest(url);
            XHttpResponse response = http.execute(request);
            if (response == null)
                return StatusCode.HTTP_EXCEPTION;

            int status = response.getStatusCode();
            if(StatusCode.isSuccess(status)) {
                Map<String, List<String>> headers = response.getAllHeaders();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    if (header.getKey().equals("boardTimeStamp")) {
                        long boardTimeStamp = Long.parseLong(header.getValue().get(0));
                        XLog.d("API", "更新版面的返回时间戳:" + boardTimeStamp);
                        GlobalStateSource globalStateSource = (GlobalStateSource)
                                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
                        globalStateSource.setBoardTimeStamp(boardTimeStamp);
                    }
                }
                InputStream is = response.getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<ArrayList<Board>>() {}.getType();
                    List<Board> list = (ArrayList<Board>) GsonUtil.toObjectArray(cnt, type);
                    boardList.clear();
                    boardList.addAll(list);
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
