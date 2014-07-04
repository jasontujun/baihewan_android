package com.morln.app.lbstask.ui.main;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.ui.controls.ChangeImageLayout;
import com.morln.app.lbstask.ui.controls.ChangeImageTouchListener;
import com.morln.app.lbstask.ui.controls.DragLayer;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.res.FuncBarPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-2-22
 * Time: 下午8:15
 */
public class CFunctionBar extends XBaseComponent {
    private GlobalStateSource globalStateSource;
    private List<Item> items = new ArrayList<Item>();
    private int selectedItemIndex;

    private RelativeLayout frame;
    private RelativeLayout shadow;
    private RelativeLayout topFrame;
    private TextView usernameView, userNameTip;
    private Button topBtn;
    private ChangeImageLayout btn1Frame, btn2Frame, btn3Frame,
            btn4Frame, btn5Frame, btn6Frame, btn7Frame, btn8Frame;
    private ImageView icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8;
    private TextView otherIdLabel;
    private TextView newMailTip;// 未读邮件提醒

    public CFunctionBar(XUILayer parent) {
        super(parent);
        globalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.main_left_bar);
        frame = (RelativeLayout) findViewById(R.id.frame);
        shadow = (RelativeLayout) findViewById(R.id.shadow);
        topFrame = (RelativeLayout) findViewById(R.id.top_frame);
        usernameView = (TextView) findViewById(R.id.user_name);
        userNameTip = (TextView) findViewById(R.id.user_name_tip);
//        newMailTip = (TextView) findViewById(R.id.new_mail_tip);
        topBtn = (Button) findViewById(R.id.top_btn);
//        btn1Frame = (ChangeImageLayout) findViewById(R.id.btn1_frame);
//        btn2Frame = (ChangeImageLayout) findViewById(R.id.btn2_frame);
//        btn3Frame = (ChangeImageLayout) findViewById(R.id.btn3_frame);
//        btn4Frame = (ChangeImageLayout) findViewById(R.id.btn4_frame);
//        btn5Frame = (ChangeImageLayout) findViewById(R.id.btn5_frame);
//        btn6Frame = (ChangeImageLayout) findViewById(R.id.btn6_frame);
//        btn7Frame = (ChangeImageLayout) findViewById(R.id.btn7_frame);
//        btn8Frame = (ChangeImageLayout) findViewById(R.id.btn8_frame);
//        icon1 = (ImageView) findViewById(R.id.btn1);
//        icon2 = (ImageView) findViewById(R.id.btn2);
//        icon3 = (ImageView) findViewById(R.id.btn3);
//        icon4 = (ImageView) findViewById(R.id.btn4);
//        icon5 = (ImageView) findViewById(R.id.btn5);
//        icon6 = (ImageView) findViewById(R.id.btn6);
//        icon7 = (ImageView) findViewById(R.id.btn7);
//        icon8 = (ImageView) findViewById(R.id.btn8);
//        TextView btn1Label = (TextView) findViewById(R.id.btn1_label);
//        TextView btn2Label = (TextView) findViewById(R.id.btn2_label);
//        TextView btn3Label = (TextView) findViewById(R.id.btn3_label);
//        TextView btn4Label = (TextView) findViewById(R.id.btn4_label);
//        TextView btn5Label = (TextView) findViewById(R.id.btn5_label);
//        TextView btn6Label = (TextView) findViewById(R.id.btn6_label);
//        TextView btn7Label = (TextView) findViewById(R.id.btn7_label);
//        TextView btn8Label = (TextView) findViewById(R.id.btn8_label);
//        otherIdLabel = (TextView) findViewById(R.id.btn5_label2);
//        otherIdLabel.setText("");
//        ImageView tip1 = (ImageView) findViewById(R.id.btn_tip1);
//        ImageView tip2  = (ImageView) findViewById(R.id.btn_tip2);
//        ImageView tip3  = (ImageView) findViewById(R.id.btn_tip3);
//        ImageView tip4  = (ImageView) findViewById(R.id.btn_tip4);
//        ImageView tip5  = (ImageView) findViewById(R.id.btn_tip5);
//        ImageView tip6  = (ImageView) findViewById(R.id.btn_tip6);
//        ImageView tip7  = (ImageView) findViewById(R.id.btn_tip7);
//        ImageView tip8  = (ImageView) findViewById(R.id.btn_tip8);
//        items.add(new Item(btn1Frame, icon1, tip1, btn1Label));
//        items.add(new Item(btn2Frame, icon2, tip2, btn2Label));
//        items.add(new Item(btn3Frame, icon3, tip3, btn3Label));
//        items.add(new Item(btn4Frame, icon4, tip4, btn4Label));
//        items.add(new Item(btn5Frame, icon5, tip5, btn5Label));
//        items.add(new Item(btn6Frame, icon6, tip6, btn6Label));
//        items.add(new Item(btn7Frame, icon7, tip7, btn7Label));
//        items.add(new Item(btn8Frame, icon8, tip8, btn8Label));
        setViewBackground(shadow, FuncBarPic.SHADOW_LEFT);
        setViewBackground(topFrame, FuncBarPic.TOP_FRAME);
        btn1Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn2Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn3Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn4Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn5Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn6Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn7Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        btn8Frame.setLayoutImage(parent.getUIFrame(), FuncBarPic.FRAME_TRANSPARENT, FuncBarPic.FRAME_PRESSED);
        setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN);
        setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN);
        setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN);
        setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN);
        setImageViewPic(icon6, FuncBarPic.MAIL_BTN);
        setImageViewPic(icon7, FuncBarPic.SETTING_BTN);
        setImageViewPic(icon8, FuncBarPic.ABOUT_BTN);
