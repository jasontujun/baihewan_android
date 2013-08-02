package com.morln.app.lbstask.ui.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.UserFriendSource;
import com.morln.app.lbstask.ui.controls.ChangeImageLayout;
import com.morln.app.lbstask.ui.controls.ChangeImageTouchListener;
import com.morln.app.lbstask.ui.controls.DragLayer;
import com.morln.app.lbstask.ui.controls.XListView;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.model.Friend;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.res.FuncBarPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.ui.person.TAddFriend;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.XDataChangeListener;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-8
 * Time: 下午8:42
 */
public class CFriendBar extends XBaseComponent {
    private BbsPersonMgr bbsPersonMgr;
    private GlobalStateSource globalStateSource;
    private UserFriendSource friendSource;

    private RelativeLayout frame;
    private RelativeLayout shadow;
    private RelativeLayout topFrame;
    private Button addFriendBtn;
    private TextView nothingTip;
    private TextView noLoginTip;
    private XListView friendListView;
    private FriendListAdapter friendListAdapter;

    public CFriendBar(XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_right_bar);
        bbsPersonMgr = BbsPersonMgr.getInstance();
        friendSource = (UserFriendSource) DataRepo.getInstance().getSource(SourceName.USER_FRIEND);
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        frame = (RelativeLayout) findViewById(R.id.frame);
        shadow = (RelativeLayout) findViewById(R.id.shadow);
        topFrame = (RelativeLayout) findViewById(R.id.top_frame);
        addFriendBtn = (Button) findViewById(R.id.top_btn);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);
        noLoginTip = (TextView) findViewById(R.id.no_login_tip);
        friendListView = (XListView) findViewById(R.id.friend_list);

        setViewBackground(shadow, FuncBarPic.SHADOW_RIGHT);
        setViewBackground(topFrame, FuncBarPic.TOP_FRAME);

        noLoginTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!globalStateSource.isLogin()) {
                    new DLogin(parentLayer(), true).show();
                }
            }
        });

        friendListView.setArrowImage(R.drawable.controls_listview_arrow_gray);
        friendListView.setHeadTextColor(getContent().getResources().getColor(R.color.dark_gray));
        friendListView.setRefreshable(true);
        friendListView.setOnRefreshListener(new XListView.OnRefreshListener() { // 下拉刷新
            @Override
            public void onRefresh() {
                // TIP 登陆权限检测
                if (!globalStateSource.isLogin()) {
                    friendListView.onRefreshComplete();
                    new DLogin(parentLayer(), true).show();
                    return;
                }
                // 刷新列表
                new RefreshFriendTask().execute(null);
            }
        });
        friendListView.setOnXListScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:// 开始滚动
                        friendListAdapter.notifyDataSetChanged();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 不滚动时
                        friendListAdapter.notifyDataSetChanged();
                        break;
                }
            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
        friendListAdapter = new FriendListAdapter();
        friendSource.registerDataChangeListener(friendListAdapter);
        friendListView.setAdapter(friendListAdapter);

        // 侧边栏注册横向拖动的监听
        LMain lMain = (LMain) parentLayer();
        lMain.registerDragHorizontallyListener(new DragLayer.DragHorizontallyListener() {
            @Override
            public void onDragHorizontally() {
                friendListAdapter.notifyDataSetChanged();
            }
        });

        addFriendBtn.setBackgroundResource(R.drawable.btn_friend_add);
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TIP 登陆权限检测
                if (!globalStateSource.isLogin()) {
                    new DLogin(parentLayer(), true).show();
                    return;
                }

                // 添加好友
                String[] labelList = new String[]{"好友名称", "好友备注"};
                DialogUtil.createInputDialog(parentLayer().getUIFrame(), labelList,
                        new DialogUtil.InputListener() {
                            @Override
                            public void onInputFinished(List<String> words) {
                                new TAddFriend(parentLayer(), words.get(0), words.get(1),
                                        new TAddFriend.AddFriendListener() {
                                            @Override
                                            public void onSucceeded() {
                                                friendListView.onRefreshComplete();
                                            }

                                            @Override
                                            public void onFailed() {
                                                friendListView.onRefreshComplete();
                                            }

                                            @Override
                                            public void onCancelled() {
                                            }
                                        }).execute(null);
                            }
                        }, null).show("添加好友");
            }
        });

        allRefresh();
    }

    /**
     * 好友侧边栏整体刷新
     */
    public void allRefresh() {
        friendListAdapter.refresh();
        friendListView.onRefreshComplete();
        if (globalStateSource.isLogin()) {
            noLoginTip.setVisibility(View.GONE);
            friendListView.setVisibility(View.VISIBLE);
        } else {
            noLoginTip.setVisibility(View.VISIBLE);
            nothingTip.setVisibility(View.GONE);
            friendListView.setVisibility(View.GONE);
        }
    }

    @Override
    public int back() {
        return XBackType.SELF_BACK;
    }


    /**
     * 好友列表
     */
    public class FriendListAdapter extends BaseAdapter
            implements XDataChangeListener<Friend> {
        
        private List<Friend> friendList;
        
        public FriendListAdapter() {
            String currentUsername = globalStateSource.getCurrentUserName();
            friendList = bbsPersonMgr.getFriendList(currentUsername);
        }

        public void refresh() {
            String currentUsername = globalStateSource.getCurrentUserName();
            friendList = bbsPersonMgr.getFriendList(currentUsername);
            if (friendList == null) {
                friendList = new ArrayList<Friend>();
            }
            if (friendList.size() == 0) {
                nothingTip.setVisibility(View.VISIBLE);
            } else {
                nothingTip.setVisibility(View.GONE);
            }
            notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
            return friendList.size();
        }

        @Override
        public Object getItem(int i) {
            return friendList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public ChangeImageLayout itemFrame;
            public ImageView icon;
            public TextView usernameView;
            public TextView customNameView;
            public Button mailBtn;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if (item == null) {
                return null;
            }
            final Friend friend = (Friend) item;
            if (friend.getUserInfo().getUsername() == null ||
                    friend.getUserInfo().getUsername().equals("")) {
                return null;
            }

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_friend_list_item, null);
                holder = new ViewHolder();
                holder.itemFrame = (ChangeImageLayout) convertView.findViewById(R.id.frame);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.usernameView = (TextView) convertView.findViewById(R.id.user_name);
                holder.customNameView = (TextView) convertView.findViewById(R.id.nick_name);
                holder.mailBtn = (Button) convertView.findViewById(R.id.mail_btn);
                holder.itemFrame.setLayoutImage(parentLayer().getUIFrame(),
                        FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final String friendName = friend.getUserInfo().getUsername();
            holder.usernameView.setText(friendName);
            holder.customNameView.setText(friend.getCustomName());
            holder.mailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Handler handler = getFrameHandler();
                        Message msg = handler.obtainMessage();
                        msg.what = MainMsg.WRITE_MAIL;
                        Bundle bundle = new Bundle();
                        bundle.putString("receiver", friendName);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                }
            });

            holder.itemFrame.resetBg();// TIP 还原背景
            holder.itemFrame.setOnPressListener(new ChangeImageTouchListener.OnPressListener() {// 监听
                @Override
                public void onFirstTouch(View view, MotionEvent e) {
                }
                @Override
                public void onMoving(View view, MotionEvent e) {
                }
                @Override
                public void onRelease(View view, MotionEvent e) {
                    Handler handler1 = getLayerHandler();
                    Message msg = handler1.obtainMessage();
                    msg.what = BbsMsg.BBS_PERSON_INFO;
                    Bundle bundle = new Bundle();
                    bundle.putString("id",friend.getUserInfo().getUsername());
                    msg.setData(bundle);
                    handler1.sendMessage(msg);
                }
            });
            return convertView;
        }

        @Override
        public void onChange() {
            handler.sendMessage(handler.obtainMessage(REFRESH));
        }

        @Override
        public void onAdd(Friend item) {
            handler.sendMessage(handler.obtainMessage(REFRESH));
        }

        @Override
        public void onAddAll(List<Friend> items) {
            handler.sendMessage(handler.obtainMessage(REFRESH));
        }

        @Override
        public void onDelete(Friend item) {
            handler.sendMessage(handler.obtainMessage(REFRESH));
        }

        @Override
        public void onDeleteAll(List<Friend> items) {
            // TODO 为了防止刷新好友时（先删再加），出现两次刷新操作
//            handler.sendMessage(handler.obtainMessage(REFRESH));
        }
    }

    private static final int REFRESH = 1;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH:
                    friendListAdapter.refresh();
                    break;
            }
        }
    };


    /**
     * 刷新好友的异步线程
     */
    private class RefreshFriendTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode1 = bbsPersonMgr.getFriendsFromWeb();
            if (StatusCode.isSuccess(resultCode1) &&
                    globalStateSource.getLoginStatus() == GlobalStateSource.LOGIN_STATUS_ALL_LOGIN){
                int resultCode2 = bbsPersonMgr.uploadFriendList(getContext());// 把好友数据上传到服务器
                return resultCode2;
            } else {
                return resultCode1;
            }
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            friendListView.onRefreshComplete();

            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "刷新好友成功！", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "刷新好友失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            friendListView.onRefreshComplete();
            Toast.makeText(getContext(), "刷新好友失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
