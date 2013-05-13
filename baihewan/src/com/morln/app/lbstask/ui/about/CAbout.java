package com.morln.app.lbstask.ui.about;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.res.SystemPic;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-3-18
 * Time: 上午1:43
 */
public class CAbout extends XBaseComponent {

    private ImageView title;
    private TextView feedback;

    public CAbout(final XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_about);
        
        title = (ImageView) findViewById(R.id.title);
        feedback = (TextView) findViewById(R.id.feedback);
        ImageView logo = (ImageView) findViewById(R.id.logo);

        setImageViewPic(title, SystemPic.TITLE);
        setViewBackground(logo, SystemPic.LOGO);

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