//        setImageViewPic(tip1, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip2, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip3, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip4, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip5, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip6, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip7, FuncBarPic.BTN_TIP);
//        setImageViewPic(tip8, FuncBarPic.BTN_TIP);


        refreshTopFrame();// 初始化顶栏
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!globalStateSource.isLogin()) {
                    new DLogin(parentLayer(), true).show();
                } else {
                    DialogUtil.createConfirmDialog(parentLayer().getUIFrame().getContext(),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new LogoutTask().execute();
                                }
                            }, null).show("确定注销？", null);
                }
            }
        });
        // 按钮监听
        for (int i = 0; i < items.size(); i++) {
            final int cIndex = i;
            // 按下图片效果
            items.get(i).itemFrame.setOuterBeforeAfterListener(new ChangeImageTouchListener.OuterBeforeAfterListener() {
                @Override
                public void beforeFirstTouch(View view) {
                    Resources res = getContext().getResources();
                    items.get(cIndex).label.setTextColor(res.getColor(R.color.yellow_green));
                    switch (cIndex){
                        case 0:
                            setImageViewPic(icon1, FuncBarPic.HOT_BTN_PRESSED);
                            break;
                        case 1:
                            setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN_PRESSED);
                            break;
                        case 2:
                            setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN_PRESSED);
                            break;
                        case 3:
                            setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN_PRESSED);
                            break;
                        case 4:
                            setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN_PRESSED);
                            break;
                        case 5:
                            setImageViewPic(icon6, FuncBarPic.MAIL_BTN_PRESSED);
                            break;
                        case 6:
                            setImageViewPic(icon7, FuncBarPic.SETTING_BTN_PRESSED);
                            break;
                        case 7:
                            setImageViewPic(icon8, FuncBarPic.ABOUT_BTN_PRESSED);
                            break;
                    }
                }
                @Override
                public void afterRelease(View view) {
                    if (cIndex == selectedItemIndex){
                        Resources res = getContext().getResources();
                        items.get(cIndex).label.setTextColor(res.getColor(R.color.yellow_green));
                        switch (cIndex){
                            case 0:
                                setImageViewPic(icon1, FuncBarPic.HOT_BTN_PRESSED);
                                break;
                            case 1:
                                setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN_PRESSED);
                                break;
                            case 2:
                                setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN_PRESSED);
                                break;
                            case 3:
                                setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN_PRESSED);
                                break;
                            case 4:
                                setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN_PRESSED);
                                break;
                            case 5:
                                setImageViewPic(icon6, FuncBarPic.MAIL_BTN_PRESSED);
                                break;
                            case 6:
                                setImageViewPic(icon7, FuncBarPic.SETTING_BTN_PRESSED);
                                break;
                            case 7:
                                setImageViewPic(icon8, FuncBarPic.ABOUT_BTN_PRESSED);
                                break;
                        }
                    }else {
                        Resources res = getContext().getResources();
                        items.get(cIndex).label.setTextColor(res.getColor(R.color.dark_gray));
                        switch (cIndex){
                            case 0:
                                setImageViewPic(icon1, FuncBarPic.HOT_BTN);
                                break;
                            case 1:
                                setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN);
                                break;
                            case 2:
                                setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN);
                                break;
                            case 3:
                                setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN);
                                break;
                            case 4:
                                setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN);
                                break;
                            case 5:
                                setImageViewPic(icon6, FuncBarPic.MAIL_BTN);
                                break;
                            case 6:
                                setImageViewPic(icon7, FuncBarPic.SETTING_BTN);
                                break;
                            case 7:
                                setImageViewPic(icon8, FuncBarPic.ABOUT_BTN);
                                break;
                        }
                    }
                }
            });
            // 有效操作监听(界面跳转)
            items.get(i).itemFrame.setOnPressListener(new ChangeImageTouchListener.OnPressListener() {
                @Override
                public void onFirstTouch(View view, MotionEvent e) {
                }
                @Override
                public void onMoving(View view, MotionEvent e) {
                }
                @Override
                public void onRelease(View view, MotionEvent e) {
                    XLog.d("FK", "功能栏选择了:" + cIndex);
                    Handler handler = getLayerHandler();
                    Message msg = handler.obtainMessage();
                    msg.what = BbsMsg.BBS_FUNC_SWITCH;
                    msg.arg1 = cIndex;
                    handler.sendMessage(msg);
                }
            });
        }
        // 侧边栏注册横向拖动的监听
        LMain lMain = (LMain) parentLayer();
        lMain.registerDragHorizontallyListener(new DragLayer.DragHorizontallyListener() {
            @Override
            public void onDragHorizontally() {
                refreshItem();
            }
        });

        // 监听新邮件
        BbsMailMgr.getInstance().registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
                refreshMailTip(newMailNumber);
            }
        });

        // 一开始选择第一个功能
        selectItem(0);
    }


    /**
     * 设置选择中的条目。
     * TIP 由CFunctionBar的外部使用者调用！
     * @param index
     */
    public void selectItem(int index) {
        selectedItemIndex = index;
        refreshItem();
    }


    /**
     * 设置Ta的百合的可见性
     * @param visible
     */
    public void setOtherInfoItemVisible(boolean visible) {
        if (visible) {
            btn5Frame.setVisibility(View.VISIBLE);
        } else {
            btn5Frame.setVisibility(View.GONE);
        }
    }


    /**
     * 设置被查看对象的id
     * @param userId
     */
    public void initOtherId(String userId) {
        this.otherIdLabel.setText(userId);
    }


    /**
     * 重新刷新顶栏（用于游客登录后）
     */
    public void refreshTopFrame() {
        if (globalStateSource.isLogin()) {
            usernameView.setText(globalStateSource.getCurrentUserName());
            userNameTip.setText("的百荷湾");
            topBtn.setBackgroundResource(R.drawable.btn_logout);
        } else {
            usernameView.setText("游客");
            userNameTip.setText("欢迎来到百荷湾");
            topBtn.setBackgroundResource(R.drawable.btn_login_gray);
        }
        this.getContent().invalidate();
    }

    /**
     * 刷新邮件提示（通过handler异步执行）
     * @param newMailNumber
     */
    private void refreshMailTip(int newMailNumber) {
        Message msg = localHandler.obtainMessage(REFRESH_MAIL_TIP);
        msg.arg1 = newMailNumber;
        localHandler.sendMessage(msg);
    }
    private static final int REFRESH_MAIL_TIP = 1;
    private Handler localHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_MAIL_TIP:
                    if (msg.arg1 == 0) {
                        newMailTip.setVisibility(View.GONE);
                    } else {
                        newMailTip.setVisibility(View.VISIBLE);
                        newMailTip.setText("" + msg.arg1);
                    }
                    break;
            }
        }
    };

    /**
     * 重新刷新条目背景
     */
    private void refreshItem() {
        refreshMailTip(globalStateSource.getNewMailNum());

        for (int i = 0; i< items.size(); i++) {
            items.get(i).itemFrame.resetBg();
            if (i == selectedItemIndex) {
                items.get(i).tip.setVisibility(View.VISIBLE);
                Resources res = getContext().getResources();
                items.get(i).label.setTextColor(res.getColor(R.color.yellow_green));
                switch (i) {
                    case 0:
                        setImageViewPic(icon1, FuncBarPic.HOT_BTN_PRESSED);
                        break;
                    case 1:
                        setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN_PRESSED);
                        break;
                    case 2:
                        setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN_PRESSED);
                        break;
                    case 3:
                        setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN_PRESSED);
                        break;
                    case 4:
                        setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN_PRESSED);
                        break;
                    case 5:
                        setImageViewPic(icon6, FuncBarPic.MAIL_BTN_PRESSED);
                        break;
                    case 6:
                        setImageViewPic(icon7, FuncBarPic.SETTING_BTN_PRESSED);
                        break;
                    case 7:
                        setImageViewPic(icon8, FuncBarPic.ABOUT_BTN_PRESSED);
                        break;
                }
            }else {
                items.get(i).tip.setVisibility(View.INVISIBLE);
                setViewBackground(items.get(i).itemFrame, FuncBarPic.FRAME_TRANSPARENT);
                Resources res = getContext().getResources();
                items.get(i).label.setTextColor(res.getColor(R.color.dark_gray));
                switch (i) {
                    case 0:
                        setImageViewPic(icon1, FuncBarPic.HOT_BTN);
                        break;
                    case 1:
                        setImageViewPic(icon2, FuncBarPic.MY_COLLECTION_BTN);
                        break;
                    case 2:
                        setImageViewPic(icon3, FuncBarPic.LILY_BOARD_BTN);
                        break;
                    case 3:
                        setImageViewPic(icon4, FuncBarPic.MY_LILY_BTN);
                        break;
                    case 4:
                        setImageViewPic(icon5, FuncBarPic.MY_LILY_BTN);
                        break;
                    case 5:
                        setImageViewPic(icon6, FuncBarPic.MAIL_BTN);
                        break;
                    case 6:
                        setImageViewPic(icon7, FuncBarPic.SETTING_BTN);
                        break;
                    case 7:
                        setImageViewPic(icon8, FuncBarPic.ABOUT_BTN);
                        break;
                }
            }
        }
    }

    @Override
    public int back() {
        return XBackType.SELF_BACK;
    }


    private class Item {
        public ChangeImageLayout itemFrame;
        public ImageView btn;
        public ImageView tip;
        public TextView label;

        public Item(ChangeImageLayout itemFrame, ImageView btn,
                    ImageView tip, TextView label) {
            this.itemFrame = itemFrame;
            this.btn = btn;
            this.tip = tip;
            this.label = label;
        }
    }


    /**
     * 登陆通信
     */
    private class LogoutTask extends AsyncTask<Void, Void, Void> {

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show("注销中，请稍后……");
        }
        @Override
        protected Void doInBackground(Void... para) {
            resultCode = LoginMgr.getInstance().logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (StatusCode.isSuccess(resultCode) || resultCode == StatusCode.SYSTEM_LOGIN_FAIL) {
                Toast.makeText(getContext(), "注销成功", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getContext(), "网络有问题...", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "注销失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if (resultCode != StatusCode.HTTP_EXCEPTION) {
                globalStateSource.setLoginStatus(GlobalStateSource.LOGIN_STATUS_NO_LOGIN);
                globalStateSource.setCurrentUser("", "");
                globalStateSource.clearToken();
                refreshTopFrame();

                Handler handler = getFrameHandler();
                handler.sendMessage(handler.obtainMessage(MainMsg.INIT_MAIN));
                handler.sendMessage(handler.obtainMessage(MainMsg.LOGOUT));
            }
            waitingDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            Toast.makeText(getContext(), "注销失败", Toast.LENGTH_SHORT).show();
        }
    }
}
