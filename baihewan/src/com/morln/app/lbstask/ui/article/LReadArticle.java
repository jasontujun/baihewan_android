package com.morln.app.lbstask.ui.article;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.ArticleDetail;
import com.morln.app.lbstask.data.model.Board;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseLayer;
import com.xengine.android.system.ui.XUIFrame;
import com.xengine.android.utils.XLog;

/**
 * 显示帖子详情的界面
 * Created by jasontujun.
 * Date: 12-2-27
 * Time: 下午3:09
 */
public class LReadArticle extends XBaseLayer {

    private String articleId;// 此贴id
    private String articleBoard;// 此贴版块
    private int articlePage;// 此贴页数

    // 界面
    private RelativeLayout frame;
    private TextView boardIdTip, boardChineseTip;// 所属版面
    private ImageView backBtn;
    private LinearLayout functionBtnFrame;
    private Button preBtn;
    private Button nextBtn;
    private Button collectBtn;
    private TextView collectBtnLabel;
    private Button quickReplyBtn;
    private Button refreshBtn;
    private Button topBtn;
    private ListView content;
    private AArticle articleAdapter;

    private TextView deleteArticleTip;
    private TextView nothingTip;
    private LinearLayout getContentBtnFrame;
    private Button getContentBtn;

    public LReadArticle(XUIFrame uiFrame, String aId, String aBoard) {
        super(uiFrame);

        this.articleId = aId;
        this.articleBoard = aBoard;

        setContentView(R.layout.bbs_read_article);
        frame = (RelativeLayout) findViewById(R.id.frame);
        boardIdTip = (TextView) findViewById(R.id.board_id_tip);
        boardChineseTip = (TextView) findViewById(R.id.board_chinese_tip);
        backBtn = (ImageView) findViewById(R.id.back_btn);
        deleteArticleTip = (TextView) findViewById(R.id.delete_article_tip);
        functionBtnFrame = (LinearLayout) findViewById(R.id.function_btn_frame);
        preBtn = (Button) findViewById(R.id.pre_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        collectBtn = (Button) findViewById(R.id.collect_btn);
        collectBtnLabel = (TextView) findViewById(R.id.collect_btn_label);
        quickReplyBtn = (Button) findViewById(R.id.quick_reply_btn);
        refreshBtn = (Button) findViewById(R.id.refresh_btn);
        topBtn = (Button) findViewById(R.id.top_btn);
        content = (ListView) findViewById(R.id.content);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);
        getContentBtn = (Button) findViewById(R.id.get_content_btn);
        getContentBtnFrame = (LinearLayout) findViewById(R.id.get_content_btn_frame);

        // 按钮监听
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleAdapter.clearImgAsyncTasks();// 清空后台任务

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_ARTICLE_BACK;
                handler1.sendMessage(msg);
            }
        });
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 初始化“回到顶部”按钮
                if (!content.isStackFromBottom()) {
                    content.setStackFromBottom(true);
                }
                content.setStackFromBottom(false);
            }
        });
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleAdapter.clearImgAsyncTasks();// 清除后台线程

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_PRE_ARTICLE;
                handler1.sendMessage(msg);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleAdapter.clearImgAsyncTasks();// 清除后台线程

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_NEXT_ARTICLE;
                handler1.sendMessage(msg);
            }
        });
        quickReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 登陆权限检测
                GlobalStateSource globalStateSource = (GlobalStateSource) DefaultDataRepo.
                        getInstance().getSource(SourceName.GLOBAL_STATE);
                if(!globalStateSource.isLogin()) {
                    new DLogin(LReadArticle.this, true).show();
                    return;
                }

                ArticleDetail articleDetail = BbsArticleMgr.getInstance()
                        .getThemeArticleFromLocal(articleId, articleBoard);
                if(articleDetail != null) {
                    String title = articleDetail.getTitle();
                    XLog.d("REPLY", "title:" + title);
                    new DQuickReply(LReadArticle.this, articleId, articleBoard, title).show();
                }else {
                    Toast.makeText(getContext(), "帖子不存在，无法回复", Toast.LENGTH_SHORT).show();
                }
            }
        });
        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BbsPersonMgr bbsPersonMgr = BbsPersonMgr.getInstance();
                if(bbsPersonMgr.containsCollectedArticle(articleId)) {
                    bbsPersonMgr.deleteCollectedArticle(articleId);
                    Toast.makeText(getContext(), "取消收藏~", Toast.LENGTH_SHORT).show();
                }else {
                    bbsPersonMgr.addCollectArticle(articleBoard, articleId);
                    Toast.makeText(getContext(), "收藏成功！", Toast.LENGTH_SHORT).show();
                }
                refreshTopBtnFrame();
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshArticle();
            }
        });
        getContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取删除十大功能
                new GetArticleFromServerTask().execute(null);
            }
        });


        articleAdapter = new AArticle(LReadArticle.this, content);
        articleAdapter.setRefreshListener(new AArticle.RefreshListener() {
            @Override
            public void isArticleDeleted(boolean isDeleted) {
                if(isDeleted)
                    setDeleteArticleContentVisibility(true);
                else
                    setArticleContentVisibility(true);
            }
        });
        content.setAdapter(articleAdapter);
        content.setFastScrollEnabled(true);
        content.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                articleAdapter.onScrollStateChanged(absListView, i);
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                articleAdapter.onScroll(absListView, firstVisibleItem,
                        visibleItemCount, totalItemCount);
                View v = absListView.getChildAt(0);
                if (v == null) {
                    return;
                }

                if (firstVisibleItem == 0 && v.getTop() >= 0) {
                    topBtn.setVisibility(View.GONE);
                } else {
                    topBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        // 显示文章
        showArticle(articleId, articleBoard);
    }


    /**
     * 设置正常帖子的可见性
     * @param visibility
     */
    private void setArticleContentVisibility(boolean visibility) {
        functionBtnFrame.setVisibility(View.VISIBLE);
        deleteArticleTip.setVisibility(View.INVISIBLE);
        if(visibility) {
            nothingTip.setText("帖子可能被删除了");
            nothingTip.setVisibility(View.INVISIBLE);
            getContentBtnFrame.setVisibility(View.INVISIBLE);
            content.setVisibility(View.VISIBLE);
        }else {
            nothingTip.setVisibility(View.VISIBLE);
            getContentBtnFrame.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
        }
    }

    private void setDeleteArticleContentVisibility(boolean visibility) {
        if(visibility) {// 成功显示删除后的被找到的帖子
            nothingTip.setVisibility(View.INVISIBLE);
            getContentBtnFrame.setVisibility(View.INVISIBLE);
            content.setVisibility(View.VISIBLE);
            // 相关功能按钮隐藏
            functionBtnFrame.setVisibility(View.INVISIBLE);
            deleteArticleTip.setVisibility(View.VISIBLE);
        }else {// 彻底找不到帖子
            nothingTip.setText("手气不好,没找到帖子");
            nothingTip.setVisibility(View.VISIBLE);
            getContentBtnFrame.setVisibility(View.INVISIBLE);
            content.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 显示文章
     * @param aId
     * @param aBoard
     */
    public void showArticle(String aId, String aBoard) {
        this.articleId = aId;
        this.articleBoard = aBoard;
        this.articlePage = -1;// 默认为全文加载~~

        articleAdapter.clearImgAsyncTasks();// 清除后台线程

        if (BbsArticleMgr.getInstance()
                .getThemeArticleFromLocal(articleId, articleBoard) != null) {
            refreshTopBtnFrame();
            refreshBoardTip();
            refreshArticleToTop();

            articleAdapter.autoDownloadImg();// 自动下载图片
        } else {
            new GetArticleTask().execute(null);
        }
    }


    /**
     * 刷新顶部
     */
    public void refreshTopBtnFrame() {
        if(BbsPersonMgr.getInstance().containsCollectedArticle(articleId)) {
            collectBtn.setBackgroundResource(R.drawable.btn_collect_pressed);
            collectBtnLabel.setText("已收藏");
        }else {
            collectBtn.setBackgroundResource(R.drawable.btn_collect);
            collectBtnLabel.setText("收藏本篇");
        }
    }

    /**
     * 刷新所属版面
     */
    public void refreshBoardTip() {
        boardIdTip.setText(articleBoard);
        Board board = BbsBoardMgr.getInstance().getBoard(articleBoard);
        if(board != null) {
            boardChineseTip.setText(board.getChinesName());
        }
    }

    /**
     * 刷新帖子(启动异步线程)
     */
    public void refreshArticle() {
        articleAdapter.clearImgAsyncTasks();// 清除后台线程
        refreshTopBtnFrame();
        refreshBoardTip();
        new RefreshArticleTask().execute(null);
    }

    public void refreshArticleToTop() {
        articleAdapter.refresh(articleId, articleBoard);
        // 回到顶部
        if (!content.isStackFromBottom()) {
            content.setStackFromBottom(true);
        }
        content.setStackFromBottom(false);
    }



    @Override
    public int back() {
        articleAdapter.clearImgAsyncTasks();
        return XBackType.SELF_BACK;
    }

    @Override
    public Handler getLayerHandler() {
        return null;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (state != null) {
            content.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = content.onSaveInstanceState();
    }

    /**
     * 从网页抓取文章的异步线程
     */
    private class GetArticleTask extends AsyncTask<Void, Void, Void> {
        private DialogUtil.WaitingDialog waitingDialog;
        private ArticleDetail article;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            article = BbsArticleMgr.getInstance()
                    .getThemeArticleFromWeb(articleBoard, articleId, articlePage);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (article == null) {
                Toast.makeText(getContext(), "对不起，找不到这个帖子...", Toast.LENGTH_SHORT).show();
                setArticleContentVisibility(false);
            } else {
                setArticleContentVisibility(true);
                refreshArticleToTop();
                refreshTopBtnFrame();
                refreshBoardTip();

                articleAdapter.autoDownloadImg();// 自动下载图片
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }

    /**
     * 从服务器抓取文章的异步线程
     */
    private class GetArticleFromServerTask extends AsyncTask<Void, Void, Void> {
        private DialogUtil.WaitingDialog waitingDialog;
        private ArticleDetail article;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            article = BbsArticleMgr.getInstance()
                    .getArticleFromServer(getContext(), articleId, articleBoard);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (article == null) {
                Toast.makeText(getContext(), "还是找不到这个帖子哦", Toast.LENGTH_SHORT).show();
                setDeleteArticleContentVisibility(false);
            } else {
                Toast.makeText(getContext(), "手气不错哈！", Toast.LENGTH_SHORT).show();
                setDeleteArticleContentVisibility(true);
                refreshArticleToTop();

                articleAdapter.autoDownloadImg();// 自动下载图片
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }


    private class RefreshArticleTask extends AsyncTask<Void, Void, Void> {
        private DialogUtil.WaitingDialog waitingDialog;
        private ArticleDetail article;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            article = BbsArticleMgr.getInstance()
                    .getThemeArticleFromWeb(articleBoard, articleId, articlePage);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (article == null) {
                Toast.makeText(getContext(), "刷新失败...", Toast.LENGTH_SHORT).show();
            } else {
                setArticleContentVisibility(true);
                refreshArticleToTop();

                articleAdapter.autoDownloadImg();// 自动下载图片
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }

}
