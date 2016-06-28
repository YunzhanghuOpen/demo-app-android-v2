package com.easemob.redpacketui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.easemob.redpacketsdk.bean.AuthData;
import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.message.RongEmptyMessage;
import com.easemob.redpacketui.message.RongNotificationMessage;
import com.easemob.redpacketui.message.RongRedPacketMessage;
import com.easemob.redpacketui.provider.RongNotificationMessageProvider;
import com.easemob.redpacketui.provider.RongRedPacketMessageProvider;
import com.easemob.redpacketui.ui.activity.RPChangeActivity;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;

/**
 * Created by yunyu on 16/5/29.
 */
public class RPContext {
    public static final int REQUEST_CODE_SEND_MONEY = 15;
    public static final String CHAT_GROUP = "chat_group";
    public static final String CHAT_DISCUSSION = "chat_discussion";
    private String userName;
    private String userAvatar;
    private String userID;
    private AuthData mAuthData;
    private String chatType;
    private static RPContext mRPContext;

    private RPContext() {

    }

    public static RPContext getInstance() {
        if (mRPContext == null) {
            synchronized (RPContext.class) {
                if (mRPContext == null) {
                    mRPContext = new RPContext();
                }

            }
        }
        return mRPContext;
    }

    /**
     * 初始化Token
     * @param authPartner
     * @param authUserId
     * @param authTimestamp
     * @param authSign
     */
    public void initAuthData(String authPartner,String authUserId,String authTimestamp,String authSign) {
        mAuthData=new AuthData();
        mAuthData.authPartner=authPartner;
        mAuthData.authUserId=authUserId;
        mAuthData.authTimestamp=authTimestamp;
        mAuthData.authSign=authSign;
    }

    public AuthData getmAuthData() {
        return mAuthData;
    }

    public void setmAuthData(AuthData mAuthData) {
        this.mAuthData = mAuthData;
    }

    /**
     * 初始化用户信息
     *
     * @param userID     用户ID
     * @param userName   用户名字
     * @param userAvatar 用户头像
     */

    public void initUserInfo(String userID, String userName, String userAvatar) {
        this.userID = userID;
        this.userName = userName;
        this.userAvatar = userAvatar;
        if (TextUtils.isEmpty(userID)) {
            this.userID = "default";
        }
        if (TextUtils.isEmpty(userName)) {
            this.userName = "default";
        }
        if (TextUtils.isEmpty(userAvatar)) {
            this.userAvatar = "default";
        }
    }

    /**
     * 注册消息类型以及消息展示模板
     */
    public void registerMsgTypeAndTemplate(Context mContext) {
        RongIM.registerMessageType(RongRedPacketMessage.class);
        RongIM.registerMessageType(RongNotificationMessage.class);
        RongIM.registerMessageType(RongEmptyMessage.class);
        RongIM.registerMessageTemplate(new RongRedPacketMessageProvider(mContext));
        RongIM.registerMessageTemplate(new RongNotificationMessageProvider(mContext));
    }

    /**
     * 插入消息体
     *
     * @param message 消息类型
     */
    public void insertMessage(Message message) {
        RongEmptyMessage content = (RongEmptyMessage) message.getContent();
        if (TextUtils.isEmpty(userID)) {
            userID = "default";
        }
        RongNotificationMessage rongNotificationMessage = RongNotificationMessage.obtain(content.getSendUserID(), content.getSendUserName(), content.getReceiveUserID(), content.getReceiveUserName(), content.getIsOpenMoney());
        if (content.getSendUserID().equals(userID)) {//如果当前用户是发送红包者,插入一条"XX领取了你的红包"
            RongIM.getInstance().getRongIMClient().insertMessage(message.getConversationType(), message.getTargetId(), content.getReceiveUserID(), rongNotificationMessage, null);
        }
    }

    /**
     * 跳转到零钱页
     * @param mContext
     */
    public void toChangeActivity(Context mContext) {
        Intent intent = new Intent(mContext, RPChangeActivity.class);
       // intent.putExtra(RPConstant.EXTRA_USER_NAME, userName);
       // intent.putExtra(RPConstant.EXTRA_TO_USER_AVATAR, userAvatar);
        RedPacketInfo redPacketInfo=new RedPacketInfo();
        redPacketInfo.fromNickName=userName;
        redPacketInfo.fromAvatarUrl=userAvatar;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_AUTH_INFO, RPContext.getInstance().getmAuthData());
        mContext.startActivity(intent);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}
