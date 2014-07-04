package com.morln.app.lbstask.newui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.newui.top10.Top10Fragment;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-4
 * Time: 上午11:07
 * To change this template use File | Settings | File Templates.
 */
public class MainContentFragment extends Fragment {

    private int mSelectedMenuIndex;// 当前选中的左边标签

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.main_middle, container, false);
        mSelectedMenuIndex = -1;
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        selectMenu(0);// 预先加载0
    }

    /**
     * 左边栏切换主题页面
     * @param index
     */
    public void selectMenu(int index) {
        if (index != mSelectedMenuIndex) {
            mSelectedMenuIndex = index;

            if (getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();

            Fragment fragment = null;
            Bundle args = null;
            switch (mSelectedMenuIndex) {
                case 0:
                    fragment = new Top10Fragment();
                    args = new Bundle();
                    args.putInt("tab", 0);
                    fragment.setArguments(args);
                    break;
//                case 1:
//                    fragment = new FragmentHome();
//                    args = new Bundle();
//                    args.putString("name", "搜索");
//                    fragment.setArguments(args);
//                    break;
                default:
            }
            if (fragment != null)
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .commitAllowingStateLoss();
        }
    }

    /**
     * 当前主题页面下添加页面
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.right_in, R.anim.left_out,
                        R.anim.left_in, R.anim.right_out)
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
