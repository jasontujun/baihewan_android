package com.morln.app.lbstask.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.engine.ScreenHolder;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XUIFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话框工具 TODO 有bug。某些机型必须要求dialog传入Activity的context
 * Created by 赵之韵.
 * Date: 11-12-26
 * Time: 下午12:56
 */
public class DialogUtil {

    /**
     * 创建一个警示对话框
     */
    public static WarningDialog createWarningDialog(Context context) {
        if (context == null)
            return null;
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.system_warning_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        Button ok = (Button) dialog.findViewById(R.id.dialog_ok_btn);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
        TextView msg = (TextView) dialog.findViewById(R.id.dialog_msg);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return new WarningDialog(title, msg, dialog);
    }

    /**
     * 创建一个等待的Dialog
     */
    public static WaitingDialog createWaitingDialog(Context context) {
        if (context == null)
            return null;
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.system_waiting_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        ImageView waiting = (ImageView) dialog.findViewById(R.id.waiting);
        waiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TIP 点击中间图标可取消
                dialog.cancel();
            }
        });
        TextView msg = (TextView) dialog.findViewById(R.id.msg);
        Animation rotate = AnimationUtils.loadAnimation(context, R.anim.system_waiting);

        return new WaitingDialog(waiting, msg, rotate, dialog);
    }

    /**
     * 创建一个含确认按钮的对话框
     */
    public static ConfirmDialog createConfirmDialog(Context context,
                                                    final View.OnClickListener okListener,
                                                    final View.OnClickListener cancelListener) {
        if (context == null)
            return null;
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.system_confirm_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
        TextView msg = (TextView) dialog.findViewById(R.id.dialog_msg);
        Button close = (Button) dialog.findViewById(R.id.dialog_close_btn);
        Button ok = (Button) dialog.findViewById(R.id.dialog_ok_btn);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelListener != null)
                    cancelListener.onClick(view);
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (okListener != null)
                    okListener.onClick(view);
                dialog.dismiss();
            }
        });

        return new ConfirmDialog(title, msg, dialog);
    }

    public static InputDialog createInputDialog(final Context context,
                                                final String[] inputLabelList,
                                                final InputListener inputListener,
                                                final View.OnClickListener cancelListener) {
        if (context == null)
            return null;
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.system_input_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
        LinearLayout inputListView = (LinearLayout) dialog.findViewById(R.id.dialog_input_list);
        Button okBtn = (Button) dialog.findViewById(R.id.dialog_ok_btn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.dialog_close_btn);

        final List<EditText> inputControls = new ArrayList<EditText>();
        for (String inputLabel : inputLabelList) {
            EditText item = new EditText(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ScreenHolder.getInstance().dp2px(45));
            params.setMargins(0, ScreenHolder.getInstance().dp2px(10), 0, 0);
            item.setBackgroundResource(R.drawable.frame_input);
            item.setTextColor(Color.BLACK);
            item.setHintTextColor(context.getResources().getColor(R.color.gray));
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            item.setHint(inputLabel);
            inputListView.addView(item, params);
            inputControls.add(item);
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> resultList = new ArrayList<String>();
                for (int i = 0; i < inputLabelList.length; i++) {
                    EditText inputControl = inputControls.get(i);
                    String result = inputControl.getText().toString();
                    if (TextUtils.isEmpty(result)) {
                        Toast.makeText(context, inputLabelList[i] + "不能为空！",
                                Toast.LENGTH_SHORT).show();
                        AnimationUtil.startShakeAnimation(inputControl, context);
                        return;
                    } else {
                        resultList.add(result);
                    }
                }
                if (inputListener != null) {
                    inputListener.onInputFinished(resultList);
                }
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelListener != null)
                    cancelListener.onClick(view);
                dialog.dismiss();
            }
        });

        return new InputDialog(titleView, dialog);
    }


    public static SelectDialog createSelectDialog(Context context,
                                                  String[] itemList,
                                                  final SelectListener listener) {
        if (context == null)
            return null;
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.system_select_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
        LinearLayout itemListView = (LinearLayout) dialog.findViewById(R.id.dialog_item_list);

        for (int i = 0; i<itemList.length; i++) {
            final int index = i;
            TextView item = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, ScreenHolder.getInstance().dp2px(10), 0, 0);
            item.setPadding(10, 10, 10, 10);
            item.setText(itemList[i]);
            item.setTextSize(15);
            item.setTextColor(context.getResources().getColor(R.color.light_green));
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (listener != null)
                        listener.onSelected(index);
                }
            });
            itemListView.addView(item, params);
        }

        return new SelectDialog(titleView, dialog);
    }



    /**
     * 警示对话框
     */
    public static class WarningDialog {
        private TextView title;
        private TextView msg;
        private Dialog dialog;

        public WarningDialog(TextView title, TextView msg, Dialog dialog) {
            this.title = title;
            this.msg = msg;
            this.dialog = dialog;
        }

        public void show(String title, String msg) {
            if (TextUtils.isEmpty(title)){
                this.title.setVisibility(View.GONE);
            } else {
                this.title.setVisibility(View.VISIBLE);
                this.title.setText(title);
            }
            if (TextUtils.isEmpty(msg)){
                this.msg.setVisibility(View.GONE);
            } else {
                this.msg.setVisibility(View.VISIBLE);
                this.msg.setText(msg);
            }
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }
    }

    /**
     * 等待对话框
     */
    public static class WaitingDialog {
        private Animation rotate;
        private ImageView waiting;
        private TextView msg;
        private Dialog dialog;
        private AsyncTask asyncTask;

        public WaitingDialog(ImageView waiting, TextView msg, Animation rotate, Dialog dialog) {
            this.rotate = rotate;
            this.waiting = waiting;
            this.msg = msg;
            this.dialog = dialog;
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if(asyncTask != null){
                        asyncTask.cancel(true);
                    }
                }
            });
        }

        public void show() {
            show("请稍后...");
        }

        public void show(String msg) {
            this.msg.setText(msg);
            dialog.show();
            waiting.startAnimation(rotate);
        }

        public void dismiss() {
            waiting.clearAnimation();
            dialog.dismiss();
        }

        public void setAsyncTask(AsyncTask asyncTask1){
            this.asyncTask = asyncTask1;
        }
    }

    /**
     * 确认对话框
     */
    public static class ConfirmDialog {
        private TextView title;
        private TextView msg;
        private Dialog dialog;

        public ConfirmDialog(TextView title, TextView msg, Dialog dialog) {
            this.title = title;
            this.msg = msg;
            this.dialog = dialog;
        }

        public void show(String title, String msg) {
            if (TextUtils.isEmpty(title)){
                this.title.setVisibility(View.GONE);
            } else {
                this.title.setVisibility(View.VISIBLE);
                this.title.setText(title);
            }
            if (TextUtils.isEmpty(msg)){
                this.msg.setVisibility(View.GONE);
            } else {
                this.msg.setVisibility(View.VISIBLE);
                this.msg.setText(msg);
            }
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }
    }


    /**
     * 输入对话框
     */
    public static class InputDialog {
        private TextView title;
        private Dialog dialog;

        public InputDialog(TextView title, Dialog dialog) {
            this.title = title;
            this.dialog = dialog;
        }

        public void show(String title) {
            if (TextUtils.isEmpty(title)){
                this.title.setVisibility(View.GONE);
            } else {
                this.title.setVisibility(View.VISIBLE);
                this.title.setText(title);
            }
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }
    }
    public interface InputListener {
        void onInputFinished(List<String> words);
    }


    /**
     * 选择对话框
     */
    public static class SelectDialog {
        private TextView title;
        private Dialog dialog;

        public SelectDialog(TextView title, Dialog dialog) {
            this.title = title;
            this.dialog = dialog;
        }

        public void show(String title) {
            if (TextUtils.isEmpty(title)){
                this.title.setVisibility(View.GONE);
            } else {
                this.title.setVisibility(View.VISIBLE);
                this.title.setText(title);
            }
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }

    }
    public interface SelectListener {
        void onSelected(int index);
    }
}
