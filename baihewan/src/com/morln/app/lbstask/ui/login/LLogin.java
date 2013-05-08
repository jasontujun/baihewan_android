package com.morln.app.lbstask.ui.login;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.res.SystemPic;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.system.ui.XBackType;
import com.morln.app.system.ui.XBaseLayer;
import com.morln.app.system.ui.XUIFrame;
import com.morln.app.utils.XStringUtil;

/**
 * Created by jasontujun.
 * Date: 12-2-14
 * Time: 下午10:39
 */
public class LLogin extends XBaseLayer {

    private LoginMgr loginMgr;
    private GlobalStateSource globalStateSource;
    private String username;
    private String password;

    private RelativeLayout frame;
    private ImageView title;
    private RelativeLayout loginFrame;
    private RelativeLayout dialogBg;
    private EditText usernameInputView;
    private EditText passwordInputView;
    private CheckBox rememberPasswordBox, autoLoginBox;
    private Button loginBtn;
    private Button visitorBtn;

    /**
     * 构造函数，记得调用setContentView()哦
     *
     * @param uiFrame
     */
    public LLogin(XUIFrame uiFrame) {
        super(uiFrame);

        loginMgr = LoginMgr.getInstance();
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.entrance_login);
        frame = (RelativeLayout) findViewById(R.id.bg);
        title = (ImageView) findViewById(R.id.title);
        FrameLayout content = (FrameLayout) findViewById(R.id.content_layout);

        setImageViewPic(title, SystemPic.TITLE);

        initLoginLayer(content);
    }

    /**
     * 初始化登陆栏
     * @param contentLayer
     */
    private void initLoginLayer(final FrameLayout contentLayer) {
        loginFrame = (RelativeLayout) View.inflate(getContext(), R.layout.dialog_login, null);
        dialogBg = (RelativeLayout) loginFrame.findViewById(R.id.dialog_frame);
        usernameInputView = (EditText) loginFrame.findViewById(R.id.username_input);
        passwordInputView = (EditText) loginFrame.findViewById(R.id.password_input);
        rememberPasswordBox = (CheckBox) loginFrame.findViewById(R.id.remember_pw_box);
        autoLoginBox = (CheckBox) loginFrame.findViewById(R.id.auto_login_box);
        loginBtn = (Button) loginFrame.findViewById(R.id.left_btn);
        visitorBtn = (Button) loginFrame.findViewById(R.id.right_btn);
        contentLayer.addView(loginFrame);

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

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = usernameInputView.getText().toString();
                if (XStringUtil.isNullOrEmpty(username)) {
                    Toast.makeText(getContext(), "请填写用户名……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(usernameInputView, getContext());
                    return;
                }
                password = passwordInputView.getText().toString();
                if (XStringUtil.isNullOrEmpty(password)) {
                    Toast.makeText(getContext(), "请填写密码……", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(passwordInputView, getContext());
                    return;
                }
                new TLogin(getUIFrame(), username, password, new TLogin.LoginListener() {
                    @Override
                    public void onSucceeded(String username, String password) {
                        loginMgr.setRememberPassword(rememberPasswordBox.isChecked());
                        loginMgr.setAutoLogin(autoLoginBox.isChecked());
                        // 跳转到主界面
                        Handler handler = getFrameHandler();
                        Message msg = handler.obtainMessage(MainMsg.GO_TO_MAIN);
                        handler.sendMessage(msg);
                        // 刷新界面
                        Message msg2 = handler.obtainMessage(MainMsg.LOGIN_REFRESH);
                        handler.sendMessage(msg2);
                    }
                    @Override
                    public void onFailed(String username, String password) {
                        setVisibility(true);
                    }
                    @Override
                    public void onCanceled(String username, String password) {
                        setVisibility(true);
                    }
                }, true).execute(null);
                setVisibility(false);
            }
        });
        visitorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalStateSource.setCurrentUser("", "");
                Handler handler = getFrameHandler();
                Message msg = handler.obtainMessage();
                msg.what = MainMsg.GO_TO_MAIN;
                handler.sendMessage(msg);
            }
        });

        setVisibility(true);
    }

    public void setVisibility(boolean visibility) {
        if(visibility) {
            loginFrame.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            // 再初始化一下账号密码
            usernameInputView.setText(globalStateSource.getLastUserName());
            if(loginMgr.isRememberPassword()) {
                rememberPasswordBox.setChecked(true);
                passwordInputView.setText(globalStateSource.getLastUserPassword());
            }else {
                rememberPasswordBox.setChecked(false);
            }
            if(loginMgr.isAutoLogin()) {
                autoLoginBox.setChecked(true);
            }else {
                autoLoginBox.setChecked(false);
            }
        }else {
            loginFrame.setVisibility(View.INVISIBLE);
            title.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }

    @Override
    public Handler getLayerHandler() {
        return null;
    }
}
