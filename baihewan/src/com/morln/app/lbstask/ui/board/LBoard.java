package com.morln.app.lbstask.ui.board;

import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.ui.controls.XListView;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseLayer;
import com.xengine.android.system.ui.XUIFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-9-9
 * Time: 下午12:54
 */
public class LBoard extends XBaseLayer implements Linear<ArticleBase> {
    private GlobalStateSource globalStateSource;
    private BbsBoardMgr bbsBoardMgr;

    private String currentBoard;// 当前版面
    private List<ArticleBase> currentArticleList = new ArrayList<ArticleBase>();// 当前帖子列表
    private ArticleBase currentReadArticle;// 当前阅读的帖子

    // 界面
    private RelativeLayout frame;
    private ImageView backBtn;
    private Button writeBtn, rssBtn;
    private TextView rssBtnLabel;
    private Button topBtn;// 回到顶部按钮
    private XListView articleList;
    private ArticleListAdapter articleListAdapter;
    private TextView boardIdTip, boardChineseTip;

    // 异步线程
    private RefreshArticleListTask refreshTask;
    private MoreThemeArticleListTask moreTask;

    /**
     * 构造函数，记得调用setContentView()哦
     *
     * @param uiFrame
     */
    public LBoard(XUIFrame uiFrame, String board) {
        super(uiFrame);
        this.currentBoard = board;
        bbsBoardMgr = BbsBoardMgr.getInstance();
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.bbs_board);
        frame = (RelativeLayout) findViewById(R.id.frame);
        backBtn = (ImageView) findViewById(R.id.back_to_board_btn);
        writeBtn = (Button) findViewById(R.id.top_btn1);
        rssBtn = (Button) findViewById(R.id.top_btn2);
        rssBtnLabel = (TextView) findViewById(R.id.top_btn2_label);
        boardIdTip = (TextView) findViewById(R.id.current_board_id_tip);
        boardChineseTip = (TextView) findViewById(R.id.current_board_chinese_tip);
        articleList = (XListView) findViewById(R.id.content_list);
        topBtn = (Button) findViewById(R.id.top_btn);

        // 按钮监听
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = getFrameHandler();
                Message msg = handler.obtainMessage(MainMsg.SEE_BOARD_BACK);
                handler.sendMessage(msg);
            }
        });
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!articleList.isStackFromBottom()) {
                    articleList.setStackFromBottom(true);
                }
                articleList.setStackFromBottom(false);

