package com.morln.app.lbstask.utils;

import android.view.View;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.UserBase;

/**
 * Created by jasontujun.
 * Date: 12-9-20
 * Time: 下午12:37
 */
public class ViewUtil {

    public static void initGender(View genderView, int gender) {
        switch (gender) {
            case UserBase.GENDER_MALE:
                genderView.setVisibility(View.VISIBLE);
                genderView.setBackgroundResource(R.drawable.gender_male);
                break;
            case UserBase.GENDER_FEMALE:
                genderView.setVisibility(View.VISIBLE);
                genderView.setBackgroundResource(R.drawable.gender_female);
                break;
            case UserBase.GENDER_UNKNOWN:
                genderView.setVisibility(View.VISIBLE);
                genderView.setBackgroundResource(R.drawable.gender_unknown);
                break;
            default:
                genderView.setVisibility(View.GONE);
                break;
        }
    }
}
