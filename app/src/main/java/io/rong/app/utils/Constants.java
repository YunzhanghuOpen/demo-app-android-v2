package io.rong.app.utils;

/**
 * Created by Bob on 2015/4/17.
 */
public class Constants {
    public static final String DEBUG = "--bob---";
    //新的好友
    public static final int FRIENDLIST_REQUESTCODE = 1001;
    //搜索
    public static final int SEARCH_REQUESTCODE = 1002;
    //添加好友
    public static final int PERSONAL_REQUESTCODE = 1003;
    //加入群组
    public static final int GROUP_JOIN_REQUESTCODE = 1004;
    //退出群组
    public static final int GROUP_QUIT_REQUESTCODE = 1005;
    //修改用户名称
    public static final int FIX_USERNAME_REQUESTCODE = 1006;
    //删除好友
    public static final int DELETE_USERNAME_REQUESTCODE = 1007;
    //修改讨论组名称
    public static final int FIX_DISCUSSION_NAME = 1008;
    //修改群名片
    public static final int FIX_GROUP_INFO = 1010;
    //修改设置页面
    public static final int UPDATE_DISCUTION_NUMBER = 1009;
    //@消息
    public static final int MESSAGE_REPLY = 1010;
    public static final String DEFAULT = "default";
    public static final String APP_TOKEN = "DEMO_TOKEN";
    public static final String APP_USER_ID = "DEMO_USERID";
    public static final String APP_USER_NAME = "DEMO_USER_NAME";
    public static final String APP_USER_PORTRAIT = "DEMO_USER_PORTRAIT";
    //红包所需要的常量可以自己定义
    public static final int ITEM_SEND_MONEY = 15;
    public static final int MESSAGE_TYPE_RECV_MONEY = 5;
    public static final int MESSAGE_TYPE_SEND_MONEY = 6;
    public static final int MESSAGE_TYPE_SEND_LUCKY = 7;
    public static final int MESSAGE_TYPE_RECV_LUCKY = 8;
    public static final int REQUEST_CODE_SEND_MONEY = 15;
    //红包相关常量值
    public static final String REFRESH_GROUP_MONEY_ACTION = "refresh_group_money_action";
    public static final String EXTRA_LUCKY_MONEY_SENDER_ID = "money_sender_id";
    public static final String EXTRA_LUCKY_MONEY_RECEIVER_ID = "money_receiver_id";
    public static final String MESSAGE_ATTR_IS_OPEN_MONEY_MESSAGE = "is_open_money_msg";
    public static final String EXTRA_LUCKY_MONEY_SENDER = "money_sender";
    public static final String EXTRA_LUCKY_MONEY_RECEIVER = "money_receiver";
}
