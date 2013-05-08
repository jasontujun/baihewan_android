package com.morln.app.lbstask.ui;

/**
 * 线性特点。可以向前阅读，也可以向后阅读
 * Created by jasontujun.
 * Date: 12-3-8
 * Time: 下午3:22
 */
public interface Linear<T> {
    /**
     * 获取上一篇
     * @return
     */
    T getPre();

    /**
     * 获取下一篇
     * @return
     */
    T getNext();
}
