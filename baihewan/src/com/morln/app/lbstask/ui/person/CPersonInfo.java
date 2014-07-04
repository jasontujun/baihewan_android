package com.morln.app.lbstask.ui.person;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.BbsUserBase;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.morln.app.lbstask.utils.ViewUtil;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 下午12:21
 */
public class CPersonInfo extends XBaseComponent {
    private BbsPersonMgr bbsPersonMgr;
    private GlobalStateSource globalStateSource;
    private String userId;// 查看对象的id
    private BbsUserBase userInfo;

    // 界面
    private View contentFrame;
    // 个人资料
    private TextView infoLabel;
    private ImageView genderView;
    private TextView userNameView, nickNameView, starView, loginNumView, sendNumView,
            lastDateView, ipView, mailBoxView, expView, levelView, showNumView,
            showWordView, hpView, roleView;
    // 个人签名
    private View signatureFrame;
    private TextView signatureView;
    private Button editSignatureBtn;
    // 访客操作栏
    private View visitorFrame;
    private Button mailBtn;
    private Button friendBtn;
    // 异常按钮
    private TextView nothingTip;

    public CPersonInfo(XUILayer parent) {
        super(parent);
        bbsPersonMgr = BbsPersonMgr.getInstance();
        globalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.bbs_person_info);
        contentFrame = findViewById(R.id.content_frame);
        infoLabel = (TextView) findViewById(R.id.info_label);
        genderView = (ImageView) findViewById(R.id.gender);
        userNameView = (TextView) findViewById(R.id.user_id);
        nickNameView = (TextView) findViewById(R.id.nick_name);
        starView = (TextView) findViewById(R.id.star);
        loginNumView = (TextView) findViewById(R.id.login_num);
        sendNumView = (TextView) findViewById(R.id.send_num);
        lastDateView = (TextView) findViewById(R.id.last_date);
        ipView = (TextView) findViewById(R.id.ip);
        mailBoxView = (TextView) findViewById(R.id.mail_box);
        expView = (TextView) findViewById(R.id.exp);
        levelView = (TextView) findViewById(R.id.level);
        showNumView = (TextView) findViewById(R.id.show_num);
        showWordView = (TextView) findViewById(R.id.show_word);
        hpView = (TextView) findViewById(R.id.hp);
        roleView = (TextView) findViewById(R.id.role);
        signatureFrame = findViewById(R.id.signature_frame);
        signatureView = (TextView) findViewById(R.id.signature_word);
        editSignatureBtn = (Button) findViewById(R.id.signature_btn);
        visitorFrame = findViewById(R.id.visitor_frame);
        mailBtn = (Button) findViewById(R.id.mail_btn);
        friendBtn = (Button) findViewById(R.id.friend_btn);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);

        // 按钮监听
        nothingTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TPersonInfo(parentLayer().getUIFrame(), userId, true,
                        new TPersonInfo.GetUserInfoListener() {
                            @Override
                            public void onGettingUserInfo(BbsUserBase user) {
                                userInfo = user;
                                refresh();
                            }
                            @Override
                            public void onCancelled() {
                                refresh();
                            }
                        }).execute(null);
            }
        });
        editSignatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DEditSignature(parentLayer().getUIFrame(), new Runnable() {
                    @Override
                    public void run() {
                        refreshSignatureFrame();
                    }
                }).show();
            }
        });
        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userId != null) {
                    Handler handler = getFrameHandler();
                    Message msg = handler.obtainMessage();
                    msg.what = MainMsg.WRITE_MAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("receiver", userId);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } else {
                    Toast.makeText(getContext(), "无法发站内信", Toast.LENGTH_SHORT).show();
                }
            }
        });
        friendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.createConfirmDialog(parentLayer().getUIFrame().getContext(),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new DeleteFriendTask(userId).execute();
                            }
                        }, null).show("确定删除好友" + userId + "?", null);
            }
        });

        refresh();
    }

    /**
     * 抓取并显示某个人的个人信息
     * @param userId
     */
    public void show(String userId) {
        if (userId == null) {
            return;
        }

        this.userId = userId;
        if (userId.equals(globalStateSource.getCurrentUserName())){
            infoLabel.setText("我的个人资料");
        } else {
            infoLabel.setText("Ta的个人资料");
        }

        // 先从本地缓存中获取
        userInfo = bbsPersonMgr.getBbsUserInfoFromLocal(userId);
        if (userInfo != null) {
            refresh();
        } else {
            // 如果没有，从网上抓取
            new TPersonInfo(parentLayer().getUIFrame(), userId, true,
                    new TPersonInfo.GetUserInfoListener() {
                        @Override
                        public void onGettingUserInfo(BbsUserBase user) {
                            userInfo = user;
                            refresh();
                        }
                        @Override
                        public void onCancelled() {
                            refresh();
                        }
                    }).execute(null);
        }
    }

    private void refresh() {
        if (userInfo != null) {
            nothingTip.setVisibility(View.GONE);
            contentFrame.setVisibility(View.VISIBLE);

            ViewUtil.initGender(genderView, userInfo.getGender());
            userNameView.setText(userInfo.getUsername());
            nickNameView.setText(userInfo.getNickname());
            starView.setText(userInfo.getStar());
            loginNumView.setText(userInfo.getLoginNum());
            sendNumView.setText(userInfo.getSendNum());
            lastDateView.setText(userInfo.getLastLoginTime());
            ipView.setText(userInfo.getIp());
            mailBoxView.setText(userInfo.getMailBox());
            expView.setText(userInfo.getBbsExp());
            levelView.setText(userInfo.getLevel());
            showNumView.setText(userInfo.getShowNum());
            showWordView.setText(userInfo.getShowWord());
            hpView.setText(userInfo.getHp());
            String role = userInfo.getRole();
            if (TextUtils.isEmpty(role)) {
                roleView.setText("一介布衣");
            } else {
                roleView.setText(role);
            }

            if (userId.equals(globalStateSource.getCurrentUserName())) {
                refreshSignatureFrame();// 显示自己的资料,出现手机签名栏
            } else {
                refreshVisitorFrame();// 显示别人的资料,出现访客按钮栏
            }
        }else {// 默认个人资料
            nothingTip.setVisibility(View.VISIBLE);
            contentFrame.setVisibility(View.GONE);
        }
    }

    private void refreshSignatureFrame() {
        signatureFrame.setVisibility(View.VISIBLE);
        visitorFrame.setVisibility(View.GONE);
        if (TextUtils.isEmpty(bbsPersonMgr.getMobileSignature())) {
            signatureView.setText("小爪机木有签名好可怜……");
            signatureView.setTextColor(getContext().getResources().getColor(R.color.dark_gray));
        } else {
            signatureView.setText(bbsPersonMgr.getMobileSignature());
            signatureView.setTextColor(getContext().getResources().getColor(R.color.black));
        }
    }

    private void refreshVisitorFrame() {
        signatureFrame.setVisibility(View.GONE);
        visitorFrame.setVisibility(View.VISIBLE);
        if (bbsPersonMgr.isFriend(userId)) {// 是好友，显示删除好友按钮
            friendBtn.setVisibility(View.VISIBLE);
        } else {// 不是好友
            friendBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }


    /**
     * 删除好友的异步线程
     */
    private class DeleteFriendTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        private String username;

        public DeleteFriendTask(String username) {
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsPersonMgr.deleteFriend(username);
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            waitingDialog.dismiss();
            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "成功删除了好友"+username+"……", Toast.LENGTH_SHORT).show();
                refresh();
            } else {
                switch (resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "删除好友失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            Toast.makeText(getContext(), "删除好友失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
