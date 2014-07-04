package com.morln.app.lbstask.newui.login;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.morln.app.lbstask.logic.LoginMgr;
import com.morln.app.lbstask.session.StatusCode;
import com.morln.app.lbstask.utils.DialogUtil;

/**
 * <pre>
 * 登陆的异步任务。
 * User: jasontujun
 * Date: 14-7-4
 * Time: 上午11:07
 * </pre>
 */
public class LoginTask extends AsyncTask<Void, Void, Void> {

    private DialogUtil.WaitingDialog waitingDialog;

    private int resultCode;

    private Context context;
    private String username;
    private String password;
    private LoginListener listener;
    private boolean hasDialog;

    public LoginTask(Context context, String username, String password,
                  LoginListener listener, boolean hasDialog) {
        this.context = context;
        this.username = username;
        this.password = password;
        this.listener = listener;
        this.hasDialog = hasDialog;
    }

    @Override
    protected void onPreExecute() {
        if(hasDialog) {
            waitingDialog = DialogUtil.createWaitingDialog(context);
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Void... para) {
        resultCode = LoginMgr.getInstance().login(context, username, password);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (StatusCode.isSuccess(resultCode)
                || resultCode == StatusCode.SYSTEM_LOGIN_FAIL) {
            Toast.makeText(context, "登录成功！", Toast.LENGTH_SHORT).show();
            if(listener != null) {
                listener.onSucceeded(username, password);
            }
        } else {
            switch (resultCode) {
                case StatusCode.HTTP_EXCEPTION:
                    Toast.makeText(context, "登陆失败，网络通信异常...", Toast.LENGTH_SHORT).show();
                    break;
                case StatusCode.LOGIN_BAD_REQUEST:
                    Toast.makeText(context, "账号名密码错误...", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "登陆失败，请稍后重试...", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context, "登陆失败，请稍后重试...", Toast.LENGTH_SHORT).show();
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
