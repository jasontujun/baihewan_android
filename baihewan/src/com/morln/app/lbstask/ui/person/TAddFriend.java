package com.morln.app.lbstask.ui.person;

import android.os.AsyncTask;
import android.widget.Toast;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-9-20
 * Time: 下午2:43
 */
public class TAddFriend extends AsyncTask<Void, Void, Integer> {

    private XUILayer uiLayer;

    private DialogUtil.WaitingDialog waitingDialog;

    private String username, customName;

    private AddFriendListener listener;

    public TAddFriend(XUILayer uiLayer, String username, String customName,
                      AddFriendListener listener) {
        this.uiLayer = uiLayer;
        this.customName = customName;
        this.username = username;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        waitingDialog = DialogUtil.createWaitingDialog(uiLayer.getUIFrame());
        waitingDialog.setAsyncTask(this);
        waitingDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... para) {
        return BbsPersonMgr.getInstance().addFriend(username, customName);
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        waitingDialog.dismiss();
        if(StatusCode.isSuccess(resultCode)) {
            Toast.makeText(uiLayer.getContext(), "成功添加了好友" + username + "！", Toast.LENGTH_SHORT).show();
            if(listener != null) {
                listener.onSucceeded();
            }
        }else {
            switch (resultCode) {
                case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                    new DLogin(uiLayer, true).show("由于长时间发呆，要重新登录哦");
                    Toast.makeText(uiLayer.getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                    break;
                case StatusCode.NOT_EXIST_USER:
                    Toast.makeText(uiLayer.getContext(), "添加失败，该用户不存在！", Toast.LENGTH_SHORT).show();
                    break;
                case StatusCode.ALREADY_FRIEND:
                    Toast.makeText(uiLayer.getContext(), "添加失败，Ta已经是你的好友了！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(uiLayer.getContext(), "添加好友失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
            if(listener != null) {
                listener.onFailed();
            }
        }
    }

    @Override
    protected void onCancelled() {
        waitingDialog.dismiss();
        Toast.makeText(uiLayer.getContext(), "添加好友失败！", Toast.LENGTH_SHORT).show();
        if(listener != null) {
            listener.onCancelled();
        }
    }


    public interface AddFriendListener {
        void onSucceeded();

        void onFailed();

        void onCancelled();
    }
}
