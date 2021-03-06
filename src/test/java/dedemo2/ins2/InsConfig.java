package dedemo2.ins2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nobody on 2018/2/4.
 * 配置类，
 * 比如用哪个用户，是否需要重新登陆
 * 使用哪些目标mogodb集合
 */
public class InsConfig {

    //当前使用的一个用户名
    private String curUserName;

    //当前用户的秘密
    private String curUserPassword;

    //当前用户的已关注用户的mongodb集合名称
    private String colWMyYgz;

    //当前用户的 处于"已发送"的用户的mongodb集合名称
    private String colWMyYfs;

    //String col_w_my_maozebei6368 ="col_w_my_maozebei6368";//我的某个账户关注的用户

    //所有可用的用户密码对
    private Map<String,String> user;

    //是否需要切换登陆用户
    private boolean needChangeUser;


    public String getColWMyYfs() {
        return colWMyYfs;
    }

    public void setColWMyYfs(String colWMyYfs) {
        this.colWMyYfs = colWMyYfs;
    }

    public String getColWMyYgz() {
        return colWMyYgz;
    }

    public void setColWMyYgz(String colWMyYgz) {
        this.colWMyYgz = colWMyYgz;
    }

    public String getCurUserName() {
        return curUserName;
    }

    public void setCurUserName(String curUserName) {
        this.curUserName = curUserName;
    }

    public String getCurUserPassword() {
        return curUserPassword;
    }

    public void setCurUserPassword(String curUserPassword) {
        this.curUserPassword = curUserPassword;
    }

    public boolean isNeedChangeUser() {
        return needChangeUser;
    }

    public void setNeedChangeUser(boolean needChangeUser) {
        this.needChangeUser = needChangeUser;
    }

    public InsConfig(String curUserName, String curUserPassword) {
        this.curUserName = curUserName;
        this.curUserPassword = curUserPassword;
    }

    public Map<String, String> getUser() {
        return user;
    }

    public void setUser(Map<String, String> user) {
        this.user = user;
    }

    public InsConfig() {
        user = new HashMap<String,String>();
        //这些数据可以放在mongodb
        user.put("jiangchunyun88","alexisgood");
        user.put("maozebei6368","maozebei63681");
        needChangeUser=false;
    }

    public void setOneUser(String username){
        this.curUserName =username;
        this.curUserPassword =this.user.get(username);
    }


}
