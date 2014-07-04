package com.morln.app.lbstask.ui.about;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-3-18
 * Time: 上午1:43
 */
public class CAbout extends XBaseComponent {

    public CAbout(final XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_about);

        TextView feedback = (TextView) findViewById(R.id.feedback);
        feedback.setText(Html.fromHtml("<u>" + "给我反馈" + "</u>"));// 加下划线
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DFeedback(parent.getUIFrame()).show();
            }
        });
    }


    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }
}
