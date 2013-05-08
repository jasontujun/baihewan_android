package com.morln.app.lbstask.ui.top10;

import java.util.List;

/**
 * 组别信息。
 * Created by jasontujun.
 * Date: 12-2-26
 * Time: 下午1:33
 */
public interface Groups<T> {
    int getGroupItemSize(int groupIndex);

    int getGroupSize();

    T getItem(int groupIndex, int itemIndex);

    List<T> getGroup(int groupIndex);
    
    String getGroupName(int groupIndex);

    int getGroupIndex(String name);

    /**
     * 添加组到对应位置。
     * @param groupName
     * @param items
     * @param index     组的位置
     */
    void addGroup(String groupName, List<T> items, int index);

    /**
     * 添加条目到对应的组里面。如果此组不存在则新建一组,并把此组添加到index位置。
     * @param groupName
     * @param item
     * @param gIndex     新建组的位置
     */
    void addItem(String groupName, T item, int gIndex);
    
    void deleteGroup(String groupName);

    void deleteGroup(int groupIndex);

    void deleteItem(T item);

    void deleteItem(int groupIndex, int itemIndex);
}
