package com.morln.app.lbstask.session.bbs;

import android.text.TextUtils;
import com.morln.app.lbstask.data.cache.BoardSource;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.model.*;
import com.morln.app.lbstask.engine.HttpClientHolder;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.session.http.XHttp;
import com.xengine.android.session.http.XHttpRequest;
import com.xengine.android.session.http.XHttpResponse;
import com.xengine.android.session.http.XHttpUtil;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小百合的API层(采用最新xengine的session模块)
 * Created by jasontujun.
 * Date: 12-4-16
 * Time: 上午11:57
 */
public class BbsAPINew {
    /**
     * 检测通信的返回是否是未登录，判断是否由于Token无效引起的
     * @param document
     * @return
     */
    private static boolean isTokenLoseEffectiveness(Document document) {
        if (document == null)
            return false;

        Elements headElements = document.getElementsByTag("head");
        for (Element headElement : headElements) {
            String headString = headElement.toString();
            if (headString.indexOf("错误! 您尚未登录, 请先登录!") >= 0)
                return false;
        }
        return true;
    }


    /**
     * 登陆bbs
     * @param username
     * @param password
     * @return
     */
    public static int login(String username, String password) {
        try {
            GlobalStateSource globalStateSource = (GlobalStateSource)
                    DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
            Random random = new Random();
            int code = random.nextInt(99999) % (90000) + 10000;

            String urlString = BbsUrlUtil.loginUrl(code, username, password);
            XLog.d("BBSAPI", "尝试登陆bbs url:" + urlString);
            XHttp http = HttpClientHolder.getMainHttpClient();
            XHttpRequest request = http
                    .newRequest(urlString)
                    .setMethod(XHttpRequest.HttpMethod.GET);
            XHttpResponse response = http.execute(request);
            if (response == null)
                return StatusCode.HTTP_EXCEPTION;

            Document document = Jsoup.parse(response.getContent(), "gb2312", "");
            String doc = document.toString();
            XLog.d("BBSAPI","登陆bbs 返回:"+doc);

            int t = doc.indexOf("setCookie");
            response.consumeContent();
            if (t < 0)
                return StatusCode.LOGIN_BAD_REQUEST;

            // 保存cookie(用于后面的请求)
            String tempString = doc.substring(t);
            tempString = tempString.substring(11, tempString.indexOf(")")-1);
            String[] tm =  tempString.split("\\+");
            String _U_KEY = String.valueOf(Integer.parseInt(tm[1])-2);
            String[] tm2 = tm[0].split("N");
            String _U_UID = tm2[1];
            String _U_NUM = "" + String.valueOf(Integer.parseInt(tm2[0]) + 2);
            BasicClientCookie UkeyCookie = new BasicClientCookie("_U_KEY", _U_KEY);
            BasicClientCookie UuidCookie = new BasicClientCookie("_U_UID", _U_UID);
            BasicClientCookie UnumCookie = new BasicClientCookie("_U_NUM", _U_NUM);
            UkeyCookie.setDomain(BbsUrlUtil.bbsDomain);
            UuidCookie.setDomain(BbsUrlUtil.bbsDomain);
            UnumCookie.setDomain(BbsUrlUtil.bbsDomain);
            UkeyCookie.setPath(BbsUrlUtil.bbsPath);
            UuidCookie.setPath(BbsUrlUtil.bbsPath);
            UnumCookie.setPath(BbsUrlUtil.bbsPath);
            http.setCookie(UkeyCookie);
            http.setCookie(UuidCookie);
            http.setCookie(UnumCookie);
            // 取出对应数据存入数据源
            globalStateSource.setCurrentUser(username, password);// 登陆成功，记住账户名和密码
            globalStateSource.setLastUser(username, password);// 登陆成功，记录历史记录
            globalStateSource.setBbsCode(String.valueOf(code));
            return StatusCode.LOGIN_SUCCESS;
        } catch (IOException e) {
            return StatusCode.HTTP_EXCEPTION;
        }
    }


    /**
     * 注销登录
     * @param code
     * @return
     */
    public static int logout(String code) {
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.logoutUrl(code))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            response.consumeContent();
            XLog.d("BBSAPI","logout:"+doc.toString());

            if (doc.toString().indexOf("错误! 你没有登录!") != -1){
                return StatusCode.FAIL;
            } else {
                XLog.d("BBSAPI","logout:成功");
                http.clearCookie();// 清除cookie
                return StatusCode.SUCCESS;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StatusCode.HTTP_EXCEPTION;
    }


    /**
     * 获取十大列表
     * @param top10List
     * @return
     */
    public static int getTop10FromWeb(List<Top10ArticleBase> top10List) {
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getTop10Url())
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            response.consumeContent();
            Elements trs = doc.select("tr");
            if (trs.size() == 0) {
                return StatusCode.FAIL;
            }
            trs.remove(0);
            int rank = 1;
            for (Element tr : trs) {
                Elements tds = tr.select("td");
                Top10ArticleBase articleBase = new Top10ArticleBase();
                articleBase.setBoard(tds.get(1).select("a").text());
                articleBase.setTitle(tds.get(2).select("a").text());
                articleBase.setAuthorName(tds.get(3).select("a").text());
                articleBase.setReplyCount(Integer.parseInt(tds.get(4).text()));
                articleBase.setType(ArticleBase.ARTICAL_THEME);
                // 日期
                articleBase.setDate(XStringUtil.date2calendarStr(new Date(System.currentTimeMillis())));
                articleBase.setLastTime(new Date(System.currentTimeMillis()));
                // id
                String urlStr = tds.get(2).select("a").attr("href");
                String id = urlStr.substring(urlStr.indexOf("file=")+5);
                articleBase.setId(id);
                // 排名
                List<Integer> rankList = new ArrayList<Integer>();
                rankList.add(rank);
                articleBase.setRankList(rankList);
                top10List.add(articleBase);
                rank++;
            }
            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }

    /**
     * 获取主题模式下的帖子详情
     *
     *
     http://bbs.nju.edu.cn/file/S_Chemistry/IMG0986A.jpg

     今日陪老大去买饭缸，没有尽到职责，让老大抱回了饭缸中的战斗缸，话说连钢尺都量不
     了，直接掉里面了，抱这去食堂吃饭，果断划算啊，会员价，只卖4块9 ，只卖4块9。。。

     注：饭缸性别女。。。
     PS盖到100楼果断送出S玉照。。。--
     楼主感谢各位辛苦盖楼，S小姐已阵亡，下附精美玉照，低清无码。。。



     ※ 来源:．南京大学小百合站 http://bbs.nju.edu.cn [FROM: 172.17.250.145]

     ※ 修改:．shinfriend 於 Sep 22 19:20:02 2012 修改本文．[FROM: 172.17.250.145]
     ※ 修改:．shinfriend 於 Sep 23 10:25:05 2012 修改本文．[FROM: 172.17.250.145]
     *
     *
     * @param boardStr
     * @param articleIdStr
     * @param pageStr
     * @param articleDetailList 返回数据 ： 主贴 + 回帖
     * @return
     */
    public static int getThemeArticleFromWeb(String boardStr, String articleIdStr,
                                             int pageStr, List<ArticleDetail> articleDetailList) {
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getThemeArticleDetailUrl(boardStr, articleIdStr, pageStr))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            response.consumeContent();
            // 通过正则表达式解析出图片,从而区分图片和文字
            // TIP 图片url有N种：（无法穷尽）
            // TIP http://bbs.nju.edu.cn/file/……
            // TIP http://lilybbs.nju.edu.cn/file/……
            // TIP http://www.nju.edu.cn/……
