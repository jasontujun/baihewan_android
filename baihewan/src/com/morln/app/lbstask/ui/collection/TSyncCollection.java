package com.morln.app.lbstask.ui.collection;

import android.os.AsyncTask;
import android.widget.Toast;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XUIFrame;

/**
 * Created by jasontujun.
 * Date: 12-9-25
 * Time: 下午8:00
 */
public class TSyncCollection extends AsyncTask<Void, Void, Integer> {

    private XUIFrame uiFrame;

    private boolean hasDialog;

    private DialogUtil.WaitingDialog waitingDialog;

    private SyncCollectionListener listener;

    public TSyncCollection(XUIFrame uiFrame, boolean hasDialog,
                           SyncCollectionListener listener) {
        this.hasDialog = hasDialog;
        this.uiFrame = uiFrame;
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
    protected Integer doInBackground(Void... para) {
        int resultCode1 = BbsPersonMgr.getInstance().uploadCollection(uiFrame.getContext());
        return resultCode1;
    }
    @Override
    protected void onPostExecute(Integer resultCode) {
        if(listener != null) {
            if(StatusCode.isSuccess(resultCode)) {
                listener.onSucceeded();
            }else {
                listener.onFailed();
            }
        }

        if(hasDialog) {
            if(StatusCode.isSuccess(resultCode)) {
                Toast.makeText(uiFrame.getContext(), "同步收藏列表成功！", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(uiFrame.getContext(), "同步收藏列表失败！", Toast.LENGTH_SHORT).show();
            }
            waitingDialog.dismiss();
        }
    }
    @Override
    protected void onCancelled() {
        if(hasDialog) {
            waitingDialog.dismiss();
            Toast.makeText(uiFrame.getContext(), "同步收藏列表失败！", Toast.LENGTH_SHORT).show();
        }
        if(listener != null) {
            listener.onCanceled();
        }
    }

    public interface SyncCollectionListener {
        void onSucceeded();

        void onFailed();

        void onCanceled();
    }
}
