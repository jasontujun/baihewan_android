package com.morln.app.lbstask.bbs;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import com.morln.app.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jasontujun.
 * Date: 12-4-17
 * Time: 下午6:46
 */
public class ExpressionMap {


    private static ExpressionMap instance;

    public synchronized static ExpressionMap getInstance() {
        if(instance == null) {
            instance = new ExpressionMap();
        }
        return instance;
    }

    private static final String DIR = "pics/expression/";
    private static final String SUFFIX = ".png";

    private List<ExpressionPair> expressionPairList;
    private String rexStr;// 正则表达式匹配表情字符
    
    private ExpressionMap() {
//        rexStr = "((\\[:s\\])|(\\[:O\\])|(\\[:\\|\\])|(\\[:$\\])|(\\[:X\\])|(\\[:'(\\])|(\\[:-\\|\\])|(\\[:@\\])|(\\[:P\\])" +
//                "|(\\[:D\\])|(\\[:)\\])|(\\[:(\\])|(\\[:Q\\])|(\\[:T\\])|(\\[;P\\])|(\\[;-D\\])|(\\[:!\\])|(\\[:L\\])" +
//                "|(\\[:?\\])|(\\[:U\\])|(\\[:K\\])|(\\[:C-\\])|(\\[;X\\])|(\\[:H\\])|(\\[;bye\\])|(\\[;cool\\])|(\\[:-b\\])" +
//                "|(\\[:-8\\])|(\\[;PT\\])|(\\[:hx\\])|(\\[;K\\])|(\\[:E\\])|(\\[:-(\\])|(\\[;hx\\])|(\\[:-v\\])|(\\[;xx\\]))";
        rexStr = "\\[(:|;)[a-zA-Z0-9-!@'\\(\\)\\?\\|\\$]+\\]";

        expressionPairList = new ArrayList<ExpressionPair>();
        expressionPairList.add(new ExpressionPair("[:s]", "bhw1"));
        expressionPairList.add(new ExpressionPair("[:O]", "bhw2"));
        expressionPairList.add(new ExpressionPair("[:|]", "bhw3"));
        expressionPairList.add(new ExpressionPair("[:$]", "bhw4"));
        expressionPairList.add(new ExpressionPair("[:X]", "bhw5"));
        expressionPairList.add(new ExpressionPair("[:'(]", "bhw6"));
        expressionPairList.add(new ExpressionPair("[:-|]", "bhw7"));
        expressionPairList.add(new ExpressionPair("[:@]", "bhw8"));
        expressionPairList.add(new ExpressionPair("[:P]", "bhw9"));
        expressionPairList.add(new ExpressionPair("[:D]", "bhw10"));
        expressionPairList.add(new ExpressionPair("[:)]", "bhw11"));
        expressionPairList.add(new ExpressionPair("[:(]", "bhw12"));
        expressionPairList.add(new ExpressionPair("[:Q]", "bhw13"));
        expressionPairList.add(new ExpressionPair("[:T]", "bhw14"));
        expressionPairList.add(new ExpressionPair("[;P]", "bhw15"));
        expressionPairList.add(new ExpressionPair("[;-D]", "bhw16"));
        expressionPairList.add(new ExpressionPair("[:!]", "bhw17"));
        expressionPairList.add(new ExpressionPair("[:L]", "bhw18"));
        expressionPairList.add(new ExpressionPair("[:?]", "bhw19"));
        expressionPairList.add(new ExpressionPair("[:U]", "bhw20"));
        expressionPairList.add(new ExpressionPair("[:K]", "bhw21"));
        expressionPairList.add(new ExpressionPair("[:C-]", "bhw22"));
        expressionPairList.add(new ExpressionPair("[;X]", "bhw23"));
        expressionPairList.add(new ExpressionPair("[:H]", "bhw24"));
        expressionPairList.add(new ExpressionPair("[;bye]", "bhw25"));
        expressionPairList.add(new ExpressionPair("[;cool]", "bhw26"));
        expressionPairList.add(new ExpressionPair("[:-b]", "bhw27"));
        expressionPairList.add(new ExpressionPair("[:-8]", "bhw28"));
        expressionPairList.add(new ExpressionPair("[;PT]", "bhw29"));
        expressionPairList.add(new ExpressionPair("[:hx]", "bhw30"));
        expressionPairList.add(new ExpressionPair("[;K]", "bhw31"));
        expressionPairList.add(new ExpressionPair("[:E]", "bhw32"));
        expressionPairList.add(new ExpressionPair("[:-(]", "bhw33"));
        expressionPairList.add(new ExpressionPair("[;hx]", "bhw34"));
        expressionPairList.add(new ExpressionPair("[:-v]", "bhw35"));
        expressionPairList.add(new ExpressionPair("[;xx]", "bhw36"));
    }
    
    public int size() {
        return expressionPairList.size();
    }
    
    public String getBbsExpression(int i) {
        if(0 <= i && i < size()) {
            return expressionPairList.get(i).bbsExpression;
        }else {
            return null;
        }
    }

    public String getLocalExpression(int i) {
        if(0 <= i && i < size()) {
            return expressionPairList.get(i).localExpression;
        }else {
            return null;
        }
    }
    
    public String bbsExp2localExp(String bbsExp) {
        for(int i = 0; i<expressionPairList.size(); i++) {
            if(expressionPairList.get(i).bbsExpression.equals(bbsExp)) {
                return expressionPairList.get(i).localExpression;
            }
        }
        return "";
    }

    public String localExp2bbsExp(String localExp) {
        for(int i = 0; i<expressionPairList.size(); i++) {
            if(expressionPairList.get(i).localExpression.equals(localExp)) {
                return expressionPairList.get(i).bbsExpression;
            }
        }
        return "";
    }

    public CharSequence changeToSpanString(XUILayer uiLayer, CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Pattern expPattern = Pattern.compile(rexStr);
        Matcher matcher = expPattern.matcher(text);
        while (matcher.find()) {
            String bbsExpression = matcher.group();
            String localExpression = bbsExp2localExp(bbsExpression);
            Drawable d = new BitmapDrawable(uiLayer.getBitmap(localExpression));
            d.setBounds(0, 0, uiLayer.screen().dp2px(25), uiLayer.screen().dp2px(25));
            builder.setSpan(new ImageSpan(d, ImageSpan.ALIGN_BOTTOM),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    /**
     * 将bbs表情字符转为本地带html表情的字符串
     * @param source
     * @return
     */
    public String str2html(String source) {
        for(int i = 0; i<expressionPairList.size(); i++) {
            String bbsStr = expressionPairList.get(i).bbsExpression;
            String localStr = expressionPairList.get(i).localExpression;
            source = source.replace(bbsStr, "<img src='"+ localStr +"'/>");
        }
        return source;
    }

    /**
     * 将本地带html表情的字符串转为bbs表情字符
     * @param source
     * @return
     */
    public String html2str(String source) {
        for(int i = 0; i<expressionPairList.size(); i++) {
            String bbsStr = expressionPairList.get(i).bbsExpression;
            String localStr = expressionPairList.get(i).localExpression;
            source = source.replace( "<img src='"+ localStr +"'/>", bbsStr);
        }
        return source;
    }
    
    class ExpressionPair {
        public String bbsExpression;
        public String localExpression;
        
        public ExpressionPair(String bbsExpression, String localExpression) {
            this.bbsExpression = bbsExpression;
            this.localExpression = DIR + localExpression + SUFFIX;
        }
    }
}
