package com.morln.app.lbstask.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-5-18
 * Time: 上午4:43
 */
public class LoadingWordUtil {
    private static LoadingWordUtil instance;

    public synchronized static LoadingWordUtil getInstance() {
        if(instance == null) {
            instance = new LoadingWordUtil();
        }
        return instance;
    }

    private List<String> wordList;

    private LoadingWordUtil() {
        wordList = new ArrayList<String>();
        wordList.add("人生哪有不2时~");
        wordList.add("及时当勉励，岁月不待人");
        wordList.add("我还年轻\n我渴望上路");
        wordList.add("我不好\n但我走在通往很好的路上");
        wordList.add("抓住幸福其实比忍耐痛苦更需要勇气");
        wordList.add("爱情和梦想都是很奇妙的东西，\n不用说不用听，你就能感受到它");
        wordList.add("从\"知道\",到达\"懂得\"\n这是成长");
        wordList.add("世间最好的感受\n就是发现自己的心在微笑..");
        wordList.add("亲爱的小孩\n不要哭");
        wordList.add("我一生荒芜\n但我记得和你在一起");
        wordList.add("我不怕你变得非常优秀，\n我只害怕不能和你势均力敌\n" +
                "          ———— DevilBlue");
        wordList.add("只要活着一定会遇上好事的\n" +
                "          ———— 樱桃小丸子");
        wordList.add("只有一种英雄主义\n就是在认清生活的真相之后依然热爱生活\n" +
                "          ———— 罗曼•罗兰");
        wordList.add("使这个世界灿烂的\n不是阳光，是女生的微笑");
        wordList.add("你知道吗？\n南大前身是创建于1902年的三江师范学堂");
        wordList.add("献给所有可爱的南大人");
        wordList.add("诚朴雄伟 励学敦行");
        wordList.add("幸福没那么容易\n才让人特别着迷");
        wordList.add("感觉快乐就忙东忙西\n感觉累了就放空自己~");
        wordList.add("小百合 \n温馨的家");
        wordList.add("那些无需付出的岁月\n决不是真正的生活");
        wordList.add("跌倒了，爬起来再哭");
        wordList.add("青梅枯萎，竹马老去，\n从此我爱上的人都很像你。");
        wordList.add("一霎风雨，我爱过你\n几度雨停，我爱自己");
        wordList.add("天真岁月不忍欺\n青春荒唐我不负你");
        wordList.add("青春的黑夜挑灯流浪\n青春的爱情不回望" +
                "\n不回想，不回答，不回忆，不回眸，反正也不回头");
    }
    
    public String getWord() {
        Date date = new Date(System.currentTimeMillis());
        if(date.getMonth() == 4 && date.getDate() == 20) {
            return "今天，大声说出\"我爱你\"！";
        }else {
            int randomIndex = (int) (Math.random() * wordList.size());
            return wordList.get(randomIndex);
        }
    }
}
