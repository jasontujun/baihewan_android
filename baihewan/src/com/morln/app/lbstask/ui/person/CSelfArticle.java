package com.morln.app.lbstask.ui.person;

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
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 下午12:59
 */
public class CSelfArticle extends XBaseComponent implements Linear<ArticleBase> {
    private BbsBoardMgr bbsBoardMgr;
    private BbsPersonMgr bbsPersonMgr;

    private String currentUserName;
    private String currentArticleId;
    private String userId;

    // 界面
    private RelativeLayout topBarFrame;
    private XListView contentList;
    private ArticleListAdapter articleListAdapter;
    private TextView nothingTip;
    private Button topBtn;// 回到顶部按钮

    public CSelfArticle(XUILayer parent) {
        super(parent);
        bbsPersonMgr = BbsPersonMgr.getInstance();
        bbsBoardMgr = BbsBoardMgr.getInstance();
        GlobalStateSource globalStateSource = (GlobalStateSource)
                DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        currentUserName = globalStateSource.getCurrentUserName();

        setContentView(R.layout.bbs_self_articles);
        topBarFrame = (RelativeLayout) findViewById(R.id.top_frame);
        contentList = (XListView) findViewById(R.id.content_list);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);
        topBtn = (Button) findViewById(R.id.top_btn);

        // 监听
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!contentList.isStackFromBottom()) {
                    contentList.setStackFromBottom(true);
                }
                contentList.setStackFromBottom(false);

