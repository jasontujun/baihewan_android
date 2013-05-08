package com.morln.app.lbstask.ui.login;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.system.ui.XBaseFrame;
import com.morln.app.system.ui.XDialog;
import com.morln.app.system.ui.XUILayer;

/**
 * 登陆对话框
 * Created by jasontujun.
 * Date: 12-2-15
 * Time: 上午10:47
 */
public class DLogin implements XDialog {
    private LoginMgr loginMgr;
    private GlobalStateSource globalStateSource;
    private String username, password;

    private Dialog dialog;
    private XUILayer uiLayer;
    private RelativeLayout dialogBg;
    private TextView titleView;
    private CheckBox rememberPasswordBox, autoLoginBox;
    private Button okBtn;
    private Button cancelBtn;

    public DLogin(final XUILayer uiLayer, boolean isDim) {
        this.uiLayer = uiLayer;
        loginMgr = LoginMgr.getInstance();
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        if(!isDim) {// 背景不变黑
            WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
            lp.dimAmount=0.0f;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.setContentView(R.layout.dialog_login2);
        dialogBg = (RelativeLayout) dialog.findViewById(R.id.dialog_frame);
        final EditText usernameInputView = (EditText) dialog.findViewById(R.id.username_input);
        final EditText passwordInputView = (EditText) dialog.findViewById(R.id.password_input);
        rememberPasswordBox = (CheckBox) dialog.findViewById(R.id.remember_pw_box);
        autoLoginBox = (CheckBox) dialog.findViewById(R.id.auto_login_box);
        okBtn = (Button) dialog.findViewById(R.id.left_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.right_btn);
        titleView = (TextView) dialog.findViewById(R.id.title);


        usernameInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                passwordInputView.setText("");
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameInputView.getText().toString();
                if (username == null || username.equals("")) {
                    Toast.makeText(uiLayer.getContext(), "请填写用户名……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(usernameInputView, uiLayer.getContext());
                    return;
                }
                password = passwordInputView.getText().toString();
                if (password == null || password.equals("")) {
                    Toast.makeText(uiLayer.getContext(), "请填写密码……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(passwordInputView, uiLayer.getContext());
                    return;
                }
                new TLogin(uiLayer.getUIFrame(), username, password, new TLogin.LoginListener() {
                    @Override
                    public void onSucceeded(String username, String password) {
                        loginMgr.setRememberPassword(rememberPasswordBox.isChecked());
                        loginMgr.setAutoLogin(autoLoginBox.isChecked());
                        // 登录成功，刷新界面
                        Handler handler = uiLayer.getFrameHandler();// TIP 注意是Frame的handler
                        Message msg = handler.obtainMessage();
                        msg.what = MainMsg.LOGIN_REFRESH;
                        handler.sendMessage(msg);
                    }
                    @Override
                    public void onFailed(String username, String password) {
                    }
                    @Override
                    public void onCanceled(String username, String password) {
                    }
                }, true).execute(null);

                dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // 初始化参数变量
        usernameInputView.setText(globalStateSource.getLastUserName());
        if(loginMgr.isRememberPassword()) {
            rememberPasswordBox.setChecked(true);
            passwordInputView.setText(globalStateSource.getLastUserPassword());
        }
        if(loginMgr.isAutoLogin()) {
            autoLoginBox.setChecked(true);
        }else {
            autoLoginBox.setChecked(false);
        }
    }

    public void show(String titleStr) {
        titleView.setText(titleStr);
        dialog.show();
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
