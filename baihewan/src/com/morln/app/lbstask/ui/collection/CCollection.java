package com.morln.app.lbstask.ui.collection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.data.cache.XDataChangeListener;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.cache.CollectArticleSource;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.bbs.model.CollectedArticleBase;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.system.ui.XBackType;
import com.morln.app.system.ui.XBaseComponent;
import com.morln.app.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-6
 * Time: 下午6:47
 */
public class CCollection extends XBaseComponent implements Linear<ArticleBase> {
    private CollectArticleSource collectArticleSource;
    private BbsBoardMgr bbsBoardMgr;
    private BbsPersonMgr bbsPersonMgr;
    private String  currentArticleId;

    private int mState;// 状态
    private static final int STATE_NORMAL = 0;
    private static final int STATE_DELETE = 1;

    // 界面
    private LinearLayout operationBtnFrame;
    private Button syncBtn, mgrBtn;
    private LinearLayout confirmBtnFrame;
    private Button okBtn, cancelBtn;
    private ListView contentList;
    private ArticleListAdapter articleListAdapter;
    private TextView nothingTip;

    private List<String> deletedList;// 删除的列表

    public CCollection(XUILayer parent) {
        super(parent);
        collectArticleSource = (CollectArticleSource) DataRepo.getInstance().getSource(SourceName.BBS_COLLECTION_ARTICLE);
        bbsBoardMgr = BbsBoardMgr.getInstance();
        bbsPersonMgr = BbsPersonMgr.getInstance();

        setContentView(R.layout.bbs_collection);
        operationBtnFrame = (LinearLayout) findViewById(R.id.operation_btn_frame);
        confirmBtnFrame = (LinearLayout) findViewById(R.id.confirm_btn_frame);
        syncBtn = (Button) findViewById(R.id.top_btn1);
        mgrBtn = (Button) findViewById(R.id.top_btn2);
        okBtn = (Button) findViewById(R.id.ok_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        contentList = (ListView) findViewById(R.id.content_list);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);
        nothingTip.setText("还没有收藏呢\n尝试下同步吧");

        // 按钮监听
        mgrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(articleListAdapter.getCount() == 0) {
                    Toast.makeText(getContext(), "没有可以删除的了", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(mgrBtn, getContext());
                }else {
                    if(deletedList == null) {
                        deletedList = new ArrayList<String>();
                    }else {
                        deletedList.clear();
                    }

                    mState = STATE_DELETE;
                    refresh();
                }
            }
        });
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 同步
                new TSyncCollection(parentLayer().getUIFrame(), true, new TSyncCollection.SyncCollectionListener() {
                    @Override
                    public void onSucceeded() {
                        refresh();
                    }
                    @Override
                    public void onFailed() {
                    }
                    @Override
                    public void onCanceled() {
                    }
                }).execute(null);
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bbsPersonMgr.deleteAllCollectedArticle(deletedList);// 修改到本地
                mState = STATE_NORMAL;
                refresh();
                // 后台同步
                if(deletedList != null && deletedList.size() > 0) {
                    new TSyncCollection(parentLayer().getUIFrame(), false, null).execute(null);
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mState = STATE_NORMAL;
                refresh();
            }
        });

        articleListAdapter = new ArticleListAdapter();
        contentList.setAdapter(articleListAdapter);
        collectArticleSource.registerDataChangeListener(articleListAdapter);
        refreshList();
    }

    /**
     * 根据状态,刷新整个界面
     */
    public void refresh() {
        switch (mState) {
            case STATE_NORMAL: {
                operationBtnFrame.setVisibility(View.VISIBLE);
                confirmBtnFrame.setVisibility(View.GONE);
                break;
            }
            case STATE_DELETE: {
                operationBtnFrame.setVisibility(View.GONE);
                confirmBtnFrame.setVisibility(View.VISIBLE);
                break;
            }
        }
        refreshList();
    }

    @Override
    public ArticleBase getPre() {
        List<ArticleBase> collectedList = articleListAdapter.getArticleList();
        // 判断当前是哪一组的那篇帖子
        int articleIndex = -1;
        for(int i =0; i<collectedList.size(); i++) {
            if(collectedList.get(i).getId().equals(currentArticleId)) {
                articleIndex = i;
                break;
            }
        }
        // 返回上一篇帖子
        if(articleIndex > 0){
            ArticleBase article = collectedList.get(articleIndex - 1);
            currentArticleId = article.getId();
            return article;
        }
        return null;
    }

    @Override
    public ArticleBase getNext() {
        List<ArticleBase> collectedList = articleListAdapter.getArticleList();
        // 判断当前是哪一组的那篇帖子
        int articleIndex = -1;
        for(int i =0; i<collectedList.size(); i++) {
            if(collectedList.get(i).getId().equals(currentArticleId)) {
                articleIndex = i;
                break;
            }
        }
        // 返回下一篇帖子
        if(articleIndex != -1 && articleIndex < collectedList.size()-1) {
            ArticleBase article = collectedList.get(articleIndex + 1);
            currentArticleId = article.getId();
            return article;
        }
        return null;
    }

    @Override
    public int back() {
        if(mState != STATE_NORMAL) {
            mState = STATE_NORMAL;
            refresh();
            return XBackType.CHILD_BACK;
        }
        return XBackType.NOTHING_TO_BACK;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if(state != null) {
            if(mState == STATE_NORMAL) {
                contentList.onRestoreInstanceState(state);
            }
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        if(mState == STATE_NORMAL) {
            state = contentList.onSaveInstanceState();
        }
    }


    /**
     * 帖子列表
     */
    public class ArticleListAdapter extends BaseAdapter implements XDataChangeListener<CollectedArticleBase> {

        private List<ArticleBase> articleList = new ArrayList<ArticleBase>();

        public void setArticleList(List<ArticleBase> articleList1) {
            if(articleList1 == null) {
                articleList = new ArrayList<ArticleBase>();
            }else {
                articleList = new ArrayList<ArticleBase>(articleList1);
            }
            notifyDataSetChanged();
            // 无内容的提示
            if(getCount() == 0) {
                nothingTip.setVisibility(View.VISIBLE);
            }else {
                nothingTip.setVisibility(View.INVISIBLE);
            }
        }

        public List<ArticleBase> getArticleList() {
            return articleList;
        }

        public List<ArticleBase> copy() {
            return new ArrayList<ArticleBase>(articleList);
        }


        @Override
        public int getCount() {
            return articleList.size();
        }

        @Override
        public Object getItem(int i) {
            int index = articleList.size() -1 -i;// 逆序
            return articleList.get(index);
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
            holder.decoration.setImageResource(R.color.light_green);
            // 作者
            holder.author.setText(article.getAuthorName());
            // 人气栏
            holder.popFrame.setVisibility(View.GONE);
            // 删除栏
            if(mState == STATE_DELETE) {
                holder.deleteFrame.setVisibility(View.VISIBLE);
                holder.deleteFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletedList.add(article.getId());
                        articleList.remove(article);
                        notifyDataSetChanged();
                    }
                });
            }else {
                holder.deleteFrame.setVisibility(View.GONE);
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
            // 设置监听
            if(mState == STATE_NORMAL) {
                holder.frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentArticleId = article.getId();
                        Handler handler1 = getFrameHandler();
                        Message msg = handler1.obtainMessage(MainMsg.SEE_ARTICLE_DETAIL);
                        Bundle bundle = new Bundle();
                        bundle.putString("id", article.getId());
                        bundle.putString("board", article.getBoard());
                        msg.setData(bundle);
                        handler1.sendMessage(msg);
                    }
                });
            }else {
                holder.frame.setOnClickListener(null);
            }

            return convertView;
        }

        @Override
        public void onChange() {
            refreshList();
        }

        @Override
        public void onAdd(CollectedArticleBase item) {
            refreshList();
        }

        @Override
        public void onAddAll(List<CollectedArticleBase> items) {
            refreshList();
        }

        @Override
        public void onDelete(CollectedArticleBase item) {
            refreshList();
        }

        @Override
        public void onDeleteAll(List<CollectedArticleBase> items) {
            refreshList();
        }
    }


    /**
     * 异步重新设置数据，只刷新列表
     */
    private void refreshList() {
        handler.sendMessage(handler.obtainMessage(REFRESH_LIST));
    }
    private static final int REFRESH_LIST = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_LIST:
                    articleListAdapter.setArticleList(bbsPersonMgr.getCollectedArticleIdList());
                    break;
            }
        }
    };
}
