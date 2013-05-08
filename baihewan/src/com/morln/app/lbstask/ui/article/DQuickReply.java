package com.morln.app.lbstask.ui.article;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.system.ui.XBaseFrame;
import com.morln.app.system.ui.XDialog;
import com.morln.app.system.ui.XUILayer;
import com.morln.app.utils.XStringUtil;

/**
 * Created by jasontujun.
 * Date: 12-4-7
 * Time: 下午9:29
 */
public class DQuickReply implements XDialog {
    private BbsArticleMgr bbsArticleMgr;
    private String hostId,hostBoard,hostTitle,content;// 被回复的文章相关的

    // 界面
    private Dialog dialog;
    private XUILayer uiLayer;
    private EditText inputView;
    private Button replyBtn, jumpBtn, cancelBtn;

    public DQuickReply(final XUILayer ul, String hId, String hBoard, String hTitle) {
        this.uiLayer = ul;
        this.hostId = hId;
        this.hostBoard = hBoard;
        this.hostTitle = hTitle;
        bbsArticleMgr = BbsArticleMgr.getInstance();

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setContentView(R.layout.dialog_quick_reply);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        // 背景不变黑
        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
        lp.dimAmount=0.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        inputView = (EditText) dialog.findViewById(R.id.input_view);
        replyBtn = (Button) dialog.findViewById(R.id.reply_btn);
        jumpBtn = (Button) dialog.findViewById(R.id.jump_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);

        replyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = inputView.getText().toString();
                if (XStringUtil.isNullOrEmpty(content)) {
                    Toast.makeText(uiLayer.getContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(inputView, uiLayer.getContext());
                    return;
                }

                new ReplyArticleTask().execute(null);
                dismiss();
            }
        });
        jumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = inputView.getText().toString();

                Handler handler1 = uiLayer.getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.BBS_REPLY_ARTICLE;
                Bundle bundle = new Bundle();
                bundle.putString("id", hostId);
                bundle.putString("board", hostBoard);
                bundle.putString("title", hostTitle);
                bundle.putString("content", content);
                bundle.putInt("floor", 0);
                msg.setData(bundle);
                handler1.sendMessage(msg);

                dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    /**
     * 获取版面的异步线程
     */
    private class ReplyArticleTask extends AsyncTask<Void, Void, Integer> {
        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(uiLayer.getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            String title = "Re: " + hostTitle;
            int resultCode = bbsArticleMgr.replyArticle(hostId, hostBoard, title, content);
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if(StatusCode.isSuccess(resultCode)){
                Toast.makeText(uiLayer.getContext(), "回帖成功！", Toast.LENGTH_SHORT).show();

                Handler handler1 = uiLayer.getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.BBS_REPLY_ARTICLE_BACK;
                msg.arg1 = 1;// 回帖成功
                handler1.sendMessage(msg);
            }else {
                switch(resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(uiLayer, true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(uiLayer.getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(uiLayer.getContext(), "回帖失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            Toast.makeText(uiLayer.getContext(), "回帖失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
