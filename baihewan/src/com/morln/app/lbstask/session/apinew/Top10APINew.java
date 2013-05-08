package com.morln.app.lbstask.session.apinew;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.reflect.TypeToken;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.session.HttpClientHolder;
import com.morln.app.lbstask.session.bean.top10.Top10ArticleBase;
import com.morln.app.lbstask.utils.GsonUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.session.http.XHttp;
import com.morln.app.session.http.XURLBuilder;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beryl.
 * Date: 12-3-3
 * Time: 下午7:35
 */
public class Top10APINew {
    private XHttp http;

    private String hostUrl;
    private String apiUrlByIndex;
    private String apiUrlByDay;

    public Top10APINew(Context context) {
        http = HttpClientHolder.getMainHttpClient();

        Resources res = context.getResources();
        hostUrl = res.getString(R.string.host);
        apiUrlByIndex = res.getString(R.string.top10_by_index);
        apiUrlByDay = res.getString(R.string.top10_by_day);
    }


    /**
     * 通过序号获取十大
     * @param bIndex
     * @param eIndex
     * @param top10List
     * @return
     */
    public int getTop10ByIndex(int bIndex, int eIndex, List<Top10ArticleBase> top10List) {
        if(bIndex < 0){
            bIndex = 0;
        }

        XURLBuilder.setDefaultHost(hostUrl);
        String url = XURLBuilder.createInstance(apiUrlByIndex)
                .addIntQueryParam("bIndex", bIndex)
                .addIntQueryParam("eIndex", eIndex)
                .build();
        try {
            HttpPut httpPut = new HttpPut(url);
            HttpResponse response = http.execute(httpPut, false);

            int status = response.getStatusLine().getStatusCode();
            if (StatusCode.isSuccess(status)) {
                InputStream is = response.getEntity().getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<ArrayList<Top10ArticleBase>>() {}.getType();
                    List<Top10ArticleBase> list = (ArrayList<Top10ArticleBase>) GsonUtil.toObjectArray(cnt, type);
                    top10List.addAll(list);
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

    public int getTop10ByDate(int year, int month, int date, List<Top10ArticleBase> top10List) {
        XURLBuilder.setDefaultHost(hostUrl);
        String url = XURLBuilder.createInstance(apiUrlByDay)
                .addIntQueryParam("year", year)
                .addIntQueryParam("month", month)
                .addIntQueryParam("date", date)
                .build();
        XLog.d("API", "根据日期获取十大的url:" + url);

        try {
            HttpGet httpPut = new HttpGet(url);
            HttpResponse response = http.execute(httpPut, false);

            if(response == null) {
                return StatusCode.HTTP_EXCEPTION;
            }

            int status = response.getStatusLine().getStatusCode();
            XLog.d("API", "根据日期获取十大的返回码:" + status);
            if (StatusCode.isSuccess(status)) {
                InputStream is = response.getEntity().getContent();
                String cnt = XStringUtil.convertStreamToString(is).replaceAll("\\\\r", "");
                if (cnt != null) {
                    Type type = new TypeToken<List<Top10>>() {}.getType();
                    List<Top10> list = (List<Top10>) GsonUtil.toObjectArray(cnt, type);
                    List<Top10ArticleBase> localList = new ArrayList<Top10ArticleBase>();
                    for(Top10 sessionBean : list) {
                        localList.add(toLocalBean(sessionBean));
                    }
                    top10List.addAll(localList);
                    XLog.d("API", "根据日期获取十大的数量:" + list.size());
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


    public class Top10 {
        int[] rankList;
        String title;
        String url;
        String authorName;
        String board;
        int replyCount;
        String no;
        long firstTime;
        long lastTime;

        public int[] getRankList() {
            return rankList;
        }

        public void setRankList(int[] rankList) {
            this.rankList = rankList;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getBoard() {
            return board;
        }

        public void setBoard(String board) {
            this.board = board;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(int replyCount) {
            this.replyCount = replyCount;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public long getFirstTime() {
            return firstTime;
        }

        public void setFirstTime(long firstTime) {
            this.firstTime = firstTime;
        }

        public long getLastTime() {
            return lastTime;
        }

        public void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }
    }

    public static Top10ArticleBase toLocalBean(Top10 sessionBean) {
        Top10ArticleBase localBean = new Top10ArticleBase();
        localBean.setUrl(sessionBean.getUrl());
        localBean.setBoard(sessionBean.getBoard());
        localBean.setTitle(sessionBean.getTitle());
        localBean.setAuthorName(sessionBean.getAuthorName());
        localBean.setFirstTime(sessionBean.getFirstTime());
        localBean.setLastTime(sessionBean.getLastTime());
        localBean.setNo(sessionBean.getNo());
        localBean.setReplyCount(sessionBean.getReplyCount());

        List<Integer> rankList2 = new ArrayList<Integer>();
        int[] rankList = sessionBean.getRankList();
        for(int i = 0; i<rankList.length; i++) {
            rankList2.add(rankList[i]);
        }
        localBean.setRankList(rankList2);

        return localBean;
    }
}