//                content.smoothScrollToPosition(0);// TIP 要求2.2以上
            }
        });

        // 列表适配器
        articleListAdapter = new ArticleListAdapter();
        contentList.setAdapter(articleListAdapter);
        contentList.setRefreshable(true);
        contentList.setOnRefreshListener(new XListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetSelfArticleTask(false).execute(null);
            }
        });
        contentList.setOnXListScrollListener(new AbsListView.OnScrollListener() {
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
    }

    /**
     * 搜索并显示某人的帖子
     * @param userId
     */
    public void show(String userId) {
        this.userId = userId;

        List<ArticleBase> articleList = bbsPersonMgr.getPersonArticlesFromLocal(userId);
        if(articleList.size() == 0) {
            new GetSelfArticleTask(true).execute(null);
        }else {
            articleListAdapter.setArticleList(articleList);
        }
    }

    @Override
    public ArticleBase getPre() {
        // 判断当前是哪一组的那篇帖子
        List<ArticleBase> articleList = articleListAdapter.getArticleList();
        int articleIndex = -1;
        for(int i = 0; i<articleList.size(); i++) {
            if(articleList.get(i).getId().equals(currentArticleId)) {
                articleIndex = i;
                break;
            }
        }
        // 返回上一篇帖子
        if(articleIndex > 0) {
            currentArticleId = articleList.get(articleIndex - 1).getId();
            return articleList.get(articleIndex);
        }
        return null;
    }

    @Override
    public ArticleBase getNext() {
        // 判断当前是哪一组的那篇帖子
        List<ArticleBase> articleList = articleListAdapter.getArticleList();
        int articleIndex = -1;
        for(int i =0; i<articleList.size(); i++) {
            if(articleList.get(i).getId().equals(currentArticleId)) {
                articleIndex = i;
                break;
            }
        }
        // 返回下一篇帖子
        if(articleIndex != -1 && articleIndex < articleList.size()-1) {
            currentArticleId = articleList.get(articleIndex + 1).getId();
            return articleList.get(articleIndex);
        }
        return null;
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if(state != null) {
            contentList.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = contentList.onSaveInstanceState();
    }


    /**
     * 帖子列表
     */
    public class ArticleListAdapter extends BaseAdapter {

        private List<ArticleBase> articleList = new ArrayList<ArticleBase>();

        public List<ArticleBase> getArticleList() {
            return articleList;
        }

        public void setArticleList(List<ArticleBase> articleList) {
            if(articleList == null) {
                this.articleList = new ArrayList<ArticleBase>();
            }else {
                this.articleList = articleList;
            }
            notifyDataSetChanged();

            if (getCount() != 0) {
                nothingTip.setVisibility(View.INVISIBLE);
            }else {
                nothingTip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getCount() {
            int count = articleList.size();
            return count;
        }

        /**
         * 展示搜索帖子的顺序：新->旧
         * @param i
         * @return
         */
        @Override
        public Object getItem(int i) {
            int index = articleList.size() - 1 - i;// 逆序
            if(0 <= index && index <articleList.size()) {
                return articleList.get(index);
            }else {
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decoration;
            public TextView author;
            public LinearLayout popFrame;
            public LinearLayout deleteFrame;
            public TextView title;
            public TextView time;
            public LinearLayout boardTipFrame;
            public TextView boardIdTip;
            public TextView boardChineseTip;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if(item == null) {
                return null;
            }

            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_article_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decoration = (ImageView) convertView.findViewById(R.id.decoration);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.popFrame = (LinearLayout) convertView.findViewById(R.id.pop_frame);
                holder.deleteFrame = (LinearLayout) convertView.findViewById(R.id.delete_frame);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.boardTipFrame = (LinearLayout) convertView.findViewById(R.id.board_tip_frame);
                holder.boardIdTip = (TextView) convertView.findViewById(R.id.board_id_tip);
                holder.boardChineseTip = (TextView) convertView.findViewById(R.id.board_chinese_tip);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ArticleBase article = (ArticleBase) getItem(i);
            // 背景
            holder.frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentArticleId = article.getId();
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
            holder.decoration.setImageResource(R.color.light_green);
            // 作者
            holder.author.setText(article.getAuthorName());
            // 人气栏
            holder.popFrame.setVisibility(View.GONE);
            // 删除栏(自己帖子才可以删除)
            if(userId.equals(currentUserName)) {
                holder.deleteFrame.setVisibility(View.VISIBLE);
                holder.deleteFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogUtil.createConfirmDialog(parentLayer().getUIFrame(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        new DeleteArticleTask(article.getBoard(), article.getId()).execute(null);
                                    }
                                }, null).show("确定删除帖子？", null);
                    }
                });
            }else {
                holder.deleteFrame.setVisibility(View.GONE);
                holder.deleteFrame.setOnClickListener(null);
            }
            // 标题
            holder.title.setText(article.getTitle());
            // 时间
            holder.time.setText(article.getDate());
            // 所属版面(显示)
            holder.boardTipFrame.setVisibility(View.VISIBLE);
            holder.boardIdTip.setText(article.getBoard());
            Board board = bbsBoardMgr.getBoard(article.getBoard());
            if(board != null) {
                holder.boardChineseTip.setText(board.getChinesName());
            }else {
                holder.boardChineseTip.setText("");
            }

            return convertView;
        }
    }


    /**
     * 搜索文章的异步线程
     */
    private class GetSelfArticleTask extends AsyncTask<Void, Void, Void> {

        private boolean hasDialog;

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        private List<ArticleBase> resultList = new ArrayList<ArticleBase>();

        private GetSelfArticleTask(boolean hasDialog) {
            this.hasDialog = hasDialog;
        }

        @Override
        protected void onPreExecute() {
            if(hasDialog) {
                waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
                waitingDialog.setAsyncTask(this);
                waitingDialog.show();
            }
        }
        @Override
        protected Void doInBackground(Void... para) {
            resultCode = bbsPersonMgr.getPersonArticlesFromWeb(userId, resultList);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if(hasDialog) {
                waitingDialog.dismiss();
            }
            contentList.onRefreshComplete();

            if(StatusCode.isSuccess(resultCode)) {
                articleListAdapter.setArticleList(resultList);
            }else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getContext(), "网络连接异常...", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "没有找到哦...", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        protected void onCancelled() {
            if(hasDialog) {
                waitingDialog.dismiss();
            }
            contentList.onRefreshComplete();
        }
    }


    private class DeleteArticleTask extends AsyncTask<Void, Void, Void> {

        private DialogUtil.WaitingDialog waitingDialog;
        private String board;
        private String articleId;
        private int resultCode;

        public DeleteArticleTask(String board, String articleId) {
            this.board = board;
            this.articleId = articleId;
        }
        
        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            resultCode = bbsPersonMgr.deleteArticle(board, articleId);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            waitingDialog.dismiss();
            contentList.onRefreshComplete();

            if(StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "删帖成功！", Toast.LENGTH_SHORT).show();
                // 刷新界面
                List<ArticleBase> curList = bbsPersonMgr.getPersonArticlesFromLocal(userId);
                articleListAdapter.setArticleList(curList);
            }else {
                switch(resultCode) {
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "删帖失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            contentList.onRefreshComplete();
        }
    }
}
