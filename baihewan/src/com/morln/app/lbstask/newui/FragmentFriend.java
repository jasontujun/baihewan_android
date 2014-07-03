package com.morln.app.lbstask.newui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.cache.UserFriendSource;
import com.morln.app.lbstask.data.model.Friend;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.session.StatusCode;
import com.morln.app.lbstask.ui.controls.XListView;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.data.cache.XDataChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 上午11:04
 * To change this template use File | Settings | File Templates.
 */
public class FragmentFriend extends Fragment {
    private GlobalStateSource globalStateSource;
    private UserFriendSource friendSource;

    private Button addFriendBtn;
    private TextView nothingTip;
    private TextView noLoginTip;
    private XListView friendListView;
    private FriendListAdapter friendListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        friendSource = (UserFriendSource) DefaultDataRepo
                .getInstance().getSource(SourceName.USER_FRIEND);
        globalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.main_right_bar, container, false);
        addFriendBtn = (Button) rootView.findViewById(R.id.top_btn);
        nothingTip = (TextView) rootView.findViewById(R.id.nothing_tip);
        noLoginTip = (TextView) rootView.findViewById(R.id.no_login_tip);
        friendListView = (XListView) rootView.findViewById(R.id.friend_list);

        noLoginTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!globalStateSource.isLogin()) {
//                    new DLogin(parentLayer(), true).show(); TODO
                }
            }
        });

        friendListView.setArrowImage(R.drawable.controls_listview_arrow_gray);
        friendListView.setHeadTextColor(getActivity().getResources().getColor(R.color.dark_gray));
        friendListView.setRefreshable(true);
        friendListView.setOnRefreshListener(new XListView.OnRefreshListener() { // 下拉刷新
            @Override
            public void onRefresh() {
//                // TIP 登陆权限检测 TODO
//                if (!globalStateSource.isLogin()) {
//                    friendListView.onRefreshComplete();
//                    new DLogin(parentLayer(), true).show();
//                    return;
//                }
//                // 刷新列表
//                new RefreshFriendTask().execute(null);
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

        addFriendBtn.setBackgroundResource(R.drawable.btn_friend_add);
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                // TIP 登陆权限检测  TODO
//                if (!globalStateSource.isLogin()) {
//                    new DLogin(parentLayer(), true).show();
//                    return;
//                }
//
//                // 添加好友
//                String[] labelList = new String[]{"好友名称", "好友备注"};
//                DialogUtil.createInputDialog(parentLayer().getUIFrame(), labelList,
//                        new DialogUtil.InputListener() {
//                            @Override
//                            public void onInputFinished(List<String> words) {
//                                new TAddFriend(parentLayer(), words.get(0), words.get(1),
//                                        new TAddFriend.AddFriendListener() {
//                                            @Override
//                                            public void onSucceeded() {
//                                                friendListView.onRefreshComplete();
//                                            }
//
//                                            @Override
//                                            public void onFailed() {
//                                                friendListView.onRefreshComplete();
//                                            }
//
//                                            @Override
//                                            public void onCancelled() {
//                                            }
//                                        }).execute(null);
//                            }
//                        }, null).show("添加好友");
            }
        });

        allRefresh();
        return rootView;
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


    /**
     * 好友列表
     */
    public class FriendListAdapter extends BaseAdapter
            implements XDataChangeListener<Friend> {

        private List<Friend> friendList;

        public FriendListAdapter() {
            String currentUsername = globalStateSource.getCurrentUserName();
            friendList = BbsPersonMgr.getInstance().getFriendList(currentUsername);
        }

        public void refresh() {
            String currentUsername = globalStateSource.getCurrentUserName();
            friendList = BbsPersonMgr.getInstance().getFriendList(currentUsername);
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
            public RelativeLayout itemFrame;
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
                convertView = View.inflate(getActivity(), R.layout.main_right_bar_item, null);
                holder = new ViewHolder();
                holder.itemFrame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.usernameView = (TextView) convertView.findViewById(R.id.user_name);
                holder.customNameView = (TextView) convertView.findViewById(R.id.nick_name);
                holder.mailBtn = (Button) convertView.findViewById(R.id.mail_btn);
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
//                    Handler handler = getFrameHandler();
//                    Message msg = handler.obtainMessage();
//                    msg.what = MainMsg.WRITE_MAIL;
//                    Bundle bundle = new Bundle();
//                    bundle.putString("receiver", friendName);
//                    msg.setData(bundle);
//                    handler.sendMessage(msg);
                }
            });

            holder.itemFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO
//                    Handler handler1 = getLayerHandler();
//                    Message msg = handler1.obtainMessage();
//                    msg.what = BbsMsg.BBS_PERSON_INFO;
//                    Bundle bundle = new Bundle();
//                    bundle.putString("id",friend.getUserInfo().getUsername());
//                    msg.setData(bundle);
//                    handler1.sendMessage(msg);
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
            int resultCode1 = BbsPersonMgr.getInstance().getFriendsFromWeb();
            if (StatusCode.isSuccess(resultCode1) &&
                    globalStateSource.getLoginStatus() == GlobalStateSource.LOGIN_STATUS_ALL_LOGIN){
                int resultCode2 = BbsPersonMgr.getInstance().uploadFriendList(getActivity());// 把好友数据上传到服务器
                return resultCode2;
            } else {
                return resultCode1;
            }
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            friendListView.onRefreshComplete();

            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getActivity(), "刷新好友成功！", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
//                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦"); // TODO
                        Toast.makeText(getActivity(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "刷新好友失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        protected void onCancelled() {
            friendListView.onRefreshComplete();
            Toast.makeText(getActivity(), "刷新好友失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
