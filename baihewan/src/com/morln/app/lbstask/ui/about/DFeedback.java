package com.morln.app.lbstask.ui.about;

import android.app.Dialog;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.logic.SystemMgr;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.system.ui.XBaseFrame;
import com.morln.app.system.ui.XDialog;
import com.morln.app.system.ui.XUIFrame;
import com.morln.app.utils.XStringUtil;

/**
 * Created by jasontujun.
 * Date: 12-4-27
 * Time: 下午3:01
 */
public class DFeedback implements XDialog {
    private SystemMgr systemMgr;
    private String content;

    // 界面
    private Dialog dialog;
    private XBaseFrame uiFrame;
    private EditText inputView;
    private Button replyBtn, cancelBtn;

    public DFeedback(final XUIFrame uf) {
        this.uiFrame = (XBaseFrame) uf;
        systemMgr = SystemMgr.getInstance();

        dialog = new Dialog(uiFrame, R.style.dialog);
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        // 背景不变黑
        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
        lp.dimAmount=0.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        inputView = (EditText) dialog.findViewById(R.id.input_view);
        replyBtn = (Button) dialog.findViewById(R.id.reply_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);

        replyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = inputView.getText().toString();
                if (XStringUtil.isNullOrEmpty(content)) {
                    Toast.makeText(uiFrame.getContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(inputView, uiFrame.getContext());
                    return;
                }

                new FeedBackTask().execute(null);
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
    private class FeedBackTask extends AsyncTask<Void, Void, Integer> {
        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(uiFrame);
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = systemMgr.feedback(content);
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            waitingDialog.dismiss();
            if(StatusCode.isSuccess(resultCode)) {
                Toast.makeText(uiFrame.getContext(), "谢谢你的反馈！", Toast.LENGTH_SHORT).show();
            }else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(uiFrame.getContext(), "网络通信异常，请稍后重试", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusCode.NOT_AUTHORIZED:
                        Toast.makeText(uiFrame.getContext(), "未登录，请先登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(uiFrame.getContext(), "反馈失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            Toast.makeText(uiFrame.getContext(), "反馈失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
