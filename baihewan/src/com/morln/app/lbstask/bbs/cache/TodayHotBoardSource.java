package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterDataSource;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.res.BbsPic;

/**
 * 今日热门讨论区数据源
 * Created by jasontujun.
 * Date: 12-9-10
 * Time: 下午10:15
 */
public class TodayHotBoardSource extends XBaseAdapterDataSource<Board> {

    public static String name = "今日热门";

    /**
     * 颜色（界面相关）
     * 用于字体和装饰符
     */
    public static int colorRes = R.color.light_red;

    /**
     * 图标名（界面相关）
     * 默认图标为boardIconDefault.png
     */
    public static  String iconFileName = BbsPic.ICON_TODAY_HOT;

    /**
     * 跳转箭头按钮
     */
    public static String btnImgFileName = BbsPic.GOTO_BTN_RED;


    @Override
    public String getSourceName() {
        return SourceName.BBS_TODAY_HOT_BOARD;
    }
}
