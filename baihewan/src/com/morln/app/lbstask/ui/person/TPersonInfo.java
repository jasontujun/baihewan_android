package com.morln.app.lbstask.ui.person;

import android.os.AsyncTask;
import com.morln.app.lbstask.data.model.BbsUserBase;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.utils.DialogUtil;
import com.xengine.android.system.ui.XUIFrame;

/**
 * 获取用户信息的AsyncTask
 * Created by jasontujun.
 * Date: 12-9-20
 * Time: 下午1:35
 */
public class TPersonInfo extends AsyncTask<Void, Void, BbsUserBase> {
    private XUIFrame uiFrame;

    private boolean hasDialog;
    private DialogUtil.WaitingDialog waitingDialog;

    private String userId;
    private GetUserInfoListener listener;

    public TPersonInfo(XUIFrame uiFrame, String userId, boolean hasDialog,
                       GetUserInfoListener listener) {
        this.uiFrame = uiFrame;
        this.userId = userId;
        this.hasDialog = hasDialog;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        if(hasDialog) {
            waitingDialog = DialogUtil.createWaitingDialog(uiFrame.getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
    }
    @Override
    protected BbsUserBase doInBackground(Void... para) {
        return BbsPersonMgr.getInstance().getBbsUserInfoFromWeb(userId);
    }
    @Override
    protected void onPostExecute(BbsUserBase result) {
        if(hasDialog) {
            waitingDialog.dismiss();
        }
        if(listener != null) {
            listener.onGettingUserInfo(result);
        }
    }
    @Override
    protected void onCancelled() {
        if(hasDialog) {
            waitingDialog.dismiss();
        }
        if(listener != null) {
            listener.onCancelled();
        }
    }

    public interface GetUserInfoListener {

        void onGettingUserInfo(BbsUserBase userInfo);

        void onCancelled();
    }
}
