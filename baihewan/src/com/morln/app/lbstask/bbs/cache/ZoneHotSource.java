package com.morln.app.lbstask.bbs.cache;

import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.data.cache.XBaseAdapterIdDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-13
 * Time: 上午11:30
 */
public class ZoneHotSource extends XBaseAdapterIdDataSource<ArticleBase> {

    private BoardSource boardSource;

    public ZoneHotSource(BoardSource boardSource) {
        super();
        this.boardSource = boardSource;
    }

    /**
     * 获取当前存在的各区热帖的区列表
     * @return
     */
    public List<String> getZoneList() {
        List<String> resultList = new ArrayList<String>();
        for (int i = 0; i < itemList.size(); i++) {
            String boardStr = itemList.get(i).getBoard();
            Board board = boardSource.getById(boardStr);
            if (board != null) {
                String zone = board.getZoneBelong();
                if (!resultList.contains(zone))
                    resultList.add(zone);
            }
        }
        return resultList;
    }

    /**
     * 根据区名获取该区的热帖列表
     * @param zone
     * @return
     */
    public List<ArticleBase> getBaseZone(String zone) {
        List<ArticleBase> resultList = new ArrayList<ArticleBase>();
        for (int i =0; i<itemList.size(); i++) {
            String boardStr = itemList.get(i).getBoard();
            Board board = boardSource.getById(boardStr);
            if (board != null) {
                String z = board.getZoneBelong();
                if (z != null && z.equals(zone))
                    resultList.add(itemList.get(i));
            }
        }
        return resultList;
    }

    @Override
    public String getId(ArticleBase item) {
        return item.createId();
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_ZONE_HOT;
    }
}
