package com.morln.app.lbstask.ui.collection;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.utils.DialogUtil;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-9-26
 * Time: 下午1:55
 */
public class DSelectSyncMethod implements XDialog {

    // 界面
    private Dialog dialog;
    private XUILayer uiLayer;
    private Button bbsBtn, localBtn, mergeBtn;

    private DialogUtil.SelectListener listener;

    public DSelectSyncMethod(final XUILayer ul, DialogUtil.SelectListener l) {
        this.uiLayer = ul;
        this.listener = l;

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setContentView(R.layout.dialog_sync_method);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        // 背景不变黑
        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
        lp.dimAmount=0.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        bbsBtn = (Button) dialog.findViewById(R.id.btn_sync_bbs);
        localBtn = (Button) dialog.findViewById(R.id.btn_sync_local);
        mergeBtn = (Button) dialog.findViewById(R.id.btn_sync_merge);

        bbsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onSelected(0);
                }
                dismiss();
            }
        });
        localBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onSelected(1);
                }
                dismiss();
            }
        });
        mergeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onSelected(2);
                }
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }
}
