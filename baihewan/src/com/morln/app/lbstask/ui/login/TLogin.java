package com.morln.app.lbstask.ui.login;

import android.os.AsyncTask;
import android.widget.Toast;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.system.ui.XUIFrame;

/**
 * Created by jasontujun.
 * Date: 12-9-22
 * Time: 上午1:19
 */
public class TLogin extends AsyncTask<Void, Void, Void> {

    private XUIFrame uiFrame;

    private boolean hasDialog;
    private DialogUtil.WaitingDialog waitingDialog;

    private int resultCode;

    private LoginListener listener;

    private String username;
    private String password;

    public TLogin(XUIFrame uiFrame, String username, String password,
                  LoginListener listener, boolean hasDialog) {
        this.uiFrame = uiFrame;
        this.username = username;
        this.password = password;
        this.listener = listener;
        this.hasDialog = hasDialog;
    }

    @Override
    protected void onPreExecute() {
        if(hasDialog) {
            waitingDialog = DialogUtil.createWaitingDialog(uiFrame);
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... para) {
        resultCode = LoginMgr.getInstance().login(uiFrame.getContext(), username, password);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (StatusCode.isSuccess(resultCode)
                || resultCode == StatusCode.SYSTEM_LOGIN_FAIL) {
            Toast.makeText(uiFrame.getContext(), "登录成功！", Toast.LENGTH_SHORT).show();
            if(listener != null) {
                listener.onSucceeded(username, password);
            }
        } else {
            switch (resultCode) {
                case StatusCode.HTTP_EXCEPTION:
                    Toast.makeText(uiFrame.getContext(), "登陆失败，网络通信异常...", Toast.LENGTH_SHORT).show();
                    break;
                case StatusCode.LOGIN_BAD_REQUEST:
                    Toast.makeText(uiFrame.getContext(), "账号名密码错误...", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(uiFrame.getContext(), "登陆失败，请稍后重试...", Toast.LENGTH_SHORT).show();
                    break;
            }
            if(listener != null) {
                listener.onFailed(username, password);
            }
        }
        // 取消对话框
        if(hasDialog) {
            waitingDialog.dismiss();
        }
    }

    @Override
    protected void onCancelled() {
        if(hasDialog) {
            waitingDialog.dismiss();
        }
        Toast.makeText(uiFrame.getContext(), "登陆失败，请稍后重试...", Toast.LENGTH_SHORT).show();
        if(listener != null) {
            listener.onCanceled(username, password);
        }
    }


    public interface LoginListener {

        void onSucceeded(String username, String password);

        void onFailed(String username, String password);

        void onCanceled(String username, String password);
    }
}
