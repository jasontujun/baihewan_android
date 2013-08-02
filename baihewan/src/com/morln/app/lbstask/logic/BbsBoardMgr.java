package com.morln.app.lbstask.logic;

import android.content.Context;
import com.morln.app.lbstask.bbs.cache.BoardSource;
import com.morln.app.lbstask.bbs.cache.CollectBoardSource;
import com.morln.app.lbstask.bbs.cache.TodayHotBoardSource;
import com.morln.app.lbstask.bbs.cache.ZoneSource;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.bbs.model.CollectedBoard;
import com.morln.app.lbstask.bbs.model.Zone;
import com.morln.app.lbstask.bbs.session.BbsAPI;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.session.apinew.BoardAPINew;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责管理区、版块、帖子（ArticleBase）
 * Created by jasontujun.
 * Date: 12-2-29
 * Time: 下午7:20
 */
public class BbsBoardMgr {
    private static BbsBoardMgr instance;

    public synchronized static BbsBoardMgr getInstance() {
        if (instance == null) {
            instance = new BbsBoardMgr();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    private GlobalStateSource globalStateSource;
    private ZoneSource zoneSource;
    private BoardSource boardSource;
    private CollectBoardSource collectBoardSource;
    private TodayHotBoardSource todayHotBoardSource;

    private BbsBoardMgr() {
        DataRepo repo = DataRepo.getInstance();
        globalStateSource = (GlobalStateSource) repo.getSource(SourceName.GLOBAL_STATE);
        zoneSource = (ZoneSource) repo.getSource(SourceName.BBS_ZONE);
        boardSource = (BoardSource) repo.getSource(SourceName.BBS_BOARD);
        collectBoardSource = (CollectBoardSource) repo.getSource(SourceName.BBS_COLLECTION_BOARD);
        todayHotBoardSource = (TodayHotBoardSource) repo.getSource(SourceName.BBS_TODAY_HOT_BOARD);
    }

    /**
     * 通过索引位置获取版块(在所有版面的顺序)
     * @param index
     * @return
     */
    public Board getBoard(int index) {
        return boardSource.get(index);
    }

    /**
     * 通过版块Id获取board
     * @param boardId
     * @return
     */
    public Board getBoard(String boardId) {
        return boardSource.getById(boardId);
    }


    /**
     * 获取某区的所有版块列表
     * @param zoneName
     * @return
     */
    public List<Board> getBoardOfZone(String zoneName) {
        return zoneSource.get(zoneName).getBoardList();
    }


    /**
     * 获取版块内的帖子列表。(界面调用)
     * 先从本地获取。如果本地没有，从网页抓取。
     * @param boardId
     * @param page
     * @return
     */
    public List<ArticleBase> getLocalThemeArticleList(String boardId, int page) {
        Board board = getBoard(boardId);
        if (board == null) {
            return new ArrayList<ArticleBase>();
        }

        List<ArticleBase> articleList = board.getArticleList(page, true);
        return articleList;
    }

    /**
     * 从网页获取主题帖列表
     * @param page <=0抓取首页，>0抓取下一页
     * @return
     */
    public int getThemeArticleListFromWeb(String boardId, int page) {
        Board board = getBoard(boardId);
        return BbsAPI.getThemeArticleListFromWeb(board, page);
    }

    /**
     * 从网页获取一般模式下的帖子列表
     * @param page
     * @return
     */
    private List<ArticleBase> getNormalArticleListFromWeb(String boardId, int page) {
        return null;
    }


    /**
     * 在当前区中，搜索与输入字符串匹配的版面。
     * 匹配规则:版面中文或英文名包含有input字符串即可
     * @param currentZone  当前区名。如果为null或空时，全站搜索版面
     * @param input
     * @return
     */
    public List<Board> getFilteredBoardList(String currentZone, String input) {
        List<Board> resultList = new ArrayList<Board>();
        List<Board> sourceList;
        if (XStringUtil.isNullOrEmpty(currentZone)) {
            sourceList = boardSource.copyAll();
        } else {
            Zone zone = zoneSource.get(currentZone);
            sourceList = zone.getBoardList();
        }
        // 将用户的输入转化为小写
        String inputLowerCase = input.toLowerCase();
        for (int i =0; i<sourceList.size(); i++) {
            // 将版面的Id也转换为小写
            String boardIdLowerCase = sourceList.get(i).getBoardId().toLowerCase();
            if (boardIdLowerCase.contains(inputLowerCase) ||
                    sourceList.get(i).getChinesName().contains(inputLowerCase)) {
                resultList.add(sourceList.get(i));
            }
        }
        return resultList;
    }



    /**
     * 获取收藏版面的Id列表
     * @return
     */
    public List<Board> getCollectedBoards() {
        List<Board> result = new ArrayList<Board>();
        List<CollectedBoard> sourceList = collectBoardSource.getByUsername(globalStateSource.getCurrentUserName());
        for (int i = 0; i<sourceList.size(); i++) {
            String boardId = sourceList.get(i).getBoardId();
            Board board = boardSource.getById(boardId);
            if (board != null) {
                result.add(board);
            }
        }
        return result;
    }

    /**
     * 获取收藏版面列表
     * @return
     */
    public List<String> getCollectedBoardIds() {
        List<String> result = new ArrayList<String>();
        List<CollectedBoard> sourceList = collectBoardSource.getByUsername(globalStateSource.getCurrentUserName());
        for (int i = 0; i<sourceList.size(); i++) {
            result.add(sourceList.get(i).getBoardId());
        }
        return result;
    }

    /**
     * 判断是否已经订阅此版面
     * @param boardId
     * @return
     */
    public boolean containsCollectedBoard(String boardId) {
        if (XStringUtil.isNullOrEmpty(boardId)) {
            return false;
        }

        String username = globalStateSource.getCurrentUserName();
        return collectBoardSource.getIndexByUsernameId(username, boardId) != -1;
    }

    /**
     * 添加收藏版面
     * @param boardId
     */
    public void addCollectedBoard(String boardId) {
        if (XStringUtil.isNullOrEmpty(boardId)) {
            return;
        }

        String username = globalStateSource.getCurrentUserName();
        CollectedBoard board = new CollectedBoard(username, boardId);
        collectBoardSource.add(board);
        collectBoardSource.saveToDatabase();// 同步到数据库
    }

    /**
     * 添加收藏版面
     * @param boardIds
     */
    public void addAllCollectedBoards(List<String> boardIds) {
        List<CollectedBoard> collectedBoardList = new ArrayList<CollectedBoard>();
        for (int i = 0; i<boardIds.size(); i++) {
            collectedBoardList.add(new CollectedBoard(globalStateSource.getCurrentUserName(), boardIds.get(i)));
        }
        collectBoardSource.addAll(collectedBoardList);
        collectBoardSource.saveToDatabase();// 同步到数据库
    }

    /**
     * 删除收藏版面
     * @param boardId
     */
    public void deleteCollectedBoard(String boardId) {
        if (XStringUtil.isNullOrEmpty(boardId)) {
            return;
        }
        CollectedBoard board = new CollectedBoard(globalStateSource.getCurrentUserName(), boardId);
        collectBoardSource.deleteByUsernameId(board.getUserName(), board.getBoardId());
        collectBoardSource.saveToDatabase();// 同步到数据库
    }

    /**
     * 删除收藏版面
     * @param boardIds
     */
    public void deleteAllCollectedBoards(List<String> boardIds) {
        collectBoardSource.deleteAllByUsernameId(globalStateSource.getCurrentUserName(), boardIds);
        collectBoardSource.saveToDatabase();// 同步到数据库
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
    public int searchArticle(String author, String contain1, String contain2,
                                           String notcontain, String startDay, String endDay,
                                           List<ArticleBase> resultList) {
        return BbsAPI.searchArticle(author, contain1, contain2,
                notcontain, startDay, endDay, resultList);
    }


    /**
     * 获得热门版面（解析js代码）
     * @return
     */
    public int getHotBoard() {
        List<Board> resultList = new ArrayList<Board>();
        int resultCode = BbsAPI.getHotBoard(resultList);
        if (StatusCode.isSuccess(resultCode)) {
            todayHotBoardSource.clear();
            todayHotBoardSource.addAll(resultList);
        }
        return resultCode;
    }


    /**
     * 同步订阅讨论区（合并，只增不减）
     * @return
     */
    public int mergeRssBoard() {
        List<String> webBoardList = new ArrayList<String>();// 网上的版面数据
        int getRssResultCode = BbsAPI.getRssBoard(webBoardList);
        if (StatusCode.isSuccess(getRssResultCode)) {
            // 将抓取的版面合并到本地
            addAllCollectedBoards(webBoardList);
            collectBoardSource.saveToDatabase();

            // 上传到小百合
            return uploadRssBoard();
        }
        return getRssResultCode;
    }

    /**
     * 下载并覆盖本地的订阅
     * @return
     */
    public int downloadRssBoard() {
        List<String> webBoardList = new ArrayList<String>();// 网上的版面数据
        int resultCode = BbsAPI.getRssBoard(webBoardList);
        if (StatusCode.isSuccess(resultCode)) {
            collectBoardSource.deleteByUsername(globalStateSource.getCurrentUserName());
            addAllCollectedBoards(webBoardList);
            collectBoardSource.saveToDatabase();
        }
        return resultCode;
    }

    /**
     * 上传订阅
     * @return
     */
    public int uploadRssBoard() {
        String username = globalStateSource.getCurrentUserName();
        List<CollectedBoard> collectedBoardList = collectBoardSource.getByUsername(username);
        List<String> boardIds = new ArrayList<String>();
        for (int i = 0; i < collectedBoardList.size(); i++) {
            boardIds.add(collectedBoardList.get(i).getBoardId());
        }
        int sendRssResultCode = BbsAPI.sendRssBoard(boardIds);
        XLog.d("BBSAPI", "上传RSS版面的返回码：" + sendRssResultCode);
        return sendRssResultCode;
    }


    /**
     * 更新版面数据
     * @return
     */
    public int updateBoard(Context context) {
        List<com.morln.app.lbstask.session.bean.board.Board> sessionList =
                new ArrayList<com.morln.app.lbstask.session.bean.board.Board>();
        int resultCode = new BoardAPINew(context).downloadBoard(sessionList,
                globalStateSource.getBoardTimeStamp());
        XLog.d("API", "更新版面数据的返回码：" + resultCode);
        if (StatusCode.isSuccess(resultCode)) {
            List<Board> localList = new ArrayList<Board>();
            for (com.morln.app.lbstask.session.bean.board.Board sessionBean : sessionList) {
                localList.add(new Board(sessionBean));
            }
            XLog.d("API", "更新版面数据的数量：" + localList.size());
            boardSource.addAll(localList);
            boardSource.saveToDatabase();
        }
        return resultCode;

    }

}
