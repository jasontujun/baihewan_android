package com.morln.app.lbstask.bbs.cache;

import com.morln.app.lbstask.bbs.db.BoardTable;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.cache.SourceName;
import com.xengine.android.data.cache.XBaseAdapterIdDBDataSource;
import com.xengine.android.data.db.XDBTable;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储所有版块，以及每个版块的帖子列表(ArticleBase)
 * Created by jasontujun.
 * Date: 12-2-25
 * Time: 下午8:40
 */
public class BoardSource extends XBaseAdapterIdDBDataSource<Board> {

    /**
     * 初始化数据库中的数据，或452个默认版面
     */
    public void init() {
        loadFromDatabase();
        if(size() == 0) {
            XLog.d("DB", "版面的数据库表，加载了0个数据");
            initDefaultData();
        }
    }

    /**
     * 版面数据源的缺省数据
     */
    private void initDefaultData() {
        itemList.add(new Board("Advice","百合共创","本站","SYSOP","本站系统"));
        itemList.add(new Board("Announce","站务公告栏","站务","SYSOP","本站系统"));
        itemList.add(new Board("BBSHelp","新手求助","本站","xiaoer342","本站系统"));
        itemList.add(new Board("bbslists","本站的各类统计列表与记录","本站","SYSOP","本站系统"));
        itemList.add(new Board("Blog","博客论坛","本站","Veikay","本站系统"));
        itemList.add(new Board("BMManager","版主监督和管理","站务","诚征版主中","本站系统"));
        itemList.add(new Board("Board","版主竞选与请辞","站务","SYSOP","本站系统"));
        itemList.add(new Board("BoardManage","版面开设与变更","站务","SYSOP","本站系统"));
        itemList.add(new Board("Chat","聊天版","本站","SYSOP","本站系统"));
        itemList.add(new Board("Complain","投诉与举报","站务","SYSOP","本站系统"));
        itemList.add(new Board("EnglishCorner","英语聊天版","本站","SYSOP","本站系统"));
        itemList.add(new Board("ExcellentBM","版务评优","站务","SYSOP","本站系统"));
        itemList.add(new Board("LilyDigest","百合精华","本站","SYSOP","本站系统"));
        itemList.add(new Board("LilyFestival","百合站庆","本站","SYSOP","本站系统"));
        itemList.add(new Board("LilyLinks","百合友情链接","本站","LilyAD","本站系统"));
        itemList.add(new Board("newcomers","新手上路","本站","SYSOP","本站系统"));
        itemList.add(new Board("Nirvana","桫椤双树园","本站","SYSOP","本站系统"));
        itemList.add(new Board("notepad","酸甜苦辣留言版","本站","SYSOP","本站系统"));
        itemList.add(new Board("Ourselves","百合原创","本站","SYSOP","本站系统"));
        itemList.add(new Board("Paint","涂鸦论坛","本站","BlogOP","本站系统"));
        itemList.add(new Board("PersonalCorpus","个人文集","本站","SYSOP","本站系统"));
        itemList.add(new Board("sysop","站长的工作室","本站","SYSOP","本站系统"));
        itemList.add(new Board("test","这是站内测试版","本站","SYSOP","本站系统"));
        itemList.add(new Board("vote","本站各项投票与结果","本站","SYSOP","本站系统"));
        itemList.add(new Board("VoteBoard","选举版","站务","LilyOP","本站系统"));

        itemList.add(new Board("AcademicReport","学术讲座","本校","laoniu559","南京大学"));
        itemList.add(new Board("C_Inter","国际学院","院系","lihy690","南京大学"));
        itemList.add(new Board("CCAS","中美中心","本校","zmzx","南京大学"));
        itemList.add(new Board("Contest","大学生竞赛","本校","laoniu559","南京大学"));
        itemList.add(new Board("D_Chinese","中文系","院系","ttzhu","南京大学"));
        itemList.add(new Board("D_Computer","计算机系","院系","Amati","南京大学"));
        itemList.add(new Board("D_EarthScience","地球科学与工程学院","院系","诚征版主中","南京大学"));
        itemList.add(new Board("D_EE","电子科学与工程学院","院系","e06084","南京大学"));
        itemList.add(new Board("D_History","历史系","院系","txduyu","南京大学"));
        itemList.add(new Board("D_Materials","材料科学系","院系","MSEYH","南京大学"));
        itemList.add(new Board("D_Maths","数学系","院系","zhaoying","南京大学"));
        itemList.add(new Board("D_Philosophy","哲学系","院系","88579025","南京大学"));
        itemList.add(new Board("D_Physics","物理学院","院系","olma","南京大学"));
        itemList.add(new Board("D_SocialSec","社会保障系","院系","诚征版主中","南京大学"));
        itemList.add(new Board("DII","匡亚明学院_强化部","院系","smiling99","南京大学"));
        itemList.add(new Board("IFA_IS","美术研究院_雕塑艺术研究所","院系","诚征版主中","南京大学"));
        itemList.add(new Board("IFIS","海外教育学院","院系","cagali","南京大学"));
        itemList.add(new Board("MARC","模式动物研究所","院系","Ashin","南京大学"));
        itemList.add(new Board("NJU_HOME","南大和园","本校","Zergler","南京大学"));
        itemList.add(new Board("NJUExpress","南大校园生活","本校","SYSOP","南京大学"));
        itemList.add(new Board("Postdoc","博士后之家","本校","zhouxinxin","南京大学"));
        itemList.add(new Board("PuKouCampus","浦园风景线","本校","诚征版主中","南京大学"));
        itemList.add(new Board("S_Astronomy","天文与空间科学学院","院系","astrocatcher","南京大学"));
        itemList.add(new Board("S_Atmosphere","大气科学学院","院系","lisalovevivi","南京大学"));
        itemList.add(new Board("S_Business","商学院","院系","lovered","南京大学"));
        itemList.add(new Board("S_Chemistry","化学化工学院","院系","apachemz","南京大学"));
        itemList.add(new Board("S_Education","教育研究院","院系","shishangappl","南京大学"));
        itemList.add(new Board("S_Environment","环境学院","院系","smile520","南京大学"));
        itemList.add(new Board("S_ForeignLang","外国语学院","院系","happyhome","南京大学"));
        itemList.add(new Board("S_Geography","地理学院","院系","423459306","南京大学"));
        itemList.add(new Board("S_GOV","政府管理学院","院系","Txccj","南京大学"));
        itemList.add(new Board("S_Graduate","研究生之家","本校","诚征版主中","南京大学"));
        itemList.add(new Board("S_Information","信息管理学院","院系","snowy1007","南京大学"));
        itemList.add(new Board("S_Journalism","新闻传播学院","院系","nd050701","南京大学"));
        itemList.add(new Board("S_Law","法学院","院系","fxg2003","南京大学"));
        itemList.add(new Board("S_LifeScience","生命科学院","院系","fisherman","南京大学"));
        itemList.add(new Board("S_Medicine","医学院","院系","lfforever","南京大学"));
        itemList.add(new Board("S_MSE","工程管理学院","院系","GGbondg","南京大学"));
        itemList.add(new Board("S_Sociology","社会学院","院系","God02","南京大学"));
        itemList.add(new Board("S_Software","软件学院","院系","felspar","南京大学"));
        itemList.add(new Board("SAU","建筑与城市规划学院","院系","architecture","南京大学"));

        itemList.add(new Board("AnHui","淮水皖风","地区","mooder","乡情校谊"));
        itemList.add(new Board("BeiJing","首都北京","地区","18ga","乡情校谊"));
        itemList.add(new Board("CAS","中国科学院","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("ChangZhou","锦绣常州","地区","ultraman","乡情校谊"));
        itemList.add(new Board("CPU","中国药科大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("CUG","中国地质大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("CUMT","中国矿业大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("DongBei","白山黑水","地区","ach1o","乡情校谊"));
        itemList.add(new Board("FDU","复旦大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("FuJian","八闽畅怀","地区","xiaozhu0623a","乡情校谊"));
        itemList.add(new Board("GuangDong","粤是故乡名","地区","KeithYib","乡情校谊"));
        itemList.add(new Board("GuangXi","八桂大地","地区","w5gy1","乡情校谊"));
        itemList.add(new Board("HaiNan","天涯海角","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("HeBei","燕赵大地","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("HeNan","九州之中","地区","Aspartame","乡情校谊"));
        itemList.add(new Board("HHU","河海大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("HKU","香港大学","友校","mitchphysics","乡情校谊"));
        itemList.add(new Board("HuaiAn","淮水楚云","地区","499087314","乡情校谊"));
        itemList.add(new Board("HuBei","荆楚大地","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("HuNan","三湘四水","地区","hsboy","乡情校谊"));
        itemList.add(new Board("Inner_Mongolia","塞外风情","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("JiangXi","江南西道","地区","498095724","乡情校谊"));
        itemList.add(new Board("JLU","吉林大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("LianYunGang","花果山下","地区","xiaogang","乡情校谊"));
        itemList.add(new Board("LZU","兰州大学","友校","ST","乡情校谊"));
        itemList.add(new Board("NanTong","江风海韵","地区","audury","乡情校谊"));
        itemList.add(new Board("NJAU","南京农业大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NJMU","南京医科大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NJNU","南京师范大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NJUPT","南京邮电大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NJUT","南京工业大学","友校","5920","乡情校谊"));
        itemList.add(new Board("NKU","南开大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NUAA","南京航空航天大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("NUST","南京理工大学","友校","yurham","乡情校谊"));
        itemList.add(new Board("NZY","南京中医药大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("OUC","中国海洋大学","友校","yulang","乡情校谊"));
        itemList.add(new Board("Overseas","海外游子","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("PKU","北京大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("SE_Association","阳光海岸（华南网友版）","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("SEU","东南大学","友校","nuanbing","乡情校谊"));
        itemList.add(new Board("ShanDong","齐鲁青未了","地区","nicole1906","乡情校谊"));
        itemList.add(new Board("ShangHai","寻梦海上花","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("ShanXi","三晋梦萦","地区","EDTA","乡情校谊"));
        itemList.add(new Board("SJTU","上海交通大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("SuQian","西楚下相","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("SuZhou","雨渍东吴","地区","cat810","乡情校谊"));
        itemList.add(new Board("TaiZhou","古韵泰州","地区","LDA","乡情校谊"));
        itemList.add(new Board("THU","清华大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("TianJin","九河下梢天津卫","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("USTC","中国科技大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("WHU","武汉大学","友校","yilimomo","乡情校谊"));
        itemList.add(new Board("WuXi","梁溪寄畅","地区","诚征版主中","乡情校谊"));
        itemList.add(new Board("XiBei","壮哉大西北","地区","lunar0228","乡情校谊"));
        itemList.add(new Board("XiNan","风起西南","地区","Polystyrene","乡情校谊"));
        itemList.add(new Board("XinJiang","我们新疆亚克西","地区","SYSOP","乡情校谊"));
        itemList.add(new Board("XJTU","西安交通大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("XMU","厦门大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("XuZhou","古彭汉风","地区","jabbar","乡情校谊"));
        itemList.add(new Board("YanCheng","登瀛渔火","地区","sanfen","乡情校谊"));
        itemList.add(new Board("YangZhou","五亭烟雨","地区","824","乡情校谊"));
        itemList.add(new Board("ZheJiang","钱江潮","地区","PiPa","乡情校谊"));
        itemList.add(new Board("ZhenJiang","古城镇江","地区","sweetlayla","乡情校谊"));
        itemList.add(new Board("ZJU","浙江大学","友校","诚征版主中","乡情校谊"));
        itemList.add(new Board("ZSU","中山大学","友校","诚征版主中","乡情校谊"));

        itemList.add(new Board("AI","人工智能","应用","redeast","电脑技术"));
        itemList.add(new Board("Algorithm","算法","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Apple","苹果电脑","电脑","imars","电脑技术"));
        itemList.add(new Board("BBSDev","BBS的安装与设置","系统","SYSOP","电脑技术"));
        itemList.add(new Board("BitTorrent","比特洪流","电脑","Amati","电脑技术"));
        itemList.add(new Board("Borland","Borland世界","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Computer_ABC","电脑菜鸟要学飞","应用","waful","电脑技术"));
        itemList.add(new Board("CPlusPlus","C++程序设计语言","语言","Cwind","电脑技术"));
        itemList.add(new Board("Database","数据库系统","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("DotNet",".net技术","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Embedded","嵌入式系统","系统","诚征版主中","电脑技术"));
        itemList.add(new Board("Flash","闪客世界","应用","Richard","电脑技术"));
        itemList.add(new Board("Fortran","Fortran语言","语言","诚征版主中","电脑技术"));
        itemList.add(new Board("Graphics","电脑图形处理","应用","诚征版主中","电脑技术"));
        itemList.add(new Board("Hacker","黑客的摇篮","网络","long8k8","电脑技术"));
        itemList.add(new Board("Hardware","硬件工作室","系统","john8425","电脑技术"));
        itemList.add(new Board("HPC","高性能计算","系统","诚征版主中","电脑技术"));
        itemList.add(new Board("Image","图像世界","应用","sunyicheng","电脑技术"));
        itemList.add(new Board("ITClub","IT俱乐部","电脑","诚征版主中","电脑技术"));
        itemList.add(new Board("Java","Java语言","语言","诚征版主中","电脑技术"));
        itemList.add(new Board("LilyStudio","小百合工作室项目反馈","开发","ciel54","电脑技术"));
        itemList.add(new Board("LinuxUnix","Linux和Unix","系统","OP07","电脑技术"));
        itemList.add(new Board("Mobile","手机天地","系统","photonics","电脑技术"));
        itemList.add(new Board("MSWindows","美丽的微软窗口","系统","enation","电脑技术"));
        itemList.add(new Board("Network","网络世界","网络","yaoge123","电脑技术"));
        itemList.add(new Board("NoteBook","本本梦工厂","电脑","x201","电脑技术"));
        itemList.add(new Board("Program","程序员的休闲室","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Python","Python语言","语言","诚征版主中","电脑技术"));
        itemList.add(new Board("SoftEng","软件工程","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Software","软件天地","应用","clippit","电脑技术"));
        itemList.add(new Board("TeX","科技文献排版","系统","诚征版主中","电脑技术"));
        itemList.add(new Board("VC","VisualC++版","开发","诚征版主中","电脑技术"));
        itemList.add(new Board("Virus","可恶可怕的病毒","系统","317","电脑技术"));
        itemList.add(new Board("WebDesign","网站设计","网络","诚征版主中","电脑技术"));

        itemList.add(new Board("1937_12_13","南京大屠杀","社科","诚征版主中","学术科学"));
        itemList.add(new Board("Actuary","保险精算","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Aerospace","宇航","理工","诚征版主中","学术科学"));
        itemList.add(new Board("America","美利坚之窗","社科","lonestar","学术科学"));
        itemList.add(new Board("AtmosphereSci","大气科学论坛","理工","huobao","学术科学"));
        itemList.add(new Board("CFD","计算流体论坛","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Chemistry","化学版","理工","weidayanqing","学术科学"));
        itemList.add(new Board("Chrematistics","理财","社科","诚征版主中","学术科学"));
        itemList.add(new Board("Christianity","基督教研究","社科","graceus","学术科学"));
        itemList.add(new Board("Consultant","咨询","学术","talentyjx","学术科学"));
        itemList.add(new Board("CPA","注册会计师","社科","lemonrain","学术科学"));
        itemList.add(new Board("Deutsch","德文堂","语言","诚征版主中","学术科学"));
        itemList.add(new Board("E_Business","电子商务特区","学术","诚征版主中","学术科学"));
        itemList.add(new Board("EarthSciences","地球科学","理工","antivirus","学术科学"));
        itemList.add(new Board("Economics","经济学","社科","eXD","学术科学"));
        itemList.add(new Board("Education","教育论坛","社科","诚征版主中","学术科学"));
        itemList.add(new Board("EEtechnology","电子技术版","理工","诚征版主中","学术科学"));
        itemList.add(new Board("English","英语世界","语言","linglingzhai","学术科学"));
        itemList.add(new Board("Esperanto","世说新语","语言","诚征版主中","学术科学"));
        itemList.add(new Board("Finance","金融天下","学术","VicSi","学术科学"));
        itemList.add(new Board("Forum","百合论坛","社科","andyhua","学术科学"));
        itemList.add(new Board("French","浪漫法兰西","语言","诚征版主中","学术科学"));
        itemList.add(new Board("Geography","地理科学","理工","xzwnju","学术科学"));
        itemList.add(new Board("GIS","地理信息科学","理工","T66","学术科学"));
        itemList.add(new Board("GreeceRome","希腊罗马","学术","earendil","学术科学"));
        itemList.add(new Board("History","历史","社科","Thucydides","学术科学"));
        itemList.add(new Board("HotZone","战场","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Human","人类漫谈","学术","Hokkianese","学术科学"));
        itemList.add(new Board("Info_Manage","信息管理技术","学术","snowy1007","学术科学"));
        itemList.add(new Board("IR","国际关系","学术","alex636","学术科学"));
        itemList.add(new Board("Japanese","日语学习","语言","诚征版主中","学术科学"));
        itemList.add(new Board("Journalism","新闻传播研究","社科","诚征版主中","学术科学"));
        itemList.add(new Board("Law","法律学","社科","miaoyz","学术科学"));
        itemList.add(new Board("LectureHall","学术交流","学术","诚征版主中","学术科学"));
        itemList.add(new Board("LifeScience","生命科学","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Linguistics","语言与语言学","社科","nergend","学术科学"));
        itemList.add(new Board("Management","管理学","社科","pishehu","学术科学"));
        itemList.add(new Board("Mathematics","数学版","理工","zr9558","学术科学"));
        itemList.add(new Board("MathTools","数学工具软件","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Mediastudy","媒介文化研究","学术","leeood","学术科学"));
        itemList.add(new Board("Medicine","医学与健康","理工","lfforever","学术科学"));
        itemList.add(new Board("Microwave","电磁场与微波技术","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Military","军事科学","社科","Halifax","学术科学"));
        itemList.add(new Board("NanoST","纳米科技","理工","particle","学术科学"));
        itemList.add(new Board("People","人物","社科","xukejia","学术科学"));
        itemList.add(new Board("Philosophy","哲学与思考","社科","诚征版主中","学术科学"));
        itemList.add(new Board("Physics","物理学","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Politics","政治科学","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Psychology","心理健康","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Russia","风雪俄罗斯","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Spanish","西班牙语","语言","诚征版主中","学术科学"));
        itemList.add(new Board("Taiwan","宝岛之恋","社科","诚征版主中","学术科学"));
        itemList.add(new Board("TCM","古意中医","学术","abcde","学术科学"));
        itemList.add(new Board("Theoretical_CS","理论计算机科学","理工","诚征版主中","学术科学"));
        itemList.add(new Board("Thesis","论文","学术","诚征版主中","学术科学"));
        itemList.add(new Board("Tibet","雪域桑烟","社科","Yak","学术科学"));
        itemList.add(new Board("UrbanPlan","城市规划","学术","kuns","学术科学"));
        itemList.add(new Board("US_JP_Research","美日研究","社科","诚征版主中","学术科学"));
        itemList.add(new Board("Wisdom","儒释道","社科","bodhisattva","学术科学"));
        itemList.add(new Board("YangtzeDelta","长江三角洲发展论坛","社科","342631","学术科学"));

        itemList.add(new Board("7th_Art","第七艺术","艺术","AngelinaAnne","文化艺术"));
        itemList.add(new Board("AD_Art","广告艺术","艺术","IvyRose","文化艺术"));
        itemList.add(new Board("Archaism","古文观止","文艺","suxipo","文化艺术"));
        itemList.add(new Board("Archeology","考古","文化","txduyu","文化艺术"));
        itemList.add(new Board("ASCIIArt","ASCII艺术","艺术","MoRnT","文化艺术"));
        itemList.add(new Board("BeautyOfNJU","南大之美","文化","07zr","文化艺术"));
        itemList.add(new Board("Budaixi","布袋戏","文化","shangxian","文化艺术"));
        itemList.add(new Board("Calligraphy","中国书法","艺术","诚征版主中","文化艺术"));
        itemList.add(new Board("ChunQiu_ZhanGuo","春秋战国","文化","suyp","文化艺术"));
        itemList.add(new Board("Classical_Poem","古典诗词","艺术","shangtong","文化艺术"));
        itemList.add(new Board("ClassicalCulture","古韵悠长","文化","SlightSky","文化艺术"));
        itemList.add(new Board("ClassicalMusic","古典音乐","音乐","magic0511","文化艺术"));
        itemList.add(new Board("Comic","动漫世界","文化","tracey","文化艺术"));
        itemList.add(new Board("Couplet","对联","文艺","Lineker","文化艺术"));
        itemList.add(new Board("Cross_Strait","穿越海峡","文化","诚征版主中","文化艺术"));
        itemList.add(new Board("CrossShow","十字绣坊","艺术","IvyRose","文化艺术"));
        itemList.add(new Board("Debate","辩者无敌","文化","诚征版主中","文化艺术"));
        itemList.add(new Board("Detective","侦探推理","文化","Jesus","文化艺术"));
        itemList.add(new Board("Discovery","神秘之旅","文化","spacealien","文化艺术"));
        itemList.add(new Board("Drama","戏剧春秋","艺术","lususlee","文化艺术"));
        itemList.add(new Board("Drawing","绘画艺术","艺术","tzqzwd","文化艺术"));
        itemList.add(new Board("DV_Studio","DV工作室","艺术","heihan","文化艺术"));
        itemList.add(new Board("ElectronicMusic","电子音乐","音乐","诚征版主中","文化艺术"));
        itemList.add(new Board("Emprise","武侠小说","文艺","332244","文化艺术"));
        itemList.add(new Board("F_Literature","外国文学","文艺","诚征版主中","文化艺术"));
        itemList.add(new Board("FairyTale","七色花","文艺","MoRnT","文化艺术"));
        itemList.add(new Board("Fantasy","奇幻天地","文化","Alvin","文化艺术"));
        itemList.add(new Board("Fiction","科幻世界","文艺","datoo","文化艺术"));
        itemList.add(new Board("Flowers","花草园艺","艺术","诚征版主中","文化艺术"));
        itemList.add(new Board("Folk_Country","民谣及乡村音乐","音乐","诚征版主中","文化艺术"));
        itemList.add(new Board("Guitar","吉它","音乐","onlyvae","文化艺术"));
        itemList.add(new Board("HiFi","发烧天堂","音乐","yaoge123","文化艺术"));
        itemList.add(new Board("J_Ent","和风艺影","艺术","robertlq","文化艺术"));
        itemList.add(new Board("Jazz_Blues","爵士蓝调","音乐","samsara","文化艺术"));
        itemList.add(new Board("Magic","魔术","艺术","诚征版主中","文化艺术"));
        itemList.add(new Board("Marvel","鬼故事","文艺","madderxixi","文化艺术"));
        itemList.add(new Board("Modern_Poem","现代诗歌","艺术","hcy520","文化艺术"));
        itemList.add(new Board("Movies","露天电影院","艺术","cherry1986","文化艺术"));
        itemList.add(new Board("Musical","音乐剧之家","音乐","诚征版主中","文化艺术"));
        itemList.add(new Board("Mythlegend","神话传说","文艺","goon","文化艺术"));
        itemList.add(new Board("Names","姓名文化","文化","Gilgamesh","文化艺术"));
        itemList.add(new Board("NewAge","新世纪音乐","音乐","historylover","文化艺术"));
        itemList.add(new Board("Novel","小说","文艺","laona1983","文化艺术"));
        itemList.add(new Board("OurCustom","民俗民风","文化","OurCustom","文化艺术"));
        itemList.add(new Board("Photography","摄影艺术","艺术","Apus","文化艺术"));
        itemList.add(new Board("Piano","钢琴艺术","音乐","Astral","文化艺术"));
        itemList.add(new Board("PopMusic","流行音乐天地","音乐","royyoung","文化艺术"));
        itemList.add(new Board("QuYi","曲苑杂谈","艺术","renzhen","文化艺术"));
        itemList.add(new Board("Reading","读书","文艺","诚征版主中","文化艺术"));
        itemList.add(new Board("RockMusic","摇滚乐世界","音乐","Redemption","文化艺术"));
        itemList.add(new Board("SanGuo","三国风云","文艺","emri","文化艺术"));
        itemList.add(new Board("Sculpture","雕塑艺术","艺术","诚征版主中","文化艺术"));
        itemList.add(new Board("Seasons","欧美电视剧","艺术","sprite2200","文化艺术"));
        itemList.add(new Board("Shows","综艺大秀场","艺术","lanfeng","文化艺术"));
        itemList.add(new Board("StoneStory","红楼逸梦","文艺","Loreley","文化艺术"));
        itemList.add(new Board("Story","故事会","文艺","jevons","文化艺术"));
        itemList.add(new Board("TV","电视","艺术","moonminyue","文化艺术"));
        itemList.add(new Board("ZhuangXiu","家居装修","艺术","诚征版主中","文化艺术"));

        itemList.add(new Board("AutoSpeed","车迷世界","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Badminton","羽毛球","体育","kikikuku","体育娱乐"));
        itemList.add(new Board("Basketball","篮球","体育","cobratoxin","体育娱乐"));
        itemList.add(new Board("Billiards","台球","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("BNGames","南大战网游戏","游戏","walle","体育娱乐"));
        itemList.add(new Board("BoardGame","桌面游戏","游戏","pangting","体育娱乐"));
        itemList.add(new Board("Bowling","保龄球馆","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Boxing_Fight","拳击与格斗","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Bridge","桥牌联谊会","棋牌","xyhjqka","体育娱乐"));
        itemList.add(new Board("Chess","象棋","棋牌","fjlanx2","体育娱乐"));
        itemList.add(new Board("ChinaFootball","中国足球","体育","choi","体育娱乐"));
        itemList.add(new Board("Cube","魔方","体育","tent632","体育娱乐"));
        itemList.add(new Board("Cycling","自行车运动","体育","mew","体育娱乐"));
        itemList.add(new Board("Dance","舞蹈天地","体育","carol1121","体育娱乐"));
        itemList.add(new Board("DotaAllstars","远古之守护V","游戏","qinghua","体育娱乐"));
        itemList.add(new Board("E_Sports","电子竞技","游戏","诚征版主中","体育娱乐"));
        itemList.add(new Board("F1","一级方程式赛车","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Fishing","碧波垂钓","体育","pengchen","体育娱乐"));
        itemList.add(new Board("Fitness","健美与健身","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("GJ","够级艺术","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("JSSports","江苏体育","体育","lixs1985","体育娱乐"));
        itemList.add(new Board("MaJiang","麻将","棋牌","274462108","体育娱乐"));
        itemList.add(new Board("MudLife","MUD人生","游戏","诚征版主中","体育娱乐"));
        itemList.add(new Board("OLGames","网络游戏","游戏","ELRais","体育娱乐"));
        itemList.add(new Board("Olympics","奥林匹克运动","体育","Lineker","体育娱乐"));
        itemList.add(new Board("PCGames","电脑游戏","游戏","usbstormer","体育娱乐"));
        itemList.add(new Board("Renju","五子连珠","棋牌","yulang","体育娱乐"));
        itemList.add(new Board("RunForEver","田径","体育","lanben","体育娱乐"));
        itemList.add(new Board("SiGuo","缥缈四国","棋牌","诚征版主中","体育娱乐"));
        itemList.add(new Board("SJ","升级艺术","游戏","dafunk","体育娱乐"));
        itemList.add(new Board("Skating","溜冰人生","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Sudoku","快乐数独","游戏","liyiting23","体育娱乐"));
        itemList.add(new Board("Swimming","游泳","体育","magic0511","体育娱乐"));
        itemList.add(new Board("TableTennis","乒乓球","体育","lightling","体育娱乐"));
        itemList.add(new Board("Taekwondo","跆拳道","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("Tennis","网球天地","体育","Intothenight","体育娱乐"));
        itemList.add(new Board("TVGames","电视游戏","游戏","Ayanami","体育娱乐"));
        itemList.add(new Board("Volleyball","排球版","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("WebGames","网页游戏","游戏","454985732","体育娱乐"));
        itemList.add(new Board("WeiQi","围棋","棋牌","zyhu83","体育娱乐"));
        itemList.add(new Board("WesternstyleChess","国际象棋","棋牌","诚征版主中","体育娱乐"));
        itemList.add(new Board("WorldFootball","世界足球","体育","DidierDrogba","体育娱乐"));
        itemList.add(new Board("WuShu","中华武术","体育","诚征版主中","体育娱乐"));
        itemList.add(new Board("YOGA","南大瑜伽","体育","诚征版主中","体育娱乐"));

        itemList.add(new Board("AlbumShow","个人写真","休闲","SYSOP","感性休闲"));
        itemList.add(new Board("Astrology","星月童话","休闲","Francesco","感性休闲"));
        itemList.add(new Board("Bless","温馨祝福V","感性","SYSOP","感性休闲"));
        itemList.add(new Board("Boys","男生世界","感性","lyandlj","感性休闲"));
        itemList.add(new Board("Collections","收藏爱好","休闲","1900","感性休闲"));
        itemList.add(new Board("Dream","青春有梦","感性","Linhir","感性休闲"));
        itemList.add(new Board("Drink","酒吧与咖啡馆","休闲","drink","感性休闲"));
        itemList.add(new Board("Esquire","时尚男生","休闲","jackpot","感性休闲"));
        itemList.add(new Board("FamilyLife","家庭生活","感性","laputa","感性休闲"));
        itemList.add(new Board("Fashion","美丽流行风","休闲","9D","感性休闲"));
        itemList.add(new Board("Feelings","感情世界","感性","dox","感性休闲"));
        itemList.add(new Board("FOOD","雅舍谈吃","休闲","raphaellaw","感性休闲"));
        itemList.add(new Board("Friendship","友情久久","感性","magics","感性休闲"));
        itemList.add(new Board("Girls","女生天地","感性","lyandlj","感性休闲"));
        itemList.add(new Board("GreatTurn","脑筋急转弯","休闲","诚征版主中","感性休闲"));
        itemList.add(new Board("HandiCraft","精致手工","休闲","Linhir","感性休闲"));
        itemList.add(new Board("Hometown","游子情深","感性","5920","感性休闲"));
        itemList.add(new Board("ID","掀起你的盖头来","感性","rakepunk","感性休闲"));
        itemList.add(new Board("Joke","笑话版","休闲","SYSOP","感性休闲"));
        itemList.add(new Board("KaraOK","卡拉永远OK","休闲","hebbe","感性休闲"));
        itemList.add(new Board("Korea","恋恋韩风","休闲","诚征版主中","感性休闲"));
        itemList.add(new Board("Life","生活","感性","IAMHERE","感性休闲"));
        itemList.add(new Board("Love","情爱悠悠","感性","amour","感性休闲"));
        itemList.add(new Board("Memory","似水流年","感性","dox","感性休闲"));
        itemList.add(new Board("Model_Space","模型空间","休闲","zhouhangxx","感性休闲"));
        itemList.add(new Board("NanJing","古都南京","感性","longwood","感性休闲"));
        itemList.add(new Board("OfficeStaff","上班一族","感性","cjf80","感性休闲"));
        itemList.add(new Board("Party_of_Killer","杀手的童话","休闲","诚征版主中","感性休闲"));
        itemList.add(new Board("PetsEden","宠物乐园","休闲","fineny","感性休闲"));
        itemList.add(new Board("Pictures","贴图版","休闲","SYSOP","感性休闲"));
        itemList.add(new Board("Radio","空中梦想家","感性","诚征版主中","感性休闲"));
        itemList.add(new Board("Riddle","射一射老虎","休闲","LinuxUnix","感性休闲"));
        itemList.add(new Board("RoomChating","寝室夜话","感性","74741","感性休闲"));
        itemList.add(new Board("Shopping","购物天堂","休闲","诚征版主中","感性休闲"));
        itemList.add(new Board("ShortMessage","短信大家聊","休闲","qiwn","感性休闲"));
        itemList.add(new Board("Single","单身一族","感性","诚征版主中","感性休闲"));
        itemList.add(new Board("Travel","遍览天下","休闲","nzzhang","感性休闲"));
        itemList.add(new Board("WarAndPeace","百年好合","感性","Xiaozhao","感性休闲"));

        itemList.add(new Board("Abroad","飞越重洋","信息","18ga","新闻信息"));
        itemList.add(new Board("Agent","代理","信息","Kelsuzard","新闻信息"));
        itemList.add(new Board("Britain","行走英伦","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("ChinaNews","国内新闻","新闻","诚征版主中","新闻信息"));
        itemList.add(new Board("Civil_Servant","公务员之家","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("DigiMusic","数码音乐设备","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("DigitalWorld","数码世界","信息","spacealien","新闻信息"));
        itemList.add(new Board("DiscZone","碟碟不休","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("ExchangeStudent","交换生天地","信息","Hanamichi","新闻信息"));
        itemList.add(new Board("FleaMarket","跳蚤市场","信息","SYSOP","新闻信息"));
        itemList.add(new Board("GoToUniversity","高考招生信息","信息","kernel","新闻信息"));
        itemList.add(new Board("GRE_TOEFL","GRE&TOEFL专题讨论","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("IELTS","清谈雅思","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("Intern","实习","信息","water1529","新闻信息"));
        itemList.add(new Board("ITExam","IT认证考试","信息","lcns","新闻信息"));
        itemList.add(new Board("JobAndWork","创业与求职","信息","cyldo","新闻信息"));
        itemList.add(new Board("JobExpress","就业特快","信息","SYSOP","新闻信息"));
        itemList.add(new Board("KaoYan","考研天地","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("LostToFind","失物招领","信息","jshmcc","新闻信息"));
        itemList.add(new Board("NetResources","网络资源","信息","Jeans236","新闻信息"));
        itemList.add(new Board("NJ_HOUSE","房屋租赁","信息","诚征版主中","新闻信息"));
        itemList.add(new Board("PartTimeJob","兼职工作信息","信息","coldsummer","新闻信息"));
        itemList.add(new Board("RealEstate","房地产","信息","342631","新闻信息"));
        itemList.add(new Board("SportsNews","体坛快讯","新闻","诚征版主中","新闻信息"));
        itemList.add(new Board("Stock","股市风云","新闻","诚征版主中","新闻信息"));
        itemList.add(new Board("SuperGirls","超级女声","信息","SYSOP","新闻信息"));
        itemList.add(new Board("Traffic_Info","交通信息","信息","HMPT","新闻信息"));
        itemList.add(new Board("Train","汽笛声声","信息","laser1987","新闻信息"));
        itemList.add(new Board("WorldNews","国际新闻","新闻","诚征版主中","新闻信息"));
        itemList.add(new Board("Zjl_Online","珠江路热线","信息","piyazhou","新闻信息"));

        itemList.add(new Board("AntiMalfeasant","反腐倡廉","社会","诚征版主中","百合广角"));
        itemList.add(new Board("AntiRumor","反谣言中心","群体","诚征版主中","百合广角"));
        itemList.add(new Board("BodilyForm","高矮胖瘦","群体","patpat","百合广角"));
        itemList.add(new Board("Guilt","罪与罚","社会","applecrystal","百合广角"));
        itemList.add(new Board("HomoSky","同一片天空","群体","bodhisattva","百合广角"));
        itemList.add(new Board("Nature","人与自然","社会","诚征版主中","百合广角"));
        itemList.add(new Board("Peer_Edu","青春伊甸园","群体","SYSOP","百合广角"));
        itemList.add(new Board("PeerCounseling","朋辈咨询","群体","yizhi401","百合广角"));
        itemList.add(new Board("Smoking","淡烟人生","社会","31060004","百合广角"));
        itemList.add(new Board("Vegetarian","素食者","群体","yuxinsnow","百合广角"));
        itemList.add(new Board("West_Volunteer","支教岁月","群体","zhang66","百合广角"));

        itemList.add(new Board("bulletin","校务公告","信箱","yungi","校务信箱"));
        itemList.add(new Board("M_Academic","教务处处长信箱","信箱","wkpope","校务信箱"));
        itemList.add(new Board("M_CMHER","心理中心主任信箱","信箱","cck","校务信箱"));
        itemList.add(new Board("M_Gonghui","工会主席信箱版","信箱","saycc","校务信箱"));
        itemList.add(new Board("M_Graduate","研究生院院长信箱","信箱","gra","校务信箱"));
        itemList.add(new Board("M_GraduateUnion","研究生会主席信箱","信箱","nandayanhui","校务信箱"));
        itemList.add(new Board("M_Guard","保卫处处长信箱","信箱","xxw","校务信箱"));
        itemList.add(new Board("M_Hospital","校医院院长信箱","信箱","ndyyyzxx","校务信箱"));
        itemList.add(new Board("M_Job","就业创业指导中心主任信箱","信箱","zgl","校务信箱"));
        itemList.add(new Board("M_League","团委书记信箱","信箱","ndqgx","校务信箱"));
        itemList.add(new Board("M_Library","图书馆馆长信箱","信箱","xqzhang","校务信箱"));
        itemList.add(new Board("M_Logistic","后勤工作信箱","信箱","jijian","校务信箱"));
        itemList.add(new Board("M_NIC","网络中心主任信箱","信箱","njunic","校务信箱"));
        itemList.add(new Board("M_Student","学生工作处处长信箱","信箱","sumer","校务信箱"));
        itemList.add(new Board("M_StudentUnion","学生会主席信箱","信箱","XueShengHui","校务信箱"));
        itemList.add(new Board("V_Suggestions","校长信箱","信箱","Mumu","校务信箱"));

        itemList.add(new Board("Association_Union","社团联合会主席信箱","社团","zyyz","社团群体"));
        itemList.add(new Board("CCP","礼仪中心","社团","Athenaalice","社团群体"));
        itemList.add(new Board("Chorus","声乐艺术爱好者（南大合唱团）","社团","Hanamichi","社团群体"));
        itemList.add(new Board("FanBu","反哺学社","社团","darkser","社团群体"));
        itemList.add(new Board("FEA","对外交流协会","社团","860817","社团群体"));
        itemList.add(new Board("Folk_Music","国乐飘香（民乐团）","社团","7-May","社团群体"));
        itemList.add(new Board("GAFA","天文爱好者协会","社团","yimingleon","社团群体"));
        itemList.add(new Board("GEC","研究生英语俱乐部","社团","诚征版主中","社团群体"));
        itemList.add(new Board("GreenEarth","南大环境保护协会","社团","诚征版主中","社团群体"));
        itemList.add(new Board("GuQin","古琴社","社团","WindKing","社团群体"));
        itemList.add(new Board("LifeLeague","生命协会","社团","诚征版主中","社团群体"));
        itemList.add(new Board("LSCMA","物流与供应链管理协会","社团","Jake","社团群体"));
        itemList.add(new Board("Marketing_Zone","营销学社","社团","诚征版主中","社团群体"));
        itemList.add(new Board("MSTClub","微软技术俱乐部","社团","tangtang","社团群体"));
        itemList.add(new Board("NJU_Graduate","南大研究生报","社团","water2050","社团群体"));
        itemList.add(new Board("NJU_TIC","南京大学腾讯创新俱乐部","社团","sanyo3","社团群体"));
        itemList.add(new Board("NJU_Youth","南大青年报","社团","shrekzhm","社团群体"));
        itemList.add(new Board("NJU_zhixing","南京大学知行社","社团","诚征版主中","社团群体"));
        itemList.add(new Board("NJUMUN","模拟联合国协会","社团","诚征版主中","社团群体"));
        itemList.add(new Board("Orchestra","南大交响乐团","社团","诚征版主中","社团群体"));
        itemList.add(new Board("ReadyForJob","阳光工作室","社团","cyldo","社团群体"));
        itemList.add(new Board("RedCross","红十字运动","社团","baoge","社团群体"));
        itemList.add(new Board("SCDA","学生职业发展协会","社团","twxqsj","社团群体"));
        itemList.add(new Board("SIFE_NJU","国际大学生企业家联盟","社团","诚征版主中","社团群体"));
        itemList.add(new Board("SiYuan","南大思源社","社团","zyg","社团群体"));
        itemList.add(new Board("SPA","南大学生心理协会","社团","诚征版主中","社团群体"));
        itemList.add(new Board("StaffPhotography","视觉南大","团体","baiyan2","社团群体"));
        itemList.add(new Board("StoneCity","石头城","团体","aqe","社团群体"));
        itemList.add(new Board("TianJian","南大天健社","社团","dizhonghai","社团群体"));
        itemList.add(new Board("Volunteer","青年志愿者协会","社团","523","社团群体"));
        itemList.add(new Board("xinhongji","新鸿基社","社团","wangD","社团群体"));
        itemList.add(new Board("YangTaiChi","杨式太极拳协会","社团","诚征版主中","社团群体"));
    }

    /**
     * 给某版面添加帖子（ArticleBase）
     * @param article
     */
    public void addArticle(ArticleBase article) {
        if(article == null) {
            return;
        }
        Board board = getById(article.getBoard());
        if(board != null) {
            board.addArticleBase(article);
        }
    }

    /**
     * 给某版面删除帖子（ArticleBase）
     * @param article
     */
    public void deleteArticle(ArticleBase article) {
        if(article == null) {
            return;
        }
        Board board = getById(article.getBoard());
        if(board != null) {
            board.deleteArticleBase(article);
        }
    }

    /**
     * 根据id获取某篇帖子的ArticleBase
     * @param articleId
     * @return
     */
    public ArticleBase getArticle(String boardId, String articleId) {
        Board board = getById(boardId);
        if(board == null) {
            return null;
        }
        List<ArticleBase> articleList = board.getAllArticle();
        for(int i =0; i < articleList.size(); i++) {
            if(articleList.get(i).getId().equals(articleId)) {
                return articleList.get(i);
            }
        }
        return null;
    }

    /**
     * 把版面所有数据打成字符串数组
     * @return
     */
    public List<String> toStringArray() {
        List<String> result = new ArrayList<String>();
        for(int i =0; i< itemList.size(); i++) {
            result.add(itemList.get(i).getBoardId());
            result.add(itemList.get(i).getChinesName());
        }
        return result;
    }

    @Override
    public String getSourceName() {
        return SourceName.BBS_BOARD;
    }

    @Override
    public XDBTable<Board> getDatabaseTable() {
        return new BoardTable();
    }

    @Override
    public String getId(Board item) {
        return item.getBoardId();
    }


    @Override
    public void replace(int index, Board newItem) {
        Board oldBoard = itemList.get(index);
        oldBoard.setChinesName(newItem.getChinesName());
        oldBoard.setBoardOwnerId(newItem.getBoardOwnerId());
        oldBoard.setBoardType(newItem.getBoardType());
        oldBoard.setZoneBelong(newItem.getZoneBelong());
    }
}
