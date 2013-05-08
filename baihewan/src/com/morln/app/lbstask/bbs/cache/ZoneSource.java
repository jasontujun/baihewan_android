package com.morln.app.lbstask.bbs.cache;

import com.morln.app.data.cache.XBaseAdapterDataSource;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.bbs.model.Zone;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.utils.XStringUtil;

/**
 * 存储所有区List<Zone>
 * Zone = List<Board>
 * Board = List<Article>
 *
 * Created by jasontujun.
 * Date: 12-3-1
 * Time: 上午12:49
 */
public class ZoneSource extends XBaseAdapterDataSource<Zone> {

    public ZoneSource() {
        super();
        initDefaultData();
    }

    /**
     * 区数据源的缺省数据
     */
    private void initDefaultData() {
        // 初始化数据
//        Zone myRss = new Zone("我的订阅");
//        myRss.setIconFileName(BbsPic.ICON_RSS);
//        myRss.setColorRes(R.color.light_red);
//        myRss.setBtnImgFileName(BbsPic.GOTO_BTN_RED);
//        Zone todayHot = new Zone("今日热门");
//        todayHot.setIconFileName(BbsPic.ICON_TODAY_HOT);
//        todayHot.setColorRes(R.color.light_purple);
//        todayHot.setBtnImgFileName(BbsPic.GOTO_BTN_PURPLE);
//        add(todayHot);
        add(new Zone("南京大学", 1));
        add(new Zone("乡情校谊", 2));
        add(new Zone("电脑技术", 3));
        add(new Zone("学术科学", 4));
        add(new Zone("文化艺术", 5));
        add(new Zone("体育娱乐", 6));
        add(new Zone("感性休闲", 7));
        add(new Zone("新闻信息", 8));
        add(new Zone("百合广角", 9));
        add(new Zone("校务信箱", 10));
        add(new Zone("社团群体", 11));
        add(new Zone("本站系统", 0));
    }

    /**
     * 将版面映射到区,并添加到Zone对象中
     */
    public void initBoardOfZone(BoardSource boardSource) {
        for(int i = 0; i<size(); i++){
            Zone zone = get(i);
            zone.getBoardList().clear();// 清空原有的版面数据
            for(int j = 0; j<boardSource.size(); j++){
                Board board = boardSource.get(j);
                if(zone.getName().equals(board.getZoneBelong())) {
                    zone.addBoard(board);
                }
            }
        }
    }

    /**
     * 根据区名称，获取sec值。（sec用于通信参数）
     * @param name
     * @return
     */
    public int getZoneSecByName(String name) {
        if(XStringUtil.isNullOrEmpty(name)) {
            return -1;
        }

        for(int i = 0; i<size(); i++) {
            Zone zone = get(i);
            if(zone.getName().equals(name)) {
                return zone.getSec();
            }
        }
        return -1;
    }


    public String getZoneNameBySec(int sec) {
        for(int i = 0; i<size(); i++) {
            Zone zone = get(i);
            if(zone.getSec() == sec) {
                return zone.getName();
            }
        }
        return null;
    }


    public Zone get(String name) {
        for(int i = 0; i<size(); i++) {
            if(get(i).getName().equals(name)) {
                return get(i);
            }
        }
        return null;
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_ZONE;
    }
}
