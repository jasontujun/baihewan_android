package com.morln.app.lbstask.ui.top10;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.system.ui.XBaseFrame;
import com.morln.app.system.ui.XDialog;
import com.morln.app.system.ui.XUILayer;
import com.morln.app.utils.XLog;

import java.util.Calendar;

/**
 * Created by jasontujun.
 * Date: 12-4-7
 * Time: 下午9:30
 */
public class DSelectDate implements XDialog {
    private int selectedYear, selectedMonth, selectedDay;// 真实的年月日
    private Calendar today;
    private Calendar earlyDay;

    // 界面
    private Dialog dialog;
    private XUILayer uiLayer;
    private DatePicker datePicker;
    private Button okBtn, cancelBtn;

    public DSelectDate(final XUILayer ul, int cy, int cm, int cd,
                       final SelectDateListener listener) {
        this.uiLayer = ul;
        this.selectedYear = cy;
        this.selectedMonth = cm + 1;// TIP 关键！
        this.selectedDay = cd;
        XLog.d("TOP10", "create dialog:" + selectedYear + "." + selectedMonth + "." + selectedDay);

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_date_select);

        datePicker = (DatePicker) dialog.findViewById(R.id.date_picker);
        okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);

        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY,23);
        today.set(Calendar.MINUTE,59);
        today.set(Calendar.SECOND,59);
        earlyDay = Calendar.getInstance();
        earlyDay.set(2012, 4-1, 1);
        
        datePicker.init(selectedYear, selectedMonth - 1, selectedDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
                selectedYear = year;
                selectedMonth = month + 1;
                selectedDay = day;
                XLog.d("TOP10", "on date change:" + selectedYear + "." + selectedMonth + "." + selectedDay);
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar selectDate = Calendar.getInstance();
                selectDate.set(selectedYear, selectedMonth - 1, selectedDay);
                if(selectDate.before(earlyDay)) {
                    AnimationUtil.startShakeAnimation(datePicker, uiLayer.getContext());
                    Toast.makeText(uiLayer.getContext(), "木有2012.4.1日之前的历史十大...", Toast.LENGTH_SHORT).show();
                    return;
                }else if(selectDate.after(today)) {
                    AnimationUtil.startShakeAnimation(datePicker, uiLayer.getContext());
                    Toast.makeText(uiLayer.getContext(), "木有超过今天的历史十大...", Toast.LENGTH_SHORT).show();
                    return;
                }

                dismiss();

                if(listener != null) {
                    listener.onSelect(selectedYear, selectedMonth, selectedDay);
                }
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

    public interface SelectDateListener {
        void onSelect(int year, int month, int day);
    }
}