//                content.smoothScrollToPosition(0);// TIP 要求2.2以上
            }
        });
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 登陆权限检测
                if (!globalStateSource.isLogin()) {
                    new DLogin(LBoard.this, true).show();
                    return;
                }

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.WRITE_ARTICLE;
                Bundle bundle = new Bundle();
                bundle.putString("board", currentBoard);
                msg.setData(bundle);
                handler1.sendMessage(msg);
            }
        });
        rssBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bbsBoardMgr.containsCollectedBoard(currentBoard)) {
                    bbsBoardMgr.deleteCollectedBoard(currentBoard);
                    Toast.makeText(getContext(), "取消订阅~", Toast.LENGTH_SHORT).show();
                } else {
                    bbsBoardMgr.addCollectedBoard(currentBoard);
                    Toast.makeText(getContext(), "订阅成功！", Toast.LENGTH_SHORT).show();
                }
                refreshTopBtnFrame();
            }
        });

        // 版内帖子列表
        initListBottom(articleList);
        articleListAdapter = new ArticleListAdapter();
        articleList.setAdapter(articleListAdapter);
        articleList.setRefreshable(true);
        articleList.setOnRefreshListener(new XListView.OnRefreshListener() { // 下拉刷新
            @Override
            public void onRefresh() {
                refreshTask = new RefreshArticleListTask(false);
                refreshTask.execute(null);
            }
        });
        articleList.setOnXListScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                View v = absListView.getChildAt(0);
                if(v == null) {
                    return;
                }

                if(firstVisibleItem == 0 && v.getTop() >= 0) {
                    topBtn.setVisibility(View.GONE);
                }else {
                    topBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        // 刷新内容
        refresh();
        new EnterThemeArticleListTask().execute(null);
    }

    private TextView moreLabel;
    private boolean isGettingMore;
    /**
     * 初始化列表底部-“加载更多”
     * @param listView
     */
    private void initListBottom(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_article_list_item_bottom, null);
        moreLabel = (TextView) contentFrame.findViewById(R.id.more_label);
        moreLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isGettingMore) {
                    refreshListBottom(true);
                    // 异步线程加载“更多”。如果失败说明没有更多了
                    if(refreshTask != null) {
                        // 取消刷新线程，防止冲突
                        refreshTask.cancel(true);
                    }
                    moreTask = new MoreThemeArticleListTask();
                    moreTask.execute(null);
                }
            }
        });
        listView.addFooterView(contentFrame);
        refreshListBottom(false);
    }

    private void refreshListBottom(boolean gettingMore) {
        isGettingMore = gettingMore;
        if(isGettingMore) {
            moreLabel.setText("正在加载……");
        } else {
            moreLabel.setText("查看更多");
        }
    }


    public void refresh() {
        refreshTopBtnFrame();
        Board board = bbsBoardMgr.getBoard(currentBoard);
        boardIdTip.setText(currentBoard);
        boardChineseTip.setText(board.getChinesName());

        articleListAdapter.notifyDataSetChanged();
    }


    /**
     * 刷新顶部
     */
    public void refreshTopBtnFrame() {
        if(bbsBoardMgr.containsCollectedBoard(currentBoard)) {
            rssBtn.setBackgroundResource(R.drawable.btn_rss_pressed);
            rssBtnLabel.setText("已订阅");
        }else {
            rssBtn.setBackgroundResource(R.drawable.btn_rss);
            rssBtnLabel.setText("订阅版面");
        }
    }


    @Override
    public ArticleBase getPre() {
        if(currentArticleList == null) {
            return null;
        }
        // 判断当前是哪一组的那篇帖子
        int articleIndex = -1;
        for(int i =0; i<currentArticleList.size(); i++) {
            if(currentArticleList.get(i).getId().equals(currentReadArticle.getId()) &&
                    currentArticleList.get(i).getBoard().equals(currentReadArticle.getBoard())) {
                articleIndex = i;
                break;
            }
        }
        // 返回上一篇帖子
        if(articleIndex > 0) {
            currentReadArticle = currentArticleList.get(articleIndex - 1);
            return currentReadArticle;
        }
        return null;
    }

    @Override
    public ArticleBase getNext() {
        if(currentArticleList == null) {
            return null;
        }
        // 判断当前是哪一组的那篇帖子
        int articleIndex = -1;
        for(int i =0; i<currentArticleList.size(); i++) {
            if(currentArticleList.get(i).getId().equals(currentReadArticle.getId()) &&
                    currentArticleList.get(i).getBoard().equals(currentReadArticle.getBoard())) {
                articleIndex = i;
                break;
            }
        }
        // 返回下一篇帖子
        if(articleIndex != -1 && articleIndex < currentArticleList.size()-1) {
            currentReadArticle = currentArticleList.get(articleIndex + 1);
            return currentReadArticle;
        }
        return null;
    }

    @Override
    public Handler getLayerHandler() {
        return null;
    }

    @Override
    public int back() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
        if (moreTask != null) {
            moreTask.cancel(true);
        }
        return XBackType.SELF_BACK;
    }

    /**
     * 排序
     */
    private void sortArticleList() {
        if (currentArticleList != null) {
            Collections.sort(currentArticleList, ArticleBase.getBoardComparator());
        }
    }


    /**
     * 帖子列表适配器
     */
    private class ArticleListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (currentArticleList == null) {
                return 0;
            }
            return currentArticleList.size();
        }

        @Override
        public Object getItem(int i) {
            if (currentArticleList == null) {
                return null;
            }

            return currentArticleList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class BottomHolder{
            public TextView moreLabel;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decoration;
            public ImageView label;
            public TextView author;
            public TextView up;
            public TextView popularity;
            public TextView title;
            public TextView time;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if (item == null){
                return null;
            }

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_article_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decoration = (ImageView) convertView.findViewById(R.id.decoration);
                holder.label = (ImageView) convertView.findViewById(R.id.label);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.up = (TextView) convertView.findViewById(R.id.up);
                holder.popularity = (TextView) convertView.findViewById(R.id.hot);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ArticleBase article = (ArticleBase) getItem(i);
            // 背景
            holder.frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentReadArticle = article;
                    Handler handler1 = getFrameHandler();
                    Message msg = handler1.obtainMessage();
                    msg.what = MainMsg.SEE_ARTICLE_DETAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("id", article.getId());
                    bundle.putString("board", article.getBoard());
                    msg.setData(bundle);
                    handler1.sendMessage(msg);
                }
            });
            // 装饰符
            if (article.isUp()) {
                holder.decoration.setImageResource(R.color.light_red);
                holder.label.setVisibility(View.VISIBLE);
                setViewBackground(holder.label, BbsPic.LABEL_HOT);
            } else {
                if (article.getNo() <= 0) {
                    holder.decoration.setImageResource(R.color.dark_gray);
                } else {
                    holder.decoration.setImageResource(R.color.light_green);
                }
                holder.label.setVisibility(View.GONE);
            }
            // 作者
            holder.author.setText(article.getAuthorName());
            // 置顶
            if (article.isUp()){
                holder.up.setVisibility(View.VISIBLE);
            } else {
                holder.up.setVisibility(View.INVISIBLE);
            }
            // 人气
            if(article.isUp()){
                holder.popularity.setText(""+article.getPopularity());
            } else {
                holder.popularity.setText(""+article.getReplyCount()+"/"+article.getPopularity());
            }
            // 标题
            holder.title.setText(article.getTitle());
            // 时间
            holder.time.setText(article.getDate());

            return convertView;
        }
    }


    /**
     * 进入主题模式下的帖子列表（先刷新首页，再加载本地列表）
     */
    private class EnterThemeArticleListTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsBoardMgr.getThemeArticleListFromWeb(currentBoard, 0);// 先刷新首页
            if (StatusCode.isSuccess(resultCode)) {
                currentArticleList = bbsBoardMgr.getLocalThemeArticleList(currentBoard, -1);
                sortArticleList();
            }
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            waitingDialog.dismiss();
            if (StatusCode.isSuccess(resultCode)) {
                refresh();
                // 帖子列表回到顶部
                if (!articleList.isStackFromBottom()) {
                    articleList.setStackFromBottom(true);
                }
                articleList.setStackFromBottom(false);
                Toast.makeText(getContext(), "版面加载成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "版面加载失败...", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }


    /**
     * 获取版面主题模式下更多的帖子的异步线程
     * 无对话框
     */
    private class MoreThemeArticleListTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsBoardMgr.getThemeArticleListFromWeb(currentBoard, 1);
            if (StatusCode.isSuccess(resultCode)) {
                currentArticleList = bbsBoardMgr.getLocalThemeArticleList(currentBoard, -1);
                sortArticleList();
            }
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer result) {
            articleListAdapter.notifyDataSetChanged();
            refreshListBottom(false);
            if (StatusCode.isSuccess(result)) {
                Toast.makeText(getContext(), "加载更多成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "加载更多失败...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            refreshListBottom(false);
        }
    }

    /**
     * 刷新当前版面的线程(刷新首页的帖子)
     */
    private class RefreshArticleListTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        private boolean hasDialog;

        private RefreshArticleListTask(boolean hasDialog) {
            this.hasDialog = hasDialog;
        }

        @Override
        protected void onPreExecute() {
            if (hasDialog) {
                waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
                waitingDialog.setAsyncTask(this);
                waitingDialog.show();
            }
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsBoardMgr.getThemeArticleListFromWeb(currentBoard, 0);
            if (StatusCode.isSuccess(resultCode)) {
                currentArticleList = bbsBoardMgr.getLocalThemeArticleList(currentBoard, -1);
                sortArticleList();
            }
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if (hasDialog) {
                waitingDialog.dismiss();
            }
            articleListAdapter.notifyDataSetChanged();// 刷新列表
            articleList.onRefreshComplete();
            if (StatusCode.isSuccess(resultCode)) {
                if (hasDialog) {
                    AnimationUtil.startListAnimation(articleList);
                }
            }
        }
        @Override
        protected void onCancelled() {
            if (hasDialog) {
                waitingDialog.dismiss();
            }

            articleListAdapter.notifyDataSetChanged();
            articleList.onRefreshComplete();
        }
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (state != null) {
            articleList.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = articleList.onSaveInstanceState();
    }
}
