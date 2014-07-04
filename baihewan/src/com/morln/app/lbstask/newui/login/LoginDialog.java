package com.morln.app.lbstask.newui.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.newui.UiMsg;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XDialog;
import org.w3c.dom.Text;

/**
 * <pre>
 * 登陆对话框
 * User: jasontujun
 * Date: 14-7-4
 * Time: 上午10:08
 * </pre>
 */
public class LoginDialog implements XDialog {
    private String username, password;

    private Dialog dialog;
    private TextView titleView;
    private CheckBox rememberPasswordBox, autoLoginBox;
    private Button okBtn;
    private Button cancelBtn;

    public LoginDialog(final Context context, boolean isDim) {
        GlobalStateSource globalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);

        dialog = new Dialog(context, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        if(!isDim) {// 背景不变黑
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.dimAmount = 0.0f;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.setContentView(R.layout.dialog_login2);
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
                Editable un = usernameInputView.getText();
                if (un == null || TextUtils.isEmpty(un.toString())) {
                    Toast.makeText(context, "请填写用户名……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(usernameInputView, context);
                    return;
                }
                Editable pw = passwordInputView.getText();
                if (pw == null || TextUtils.isEmpty(pw.toString())) {
                    Toast.makeText(context, "请填写密码……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(passwordInputView, context);
                    return;
                }
                username = un.toString();
                password = pw.toString();
                new LoginTask(context, username, password, new LoginTask.LoginListener() {
                    @Override
                    public void onSucceeded(String username, String password) {
                        LoginMgr.getInstance().setRememberPassword(rememberPasswordBox.isChecked());
                        LoginMgr.getInstance().setAutoLogin(autoLoginBox.isChecked());
                        // 登录成功，刷新界面
                        context.sendBroadcast(new Intent(UiMsg.ACTION_LOGIN_REFRESH));
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
        if (LoginMgr.getInstance().isRememberPassword()) {
            rememberPasswordBox.setChecked(true);
            passwordInputView.setText(globalStateSource.getLastUserPassword());
        }
        if (LoginMgr.getInstance().isAutoLogin()) {
            autoLoginBox.setChecked(true);
        } else {
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
