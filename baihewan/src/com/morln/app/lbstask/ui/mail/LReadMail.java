package com.morln.app.lbstask.ui.mail;

import android.os.*;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.Mail;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseLayer;
import com.xengine.android.system.ui.XUIFrame;
import com.xengine.android.utils.XLog;

/**
 * 显示帖子的组件。
 * Created by jasontujun.
 * Date: 12-2-27
 * Time: 下午3:09
 */
public class LReadMail extends XBaseLayer {

    private String mailId;// 此站内信id

    // 界面
    private RelativeLayout frame;
    private ImageView backBtn;
    private LinearLayout functionBtnFrame;
    private Button preBtn;
    private Button nextBtn;
    private Button quickReplyBtn;
    private Button refreshBtn;
    private Button topBtn;
    private ListView content;
    private AMail mailAdapter;

    private TextView nothingTip;

    public LReadMail(XUIFrame uiFrame, String mId) {
        super(uiFrame);

        this.mailId = mId;

        setContentView(R.layout.bbs_read_mail);
        frame = (RelativeLayout) findViewById(R.id.frame);
        backBtn = (ImageView) findViewById(R.id.back_btn);
        functionBtnFrame = (LinearLayout) findViewById(R.id.function_btn_frame);
        preBtn = (Button) findViewById(R.id.pre_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        quickReplyBtn = (Button) findViewById(R.id.quick_reply_btn);
        refreshBtn = (Button) findViewById(R.id.refresh_btn);
        topBtn = (Button) findViewById(R.id.top_btn);
        content = (ListView) findViewById(R.id.content);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailAdapter.clearImgAsyncTasks();// 清空后台任务

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_MAIL_DETAIL_BACK;
                handler1.sendMessage(msg);
            }
        });
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailAdapter.clearImgAsyncTasks();// 清空后台任务

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_PRE_MAIL;
                handler1.sendMessage(msg);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailAdapter.clearImgAsyncTasks();// 清空后台任务

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.SEE_NEXT_MAIL;
                handler1.sendMessage(msg);
            }
        });
        quickReplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TIP 登陆权限检测
                GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.
                        getInstance().getSource(SourceName.GLOBAL_STATE);
                if (!globalStateSource.isLogin()) {
                    new DLogin(LReadMail.this, true).show();
                    return;
                }

                Mail mail = BbsMailMgr.getInstance().getMailDetailFromLocal(mailId);
                if (mail != null) {
                    String title = mail.getTitle();
                    if (!title.contains("Re: "))  {
                        title = "Re: " + title;
                    }
                    XLog.d("REPLY", "站内信 title:" + title);

                    Handler handler1 = getFrameHandler();
                    Message msg = handler1.obtainMessage();
                    msg.what = MainMsg.WRITE_MAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    bundle.putString("receiver", mail.getSender());
                    msg.setData(bundle);
                    handler1.sendMessage(msg);
                } else {
                    Toast.makeText(getContext(), "站内信不存在，无法回复", Toast.LENGTH_SHORT).show();
                }
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetMailDetailTask().execute(null);// 刷新一下
            }
        });
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!content.isStackFromBottom()) {
                    content.setStackFromBottom(true);
                }
                content.setStackFromBottom(false);
            }
        });

        // 初始化“回到顶部”按钮
        content.setFastScrollEnabled(true);
        content.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                View v = absListView.getChildAt(0);
                if (v == null) {
                    return;
                }

                if (firstVisibleItem == 0 && v.getTop() >= 0) {
                    topBtn.setVisibility(View.GONE);
                } else {
                    topBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        // 初始化文章
        mailAdapter = new AMail(this, content);
        content.setAdapter(mailAdapter);
        showMail(mId);
    }

    /**
     * 设置邮件内容的可见性
     * @param visibility
     */
    private void setMailContentVisibility(boolean visibility) {
        functionBtnFrame.setVisibility(View.VISIBLE);
        if (visibility) {
            nothingTip.setText("帖子可能被删除了");
            nothingTip.setVisibility(View.INVISIBLE);
            content.setVisibility(View.VISIBLE);
        } else {
            nothingTip.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 显示邮件内容
     * @param mailUrl
     */
    public void showMail(String mailUrl) {
        this.mailId = mailUrl;

        mailAdapter.clearImgAsyncTasks();// 清除后台线程

        if (BbsMailMgr.getInstance().getMailDetailFromLocal(mailUrl) != null) {
            refreshMailToTop();
        } else {
            new GetMailDetailTask().execute(null);
        }
    }


    public void refreshMailToTop() {
        mailAdapter.refresh(mailId);
        // 回到顶部
        if (!content.isStackFromBottom()) {
            content.setStackFromBottom(true);
        }
        content.setStackFromBottom(false);
    }


    @Override
    public int back() {
        mailAdapter.clearImgAsyncTasks();
        return XBackType.SELF_BACK;
    }

    @Override
    public Handler getLayerHandler() {
        return null;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (state != null) {
            content.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = content.onSaveInstanceState();
    }


    /**
     * 从网页抓取站内信的异步线程
     */
    private class GetMailDetailTask extends AsyncTask<Void, Void, Void> {
        private DialogUtil.WaitingDialog waitingDialog;
        private Mail mail;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            mail = BbsMailMgr.getInstance().getMailDetailFromWeb(mailId);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (mail == null) {
                Toast.makeText(getContext(), "对不起，邮件不存在...", Toast.LENGTH_SHORT).show();
                setMailContentVisibility(false);
            } else {
                setMailContentVisibility(true);
                refreshMailToTop();

                mailAdapter.autoDownloadImg();// 自动下载图片
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }

}
