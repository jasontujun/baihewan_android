package com.morln.app.lbstask.ui.setting;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.BoardSource;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.data.cache.SystemSettingSource;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-7-17
 * Time: 下午10:08
 */
public class CSetting extends XBaseComponent {
    private SystemSettingSource systemSettingSource;

    // 界面
    private RelativeLayout frame;
    private ImageView leftBtn, rightBtn, leftBtnTip;
    private Button autoLoginBtn, autoLogoutBtn,
            updateCollectBtn, autoDownloadImgBtn, autoMailRemind;
    private boolean isAutoLogin, isAutoLogout,
            isAutoUpdateCollect, isAutoDownloadImg, isAutoMailRemind;

    private LinearLayout updateCollectFrame, autoMailRemindFrame;
    private RadioGroup updateCollectGroup, autoDownloadGroup, autoMailRemindGroup;
    private RadioButton updateCollectOp1, updateCollectOp2, updateCollectOp3,
            autoDownloadOp1, autoDownloadOp2,
            autoMailRemindOp1,  autoMailRemindOp2, autoMailRemindOp3;
    private Button updateBoardBtn, checkUpdateBtn;


    public CSetting(XUILayer parent) {
        super(parent);
        systemSettingSource = (SystemSettingSource) DefaultDataRepo
                .getInstance().getSource(SourceName.SYSTEM_SETTING);

        setContentView(R.layout.system_setting);
        frame = (RelativeLayout) findViewById(R.id.frame);
        leftBtn = (ImageView) findViewById(R.id.left_btn);
        leftBtnTip = (ImageView) findViewById(R.id.left_btn_tip);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        autoLoginBtn = (Button) findViewById(R.id.auto_login_btn);
        autoLogoutBtn = (Button) findViewById(R.id.auto_logout_btn);
        updateCollectBtn = (Button) findViewById(R.id.update_collect_btn);
        autoDownloadImgBtn = (Button) findViewById(R.id.auto_download_img_btn);
        autoMailRemind = (Button) findViewById(R.id.auto_mail_remind_btn);
        updateCollectFrame = (LinearLayout) findViewById(R.id.update_collect_frame);
        autoMailRemindFrame = (LinearLayout) findViewById(R.id.auto_mail_remind_frame);
        updateCollectGroup = (RadioGroup) findViewById(R.id.update_collect_group);
        updateCollectOp1 = (RadioButton) findViewById(R.id.update_collect_op1);
        updateCollectOp2 = (RadioButton) findViewById(R.id.update_collect_op2);
        updateCollectOp3 = (RadioButton) findViewById(R.id.update_collect_op3);
        autoDownloadGroup = (RadioGroup) findViewById(R.id.auto_download_img_group);
        autoDownloadOp1 = (RadioButton) findViewById(R.id.auto_download_img_op1);
        autoDownloadOp2 = (RadioButton) findViewById(R.id.auto_download_img_op2);
        autoMailRemindGroup = (RadioGroup) findViewById(R.id.auto_mail_remind_group);
        autoMailRemindOp1 = (RadioButton) findViewById(R.id.auto_mail_remind_op1);
        autoMailRemindOp2 = (RadioButton) findViewById(R.id.auto_mail_remind_op2);
        autoMailRemindOp3 = (RadioButton) findViewById(R.id.auto_mail_remind_op3);
        updateBoardBtn = (Button) findViewById(R.id.update_board_btn);
        checkUpdateBtn = (Button) findViewById(R.id.check_update_btn);

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler layerHandler = getLayerHandler();
                layerHandler.sendMessage(layerHandler.obtainMessage(BbsMsg.SWITCH_LEFT));
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler layerHandler = getLayerHandler();
                layerHandler.sendMessage(layerHandler.obtainMessage(BbsMsg.SWITCH_RIGHT));
            }
        });
        BbsMailMgr.getInstance().registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
                refreshLeftBtnTip(newMailNumber);
            }
        });

        // 初始化
        isAutoLogin = systemSettingSource.isAutoLogin();
        isAutoLogout = systemSettingSource.isAutoLogout();
        isAutoUpdateCollect = systemSettingSource.isAutoUpdateCollect();
        isAutoDownloadImg = systemSettingSource.getAutoDownloadImg() != SystemSettingSource.AUTO_DOWNLOAD_IMG_CLOSE;
        isAutoMailRemind = systemSettingSource.isNewMailRemind();

        if(isAutoLogin) {
            autoLoginBtn.setBackgroundResource(R.drawable.btn_on);
        }else {
            autoLoginBtn.setBackgroundResource(R.drawable.btn_off);
        }
        if(isAutoLogout) {
            autoLogoutBtn.setBackgroundResource(R.drawable.btn_on);
        }else {
            autoLogoutBtn.setBackgroundResource(R.drawable.btn_off);
        }
        if(isAutoUpdateCollect) {
            updateCollectBtn.setBackgroundResource(R.drawable.btn_on);
            updateCollectFrame.setVisibility(View.VISIBLE);
        }else {
            updateCollectBtn.setBackgroundResource(R.drawable.btn_off);
            updateCollectFrame.setVisibility(View.GONE);
        }
        if(isAutoDownloadImg) {
            autoDownloadImgBtn.setBackgroundResource(R.drawable.btn_on);
            autoDownloadGroup.setVisibility(View.VISIBLE);
        }else {
            autoDownloadImgBtn.setBackgroundResource(R.drawable.btn_off);
            autoDownloadGroup.setVisibility(View.GONE);
        }
        if(isAutoMailRemind) {
            autoMailRemind.setBackgroundResource(R.drawable.btn_on);
            autoMailRemindFrame.setVisibility(View.VISIBLE);
        }else {
            autoMailRemind.setBackgroundResource(R.drawable.btn_off);
            autoMailRemindFrame.setVisibility(View.GONE);
        }
        if(systemSettingSource.getAutoUpdateCollectInterval() ==
                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_SHORT) {
            updateCollectOp1.setChecked(true);
        }else if(systemSettingSource.getAutoUpdateCollectInterval() ==
                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_MIDDLE) {
            updateCollectOp2.setChecked(true);
        }else if(systemSettingSource.getAutoUpdateCollectInterval() ==
                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_LONG) {
            updateCollectOp3.setChecked(true);
        }
        if(systemSettingSource.getNewMailRemindInterval() ==
                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_SHORT) {
            autoMailRemindOp1.setChecked(true);
        }else if(systemSettingSource.getNewMailRemindInterval() ==
                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_MIDDLE) {
            autoMailRemindOp2.setChecked(true);
        }else if(systemSettingSource.getNewMailRemindInterval() ==
                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_LONG) {
            autoMailRemindOp3.setChecked(true);
        }
        if(systemSettingSource.getAutoDownloadImg() == SystemSettingSource.AUTO_DOWNLOAD_IMG_WIFI) {
            autoDownloadOp1.setChecked(true);
        }else if(systemSettingSource.getAutoDownloadImg() == SystemSettingSource.AUTO_DOWNLOAD_IMG_ALWAYS) {
            autoDownloadOp2.setChecked(true);
        }


        // 监听
        autoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoLogin = !isAutoLogin;
                systemSettingSource.setAutoLogin(isAutoLogin);
                if (isAutoLogin)
                    autoLoginBtn.setBackgroundResource(R.drawable.btn_on);
                else
                    autoLoginBtn.setBackgroundResource(R.drawable.btn_off);
            }
        });
        autoLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoLogout = !isAutoLogout;
                systemSettingSource.setAutoLogout(isAutoLogout);
                if (isAutoLogout)
                    autoLogoutBtn.setBackgroundResource(R.drawable.btn_on);
                else
                    autoLogoutBtn.setBackgroundResource(R.drawable.btn_off);
            }
        });
        updateCollectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoUpdateCollect = !isAutoUpdateCollect;
                systemSettingSource.setAutoUpdateCollect(isAutoUpdateCollect);
                if (isAutoUpdateCollect) {
                    updateCollectBtn.setBackgroundResource(R.drawable.btn_on);
                    updateCollectFrame.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "开启自动同步收藏", Toast.LENGTH_SHORT).show();
                } else {
                    updateCollectBtn.setBackgroundResource(R.drawable.btn_off);
                    updateCollectFrame.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "关闭自动同步收藏", Toast.LENGTH_SHORT).show();
                }
            }
        });
        autoDownloadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoDownloadImg = !isAutoDownloadImg;
                if (isAutoDownloadImg) {
                    systemSettingSource.setAutoDownloadImg(SystemSettingSource.AUTO_DOWNLOAD_IMG_WIFI);
                    autoDownloadImgBtn.setBackgroundResource(R.drawable.btn_on);
                    autoDownloadGroup.setVisibility(View.VISIBLE);
                    autoDownloadOp1.setChecked(true);
                } else {
                    systemSettingSource.setAutoDownloadImg(SystemSettingSource.AUTO_DOWNLOAD_IMG_CLOSE);
                    autoDownloadImgBtn.setBackgroundResource(R.drawable.btn_off);
                    autoDownloadGroup.setVisibility(View.GONE);
                }
            }
        });
        autoMailRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAutoMailRemind = !isAutoMailRemind;
                systemSettingSource.setNewMailRemind(isAutoMailRemind);
                BbsMailMgr bbsMailMgr = BbsMailMgr.getInstance();
                if (isAutoMailRemind) {
                    autoMailRemind.setBackgroundResource(R.drawable.btn_on);
                    autoMailRemindFrame.setVisibility(View.VISIBLE);
                    bbsMailMgr.startMailRemindTask();
                    Toast.makeText(getContext(), "开启站内信提醒", Toast.LENGTH_SHORT).show();
                } else {
                    autoMailRemind.setBackgroundResource(R.drawable.btn_off);
                    autoMailRemindFrame.setVisibility(View.GONE);
                    bbsMailMgr.stopMailRemindTask();
                    Toast.makeText(getContext(), "关闭站内信提醒", Toast.LENGTH_SHORT).show();
                }
            }
        });
        updateCollectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.update_collect_op1:
                        systemSettingSource.setAutoUpdateCollectInterval(
                                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_SHORT);
                        Toast.makeText(getContext(), "选择op1", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.update_collect_op2:
                        systemSettingSource.setAutoUpdateCollectInterval(
                                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_MIDDLE);
                        Toast.makeText(getContext(), "选择op2", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.update_collect_op3:
                        systemSettingSource.setAutoUpdateCollectInterval(
                                SystemSettingSource.AUTO_UPDATE_COLLECT_INTERVAL_LONG);
                        Toast.makeText(getContext(), "选择op3", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        autoDownloadGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.auto_download_img_op1:
                        systemSettingSource.setAutoDownloadImg(SystemSettingSource.AUTO_DOWNLOAD_IMG_WIFI);
                        Toast.makeText(getContext(), "选择op1", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.auto_download_img_op2:
                        systemSettingSource.setAutoDownloadImg(SystemSettingSource.AUTO_DOWNLOAD_IMG_ALWAYS);
                        Toast.makeText(getContext(), "选择op2", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        autoMailRemindGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.auto_mail_remind_op1:
                        systemSettingSource.setNewMailRemindInterval(
                                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_SHORT);
                        break;
                    case R.id.auto_mail_remind_op2:
                        systemSettingSource.setNewMailRemindInterval(
                                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_MIDDLE);
                        break;
                    case R.id.auto_mail_remind_op3:
                        systemSettingSource.setNewMailRemindInterval(
                                SystemSettingSource.NEW_MAIL_REMIND_INTERVAL_LONG);
                        break;
                }
            }
        });
        updateBoardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 更新版面
                new UpdateBoardTask().execute(null);
            }
        });
        checkUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 检测更新
                UmengUpdateAgent.update(getContext());
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case 0: // has update
                                UmengUpdateAgent.showUpdateDialog(getContext(), updateInfo);
                                break;
                            case 1: // has no update
                                AnimationUtil.startShakeAnimation(checkUpdateBtn, getContext());
                                Toast.makeText(getContext(), "当前版本是最新的哦~亲", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 2: // none wifi
                                Toast.makeText(getContext(), "没有wifi连接哦", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case 3: // time out
                                Toast.makeText(getContext(), "网络通信异常，请稍后重试", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                        }
                    }
                });
            }
        });
    }



    /**
     * 异步刷新列表
     */
    private void refreshLeftBtnTip(int newNumber) {
        Message msg= handler.obtainMessage(REFRESH_LIST);
        msg.arg1 = newNumber;
        handler.sendMessage(msg);
    }
    private static final int REFRESH_LIST = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_LIST:
                    if (msg.arg1 == 0) {
                        leftBtnTip.setVisibility(View.GONE);
                    } else {
                        leftBtnTip.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };


    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }


    /**
     * 发送文章的异步线程
     */
    private class UpdateBoardTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show("版面数据较多，请稍后……");
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = BbsBoardMgr.getInstance().updateBoard(getContext());
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if(StatusCode.isSuccess(resultCode)) {
                BoardSource boardSource = (BoardSource) DefaultDataRepo
                        .getInstance().getSource(SourceName.BBS_BOARD);
                int boardNumber = boardSource.size();
                Toast.makeText(getContext(), "更新版面成功！共" + boardNumber + "版面", Toast.LENGTH_SHORT).show();
            }else {
                switch(resultCode){
                    case StatusCode.BOARD_IS_LATEST:
                        AnimationUtil.startShakeAnimation(updateBoardBtn, getContext());
                        Toast.makeText(getContext(), "您现在的版面已经是最新的了", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "更新版面失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            Toast.makeText(getContext(), "更新版面失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
