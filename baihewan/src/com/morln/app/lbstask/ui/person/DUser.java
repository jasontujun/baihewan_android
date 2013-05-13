package com.morln.app.lbstask.ui.person;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.BbsUserBase;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.ViewUtil;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUILayer;

import java.util.List;

/**
 * 用户资料对话框
 * Created by jasontujun.
 * Date: 12-4-20
 * Time: 上午10:59
 */
public class DUser implements XDialog {
    private GlobalStateSource globalStateSource;
    private BbsPersonMgr bbsPersonMgr;

    private BbsUserBase userInfo;
    
    private Dialog dialog;
    private XUILayer uiLayer;
    private TextView userIdView;
    private ImageView genderView;
    private TextView nickNameView;
    private TextView starView;
    private TextView expView;
    private TextView levelView;
    private LinearLayout addFriendFrame;
    private Button mailBtn, addFriendBtn, backBtn;

    public DUser(final XUILayer ul, final String userId) {
        this.uiLayer = ul;
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);
        bbsPersonMgr = BbsPersonMgr.getInstance();
        userInfo = bbsPersonMgr.getBbsUserInfoFromLocal(userId);

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_user);

        if(userInfo == null) {
            return;
        }

        userIdView = (TextView) dialog.findViewById(R.id.user_id);
        genderView = (ImageView) dialog.findViewById(R.id.gender);
        nickNameView = (TextView) dialog.findViewById(R.id.nick_name);
        starView = (TextView) dialog.findViewById(R.id.star);
        expView = (TextView) dialog.findViewById(R.id.exp);
        levelView = (TextView) dialog.findViewById(R.id.level);
        mailBtn = (Button) dialog.findViewById(R.id.mail_btn);
        addFriendFrame = (LinearLayout) dialog.findViewById(R.id.add_friend_frame);
        addFriendBtn = (Button) dialog.findViewById(R.id.add_friend_btn);
        backBtn = (Button) dialog.findViewById(R.id.back_btn);

        ViewUtil.initGender(genderView, userInfo.getGender());
        userIdView.setText(userInfo.getUsername());
        nickNameView.setText(userInfo.getNickname());
        nickNameView.setMovementMethod(ScrollingMovementMethod.getInstance());// 设置默认滚动方法
        starView.setText(userInfo.getStar());
        expView.setText(userInfo.getBbsExp());
        levelView.setText(userInfo.getLevel());
        if(bbsPersonMgr.isFriend(userId)) {
            // 如果是好友，隐藏添加好友按钮
            addFriendFrame.setVisibility(View.GONE);
        }else {
            addFriendFrame.setVisibility(View.VISIBLE);
        }

        // 监听
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 登陆权限检测
                if(!globalStateSource.isLogin()) {
                    AnimationUtil.startShakeAnimation(addFriendBtn, uiLayer.getContext());
                    Toast.makeText(uiLayer.getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    return;
                }

                dismiss();


                String[] labelList = new String[] {"好友备注"};
                DialogUtil.createInputDialog(uiLayer.getUIFrame(), labelList,
                        new DialogUtil.InputListener() {
                            @Override
                            public void onInputFinished(List<String> words) {
                                new TAddFriend(uiLayer, userId, words.get(0), null).execute(null);
                            }
                        }, null).show("添加好友" + userId);
            }
        });
        mailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 登陆权限检测
                if(!globalStateSource.isLogin()) {
                    AnimationUtil.startShakeAnimation(mailBtn, uiLayer.getContext());
                    Toast.makeText(uiLayer.getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    return;
                }

                dismiss();

                Handler handler = uiLayer.getFrameHandler();
                Message msg = handler.obtainMessage();
                msg.what = MainMsg.WRITE_MAIL;
                Bundle bundle = new Bundle();
                bundle.putString("receiver", userId);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }


    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }
}
