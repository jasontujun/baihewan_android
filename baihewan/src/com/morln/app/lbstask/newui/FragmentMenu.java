package com.morln.app.lbstask.newui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.session.StatusCode;
import com.morln.app.lbstask.utils.DialogUtil;
import com.xengine.android.data.cache.DefaultDataRepo;

/**
 * 菜单列表的Fragment
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 上午11:04
 * To change this template use File | Settings | File Templates.
 */
public class FragmentMenu extends Fragment {
    private GlobalStateSource mGlobalStateSource;

    private TextView mUsernameView, mUserNameTip;
    private Button mTopBtn;
    private ListView mMenuList;
    private AdapterLeftMenu mMenuAdapter;
    private AdapterView.OnItemClickListener mMenuItemListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGlobalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);

        View rootView = inflater.inflate(R.layout.main_left_bar, container, false);
        mUsernameView = (TextView) rootView.findViewById(R.id.user_name);
        mUserNameTip = (TextView) rootView.findViewById(R.id.user_name_tip);
        mTopBtn = (Button) rootView.findViewById(R.id.top_btn);
        mMenuList = (ListView) rootView.findViewById(R.id.menu_list);

        mTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
//                if (!mGlobalStateSource.isLogin()) {
//                    new DLogin(parentLayer(), true).show();
//                } else {
//                    DialogUtil.createConfirmDialog(parentLayer().getUIFrame(), new Runnable() {
//                        @Override
//                        public void run() {
//                            new LogoutTask().execute(null);
//                        }
//                    }, null).show("确定注销？", null);
//                }
            }
        });

        // TODO 监听新邮件
        BbsMailMgr.getInstance().registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
//                refreshMailTip(newMailNumber);
            }
        });

        // 初始化顶栏
        refreshTopFrame();
        // 初始化菜单列表，选择第一个菜单项
        mMenuAdapter = new AdapterLeftMenu(getActivity(), 0);
        mMenuList.setAdapter(mMenuAdapter);
        // 设置点击监听
        ActivityMain mainActivity = (ActivityMain) getActivity();
        if (mainActivity != null) {
            mMenuItemListener = mainActivity.getMenuItemClickListener();
            mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mMenuAdapter.setSelectedIndex(i);
                    mMenuItemListener.onItemClick(adapterView, view, i, l);
                }
            });
        }

        return rootView;
    }

    /**
     * 刷新邮件提醒
     * @param newMailNumber
     */
    private void refreshMailTip(int newMailNumber) {
        // TODO
    }

    /**
     * 刷新顶栏（用于游客登录后）
     */
    private void refreshTopFrame() {
        if (mGlobalStateSource.isLogin()) {
            mUsernameView.setText(mGlobalStateSource.getCurrentUserName());
            mUserNameTip.setText("的百荷湾");
            mTopBtn.setBackgroundResource(R.drawable.btn_logout);
        } else {
            mUsernameView.setText("游客");
            mUserNameTip.setText("欢迎来到百荷湾");
            mTopBtn.setBackgroundResource(R.drawable.btn_login_gray);
        }
    }

    /**
     * 注销
     */
    private class LogoutTask extends AsyncTask<Void, Void, Void> {

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        @Override
        protected void onPreExecute() {
            // TODO
//            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
//            waitingDialog.setAsyncTask(this);
//            waitingDialog.show("注销中，请稍后……");
        }
        @Override
        protected Void doInBackground(Void... para) {
            resultCode = LoginMgr.getInstance().logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (StatusCode.isSuccess(resultCode) || resultCode == StatusCode.SYSTEM_LOGIN_FAIL) {
                Toast.makeText(getActivity(), "注销成功", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getActivity(), "网络有问题...", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "注销失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if (resultCode != StatusCode.HTTP_EXCEPTION) {
                mGlobalStateSource.setLoginStatus(GlobalStateSource.LOGIN_STATUS_NO_LOGIN);
                mGlobalStateSource.setCurrentUser("", "");
                mGlobalStateSource.clearToken();
                refreshTopFrame();

                // TODO 调用外部监听者
//                Handler handler = getFrameHandler();
//                handler.sendMessage(handler.obtainMessage(MainMsg.INIT_MAIN));
//                handler.sendMessage(handler.obtainMessage(MainMsg.LOGOUT));
            }
            waitingDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            Toast.makeText(getActivity(), "注销失败", Toast.LENGTH_SHORT).show();
        }
    }

}