//                String rex = "((http://bbs\\.nju\\.edu\\.cn/file/)" +
//                        "|(http://lilybbs\\.net/file/)" +
//                        "|(http://www\\.nju\\.edu\\.cn\\/))" +
//                        "[a-zA-Z0-9/_-]+((\\.jpg)|(\\.JPG)|(\\.bmp)|(\\.BMP)|(\\.png)|(\\.PNG)|(\\.gif)|(\\.GIF)){1}";
            // TIP 故图片url的前缀允许是任意的：
            String rex = "http://([a-zA-Z0-9/_-]|\\.)+((\\.jpg)|(\\.JPG)|(\\.jpeg)|(\\.JPEG)|(\\.bmp)|(\\.BMP)|(\\.png)|(\\.PNG)|(\\.gif)|(\\.GIF)){1}";
            Pattern pattern = Pattern.compile(rex);

            boolean isTheme = true;
            ArticleDetail themeArticle = null;
            Elements bloks = doc.select("table");
            for (Element block: bloks) {
                Elements trs = block.select("tr");
                // 表格必须是两行第一行为帖子属性信息，第二行为帖子内容,如果不是就跳过此循环
                if (trs.size() != 2)
                    continue;

                ArticleDetail articleDetail = new ArticleDetail();
                //==============================表格第一行（固定）
                // 抓取回复链接(通过属性)
                articleDetail.setReplyLink(trs.get(0).select("a").get(0).attr("href"));
                // 抓取版块
                String link = articleDetail.getReplyLink();
                String tmpBoard = link.substring(link.indexOf("board=")+6);
                String board = tmpBoard.substring(0,tmpBoard.indexOf("&"));
                articleDetail.setBoard(board);
                // 抓取ID(通过属性和substring)
                String tmpId = tmpBoard.substring(tmpBoard.indexOf("file=")+5);
                String id = tmpId.substring(0, tmpId.indexOf("&"));
                articleDetail.setId(id);
                // 抓取作者名（通过split）
                String tr1Str = trs.get(0).select("td").get(0).text();
                String tmpAuthor = tr1Str.substring(tr1Str.indexOf("[本篇作者:")+7);// 注意有空格！
                String authorName = tmpAuthor.substring(0, tmpAuthor.indexOf("]"));
                articleDetail.setAuthorName(authorName);
                // 抓取人气
                String tmpPop = tmpAuthor.substring(tmpAuthor.indexOf("[本篇人气:")+7);// 注意有空格！
                String pop = tmpPop.substring(0, tmpPop.indexOf("]"));
                if (XStringUtil.isNumber(pop)) {
                    articleDetail.setPopularity(Integer.parseInt(pop));
                }
                // 抓楼层数
                String floor = trs.get(0).select("td").get(1).text();
                if (XStringUtil.isNumber(floor)) {
                    articleDetail.setFloorCount(Integer.parseInt(floor));
                }

                //==============================表格第二行（不确定有）
                String tr2Str  = trs.get(1).text();
                String[] tag = {
                        articleDetail.getAuthorName(),
                        "), 信区: ",
                        "标  题: ",
                        "发信站: 南京大学小百合站 (",
                        ")",
                        "[FROM: ",
                        "]"
                };
                // 抓取昵称
                if (tr2Str.contains(tag[0])) {
                    int nameStartIndex = tr2Str.indexOf(tag[0]);
                    if (tr2Str.contains(tag[1])) {
                        int boardStartIndex = tr2Str.indexOf(tag[1]);
                        if (boardStartIndex > nameStartIndex +tag[0].length()+2) {
                            articleDetail.setAuthorNickname(tr2Str.substring(nameStartIndex +tag[0].length()+2, boardStartIndex));
                            tr2Str = tr2Str.substring(boardStartIndex + tag[1].length());
                        }
                    }
                }
                // 抓取板块
                if (tr2Str.contains(tag[2])) {
                    int titleStartIndex = tr2Str.indexOf(tag[2]);
                    if (titleStartIndex > 0) {
//                        articleDetail.setBoard(tr2Str.substring(0, titleStartIndex));// 此处抓下的版块含换行符，坑爹
                        tr2Str = tr2Str.substring(titleStartIndex + tag[2].length());
                    }
                }
                // 抓取标题（有换行符！）
                if (tr2Str.contains(tag[3])) {
                    int timeStartIndex = tr2Str.indexOf(tag[3]);
                    if (timeStartIndex > 0) {
                        String titleStr = tr2Str.substring(0, timeStartIndex -1);
                        articleDetail.setTitle(titleStr);
                        tr2Str = tr2Str.substring(timeStartIndex + tag[3].length());
                    }
                }
                // 抓取时间
                if (tr2Str.contains(tag[4])) {
                    int timeEndIndex = tr2Str.indexOf(tag[4]);
                    if (0 < timeEndIndex  && timeEndIndex < 30) {
                        articleDetail.setDate(tr2Str.substring(0, timeEndIndex));
                        tr2Str = tr2Str.substring(timeEndIndex + tag[4].length());
                    }
                }
                // 抓取内容和图片
                String content = new String(tr2Str);
                // 去掉异常字符
                content = content.replace("\u001B", "");
                content = content.replace("\r","");
                content = content.replaceAll("\\[([0-9]|;)*m", "");
                // 去掉结尾“-- ”后面的部分
                int endIndex = content.indexOf("--\n※");
                int endIndex2 = content.indexOf("--\n\n※");
                int endIndex3 = content.indexOf("--\n\n\n※");
                int endIndex4 = content.indexOf("--\n[");
                if (endIndex >= 0) {
                    content = content.substring(0, endIndex);
                } else if (endIndex2 >= 0) {
                    content = content.substring(0, endIndex2);
                } else if (endIndex3 >= 0) {
                    content = content.substring(0, endIndex3);
                } else if (endIndex4 >= 0) {
                    content = content.substring(0, endIndex4);
                }
                // 去掉结尾“※ 来源:” “※ 修改:”
                int badWordIndex1 = content.indexOf("※ 来源:");
                if (badWordIndex1 != -1) {
                    content = content.substring(0, badWordIndex1);
                }
                int badWordIndex2 = content.indexOf("※ 修改:");
                if (badWordIndex2 != -1) {
                    content = content.substring(0, badWordIndex2);
                }

                // 处理换行符()
                StringBuffer strBuffer = new StringBuffer(content);
                int brIndex1 = -1;
                int brIndex2 = 0;
                while (-1 <= brIndex1 && brIndex1 < content.length()-1) {
                    brIndex2 = content.substring(brIndex1+1).indexOf("\n");
                    if (brIndex2 == -1) {
                        break;
                    } else if (brIndex2 >= 39) {
                        strBuffer.setCharAt(brIndex1+1+brIndex2, '\u001B');
                    }
                    brIndex1 = brIndex1 + 1 + brIndex2;
                }
                String tmp = strBuffer.toString();
                content = tmp.replace("\u001B", "");

                // 获取所有的文字块和图片块
                List<String> picUrlList = new ArrayList<String>();
                List<String> contentBlockList = new ArrayList<String>();
                Matcher m = pattern.matcher(content);
                boolean result = m.find();
                while (result) {
                    String imgUrl = m.group();
                    picUrlList.add(imgUrl);
                    result = m.find();
                }
                String[] wordBlocks = pattern.split(content);
                XLog.d("ARTICLE", "wordBlock size:"+wordBlocks.length+", img size:"+picUrlList.size());
                for (int i = 0; i<wordBlocks.length; i++) {
                    contentBlockList.add(wordBlocks[i]);
                }
                articleDetail.setImgUrls(picUrlList);
                articleDetail.setWordBlocks(contentBlockList);
                // 抓取IP（最后一次写或修改的IP）
                int ipStartIndex = tr2Str.lastIndexOf(tag[5]);
                int ipEndIndex = tr2Str.lastIndexOf(tag[6]);
                if (ipStartIndex != -1 && ipEndIndex != -1) {
                    articleDetail.setIp(tr2Str.substring(ipStartIndex + tag[5].length(), ipEndIndex));
                }

                // 帖子类型(主题帖or跟帖)
                if (isTheme) {
                    isTheme = false;
                    articleDetail.setType(ArticleDetail.ARTICLE_THEME);
                    themeArticle = articleDetail;
                } else {
                    articleDetail.setType(ArticleDetail.ARTICLE_REPLY);
                    articleDetail.setHostId(themeArticle.getId());// 别忘了设置跟帖的宿主帖id
                }
                articleDetailList.add(articleDetail);
            }
            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }


    /**
     * 结构化解析一般模式下的帖子内容
     * @param articleStr
     * @return
     */
    public static ArticleDetail parseNormalArticleContent(String articleStr) {
        if (TextUtils.isEmpty(articleStr))
            return null;

        Document doc = Jsoup.parse(articleStr, "");
        Elements bloks = doc.select("table");
        for (Element block: bloks) {
            Elements trs = block.select("tr");
            if (trs.size()!=1) {// 一般模式的帖子只有一行，为帖子内容,如果不是就跳过此循环
                continue;
            }

            ArticleDetail articleDetail = new ArticleDetail();
            //==============================表格第一行（不确定有）
            String tr2Str  = trs.get(0).text();
            String[] tag = {
                    "发信人: ",
                    " (",
                    "), 信区: ",
                    ". 本篇人气: ",
                    "标  题: ",
                    "发信站: 南京大学小百合站 (",
                    ")",
                    "[FROM: ",
                    "]"
            };
            // 抓作者名
            if (tr2Str.contains(tag[0])) {
                int nameStartIndex = tr2Str.indexOf(tag[0]);
                if (tr2Str.contains(tag[1])) {
                    int nickNameStartIndex = tr2Str.indexOf(tag[1]);
                    if (nickNameStartIndex > nameStartIndex + tag[0].length()) {
                        articleDetail.setAuthorName(tr2Str.substring(nameStartIndex + tag[0].length(), nickNameStartIndex));
                        tr2Str = tr2Str.substring(nickNameStartIndex + tag[1].length());
                    }
                }
            }
            // 抓取昵称
            if (tr2Str.contains(tag[2])) {
                int boardStartIndex = tr2Str.indexOf(tag[2]);
                if (boardStartIndex > 0) {
                    articleDetail.setAuthorNickname(tr2Str.substring(0, boardStartIndex));
                    tr2Str = tr2Str.substring(boardStartIndex + tag[2].length());
                }
            }
            // 抓取板块
            if (tr2Str.contains(tag[3]))  {
                int popStartIndex = tr2Str.indexOf(tag[3]);
                if (popStartIndex > 0) {
                    articleDetail.setBoard(tr2Str.substring(0, popStartIndex));
                    tr2Str = tr2Str.substring(popStartIndex + tag[3].length());
                }
            }
            // 抓人气
            if (tr2Str.contains(tag[4])) {
                int titleStartIndex = tr2Str.indexOf(tag[4]);
                if (titleStartIndex -1 > 0) {
                    String pop = tr2Str.substring(0, titleStartIndex-1);
//                        articleDetail.setPopularity(0);
                    tr2Str = tr2Str.substring(titleStartIndex + tag[4].length());
                }
            }
            // 抓取标题（有换行符！）
            if (tr2Str.contains(tag[5])) {
                int timeStartIndex = tr2Str.indexOf(tag[5]);
                if (timeStartIndex - 1 > 0) {
                    String titleStr = tr2Str.substring(0, timeStartIndex -1);
                    articleDetail.setTitle(titleStr);
                    tr2Str = tr2Str.substring(timeStartIndex + tag[5].length());
                }
            }
            // 抓取时间
            if (tr2Str.contains(tag[6])) {
                int timeEndIndex = tr2Str.indexOf(tag[6]);
                if (0 < timeEndIndex  && timeEndIndex < 30) {
                    articleDetail.setDate(tr2Str.substring(0, timeEndIndex));
                    tr2Str = tr2Str.substring(timeEndIndex + tag[6].length());
                }
            }
            // 抓取IP（最后一次写或修改的IP）
            int ipStartIndex = tr2Str.lastIndexOf(tag[7]);
            int ipEndIndex = tr2Str.lastIndexOf(tag[8]);
            if (ipStartIndex != -1 && ipEndIndex != -1) {
                articleDetail.setIp(tr2Str.substring(ipStartIndex + tag[7].length(), ipEndIndex));
            }

            // 抓取内容和图片
            String content = new String(tr2Str);
            // 去掉异常字符
            content = content.replace("\u001B", "");
            content = content.replace("\r","");
            content = content.replaceAll("\\[([0-9]|;)*m", "");
            // 去掉结尾“-- ※ 来源、修改”
            int endIndex = content.indexOf("--\n※");
            int endIndex2 = content.indexOf("--\n\n※");
            int endIndex3 = content.indexOf("--\n\n\n※");
            int endIndex4 = content.indexOf("--\n[");
            if (endIndex >= 0) {
                content = content.substring(0, endIndex);
            } else if (endIndex2 >= 0) {
                content = content.substring(0, endIndex2);
            } else if (endIndex3 >= 0) {
                content = content.substring(0, endIndex3);
            } else if (endIndex4 >= 0) {
                content = content.substring(0, endIndex4);
            }

            // 处理换行符()
            StringBuffer strBuffer = new StringBuffer(content);
            int brIndex1 = -1;
            int brIndex2 = 0;
            while (-1 <= brIndex1 && brIndex1 < content.length()-1) {
                brIndex2 = content.substring(brIndex1+1).indexOf("\n");
                if (brIndex2 == -1) {
                    break;
                } else if (brIndex2 >= 39) {
                    strBuffer.setCharAt(brIndex1+1+brIndex2, '\u001B');
                }
                brIndex1 = brIndex1 + 1 + brIndex2;
            }
            String tmp = strBuffer.toString();
            content = tmp.replace("\u001B", "");

            List<String> picUrlList = new ArrayList<String>();
            List<String> contentBlockList = new ArrayList<String>();
            // 通过正则表达式解析出图片,从而区分图片和文字
            String rex = "http://([a-zA-Z0-9/_-]|\\.)+((\\.jpg)|(\\.JPG)|(\\.jpeg)|(\\.JPEG)|(\\.bmp)|(\\.BMP)|(\\.png)|(\\.PNG)|(\\.gif)|(\\.GIF)){1}";
            Pattern pattern = Pattern.compile(rex);
            Matcher m = pattern.matcher(content);
            boolean result = m.find();
            while (result) {
                String imgUrl = m.group();
                picUrlList.add(imgUrl);
                result = m.find();
            }
            // 获取所有的文字块
            String[] wordBlocks = pattern.split(content);
            XLog.d("ARTICLE","wordBlock size:"+wordBlocks.length+", img size:"+picUrlList.size());
            for (int i = 0; i<wordBlocks.length; i++) {
                contentBlockList.add(wordBlocks[i]);
            }
            articleDetail.setImgUrls(picUrlList);
            articleDetail.setWordBlocks(contentBlockList);

            // 帖子类型(主题帖)
            articleDetail.setType(ArticleDetail.ARTICLE_THEME);
            return articleDetail;
        }
        return null;
    }


    /**
     * 发表文章
     * @param board
     * @param title
     * @param content
     * @param pid
     * @param reid
     * @return
     */
    public static int sendArticle(String board, String title, String content, String pid, String reid) {
        try {
            GlobalStateSource globalStateSource = (GlobalStateSource)
                    DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
            String url = BbsUrlUtil.sendArticleUrl(globalStateSource.getBbsCode(), board);
            XHttp http = HttpClientHolder.getMainHttpClient();
            XHttpRequest request = http.newRequest(url)
                    .setMethod(XHttpRequest.HttpMethod.POST)
                    .addStringParam("title", title)
                    .addStringParam("pid",pid)
                    .addStringParam("reid",reid)
                    .addStringParam("signature","1")
                    .addStringParam("author","on")
                    .addStringParam("text", content);
            request.setCharset("GB2312");
            XHttpResponse response = http.execute(request);
            if (response == null)
                return StatusCode.HTTP_EXCEPTION;;

            String strResult = XHttpUtil.toString(response);
            response.consumeContent();
            XLog.d("BBSAPI", "发文章返回结果:" + strResult);

            // 检测token无效？
            if (! isTokenLoseEffectiveness(new Document(strResult)))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            if (response.getStatusCode() == 200) {
                return StatusCode.SUCCESS;
            } else {
                return StatusCode.FAIL;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }


    public static String getPid(String articleId, String board) {
        try {
            GlobalStateSource globalStateSource = (GlobalStateSource)
                    DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
            XHttp http = HttpClientHolder.getMainHttpClient();
            XHttpRequest request = http
                    .newRequest(BbsUrlUtil.getPidUrl(globalStateSource.getBbsCode(), board, articleId))
                    .setMethod(XHttpRequest.HttpMethod.GET);
            XHttpResponse response = http.execute(request);
            if (response == null)
                return null;

            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            String pidString = doc.select("input[name=pid]").attr("value");
            response.consumeContent();
            return pidString;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 上传图片
     * @param imgFile
     * @return 返回图片在bbs上的有效url
     */
    public static String uploadImage(String board, File imgFile, String description) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // HTTP-POST 请求上传图片
        String code = globalStateSource.getBbsCode();
        String getImgUrl = BbsUrlUtil.getUploadUrl(code);
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest uploadRequest = http
                .newRequest(getImgUrl)
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addFileParam("up", imgFile)
                .addStringParam("exp", description)
                .addStringParam("ptext", "")
                .addStringParam("board", board);
        uploadRequest.setCharset("GB2312");
        XHttpResponse uploadResponse = http.execute(uploadRequest);
        if (uploadResponse == null)
            return null;
        if (uploadResponse.getStatusCode() != 200)
            return null;

        String uploadResult = null;
        try {
            uploadResult = XHttpUtil.toString(uploadResponse);
            uploadResponse.consumeContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (uploadResult == null)
            return null;
        XLog.d("UPLOAD", "HTTP-POST返回的内容:" + uploadResult);
        uploadResult = uploadResult.substring(uploadResult.indexOf("url=") + 4, uploadResult.lastIndexOf(">") - 1);
        String fileNum = uploadResult.substring(uploadResult.indexOf("file=") + 5, uploadResult.indexOf("&name"));
        String fileName = uploadResult.substring(uploadResult.indexOf("name=") + 5, uploadResult.indexOf("&exp"));

        // HTTP-GET请求获取图片url
        String getImageOnBbsUrl = BbsUrlUtil.getBbsImageLocationUrl(code,
                board, fileNum, fileName, description, "text");
        XLog.d("UPLOAD", "HTTP-GET请求获取图片url:" + getImageOnBbsUrl);
        XHttpRequest getUrlRequest = http
                .newRequest(getImageOnBbsUrl)
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse getUrlResponse = http.execute(getUrlRequest);
        if (getUrlResponse == null)
            return null;
        if (getUrlResponse.getStatusCode() != 200)
            return null;

        String getUrlResult = null;
        try {
            getUrlResult = XHttpUtil.toString(getUrlResponse);
            getUrlResponse.consumeContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getUrlResult == null)
            return null;
        XLog.d("UPLOAD", "$$$$$$$GET请求的返回内容：" + getUrlResult);
        int startIndex = getUrlResult.indexOf(BbsUrlUtil.bbsHost);
        if (startIndex < 0)
            return null;
        String tempString = getUrlResult.substring(startIndex);
        int endIndex = tempString.indexOf("\\n');");
        String imgOnBbsUrl = tempString.substring(0, endIndex);
        XLog.d("UPLOAD", "当前图片的百合url:" + imgOnBbsUrl);
        return imgOnBbsUrl;
    }


    /**
     * 获取各区热点
     * @param sec
     * @param articleBaseList
     * @return
     */
    public static int getZoneHotFromWeb(int sec, List<ArticleBase> articleBaseList){
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getZoneHotUrl(sec))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            Elements blocks = doc.select("tr");
            if (blocks.size() == 0)
                return StatusCode.FAIL;

            blocks.remove(0);// 去掉第一行
            if (blocks.size() == 0)
                return StatusCode.SUCCESS;

            for (Element block : blocks) {
                Elements links = block.select("td");
                String board = links.get(1).select("a").text();
                String title = links.get(2).text();
                String url = links.get(2).select("a").attr("href");
                String fileId = url.substring((url.indexOf("file=")) + 5);
                String authorName = links.get(3).select("a").text();
                String reply = links.get(4).text();

                ArticleBase articleBase = new ArticleBase();
                articleBase.setBoard(board);
                articleBase.setTitle(title);
                articleBase.setId(fileId);
                articleBase.setAuthorName(authorName);
                articleBase.setReplyCount(Integer.parseInt(reply));
                articleBaseList.add(articleBase);
            }
            response.consumeContent();
            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }

    //----------------------------------------------讨论区
    public static int getThemeArticleListFromWeb(Board board, int page) {
        // 根据页数生成对应url
        String url = null;
        String boardId = board.getBoardId();
        if (page <= 0) {
            url = BbsUrlUtil.getBoardFirstPageUrl(boardId, true);// 获取版面首页
        } else {
            int no = board.getBoardEarliestNo();// 获取此版最早的帖子的id
            if (no == -1) {
                url =BbsUrlUtil.getBoardFirstPageUrl(boardId, true);// 获取版面首页
            } else {
                no  = no - 22;
                url = BbsUrlUtil.getBoardArticleListUrl(boardId, no, true);
            }
        }
        XLog.d("BBSAPI", "抓取版面" + boardId + "的帖子url:" + url);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(url)
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document docTmp = Jsoup.parse(response.getContent(), "gb2312", "");
            // 去除无用标签字符
            String s = docTmp.toString();
            s = s.replace("<nobr>", "");
            s = s.replace("</nobr>", "");
            XLog.d("BBSAPI", "theme article：" + s);
            Document doc = Jsoup.parse(s);

            Elements tables = doc.select("table");
            if (tables.size() == 0)
                return StatusCode.FAIL;

            Elements trs = tables.get(0).select("tr");// 主题模式下内容在第一个table中!
            trs.remove(0);// 删除列表题头
            for (Element tr : trs) {
                Elements tds = tr.select("td");
                ArticleBase article = new ArticleBase();
                article.setType(ArticleBase.ARTICAL_THEME);
                // 版面
                article.setBoard(boardId);
                // 序号
                String noStr = tds.get(0).text();
                // 判断是否是有效的行
                if((!TextUtils.isEmpty(noStr)) && (!XStringUtil.isNumber(noStr)))  {
                    continue;
                }
                if (noStr!= null && (!noStr.equals(""))) {
                    article.setNo(Integer.parseInt(noStr));
                }
                // 状态
                String statusStr = tds.get(1).text();
                if ("置顶".equals(statusStr)) {
                    article.setUp(true);
                } else {
                    article.setUp(false);
                }
                // 作者
                article.setAuthorName(tds.get(2).text());
                // 时间
                String dateStr = tds.get(3).text();
                if (article.isUp()) {//  置顶帖的时间抓下来时需要裁减
                    dateStr = dateStr.substring(0, 13);
                }
                article.setDate(dateStr);
                // 帖子ID
                String urlStr = tds.get(4).select("a").attr("href");
                String id = urlStr.substring(urlStr.indexOf("file=")+5);
                article.setId(id);
                // 标题
                String titleStr = tds.get(4).select("a").get(0).text();
                titleStr = titleStr.substring(2);// 注意去掉装饰符"o"
                article.setTitle(titleStr);
                // 人气/回复
                if (article.isUp()) {
                    String pop = tds.get(5).select("font").get(0).text();
                    article.setPopularity(Integer.parseInt(pop));
                } else {
                    String replyCount = tds.get(5).select("font").get(0).text();
                    article.setReplyCount(Integer.parseInt(replyCount));

                    String pop = tds.get(5).select("font").get(1).text();
                    article.setPopularity(Integer.parseInt(pop));
                }
                // 添加进版块
                board.addArticleBase(article);
            }
            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } finally {
            response.consumeContent();
        }
    }

    /**
     * 获取一般模式的帖子
     * @param board
     * @param page
     * @return
     */
    public static int getNormalArticleListFromWeb(Board board, int page) {
        // TODO .......
        return -1;
    }

    /**
     * 删除帖子
     * @param board
     * @param articleId
     * @return
     */
    public static int deleteArticle(String board, String articleId) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.deleteArticleUrl(globalStateSource.getBbsCode(), board, articleId))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            if (doc.toString().indexOf("文件不存在, 删除失败") != -1 ||
                    doc.toString().indexOf("错误! 请先登录!") != -1) {
                return StatusCode.FAIL;
            } else {
                return StatusCode.SUCCESS;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StatusCode.HTTP_EXCEPTION;
    }

    /**
     * 搜索帖子
     * @param author
     * @param contain1
     * @param contain2
     * @param notcontain
     * @param startDay
     * @param endDay
     * @return
     */
    public static int searchArticle(String author, String contain1, String contain2,
                                    String notcontain, String startDay, String endDay,
                                    List<ArticleBase> resultList) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http.newRequest(BbsUrlUtil.searchArticleUrl())
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addStringParam("flag", "1")
                .addStringParam("user", author)
                .addStringParam("title", contain1)
                .addStringParam("title2", contain2)
                .addStringParam("title3", notcontain)
                .addStringParam("day", startDay)
                .addStringParam("day2", endDay);
        request.setCharset("GB2312");
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;
        try {
            String strResult = XHttpUtil.toString(response);
            XLog.d("BBSAPI", "search article:" + strResult);
            Document doc = Parser.parse(strResult, "");
            // 根据页数生成对应url
            Elements tables = doc.select("table");
            for (Element table: tables) {
                Elements trs = table.select("tr");
                // 必须是有内容的table
                if (trs.size() > 0 && trs.get(0).select("td").size() >=4) {
                    for (Element tr: trs) {
                        ArticleBase article = new ArticleBase();
                        Elements tds = tr.select("td");
                        article.setAuthorName(tds.get(1).text());

                        article.setDate(tds.get(2).text());

                        article.setTitle(tds.get(3).text());

                        String url = tds.get(3).select("a").get(0).attr("href");
                        String boardTmp = url.substring(url.indexOf("board=")+6);
                        String board = boardTmp.substring(0, boardTmp.indexOf("&"));
                        String fileTmp = url.substring(url.indexOf("file=")+5);
                        String id = fileTmp.substring(0, fileTmp.indexOf("&"));
                        article.setBoard(board);
                        article.setId(id);

                        resultList.add(article);
                    }
                }
            }
            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
        return StatusCode.FAIL;
    }

    /**
     * 获取热门讨论区（抓取js）
     * @param hotBoardList
     * @return
     */
    public static int getHotBoard(List<Board> hotBoardList) {
        BoardSource boardSource = (BoardSource)
                DefaultDataRepo.getInstance().getSource(SourceName.BBS_BOARD);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getHotBoardUrl())
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.getContent(), "gb2312"));
            String allContent = in.readLine();
            if (TextUtils.isEmpty(allContent))
                return StatusCode.FAIL;

            String finalContent = allContent.substring(allContent.indexOf("["));
            JSONObject jso = null;
            JSONArray jsonarray = new JSONArray(finalContent);
            for (int i = 0; i < jsonarray.length(); i++) {
                jso = (JSONObject) jsonarray.get(i);

                String boardId = jso.get("brd").toString();
                String chineseName = jso.get("n").toString();
                int personNum = Integer.parseInt(jso.get("on").toString());
                Board board = boardSource.getById(boardId);
                board.setPersonNum(personNum);
                hotBoardList.add(board);
            }
            return StatusCode.SUCCESS;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StatusCode.FAIL;
    }


    /**
     * 抓取订阅版面
     * @param orderBoardList
     * @return
     */
    public static int getRssBoard(List<String> orderBoardList) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getRssBoardUrl(globalStateSource.getBbsCode()))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            Elements blocks = doc.select("div");
            if (blocks == null) {
                response.consumeContent();
                return StatusCode.FAIL;
            }
            Elements links = null;
            boolean hasFound = false;
            for (Element block : blocks) {
                links = block.select("a");
                if (links == null) {
                    response.consumeContent();
                    return StatusCode.FAIL;
                }
                for (int i = 0; i < links.size(); i++) {
                    String str = links.get(i).select("a").attr("href");
                    if (str != null && str.equals("bbsmybrd")) {
                        hasFound = true;
                        break;
                    }
                }
                if (hasFound) {
                    break;
                }
            }
            if (links == null) {
                response.consumeContent();
                return StatusCode.FAIL;
            }
            for (int i = 0; i < links.size() - 1; i++) {
                String boardId = links.get(i).select("a").text();
                orderBoardList.add(boardId);
            }
            response.consumeContent();
            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StatusCode.FAIL;
    }

    /**
     * 同步订阅版面
     * @param boardList
     * @return
     */
    public static int sendRssBoard(List<String> boardList) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.syncRssBoardUrl(globalStateSource.getBbsCode()))
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addStringParam("confirm1", "1");
        for (String boardId : boardList)
            request.addStringParam(boardId, "on");
        request.setCharset("GB2312");
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        if (response.getStatusCode() == 200) {
            return StatusCode.SUCCESS;
        } else {
            return StatusCode.FAIL;
        }
    }


    /**
     * 获取用户性别
     * @param username
     * @return 返回性别。如果出现异常，返回-1
     */
    public static int getSexOfUser(String username) {
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getBbsUserInfoUrl(username))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document friendDoc = Jsoup.parse(response.getContent(), "gb2312", "");
            response.consumeContent();

            Elements blockFriend = friendDoc.select("textarea");
            // 该用户不存在
            if (blockFriend.size() == 0)
                return -1;

            String allInfo = blockFriend.get(0).text();
            //为了避免昵称中含有“篇”之类的字
            allInfo = allInfo.substring(allInfo.indexOf(") 共上站") + 5);
            allInfo = allInfo.substring(allInfo.indexOf("篇") - 1);
            allInfo = allInfo.substring(allInfo.indexOf("篇") + 7);

            if (allInfo.startsWith("35")) {
                return UserBase.GENDER_FEMALE;// 女
            } else if(allInfo.startsWith("36")) {
                return UserBase.GENDER_MALE;// 男
            } else {
                return UserBase.GENDER_UNKNOWN;// 未公开
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }


    //去掉异常字符之后,个人信息提取的标识符
    private static final String NICKNAME_LABEL_START = " (";
    private static final String NICKNAME_LABEL_END = ") 共上站 ";
    private static final String LOGIN_NUM_END = " 次，发表文章 ";
    private static final String SHOW_PAPER_NUM_END = " 篇";
    private static final String STAR_START = "[";
    private static final String STAR_IDENTIFY = "座";
    private static final String STAR_UNKNOWN = "不详";
    private static final String LAST__LOGIN_TIME_START = "]上次在 [";
    private static final String LAST__LOGIN_TIME_END = "] 从 [";
    private static final String LAST_LOGIN_IP_END = "] 到本站一游。";
    private static final String MAIL_START = "信箱：[";
    private static final String MAIL_END = "]  经验值：";
    private static final String EXPERIENCE_BETWEEN_LEVEL = "(";
    private static final String LEVEL_END = ") 表现值：[";
    private static final String SHOW_VALUE_END = "](";
    private static final String SHOW_STATE_END = ") 生命力：[";
    private static final String HP_END = "]。";
    private static final String AFTER_HP_ALL_INFO_END = "目前";
    // 提取版主信息
    private static final String ROLE_TAG = "★";
    /**
     * 获取个人信息
     * @param username
     * @return
     */
    public static BbsUserBase getBbsUserInfoFromWeb(String username) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getBbsUserInfoUrl(username))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return null;

        try {
            Document friendDoc = Jsoup.parse(response.getContent(), "gb2312", "");
            response.consumeContent();
            // 该用户不存在
            Elements blockFriend = friendDoc.select("textarea");
            if (blockFriend.size() == 0)
                return null;

            String allInfo = blockFriend.get(0).text();

            // 抓取性别
            int gender = -1;
            String tmpStr = new String(allInfo);
            tmpStr = tmpStr.substring(tmpStr.indexOf(") 共上站") + 5);
            tmpStr = tmpStr.substring(tmpStr.indexOf("篇") - 1);
            tmpStr = tmpStr.substring(tmpStr.indexOf("篇") + 7);
            if (tmpStr.startsWith("35")) {
                gender = UserBase.GENDER_FEMALE;// 女
            } else if (tmpStr.startsWith("36")) {
                gender = UserBase.GENDER_MALE;// 男
            } else {
                gender = UserBase.GENDER_UNKNOWN;// 未公开
            }

            // 去掉异常字符
            allInfo = allInfo.replace("\u001B", "");
            allInfo = allInfo.replace("\r", "");
            allInfo = allInfo.replaceAll("\\[([0-9]|;)*m", "");

            // 抓取昵称nickName
            String nickName = allInfo.substring(
                    allInfo.indexOf(NICKNAME_LABEL_START) + NICKNAME_LABEL_START.length(),
                    allInfo.indexOf(NICKNAME_LABEL_END));
            allInfo = allInfo.substring(allInfo.indexOf(NICKNAME_LABEL_END));

            // 抓取上站次数 loginNum
            String loginNum = allInfo.substring(
                    allInfo.indexOf(NICKNAME_LABEL_END) + NICKNAME_LABEL_END.length(),
                    allInfo.indexOf(LOGIN_NUM_END)
            );
            allInfo = allInfo.substring(allInfo.indexOf(LOGIN_NUM_END));

            // 抓取发表文章的篇数 sendNum
            String sendNum = allInfo.substring(
                    allInfo.indexOf(LOGIN_NUM_END) + LOGIN_NUM_END.length(),
                    allInfo.indexOf(SHOW_PAPER_NUM_END)
            );
            allInfo = allInfo.substring(allInfo.indexOf(SHOW_PAPER_NUM_END));

            // 抓取星座（*）难点，要从多个角度思考。
            String star;
            if (allInfo.contains(STAR_IDENTIFY)) {
                star = allInfo.substring(
                        allInfo.indexOf(STAR_START) + STAR_START.length(),
                        allInfo.indexOf(STAR_IDENTIFY) + STAR_IDENTIFY.length());
            }
            else {
                star = STAR_UNKNOWN;
            }
            allInfo = allInfo.substring(allInfo.indexOf(SHOW_PAPER_NUM_END) );

            // 抓取上次登录时间  lastOnTime
            String lastOnTime = allInfo.substring(
                    allInfo.indexOf(LAST__LOGIN_TIME_START)+ LAST__LOGIN_TIME_START.length(),
                    allInfo.indexOf(LAST__LOGIN_TIME_END));
            allInfo = allInfo.substring(allInfo.indexOf(LAST__LOGIN_TIME_END));

            // 抓取上次登录的IP地址 lastIP
            String lastLoginIP = allInfo.substring(
                    allInfo.indexOf(LAST__LOGIN_TIME_END) + LAST__LOGIN_TIME_END.length(),
                    allInfo.indexOf(LAST_LOGIN_IP_END));
            allInfo = allInfo.substring(allInfo.indexOf(LAST_LOGIN_IP_END) + LAST_LOGIN_IP_END.length());

            //信箱 mail
            String mail = allInfo.substring(
                    allInfo.indexOf(MAIL_START) + MAIL_START.length(),
                    allInfo.indexOf(MAIL_END));
            allInfo = allInfo.substring(allInfo.indexOf(MAIL_END) + MAIL_END.length());

            // 抓取经验值exp 。例如 中士？level
            String exp = "";
            String level = "";
            if (!allInfo.substring(
                    allInfo.indexOf(EXPERIENCE_BETWEEN_LEVEL) + EXPERIENCE_BETWEEN_LEVEL.length(),
                    allInfo.indexOf(LEVEL_END)).equals("不告诉你")) {
                exp = allInfo.substring(allInfo.indexOf(MAIL_END) + 2, allInfo.indexOf(EXPERIENCE_BETWEEN_LEVEL) -1);
                allInfo = allInfo.substring(allInfo.indexOf(EXPERIENCE_BETWEEN_LEVEL));
                level = allInfo.substring(
                        allInfo.indexOf(EXPERIENCE_BETWEEN_LEVEL) + EXPERIENCE_BETWEEN_LEVEL.length(),
                        allInfo.indexOf(LEVEL_END));
            }
            else{
                exp = "" + 0;
                level = "不告诉你";
            }
            allInfo = allInfo.substring(allInfo.indexOf(LEVEL_END));

            // 抓取表现值 showNum
            String showNum = allInfo.substring(
                    allInfo.indexOf(LEVEL_END) + LEVEL_END.length(),
                    allInfo.indexOf(SHOW_VALUE_END));
            allInfo = allInfo.substring(allInfo.indexOf(SHOW_VALUE_END) );

            // 抓取表现的状态 例如：努力中。
            String showWord = allInfo.substring(
                    allInfo.indexOf(SHOW_VALUE_END) + SHOW_VALUE_END.length(),
                    allInfo.indexOf(SHOW_STATE_END));
            allInfo = allInfo.substring(allInfo.indexOf(SHOW_STATE_END));

            //生命力 hp
            String hp = allInfo.substring(allInfo.indexOf(SHOW_STATE_END) + SHOW_STATE_END.length(), allInfo.indexOf(HP_END));
            allInfo = allInfo.substring(allInfo.indexOf(HP_END) + HP_END.length(), allInfo.indexOf(AFTER_HP_ALL_INFO_END));

            //角色 role：版主之类的
            String role = "";
            if (!TextUtils.isEmpty(allInfo) || !(allInfo.length() > 2)) {
                int startIndex = allInfo.indexOf(ROLE_TAG);
                int endIndex = allInfo.lastIndexOf(ROLE_TAG);
                if(startIndex == -1 || endIndex == -1 || startIndex == endIndex) {
                    // 隐退
                    role = allInfo;
                }else {
                    // 版主版副
                    allInfo = allInfo.substring(startIndex + ROLE_TAG.length(), endIndex);

                    String middle[] = allInfo.split("\n");
                    String middleS = "";
                    for (int i = 0; i < middle.length; i++) {
                        middleS = middleS + middle[i];
                    }
                    String middle1[] = middleS.split("\\[\\[brd\\]");
                    middleS = "";
                    for (int i = 0; i < middle1.length; i++) {
                        middleS = middleS + middle1[i];
                    }
                    String middle2[] = middleS.split("\\[/brd\\]\\]");
                    for (int i = 0; i < middle2.length; i++) {
                        role = role + middle2[i];
                    }
                }
            }

            BbsUserBase user = new BbsUserBase();
            user.setUsername(username);
            user.setGender(gender);
            user.setNickname(nickName);
            user.setStar(star);
            user.setLoginNum(loginNum);
            user.setSendNum(sendNum);
            user.setLastLoginTime(lastOnTime);
            user.setIp(lastLoginIP);
            user.setMailBox(mail);
            user.setBbsExp(exp);
            user.setLevel(level);
            user.setShowNum(showNum);
            user.setShowWord(showWord);
            user.setHp(hp);
            user.setRole(role);

            return user;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 从网页抓取好友
     * @return
     */
    public static int getFriendsFromWeb(List<Friend> friendList) {
        if (friendList == null)
            return StatusCode.FAIL;

        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getFriendUrl(globalStateSource.getBbsCode()))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            XLog.d("BBSAPI", "抓好友：" + doc.toString());

            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            String username = globalStateSource.getCurrentUserName();
            Elements blocks = doc.select("tr");
            if (blocks.size() == 0)
                return StatusCode.FAIL;

            blocks.remove(0);
            for (Element block : blocks) {
                Elements links = block.select("td");
                String friendName = links.get(1).select("a").text();// 抓好友名
                String friendCustomName = links.get(2).text();// 抓昵称

                UserBase userBase = new UserBase();
                userBase.setUsername(friendName);
                Friend friend = new Friend();
                friend.setUserInfo(userBase);
                friend.setCustomName(friendCustomName);
                friend.setOwnerName(username);
                friendList.add(friend);// 添加到返回值中
            }
            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }


    /**
     * 添加好友
     * @param username
     * @param customName
     * @return
     */
    public static int addFriend(String username, String customName) {
        if (username == null)
            return StatusCode.FAIL;

        try {
            GlobalStateSource globalStateSource = (GlobalStateSource)
                    DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
            String customName2 = URLEncoder.encode(customName, "gb2312");// 转为gb2312编码！
            String code = globalStateSource.getBbsCode();
            // 发送请求
            XHttp http = HttpClientHolder.getMainHttpClient();
            XHttpRequest request = http
                    .newRequest(BbsUrlUtil.addFriendUrl(code, username, customName2))
                    .setMethod(XHttpRequest.HttpMethod.GET);
            XHttpResponse response = http.execute(request);
            if (response == null)
                return StatusCode.HTTP_EXCEPTION;

            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            String docStr = doc.toString();
            XLog.d("BBSAPI", "加好友："+docStr);

            // 检测token无效？
            if (!isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            if (docStr.contains("错误的使用者帐号"))
                return StatusCode.NOT_EXIST_USER;
            if (docStr.contains("此人已经在你的好友名单里了"))
                return StatusCode.ALREADY_FRIEND;

            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.FAIL;
        }
    }

    /**
     * 删除好友
     * @param username
     * @return
     */
    public static int deleteFriend(String username) {
        if (TextUtils.isEmpty(username))
            return StatusCode.FAIL;

        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String code = globalStateSource.getBbsCode();
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.deleteFriendUrl(code, username))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            XLog.d("BBSAPI", "删除好友："+doc.toString());

            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc)) {
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;
            }

            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.FAIL;
        }
    }

    /**
     * 获取站内信列表（默认首页）
     * @param resultList
     * @return
     */
    public static int getDefaultMailList(List<Mail> resultList) {
        if (resultList == null)
            return StatusCode.FAIL;

        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String code = globalStateSource.getBbsCode();
        return getMailList(resultList, BbsUrlUtil.getBbsMailListUrl(code));
    }

    /**
     * 获取站内信列表（任意起始位置开始20个）
     * @param resultList
     * @param start
     * @return
     */
    public static int getMailList(List<Mail> resultList, int start) {
        if (resultList == null)
            return StatusCode.FAIL;

        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String code = globalStateSource.getBbsCode();
        return getMailList(resultList, BbsUrlUtil.getBbsMailListUrl(code, start));
    }

    /**
     * 获取站内信列表
     * @param resultList
     * @param urlStr
     * @return
     */
    private static int getMailList(List<Mail> resultList, String urlStr) {
        if (resultList == null)
            return StatusCode.FAIL;

        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(urlStr)
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            XLog.d("BBSAPI", "抓取站内信列表："+doc.toString());

            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            Elements trs = doc.select("table").select("tr");
            for (int i = 1;i<trs.size()-2;i++) {
                Element tr = trs.get(i);
                Mail mail = new Mail();
                // 抓取序号
                String noStr = tr.getElementsByTag("td").get(0).text();
                if (XStringUtil.isNumber(noStr)) {
                    int no = Integer.parseInt(noStr);
                    mail.setNo(no - 1);
                }
                // 抓取状态
                int status = tr.getElementsByTag("td").get(2).getElementsByTag("img").size();
                if (status == 0) {
                    mail.setStatus(Mail.READ);
                } else {
                    mail.setStatus(Mail.NEW);
                }
                // 抓取发送者
                mail.setSender(tr.getElementsByTag("td").get(3).text());
                // 抓取日期
                mail.setDate(tr.getElementsByTag("td").get(4).text());
                // 抓取标题  (去除异常字符和大小)
                String title = tr.getElementsByTag("td").get(5).text();
                title = title.replace("★ ", "");
                title = title.replaceAll("\\([0-9\\.]+(字节|K|M)\\)", "");
                mail.setTitle(title);
                // 抓取链接
                String mailUrl = tr.getElementsByTag("td").get(5).getElementsByTag("a").attr("href");
                mailUrl = mailUrl.substring(mailUrl.indexOf("M."), mailUrl.indexOf("&num="));
                XLog.d("BBSAPI", "站内信链接："+mailUrl);
                mail.setId(mailUrl);
                resultList.add(mail);
            }
            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.FAIL;
        }
    }

    /**
     * 获取邮件详情
     * @param mailUrl
     * @param num 站内信的索引号（从0开始计数）
     * @param result
     * @return
     */
    public static int getMailDetail(String mailUrl, int num, Mail result) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String code = globalStateSource.getBbsCode();
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getBbsMailDetailUrl(code, mailUrl, num))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            // 检测token无效？
            if (! isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            // 内容结构化解析
            String docStr = doc.toString();
            XLog.d("BBSAPI", "抓取站内信内容："+docStr);
            parseMailContent(docStr, result);

            return StatusCode.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.FAIL;
        }
    }

    /**
     * 结构化解析站内信内容
     * @param mailStr
     * @param mail
     */
    private static void parseMailContent(String mailStr, Mail mail) {
        if (TextUtils.isEmpty(mailStr) || mail == null)
            return;

        Document doc = Jsoup.parse(mailStr, "");
        Elements textAreas = doc.select("textarea");
        if (textAreas.size() == 0)
            return;

        String contentStr = textAreas.get(0).text();
        String[] tag = {
                "寄信人: ",
                " (",
                "标  题: ",
                "发信站: 南京大学小百合站 (",
                ")",
                "来  源: "
        };
        // 抓作者名
        if (contentStr.contains(tag[0])) {
            int nameStartIndex = contentStr.indexOf(tag[0]);
            if (contentStr.contains(tag[1])) {
                int nickNameStartIndex = contentStr.indexOf(tag[1]);
                if (nickNameStartIndex > nameStartIndex + tag[0].length()) {
                    if (TextUtils.isEmpty(mail.getSender())) {
                        String name = contentStr.substring(nameStartIndex + tag[0].length(), nickNameStartIndex);
                        mail.setSender(name);
                    }
                    contentStr = contentStr.substring(nickNameStartIndex + tag[1].length());
                }
            }
        }
        // 抓取昵称（有换行符和“）”！）
        if (contentStr.contains(tag[2])) {
            int titleIndex = contentStr.indexOf(tag[2]);
            if (titleIndex - 2 > 0) {
                String nickName = contentStr.substring(0, titleIndex - 2);
                mail.setSenderNickName(nickName);
                contentStr = contentStr.substring(titleIndex + tag[2].length());
            }
        }
        // 抓取标题（有换行符！）
        if (contentStr.contains(tag[3])) {
            int timeStartIndex = contentStr.indexOf(tag[3]);
            if (timeStartIndex - 1 > 0) {
                String titleStr = contentStr.substring(0, timeStartIndex -1);
                mail.setTitle(titleStr);
                contentStr = contentStr.substring(timeStartIndex + tag[3].length());
            }
        }
        // 抓取时间
        if (contentStr.contains(tag[4])) {
            int timeEndIndex = contentStr.indexOf(tag[4]);
            if (0 < timeEndIndex  && timeEndIndex < 30) {
                if (TextUtils.isEmpty(mail.getDate())) {
                    String date = contentStr.substring(0, timeEndIndex);
                    mail.setDate(date);
                }
                contentStr = contentStr.substring(timeEndIndex + tag[4].length() + 1);
            }
        }
        // 抓取IP
        int ipStartIndex = contentStr.indexOf(tag[5]);
        int ipEndIndex = contentStr.indexOf("\n");
        if (ipStartIndex != -1 && ipEndIndex != -1) {
            String ip = contentStr.substring(ipStartIndex + tag[5].length(), ipEndIndex);
            mail.setIp(ip);
            contentStr = contentStr.substring(ipEndIndex + 1);
        }

        // 抓取内容和图片
        String content = new String(contentStr);
        // 去掉异常字符
        content = content.replace("\u001B", "");
        content = content.replace("\r","");
        content = content.replaceAll("\\[([0-9]|;)*m", "");
        // 去掉结尾“-- ”后面部分
        int endIndex = content.indexOf("--\n※");
        int endIndex2 = content.indexOf("--\n\n※");
        int endIndex3 = content.indexOf("--\n\n\n※");
        int endIndex4 = content.indexOf("--\n[");
        if (endIndex >= 0) {
            content = content.substring(0, endIndex);
        } else if (endIndex2 >= 0) {
            content = content.substring(0, endIndex2);
        } else if (endIndex3 >= 0) {
            content = content.substring(0, endIndex3);
        } else if (endIndex4 >= 0) {
            content = content.substring(0, endIndex4);
        }
        // 去掉结尾“※ 来源:” “※ 修改:”
        int badWordIndex1 = content.indexOf("※ 来源:");
        if (badWordIndex1 != -1) {
            content = content.substring(0, badWordIndex1);
        }
        int badWordIndex2 = content.indexOf("※ 修改:");
        if (badWordIndex2 != -1) {
            content = content.substring(0, badWordIndex2);
        }

        // 处理换行符()
        StringBuffer strBuffer = new StringBuffer(content);
        int brIndex1 = -1;
        int brIndex2 = 0;
        while (-1 <= brIndex1 && brIndex1 < content.length()-1) {
            brIndex2 = content.substring(brIndex1+1).indexOf("\n");
            if (brIndex2 == -1) {
                break;
            } else if (brIndex2 >= 39) {
                strBuffer.setCharAt(brIndex1+1+brIndex2, '\u001B');
            }
            brIndex1 = brIndex1 + 1 + brIndex2;
        }
        String tmp = strBuffer.toString();
        content = tmp.replace("\u001B", "");

        List<String> picUrlList = new ArrayList<String>();
        List<String> contentBlockList = new ArrayList<String>();
        // 通过正则表达式解析出图片,从而区分图片和文字
        String rex = "http://([a-zA-Z0-9/_-]|\\.)+((\\.jpg)|(\\.JPG)|(\\.jpeg)|(\\.JPEG)|(\\.bmp)|(\\.BMP)|(\\.png)|(\\.PNG)|(\\.gif)|(\\.GIF)){1}";
        Pattern pattern = Pattern.compile(rex);
        Matcher m = pattern.matcher(content);
        boolean result = m.find();
        while (result) {
            String imgUrl = m.group();
            picUrlList.add(imgUrl);
            result = m.find();
        }
        // 获取所有的文字块
        String[] wordBlocks = pattern.split(content);
        XLog.d("ARTICLE","wordBlock size:"+wordBlocks.length+", img size:"+picUrlList.size());
        for (int i = 0; i<wordBlocks.length; i++) {
            contentBlockList.add(wordBlocks[i]);
        }
        mail.setImgUrls(picUrlList);
        mail.setWordBlocks(contentBlockList);
    }

    /**
     * 发送邮件
     * @param title
     * @param content
     * @param receiver
     * @return
     */
    public static int sendMail(String title, String content, String receiver) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String url = BbsUrlUtil.sendBbsMailUrl(globalStateSource.getBbsCode(), receiver);
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(url)
                .setMethod(XHttpRequest.HttpMethod.POST)
                .addStringParam("title", title)
                .addStringParam("userid", receiver)
                .addStringParam("text", content)
                .addStringParam("signature", "1");
        request.setCharset("GB2312");
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            String strResult = XHttpUtil.toString(response);
            response.consumeContent();
            XLog.d("BBSAPI", "发邮件返回结果:" + strResult);

            // 检测token无效？
            if (! isTokenLoseEffectiveness(new Document(strResult)))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            if (response.getStatusCode() == 200) {
                return StatusCode.SUCCESS;
            } else {
                return StatusCode.FAIL;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }

    /**
     * 删除邮件
     * @param mailId
     * @return
     */
    public static int deleteMail(String mailId) {
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DefaultDataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        String code = globalStateSource.getBbsCode();
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getBbsDeleteMailUrl(code, mailId))
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            String docStr = doc.toString();
            XLog.d("BBSAPI", "删除站内信：" + docStr);

            // 检测token无效？
            if (!isTokenLoseEffectiveness(doc))
                return StatusCode.BBS_TOKEN_LOSE_EFFECTIVE;

            if (docStr.contains("错误的参数"))
                return StatusCode.FAIL;

            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }

    /**
     * 获取bbs站点当前状态
     * @return
     */
    public static int getBbsStatus(BbsStatus bbsStatus) {
        // 发送请求
        XHttp http = HttpClientHolder.getMainHttpClient();
        XHttpRequest request = http
                .newRequest(BbsUrlUtil.getBbsStatusUrl())
                .setMethod(XHttpRequest.HttpMethod.GET);
        XHttpResponse response = http.execute(request);
        if (response == null)
            return StatusCode.HTTP_EXCEPTION;

        try {
            Document doc = Jsoup.parse(response.getContent(), "gb2312", "");
            String docStr = doc.toString();
            XLog.d("BBSAPI", "bbs全站状态：" + docStr);

            Elements tds = doc.select("table").select("tr").select("td");
            if (tds.size() == 0)
                return StatusCode.FAIL;

            // bbs全站线上人数
            String onlineNumberStr = tds.select("a").get(1).text();
            if (XStringUtil.isNumber(onlineNumberStr)) {
                int onlineNumber = Integer.parseInt(onlineNumberStr);
                XLog.d("BBSAPI", "bbs全站线上人数：" + onlineNumber);
                bbsStatus.setOnlineNumber(onlineNumber);
            }
            // bbs邮件总数
            String totalMailNumberStr = tds.select("a").get(3).text();
            totalMailNumberStr = totalMailNumberStr.replaceAll("\\(新信[0-9]*\\)", "");
            XLog.d("BBSAPI", "bbs邮件总数：" + totalMailNumberStr);
            if (XStringUtil.isNumber(totalMailNumberStr)) {
                int totalMailNumber = Integer.parseInt(totalMailNumberStr);
                bbsStatus.setTotalMailNumber(totalMailNumber);
            }
            // bbs新邮件数量
            if (tds.select("font").size() > 0) {
                String newMailNumberStr = tds.select("font").text();
                XLog.d("BBSAPI", "bbs新邮件数量：" + newMailNumberStr);
                if (XStringUtil.isNumber(newMailNumberStr)) {
                    bbsStatus.setNewMailNumber(Integer.parseInt(newMailNumberStr));
                }
            }
            return StatusCode.SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        } catch (IOException e) {
            e.printStackTrace();
            return StatusCode.HTTP_EXCEPTION;
        }
    }

}
