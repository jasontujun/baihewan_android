package com.morln.app.lbstask.ui.collection;

import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.CollectBoardSource;
import com.morln.app.lbstask.data.model.Board;
import com.morln.app.lbstask.data.model.CollectedBoard;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.board.CBoardSelector;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.data.cache.XDataChangeListener;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-9-9
 * Time: 下午12:52
 */
public class CRss extends XBaseComponent {

    private CollectBoardSource collectBoardSource;
    private BbsBoardMgr bbsBoardMgr;
    private List<String> deletedList;// 记录已经删除的条目

    private int mState;// 状态
    private static final int STATE_NORMAL = 0;
    private static final int STATE_DELETE = 1;
    private static final int STATE_ADD = 2;

    // 界面
    private LinearLayout operationBtnFrame;
    private Button syncBtn, mgrBtn, addBtn;
    private LinearLayout confirmBtnFrame;
    private Button okBtn, cancelBtn;
    private RelativeLayout contentFrame;
    private ListView contentList;
    private BoardListAdapter boardListAdapter;
    private TextView nothingTip;
    private CBoardSelector boardSelector;

    public CRss(XUILayer parent) {
        super(parent);
        bbsBoardMgr = BbsBoardMgr.getInstance();
        collectBoardSource = (CollectBoardSource) DefaultDataRepo
                .getInstance().getSource(SourceName.BBS_COLLECTION_BOARD);

        setContentView(R.layout.bbs_rss);
        operationBtnFrame = (LinearLayout) findViewById(R.id.operation_btn_frame);
        confirmBtnFrame = (LinearLayout) findViewById(R.id.confirm_btn_frame);
        syncBtn = (Button) findViewById(R.id.top_btn1);
        mgrBtn = (Button) findViewById(R.id.top_btn2);
        addBtn = (Button) findViewById(R.id.top_btn3);
        okBtn = (Button) findViewById(R.id.ok_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        contentFrame = (RelativeLayout) findViewById(R.id.content_frame);
        contentList = (ListView) findViewById(R.id.content_list);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);
        nothingTip.setText("还没有订阅哦\n同步下小百合上的订阅吧");
        boardSelector = new CBoardSelector(parent);

        // 按钮监听
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出对话框，选择三种方式进行同步订阅
                new DSelectSyncMethod(parentLayer(), new DialogUtil.SelectListener() {
                    @Override
                    public void onSelected(int index) {
                        new SyncRssTask(index).execute(null);
                    }
                }).show();
            }
        });
        mgrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 删除订阅
                if (boardListAdapter.getCount() == 0) {
                    Toast.makeText(getContext(), "没有可以删除的了", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(mgrBtn, getContext());
                } else {
                    if (deletedList == null) {
                        deletedList = new ArrayList<String>();
                    } else {
                        deletedList.clear();
                    }

                    mState = STATE_DELETE;
                    refresh();
                }
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 添加订阅
                mState = STATE_ADD;
                refresh();

                boardSelector.reset(bbsBoardMgr.getCollectedBoardIds());
                contentFrame.addView(boardSelector.getContent());
            }
        });


        // 显示列表
        boardListAdapter = new BoardListAdapter();
        contentList.setAdapter(boardListAdapter);
        collectBoardSource.registerDataChangeListener(boardListAdapter);

        // 初始化
        mState = STATE_NORMAL;
    }


    /**
     * 根据状态,刷新整个界面
     */
    public void refresh() {
        switch (mState) {
            case STATE_NORMAL: {
                // 顶栏
                operationBtnFrame.setVisibility(View.VISIBLE);
                confirmBtnFrame.setVisibility(View.GONE);
                // 列表
                contentList.setVisibility(View.VISIBLE);
                refreshList();
                break;
            }
            case STATE_DELETE: {
                // 顶栏
                operationBtnFrame.setVisibility(View.GONE);
                confirmBtnFrame.setVisibility(View.VISIBLE);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 修改到本地
                        bbsBoardMgr.deleteAllCollectedBoards(deletedList);
                        mState = STATE_NORMAL;
                        refresh();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mState = STATE_NORMAL;
                        refresh();
                    }
                });
                // 列表
                contentList.setVisibility(View.VISIBLE);
                refreshList();
                break;
            }
            case STATE_ADD: {
                // 顶栏
                operationBtnFrame.setVisibility(View.GONE);
                confirmBtnFrame.setVisibility(View.VISIBLE);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<String> result = boardSelector.getResult();
                        bbsBoardMgr.addAllCollectedBoards(result); // 修改到本地

                        contentFrame.removeView(boardSelector.getContent());
                        mState = STATE_NORMAL;
                        refresh();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contentFrame.removeView(boardSelector.getContent());
                        mState = STATE_NORMAL;
                        refresh();
                    }
                });
                // 列表
                contentList.setVisibility(View.GONE);
                nothingTip.setVisibility(View.GONE);
                break;
            }
        }
    }


    @Override
    public int back() {
        if (mState == STATE_ADD) {
            contentFrame.removeView(boardSelector.getContent());
            mState = STATE_NORMAL;
            refresh();
            return XBackType.CHILD_BACK;
        }
        if (mState == STATE_DELETE) {
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
        if (state != null) {
            if (mState == STATE_NORMAL) {
                contentList.onRestoreInstanceState(state);
            }
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        if (mState == STATE_NORMAL) {
            state = contentList.onSaveInstanceState();
        }
    }


    /**
     * 版块列表
     */
    private class BoardListAdapter extends BaseAdapter implements XDataChangeListener<CollectedBoard> {

        private List<Board> zoneBoardList = new ArrayList<Board>();

        public void setBoardList(List<Board> boardList1) {
            if (boardList1 == null) {
                zoneBoardList = new ArrayList<Board>();
            } else {
                zoneBoardList = new ArrayList<Board>(boardList1);
            }
            notifyDataSetChanged();
            // 无内容的提示
            if (getCount() == 0) {
                nothingTip.setVisibility(View.VISIBLE);
            } else {
                nothingTip.setVisibility(View.GONE);
            }
        }

        public List<Board> copy() {
            return new ArrayList<Board>(zoneBoardList);
        }

        @Override
        public int getCount() {
            return zoneBoardList.size();
        }

        @Override
        public Object getItem(int i) {
            return zoneBoardList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public TextView boardEnglishName;
            public TextView boardChineseName;
            public LinearLayout deleteFrame;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_rss_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.boardEnglishName = (TextView) convertView.findViewById(R.id.board_english_name);
                holder.boardChineseName = (TextView) convertView.findViewById(R.id.board_chinese_name);
                holder.deleteFrame = (LinearLayout) convertView.findViewById(R.id.delete_frame);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Board board = (Board) getItem(i);
            // 设置版块英文名
            holder.boardEnglishName.setText(board.getBoardId());
            // 设置版块中文名
            holder.boardChineseName.setText(board.getChinesName());
            // 删除按钮
            if (mState == STATE_DELETE) {
                holder.deleteFrame.setVisibility(View.VISIBLE);
                holder.deleteFrame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletedList.add(board.getBoardId());
                        zoneBoardList.remove(board);
                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.deleteFrame.setVisibility(View.GONE);
            }

            // 设置监听
            if (mState == STATE_NORMAL) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TIP 跳转到版面
                        Handler handler = getFrameHandler();
                        Message msg = handler.obtainMessage(MainMsg.SEE_BOARD);
                        Bundle bundle = new Bundle();
                        bundle.putString("board", board.getBoardId());
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                });
                holder.deleteFrame.setVisibility(View.GONE);
            } else {
                convertView.setOnClickListener(null);
                holder.deleteFrame.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public void onChange() {
        }

        @Override
        public void onAdd(CollectedBoard item) {
            refreshList();
        }

        @Override
        public void onAddAll(List<CollectedBoard> items) {
            refreshList();
        }

        @Override
        public void onDelete(CollectedBoard item) {
            refreshList();
        }

        @Override
        public void onDeleteAll(List<CollectedBoard> items) {
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
                    boardListAdapter.setBoardList(bbsBoardMgr.getCollectedBoards());
                    break;
            }
        }
    };


    private static final int SYNC_METHOD_WEB = 0;
    private static final int SYNC_METHOD_LOCAL = 1;
    private static final int SYNC_METHOD_UNION = 2;
    /**
     * 同步订阅
     */
    private class SyncRssTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        private int syncMethod;// 同步方式

        private SyncRssTask(int syncMethod) {
            this.syncMethod = syncMethod;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = -1;
            switch (syncMethod) {
                case SYNC_METHOD_WEB:
                    resultCode = bbsBoardMgr.downloadRssBoard();
                    break;
                case SYNC_METHOD_LOCAL:
                    resultCode = bbsBoardMgr.uploadRssBoard();
                    break;
                case SYNC_METHOD_UNION:
                    resultCode = bbsBoardMgr.mergeRssBoard();
                    break;
            }
            return resultCode;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            waitingDialog.dismiss();
            if (StatusCode.isSuccess(resultCode)) {
                // 帖子列表回到顶部
                if (!contentList.isStackFromBottom()) {
                    contentList.setStackFromBottom(true);
                }
                contentList.setStackFromBottom(false);

                // 刷新列表
                refreshList();
                Toast.makeText(getContext(), "同步成功！", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "同步失败...", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }
}
