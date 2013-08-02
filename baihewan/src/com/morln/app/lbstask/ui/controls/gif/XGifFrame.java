package com.morln.app.lbstask.ui.controls.gif;

import android.graphics.Bitmap;

public class XGifFrame {

    /**图片*/
    public Bitmap image;

    /**延时*/
    public int delay;

	/**
	 * 构造函数
	 * @param im 图片
	 * @param del 延时
	 */
	public XGifFrame(Bitmap im, int del) {
		image = im;
		delay = del;
	}
}
