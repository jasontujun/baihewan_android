package com.morln.app.lbstask.newui.top10;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.morln.app.lbstask.R;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 下午2:12
 * To change this template use File | Settings | File Templates.
 */
public class FragmentTop10 extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.main_bbs_content, container, false);
        return rootView;
    }
}
