package com.morln.app.lbstask.utils;

import java.util.Random;

/**
 * 数字辅助类
 * Created by jasontujun.
 * Date: 12-1-1
 * Time: 下午11:32
 */
public class NumberUtil {

    /**
     * 产生count个随机数，在0~range范围内。count必须小于等于range
     * @param count 大于0
     * @param range
     * @return
     */
    public static int[] generateSomeNumber(int count, int range) {
        Random r=new Random();
        int[] s=new int[count];
        boolean flag=true;

        s[0]=r.nextInt(range);
        int i=1;
        while(flag && i < count)
        {
            flag=false;
            s[i]=r.nextInt(range); //产生0~range的随机数
            if(s[i]==s[i-1])
                break;
            else{
                i++;
                flag=true;
            }
        }
        return s;
    }

}
