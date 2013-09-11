package com.morln.app.lbstask.ui.person;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUIFrame;
import com.xengine.android.utils.XStringUtil;

/**
 * Created by jasontujun.
 * Date: 12-4-20
 * Time: 下午3:04
 */
public class DEditSignature implements XDialog {
    private BbsPersonMgr bbsPersonMgr;
    private static final int SIGNATURE_LIMIT = 30; // 手机签名的字数限制为30
    private String currentSignature;
    
    // 界面
    private Dialog dialog;
    private EditText editView;
    private TextView currentNumView, totalNumView;
    private Button okBtn, cancelBtn;

    public DEditSignature(XUIFrame uiFrame, final Runnable okTask) {
        bbsPersonMgr = BbsPersonMgr.getInstance();

        XBaseFrame activity = (XBaseFrame) uiFrame;
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_edit_signature);

        editView = (EditText) dialog.findViewById(R.id.input_view);
        currentNumView = (TextView) dialog.findViewById(R.id.current_num);
        totalNumView = (TextView) dialog.findViewById(R.id.total_num);
        okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);

        currentSignature = bbsPersonMgr.getMobileSignature();
        totalNumView.setText("" + SIGNATURE_LIMIT);
        if (TextUtils.isEmpty(currentSignature)) {
            currentNumView.setText("" + 0);
        } else {
            currentNumView.setText("" + currentSignature.length());
            editView.setText(currentSignature);
        }

        // TODO 字数限制 有bug(输入法问题)
        editView.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;

            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                temp = s;
            }
            @Override
            public void afterTextChanged(Editable editable) {
                selectionStart = editView.getSelectionStart();
                selectionEnd = editView.getSelectionEnd();
                if (temp.length() > SIGNATURE_LIMIT) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    int tempSelection = selectionStart;
                    editView.setText(editable);
                    editView.setSelection(tempSelection);//设置光标在最后
                    currentNumView.setText("" + SIGNATURE_LIMIT);
                } else {
                    currentNumView.setText("" + editable.length());
                }
            }
        });


        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSignature = editView.getText().toString();
                bbsPersonMgr.setMobileSignature(currentSignature);

                if (okTask != null) {
                    okTask.run();
                }

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

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }
}
