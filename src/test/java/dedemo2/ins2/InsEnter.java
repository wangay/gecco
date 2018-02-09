package dedemo2.ins2;

/**
 * Created by nobody on 2018/2/9.
 * 动作的入口
 */
public class InsEnter {

    private static InsAuto insAuto;
    static{
//        insAuto= InsAuto.getInstance();
    }
    public static void main(String[] args) {

        //被关注（粉丝们）
//        com.geccocrawler.gecco.demo.ins.InsByQueryIdSpriderBean.following();
//        com.geccocrawler.gecco.demo.ins.InsByQueryIdSpriderBean.followed("bigbong");
        com.geccocrawler.gecco.demo.ins.InsByQueryIdSpriderBean.followedMany();
//        com.geccocrawler.gecco.demo.ins.InsByQueryIdSpriderBean.tag();
//        com.geccocrawler.gecco.demo.ins.InsByQueryIdSpriderBean.tagAll();



        //关注一个人
//        String url = "https://www.instagram.com/p/Bc7r7wHDMoY/?taken-by=neymarjr";
//        register.dianzan(url);
//        String url="https://www.instagram.com/jiyouchen7655/";
//        String url="https://www.instagram.com/lucky42222/";
//        String user = "lucky42222";
//        insAuto.guanzhu(url,user);

        //关注一群人
//        insAuto.guanzhuAll();
//        insAuto.yifasongAll();
//        insAuto.pinglun("https://www.instagram.com/p/BbtYLixAhO1/?taken-by=yauhongv3v");


    }
}
